package com.swanie.portfolio.billing

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Temporary Pro for Play closed testing: granted until [untilEpochMs] baked in at build time.
 * Set [CLOSED_TEST_PRO_GRANT_DAYS]=0 in local.properties for production store builds.
 */
object ClosedTestProAccess {

    fun isGrantActive(
        untilEpochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean = untilEpochMs > 0L && nowMs < untilEpochMs

    fun resolveIsProUser(
        revenueCatPro: Boolean,
        untilEpochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
    ): Boolean = revenueCatPro || isGrantActive(untilEpochMs, nowMs)

    fun formatExpiryDate(
        untilEpochMs: Long,
        locale: Locale,
    ): String {
        if (untilEpochMs <= 0L) return ""
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
        return formatter.format(
            Instant.ofEpochMilli(untilEpochMs).atZone(ZoneId.systemDefault()).toLocalDate(),
        )
    }
}
