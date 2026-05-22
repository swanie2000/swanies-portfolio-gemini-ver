#!/usr/bin/env node
/**
 * Generate beta unlock code (same algorithm as website/js/beta-unlock-code.js and Android).
 * Usage: node scripts/beta-unlock-code.mjs user@gmail.com [yyyy-MM-dd]
 * Secret: BETA_UNLOCK_SECRET env var or local.properties (read by generate-beta-unlock-code.ps1).
 */
import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const PROGRAM_END = process.env.BETA_UNLOCK_PROGRAM_END || "2027-06-01";

function readSecretFromLocalProperties() {
  const propsPath = path.join(__dirname, "..", "local.properties");
  if (!fs.existsSync(propsPath)) return "";
  const text = fs.readFileSync(propsPath, "utf8");
  for (const line of text.split(/\r?\n/)) {
    const t = line.trim();
    if (t.startsWith("BETA_UNLOCK_SECRET=")) {
      return t.slice("BETA_UNLOCK_SECRET=".length).trim();
    }
  }
  return "";
}

function normalizeEmail(email) {
  return String(email || "")
    .trim()
    .toLowerCase();
}

function pad2(n) {
  return n < 10 ? `0${n}` : String(n);
}

function toIsoDate(d) {
  return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
}

function toCompactDate(d) {
  return `${d.getFullYear()}${pad2(d.getMonth() + 1)}${pad2(d.getDate())}`;
}

function parseIsoDate(iso) {
  const [y, m, day] = iso.split("-").map((x) => parseInt(x, 10));
  if (!y || !m || !day) return null;
  return new Date(y, m - 1, day);
}

function defaultExpiry() {
  const d = new Date();
  d.setDate(d.getDate() + 365);
  return d;
}

function generateCode(email, expiryDate, secret) {
  const em = normalizeEmail(email);
  if (!em.includes("@")) throw new Error("Invalid email");
  if (!secret || secret.length < 8) {
    throw new Error("Set BETA_UNLOCK_SECRET (env or local.properties), min 8 characters");
  }
  let exp = expiryDate || defaultExpiry();
  const programEnd = parseIsoDate(PROGRAM_END);
  if (programEnd && exp > programEnd) exp = programEnd;
  const payload = `${em}|${toIsoDate(exp)}`;
  const mac = crypto.createHmac("sha256", secret).update(payload, "utf8").digest();
  const tag = mac.subarray(0, 4).toString("hex").toUpperCase();
  const code = `SWANIE-${toCompactDate(exp)}-${tag}`;
  return { code, email: em, expiryIso: toIsoDate(exp), programEnd: PROGRAM_END };
}

const email = process.argv[2];
const expiryArg = process.argv[3];
if (!email) {
  console.error("Usage: node scripts/beta-unlock-code.mjs user@gmail.com [yyyy-MM-dd]");
  process.exit(1);
}
const secret = (process.env.BETA_UNLOCK_SECRET || readSecretFromLocalProperties()).trim();
const expiry = expiryArg ? parseIsoDate(expiryArg) : defaultExpiry();
if (expiryArg && !expiry) {
  console.error("Invalid expiry date. Use yyyy-MM-dd");
  process.exit(1);
}
try {
  const result = generateCode(email, expiry, secret);
  console.log(result.code);
  console.log(`Email:   ${result.email}`);
  console.log(`Expires: ${result.expiryIso}`);
  console.log(`Program redemption ends: ${result.programEnd}`);
} catch (e) {
  console.error(e.message);
  process.exit(1);
}
