/**
 * Email-bound beta unlock codes (must match app BetaUnlockValidator + scripts/beta-unlock-code.mjs).
 * Format: SWANIE-YYYYMMDD-HHHHHHHH (8 hex chars = first 4 bytes of HMAC-SHA256)
 * Payload signed: lowercase_email|yyyy-MM-dd
 */
(function (global) {
  "use strict";

  var CODE_RE = /^SWANIE-(\d{8})-([A-F0-9]{8})$/i;

  function getConfig() {
    return global.BETA_UNLOCK_CONFIG || {};
  }

  function normalizeEmail(email) {
    return String(email || "")
      .trim()
      .toLowerCase();
  }

  function pad2(n) {
    return n < 10 ? "0" + n : String(n);
  }

  function toIsoDate(d) {
    return d.getFullYear() + "-" + pad2(d.getMonth() + 1) + "-" + pad2(d.getDate());
  }

  function toCompactDate(d) {
    return (
      String(d.getFullYear()) + pad2(d.getMonth() + 1) + pad2(d.getDate())
    );
  }

  function parseIsoDate(iso) {
    var parts = iso.split("-");
    if (parts.length !== 3) return null;
    var y = parseInt(parts[0], 10);
    var m = parseInt(parts[1], 10) - 1;
    var day = parseInt(parts[2], 10);
    var d = new Date(y, m, day);
    if (isNaN(d.getTime())) return null;
    return d;
  }

  var CODE_VALIDITY_DAYS = 30;

  function defaultExpiryDate() {
    var d = new Date();
    d.setDate(d.getDate() + CODE_VALIDITY_DAYS);
    return d;
  }

  function formatExpiryForEmail(d) {
    return d.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }

  function bytesToHex(bytes) {
    var out = "";
    for (var i = 0; i < bytes.length; i++) {
      var h = bytes[i].toString(16);
      if (h.length === 1) h = "0" + h;
      out += h;
    }
    return out.toUpperCase();
  }

  function buildPayload(email, expiryDate) {
    return normalizeEmail(email) + "|" + toIsoDate(expiryDate);
  }

  async function hmacSha256(secret, message) {
    var enc = new TextEncoder();
    var key = await crypto.subtle.importKey(
      "raw",
      enc.encode(secret),
      { name: "HMAC", hash: "SHA-256" },
      false,
      ["sign"]
    );
    var sig = await crypto.subtle.sign("HMAC", key, enc.encode(message));
    return new Uint8Array(sig);
  }

  async function generateCode(email, expiryDate) {
    var cfg = getConfig();
    var secret = (cfg.secret || "").trim();
    if (!secret) {
      return {
        ok: false,
        error:
          "BETA_UNLOCK_SECRET not configured (set GitHub Actions secret for live site, or local website/js/beta-unlock-config.js).",
      };
    }
    var em = normalizeEmail(email);
    if (!em || em.indexOf("@") < 1) {
      return { ok: false, error: "Invalid email." };
    }
    var exp = expiryDate instanceof Date ? expiryDate : defaultExpiryDate();
    var programEnd = (cfg.programEnd || "2027-06-01").trim();
    var programEndDate = parseIsoDate(programEnd);
    if (programEndDate && exp > programEndDate) {
      exp = programEndDate;
    }
    var payload = buildPayload(em, exp);
    var mac = await hmacSha256(secret, payload);
    var tag = bytesToHex(mac.subarray(0, 4));
    var code = "SWANIE-" + toCompactDate(exp) + "-" + tag;
    return {
      ok: true,
      code: code,
      email: em,
      expiryIso: toIsoDate(exp),
      expiryLabel: formatExpiryForEmail(exp),
      programEnd: programEnd,
    };
  }

  function isConfigured() {
    return Boolean((getConfig().secret || "").trim());
  }

  global.BetaUnlock = {
    CODE_VALIDITY_DAYS: CODE_VALIDITY_DAYS,
    CODE_RE: CODE_RE,
    normalizeEmail: normalizeEmail,
    defaultExpiryDate: defaultExpiryDate,
    formatExpiryForEmail: formatExpiryForEmail,
    generateCode: generateCode,
    isConfigured: isConfigured,
  };
})(typeof window !== "undefined" ? window : globalThis);
