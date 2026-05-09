import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, "..");

const args = process.argv.slice(2);
const svgPath = path.isAbsolute(args[0] ?? "")
  ? args[0]
  : path.join(root, args[0] ?? "docs/assets/swan_asset_launcher_vector_ultra_small.svg");
const outPath = path.isAbsolute(args[1] ?? "")
  ? args[1]
  : path.join(
      root,
      args[1] ?? "app/src/main/res/drawable/ic_launcher_foreground_vector.xml",
    );

const s = fs.readFileSync(svgPath, "utf8");
const dm = s.match(/<path[^>]+d="([^"]+)"/);
if (!dm) throw new Error("No path d= (single <path d=\"...\"> required)");
const d = dm[1];
const vb = s.match(/viewBox="0 0 (\d+) (\d+)"/);
if (!vb) throw new Error("No viewBox=\"0 0 W H\"");
const vw = vb[1];
const vh = vb[2];

/** Keep each pathData under ~32k (aapt STRING_TOO_LARGE); split at last closepath in range. */
const MAX_CHUNK = 28000;
function splitPathData(path) {
  const out = [];
  let i = 0;
  while (i < path.length) {
    const rest = path.length - i;
    if (rest <= MAX_CHUNK) {
      out.push(path.slice(i));
      break;
    }
    const end = i + MAX_CHUNK;
    const seg = path.slice(i, end);
    let rel = Math.max(seg.lastIndexOf("z"), seg.lastIndexOf("Z"));
    if (rel < 0) {
      throw new Error(
        `No z/Z in path slice [${i}, ${end}); cannot split safely (need subpath boundaries).`,
      );
    }
    const cut = i + rel;
    out.push(path.slice(i, cut + 1));
    i = cut + 1;
  }
  return out;
}

const chunks = splitPathData(d);
const pathEls = chunks
  .map(
    (chunk) =>
      `    <path
        android:fillColor="#FFFFFF"
        android:pathData="${chunk}" />`,
  )
  .join("\n");
const xml = `<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="${vw}dp"
    android:height="${vh}dp"
    android:viewportWidth="${vw}"
    android:viewportHeight="${vh}">
${pathEls}
</vector>
`;
fs.writeFileSync(outPath, xml);
console.log("Input:", svgPath);
console.log("pathData total length:", d.length);
console.log("chunks:", chunks.length, chunks.map((c) => c.length));
console.log("Wrote:", outPath);
