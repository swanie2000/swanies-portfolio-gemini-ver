package com.swanie.portfolio.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import com.swanie.portfolio.BuildConfig
import com.swanie.portfolio.data.local.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.use
import androidx.sqlite.db.SimpleSQLiteQuery
import org.json.JSONObject

/**
 * VER1: Encrypted local vault backup (Room DB + theme DataStore + icon files).
 * Format is opaque bytes suitable for SAF save/share; restore ends with a cold process restart.
 */
@Singleton
class VaultBackupEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
) {

    suspend fun exportToUri(outputUri: Uri, passphrase: CharArray): Result<Unit> = runCatching {
        require(passphrase.isNotEmpty()) { "Passphrase is empty." }
        val workDir = File(context.cacheDir, "vault_export_${System.currentTimeMillis()}")
        deleteDirRecursive(workDir)
        workDir.mkdirs()
        try {
            val innerZip = File(workDir, "payload.zip")
            buildZip(innerZip)
            val plain = innerZip.readBytes()
            val encrypted = encryptPayload(plain, passphrase)
            context.contentResolver.openOutputStream(outputUri)?.use { out ->
                out.write(encrypted)
            } ?: error("Could not open output stream.")
        } finally {
            runCatching { deleteDirRecursive(workDir) }
        }
    }.onFailure { Log.e(TAG, "export failed", it) }

    /**
     * Replaces local vault files from backup, closes Room, then caller must restart the process.
     */
    suspend fun importFromUri(inputUri: Uri, passphrase: CharArray): Result<Unit> = runCatching {
        require(passphrase.isNotEmpty()) { "Passphrase is empty." }
        val maxBytes = 100L * 1024 * 1024
        val encrypted = readAllBytesFromContentUri(inputUri, maxBytes)
        val plain = decryptPayload(encrypted, passphrase)
        val workDir = File(context.cacheDir, "vault_import_${System.currentTimeMillis()}")
        deleteDirRecursive(workDir)
        workDir.mkdirs()
        try {
            val innerZip = File(workDir, "payload.zip")
            innerZip.writeBytes(plain)
            val root = File(workDir, "unzipped").apply { mkdirs() }
            unzipToDirectory(innerZip, root)
            val manifestFile = File(root, MANIFEST_NAME)
            if (!manifestFile.exists()) error("Invalid backup: missing manifest.")
            val manifest = JSONObject(manifestFile.readText())
            if (manifest.optInt("formatVersion", 0) != FORMAT_VERSION) {
                error("Unsupported backup format version.")
            }
            checkpointWalFull()
            AppDatabase.closeAndClearInstance()
            context.deleteDatabase(AppDatabase.DB_NAME)
            val dbSrc = File(root, "db/${AppDatabase.DB_NAME}")
            if (!dbSrc.exists()) error("Invalid backup: missing database.")
            val dbDest = context.getDatabasePath(AppDatabase.DB_NAME)
            dbDest.parentFile?.mkdirs()
            dbSrc.copyTo(dbDest, overwrite = true)
            val prefsSrc = File(root, "prefs/$DATASTORE_FILE")
            val prefsDest = File(context.filesDir, "datastore/$DATASTORE_FILE")
            prefsDest.parentFile?.mkdirs()
            if (prefsSrc.exists()) {
                prefsSrc.copyTo(prefsDest, overwrite = true)
            } else {
                runCatching { prefsDest.delete() }
            }
            val iconsDest = File(context.filesDir, "icons")
            val customDest = File(context.filesDir, "custom_icons")
            clearChildFiles(iconsDest)
            clearChildFiles(customDest)
            copyTree(File(root, "icons"), iconsDest)
            copyTree(File(root, "custom_icons"), customDest)
        } finally {
            runCatching { deleteDirRecursive(workDir) }
        }
    }.onFailure { Log.e(TAG, "import failed", it) }

    /** `PRAGMA wal_checkpoint` returns rows; must use [query], not [execSQL]. */
    private fun checkpointWalFull() {
        database.openHelper.writableDatabase
            .query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))
            .use { }
    }

    private fun buildZip(outFile: File) {
        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            val manifest = JSONObject().apply {
                put("formatVersion", FORMAT_VERSION)
                put("appVersionName", BuildConfig.VERSION_NAME)
                put("appVersionCode", BuildConfig.VERSION_CODE)
                put("exportTimeMillis", System.currentTimeMillis())
                put("dbName", AppDatabase.DB_NAME)
            }
            zos.putNextEntry(ZipEntry(MANIFEST_NAME))
            zos.write(manifest.toString().toByteArray(Charsets.UTF_8))
            zos.closeEntry()
            checkpointWalFull()
            val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
            if (dbFile.exists()) {
                zos.putNextEntry(ZipEntry("db/${AppDatabase.DB_NAME}"))
                FileInputStream(dbFile).use { it.copyTo(zos) }
                zos.closeEntry()
            }
            val prefs = File(context.filesDir, "datastore/$DATASTORE_FILE")
            if (prefs.exists()) {
                zos.putNextEntry(ZipEntry("prefs/$DATASTORE_FILE"))
                FileInputStream(prefs).use { it.copyTo(zos) }
                zos.closeEntry()
            }
            zipDirectory(zos, File(context.filesDir, "icons"), "icons")
            zipDirectory(zos, File(context.filesDir, "custom_icons"), "custom_icons")
        }
    }

    private fun zipDirectory(zos: ZipOutputStream, dir: File, zipPrefix: String) {
        if (!dir.exists()) return
        dir.listFiles()?.filter { it.isFile }?.forEach { f ->
            zos.putNextEntry(ZipEntry("$zipPrefix/${f.name}"))
            FileInputStream(f).use { it.copyTo(zos) }
            zos.closeEntry()
        }
    }

    private fun unzipToDirectory(zipFile: File, destDir: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                if (entry.isDirectory) continue
                if (entry.name.contains("..")) continue
                val out = File(destDir, entry.name)
                out.parentFile?.mkdirs()
                FileOutputStream(out).use { fos -> zis.copyTo(fos) }
                zis.closeEntry()
            }
        }
    }

    private fun clearChildFiles(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { if (it.isFile) it.delete() }
    }

    private fun copyTree(srcDir: File, destDir: File) {
        if (!srcDir.exists()) return
        destDir.mkdirs()
        srcDir.listFiles()?.filter { it.isFile }?.forEach { f ->
            File(destDir, f.name).writeBytes(f.readBytes())
        }
    }

    private fun encryptPayload(plain: ByteArray, passphrase: CharArray): ByteArray {
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plain)
        return ByteArrayOutputStream().apply {
            write(MAGIC.toByteArray(Charsets.US_ASCII))
            write(intToBigEndian(FORMAT_VERSION))
            write(salt)
            write(iv)
            write(cipherBytes)
        }.toByteArray()
    }

    /**
     * Some document providers behave more reliably with a file descriptor than with [openInputStream].
     * A few hosts also prepend a UTF-8 BOM to "binary" saves; strip that before parsing the header.
     */
    private fun readAllBytesFromContentUri(uri: Uri, maxBytes: Long): ByteArray {
        val cr = context.contentResolver
        runCatching {
            cr.openFileDescriptor(uri, "r")?.use { pfd ->
                FileInputStream(pfd.fileDescriptor).use { ins ->
                    return readStreamCapped(ins, maxBytes)
                }
            }
        }.onFailure { Log.w(TAG, "openFileDescriptor read failed, falling back to openInputStream", it) }
        return cr.openInputStream(uri)?.use { ins -> readStreamCapped(ins, maxBytes) }
            ?: error("Could not read backup file.")
    }

    private fun readStreamCapped(ins: InputStream, maxBytes: Long): ByteArray {
        val buf = ByteArrayOutputStream()
        val chunk = ByteArray(8192)
        var total = 0L
        while (true) {
            val n = ins.read(chunk)
            if (n <= 0) break
            total += n
            if (total > maxBytes) error("Backup file is too large.")
            buf.write(chunk, 0, n)
        }
        return buf.toByteArray()
    }

    private fun stripLeadingUtf8Bom(bytes: ByteArray): ByteArray {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        ) {
            return bytes.copyOfRange(3, bytes.size)
        }
        return bytes
    }

    private fun decryptPayload(rawFileBytes: ByteArray, passphrase: CharArray): ByteArray {
        val fileBytes = stripLeadingUtf8Bom(rawFileBytes)
        if (fileBytes.size >= 2 &&
            fileBytes[0] == 0xFF.toByte() && fileBytes[1] == 0xFE.toByte()
        ) {
            error("This file looks like UTF-16 text. Choose the original .swpb backup (binary), not a copy re-saved from an editor.")
        }
        if (fileBytes.size < MAGIC.length + 4 + SALT_LEN + GCM_IV_LEN + 1) error("File too small.")
        var o = 0
        val magic = String(fileBytes, o, MAGIC.length, Charsets.US_ASCII)
        o += MAGIC.length
        if (magic != MAGIC) error("Not a Swanie vault backup file.")
        val ver = bigEndianToInt(fileBytes, o)
        o += 4
        if (ver != FORMAT_VERSION) error("Unknown backup version.")
        val salt = fileBytes.copyOfRange(o, o + SALT_LEN)
        o += SALT_LEN
        val iv = fileBytes.copyOfRange(o, o + GCM_IV_LEN)
        o += GCM_IV_LEN
        val ct = fileBytes.copyOfRange(o, fileBytes.size)
        val key = deriveKey(passphrase, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(ct)
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    private fun intToBigEndian(v: Int): ByteArray =
        byteArrayOf((v shr 24).toByte(), (v shr 16).toByte(), (v shr 8).toByte(), v.toByte())

    private fun bigEndianToInt(b: ByteArray, offset: Int): Int =
        ((b[offset].toInt() and 0xff) shl 24) or
            ((b[offset + 1].toInt() and 0xff) shl 16) or
            ((b[offset + 2].toInt() and 0xff) shl 8) or
            (b[offset + 3].toInt() and 0xff)

    private fun deleteDirRecursive(dir: File) {
        if (!dir.exists()) return
        dir.walkBottomUp().forEach { it.delete() }
    }

    companion object {
        private const val TAG = "VaultBackup"
        private const val FORMAT_VERSION = 1
        private const val MAGIC = "SWPB"
        private const val SALT_LEN = 16
        private const val GCM_IV_LEN = 12
        private const val PBKDF2_ITERATIONS = 120_000
        private const val MANIFEST_NAME = "manifest.json"
        private const val DATASTORE_FILE = "theme_settings.preferences_pb"
    }
}
