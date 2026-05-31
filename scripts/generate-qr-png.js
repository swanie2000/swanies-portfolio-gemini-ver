'use strict';

const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const qrcode = require('./lib/qrcode-generator.js');

function crc32(buf) {
  let c = ~0;
  for (let i = 0; i < buf.length; i++) {
    c ^= buf[i];
    for (let k = 0; k < 8; k++) {
      c = (c >>> 1) ^ (0xedb88320 & -(c & 1));
    }
  }
  return ~c >>> 0;
}

function pngChunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length, 0);
  const typeBuf = Buffer.from(type, 'ascii');
  const crcBuf = Buffer.alloc(4);
  crcBuf.writeUInt32BE(crc32(Buffer.concat([typeBuf, data])), 0);
  return Buffer.concat([len, typeBuf, data, crcBuf]);
}

function writeRgbPng(outPath, width, height, getRgb) {
  const rowSize = 1 + width * 3;
  const raw = Buffer.alloc(rowSize * height);
  for (let y = 0; y < height; y++) {
    raw[y * rowSize] = 0;
    for (let x = 0; x < width; x++) {
      const [r, g, b] = getRgb(x, y);
      const o = y * rowSize + 1 + x * 3;
      raw[o] = r;
      raw[o + 1] = g;
      raw[o + 2] = b;
    }
  }
  const compressed = zlib.deflateSync(raw, { level: 9 });
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8;
  ihdr[9] = 2;
  ihdr[10] = 0;
  ihdr[11] = 0;
  ihdr[12] = 0;
  const png = Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    pngChunk('IHDR', ihdr),
    pngChunk('IDAT', compressed),
    pngChunk('IEND', Buffer.alloc(0)),
  ]);
  fs.writeFileSync(outPath, png);
}

function generateQrPng(text, outPath, opts = {}) {
  const scale = opts.scale || 12;
  const margin = opts.margin ?? 4;
  const dark = opts.dark || [0, 0, 0];
  const light = opts.light || [255, 255, 255];

  const qr = qrcode(0, 'M');
  qr.addData(text);
  qr.make();
  const n = qr.getModuleCount();
  const modules = n + margin * 2;
  const size = modules * scale;

  writeRgbPng(outPath, size, size, (x, y) => {
    const mx = Math.floor(x / scale) - margin;
    const my = Math.floor(y / scale) - margin;
    if (mx < 0 || my < 0 || mx >= n || my >= n) return light;
    return qr.isDark(my, mx) ? dark : light;
  });
  return { width: size, height: size, modules: n };
}

if (require.main === module) {
  const text = process.argv[2] || 'https://swaniedesigns.com/';
  const out =
    process.argv[3] ||
    path.join(__dirname, '..', 'website', 'marketing', '_qr_temp.png');
  const scale = parseInt(process.argv[4] || '14', 10);
  const info = generateQrPng(text, out, { scale, margin: 2 });
  console.log(`Wrote ${out} (${info.width}x${info.height})`);
}

module.exports = { generateQrPng };
