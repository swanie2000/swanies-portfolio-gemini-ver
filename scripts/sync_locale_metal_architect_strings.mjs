/**
 * One-shot: insert 20 string keys present in values/strings.xml but missing from each values-xx/strings.xml.
 * Run from repo root: node scripts/sync_locale_metal_architect_strings.mjs
 */
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const RES = path.join(__dirname, "..", "app", "src", "main", "res");

/** Escape Android string inner text (single-quoted attribute style-safe for ') */
function esc(s) {
  return s.replace(/\\/g, "\\\\").replace(/'/g, "\\'").replace(/\n/g, "\\n");
}

const T = {
  ar: {
    action_clear: "مسح",
    architect_step_1_of_3: "الخطوة 1 من 3",
    architect_step_2_of_3: "الخطوة 2 من 3",
    architect_step_3_of_3: "الخطوة 3 من 3",
    architect_next_to_step: "التالي: الخطوة %1$d من 3",
    architect_saving_in_progress: "جاري الحفظ…",
    architect_premium_per_unit: "العلاوة / لكل ($)",
    architect_premium_once: "للمرة الواحدة ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "لكل وحدة",
    architect_premium_mode_once: "مرة واحدة",
    architect_icon_cta_add: "إضافة أصل",
    architect_icon_cta_update: "تحديث",
    metals_market_screen_title: "سوق المعادن",
    metal_name_gold: "ذهب",
    metal_name_silver: "فضة",
    metal_name_platinum: "بلاتين",
    metal_name_palladium: "بالاديوم",
    add_asset_screen_title: "إضافة أصل",
    asset_picker_custom_metal: "مخصص",
  },
  de: {
    action_clear: "LEEREN",
    architect_step_1_of_3: "Schritt 1 von 3",
    architect_step_2_of_3: "Schritt 2 von 3",
    architect_step_3_of_3: "Schritt 3 von 3",
    architect_next_to_step: "WEITER: Schritt %1$d von 3",
    architect_saving_in_progress: "Wird gespeichert…",
    architect_premium_per_unit: "AUFGELD / STÜCK ($)",
    architect_premium_once: "EINMALIG ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Pro Stück",
    architect_premium_mode_once: "Einmalig",
    architect_icon_cta_add: "ANLAGE HINZUFÜGEN",
    architect_icon_cta_update: "AKTUALISIEREN",
    metals_market_screen_title: "METALLMARKT",
    metal_name_gold: "GOLD",
    metal_name_silver: "SILBER",
    metal_name_platinum: "PLATIN",
    metal_name_palladium: "PALLADIUM",
    add_asset_screen_title: "ANLAGE HINZUFÜGEN",
    asset_picker_custom_metal: "INDIVIDUELL",
  },
  es: {
    action_clear: "LIMPIAR",
    architect_step_1_of_3: "Paso 1 de 3",
    architect_step_2_of_3: "Paso 2 de 3",
    architect_step_3_of_3: "Paso 3 de 3",
    architect_next_to_step: "SIGUIENTE: paso %1$d de 3",
    architect_saving_in_progress: "Guardando…",
    architect_premium_per_unit: "PRIMA / CADA UNO ($)",
    architect_premium_once: "ÚNICA VEZ ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Por cada uno",
    architect_premium_mode_once: "Una vez",
    architect_icon_cta_add: "AÑADIR ACTIVO",
    architect_icon_cta_update: "ACTUALIZAR",
    metals_market_screen_title: "MERCADO DE METALES",
    metal_name_gold: "ORO",
    metal_name_silver: "PLATA",
    metal_name_platinum: "PLATINO",
    metal_name_palladium: "PALADIO",
    add_asset_screen_title: "AÑADIR ACTIVO",
    asset_picker_custom_metal: "PERSONALIZADO",
  },
  fr: {
    action_clear: "EFFACER",
    architect_step_1_of_3: "Étape 1 sur 3",
    architect_step_2_of_3: "Étape 2 sur 3",
    architect_step_3_of_3: "Étape 3 sur 3",
    architect_next_to_step: "SUIVANT : étape %1$d sur 3",
    architect_saving_in_progress: "Enregistrement…",
    architect_premium_per_unit: "PRIME / CHAQUE ($)",
    architect_premium_once: "UNIQUE ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Par unité",
    architect_premium_mode_once: "Une fois",
    architect_icon_cta_add: "AJOUTER L'ACTIF",
    architect_icon_cta_update: "METTRE À JOUR",
    metals_market_screen_title: "MARCHÉ DES MÉTAUX",
    metal_name_gold: "OR",
    metal_name_silver: "ARGENT",
    metal_name_platinum: "PLATINE",
    metal_name_palladium: "PALLADIUM",
    add_asset_screen_title: "AJOUTER UN ACTIF",
    asset_picker_custom_metal: "PERSONNALISÉ",
  },
  hi: {
    action_clear: "साफ़ करें",
    architect_step_1_of_3: "चरण 1/3",
    architect_step_2_of_3: "चरण 2/3",
    architect_step_3_of_3: "चरण 3/3",
    architect_next_to_step: "अगला: चरण %1$d/3",
    architect_saving_in_progress: "सहेजा जा रहा है…",
    architect_premium_per_unit: "प्रीमियम / प्रति ($)",
    architect_premium_once: "एक बार ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "प्रति इकाई",
    architect_premium_mode_once: "एक बार",
    architect_icon_cta_add: "संपत्ति जोड़ें",
    architect_icon_cta_update: "अपडेट करें",
    metals_market_screen_title: "धातु बाज़ार",
    metal_name_gold: "सोना",
    metal_name_silver: "चाँदी",
    metal_name_platinum: "प्लैटिनम",
    metal_name_palladium: "पैलेडियम",
    add_asset_screen_title: "संपत्ति जोड़ें",
    asset_picker_custom_metal: "कस्टम",
  },
  in: {
    action_clear: "BERSIHKAN",
    architect_step_1_of_3: "Langkah 1 dari 3",
    architect_step_2_of_3: "Langkah 2 dari 3",
    architect_step_3_of_3: "Langkah 3 dari 3",
    architect_next_to_step: "Berikutnya: Langkah %1$d dari 3",
    architect_saving_in_progress: "Menyimpan…",
    architect_premium_per_unit: "PREMI / SATUAN ($)",
    architect_premium_once: "SEKALI BAYAR ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Per satuan",
    architect_premium_mode_once: "Sekali",
    architect_icon_cta_add: "TAMBAH ASET",
    architect_icon_cta_update: "PERBARUI",
    metals_market_screen_title: "PASAR LOGAM",
    metal_name_gold: "EMAS",
    metal_name_silver: "PERAK",
    metal_name_platinum: "PLATINA",
    metal_name_palladium: "PALADIUM",
    add_asset_screen_title: "TAMBAH ASET",
    asset_picker_custom_metal: "KHUSUS",
  },
  it: {
    action_clear: "CANCELLA",
    architect_step_1_of_3: "Passaggio 1 di 3",
    architect_step_2_of_3: "Passaggio 2 di 3",
    architect_step_3_of_3: "Passaggio 3 di 3",
    architect_next_to_step: "AVANTI: passaggio %1$d di 3",
    architect_saving_in_progress: "Salvataggio…",
    architect_premium_per_unit: "PREMIO / CAD. ($)",
    architect_premium_once: "UNA TANTUM ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Per ciascuno",
    architect_premium_mode_once: "Una volta",
    architect_icon_cta_add: "AGGIUNGI ASSET",
    architect_icon_cta_update: "AGGIORNA",
    metals_market_screen_title: "MERCATO METALLI",
    metal_name_gold: "ORO",
    metal_name_silver: "ARGENTO",
    metal_name_platinum: "PLATINO",
    metal_name_palladium: "PALLADIO",
    add_asset_screen_title: "AGGIUNGI ASSET",
    asset_picker_custom_metal: "PERSONALIZZATO",
  },
  ja: {
    action_clear: "クリア",
    architect_step_1_of_3: "ステップ1/3",
    architect_step_2_of_3: "ステップ2/3",
    architect_step_3_of_3: "ステップ3/3",
    architect_next_to_step: "次：ステップ%1$d/3",
    architect_saving_in_progress: "保存中…",
    architect_premium_per_unit: "プレミアム/各 ($)",
    architect_premium_once: "一回限り ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "各単位ごと",
    architect_premium_mode_once: "一度だけ",
    architect_icon_cta_add: "資産を追加",
    architect_icon_cta_update: "更新",
    metals_market_screen_title: "貴金属マーケット",
    metal_name_gold: "金",
    metal_name_silver: "銀",
    metal_name_platinum: "プラチナ",
    metal_name_palladium: "パラジウム",
    add_asset_screen_title: "資産を追加",
    asset_picker_custom_metal: "カスタム",
  },
  ko: {
    action_clear: "지우기",
    architect_step_1_of_3: "1/3단계",
    architect_step_2_of_3: "2/3단계",
    architect_step_3_of_3: "3/3단계",
    architect_next_to_step: "다음: %1$d/3단계",
    architect_saving_in_progress: "저장 중…",
    architect_premium_per_unit: "프리미엄/개 ($)",
    architect_premium_once: "일회성 ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "개당",
    architect_premium_mode_once: "한 번",
    architect_icon_cta_add: "자산 추가",
    architect_icon_cta_update: "업데이트",
    metals_market_screen_title: "귀금속 시장",
    metal_name_gold: "금",
    metal_name_silver: "은",
    metal_name_platinum: "백금",
    metal_name_palladium: "팔라듐",
    add_asset_screen_title: "자산 추가",
    asset_picker_custom_metal: "사용자 지정",
  },
  nl: {
    action_clear: "WISSEN",
    architect_step_1_of_3: "Stap 1 van 3",
    architect_step_2_of_3: "Stap 2 van 3",
    architect_step_3_of_3: "Stap 3 van 3",
    architect_next_to_step: "VOLGENDE: stap %1$d van 3",
    architect_saving_in_progress: "Opslaan…",
    architect_premium_per_unit: "PREMIE / STUK ($)",
    architect_premium_once: "EENMALIG ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Per stuk",
    architect_premium_mode_once: "Eenmalig",
    architect_icon_cta_add: "ACTIVA TOEVOEGEN",
    architect_icon_cta_update: "BIJWERKEN",
    metals_market_screen_title: "METALENMARKT",
    metal_name_gold: "GOUD",
    metal_name_silver: "ZILVER",
    metal_name_platinum: "PLATINA",
    metal_name_palladium: "PALLADIUM",
    add_asset_screen_title: "ACTIVA TOEVOEGEN",
    asset_picker_custom_metal: "AANGEPAST",
  },
  pl: {
    action_clear: "WYCZYŚĆ",
    architect_step_1_of_3: "Krok 1 z 3",
    architect_step_2_of_3: "Krok 2 z 3",
    architect_step_3_of_3: "Krok 3 z 3",
    architect_next_to_step: "DALEJ: krok %1$d z 3",
    architect_saving_in_progress: "Zapisywanie…",
    architect_premium_per_unit: "PREMIA / ZA SZT. ($)",
    architect_premium_once: "JEDNORAZOWO ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Za sztukę",
    architect_premium_mode_once: "Jednorazowo",
    architect_icon_cta_add: "DODAJ AKTYW",
    architect_icon_cta_update: "AKTUALIZUJ",
    metals_market_screen_title: "RYNEK METALI",
    metal_name_gold: "ZŁOTO",
    metal_name_silver: "SREBRO",
    metal_name_platinum: "PLATYNA",
    metal_name_palladium: "PALLAD",
    add_asset_screen_title: "DODAJ AKTYW",
    asset_picker_custom_metal: "NIESTANDARDOWY",
  },
  "pt-rBR": {
    action_clear: "LIMPAR",
    architect_step_1_of_3: "Etapa 1 de 3",
    architect_step_2_of_3: "Etapa 2 de 3",
    architect_step_3_of_3: "Etapa 3 de 3",
    architect_next_to_step: "PRÓXIMO: etapa %1$d de 3",
    architect_saving_in_progress: "Salvando…",
    architect_premium_per_unit: "PRÊMIO / UNID. ($)",
    architect_premium_once: "ÚNICO ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Por unidade",
    architect_premium_mode_once: "Uma vez",
    architect_icon_cta_add: "ADICIONAR ATIVO",
    architect_icon_cta_update: "ATUALIZAR",
    metals_market_screen_title: "MERCADO DE METAIS",
    metal_name_gold: "OURO",
    metal_name_silver: "PRATA",
    metal_name_platinum: "PLATINA",
    metal_name_palladium: "PALÁDIO",
    add_asset_screen_title: "ADICIONAR ATIVO",
    asset_picker_custom_metal: "PERSONALIZADO",
  },
  ru: {
    action_clear: "ОЧИСТИТЬ",
    architect_step_1_of_3: "Шаг 1 из 3",
    architect_step_2_of_3: "Шаг 2 из 3",
    architect_step_3_of_3: "Шаг 3 из 3",
    architect_next_to_step: "ДАЛЕЕ: шаг %1$d из 3",
    architect_saving_in_progress: "Сохранение…",
    architect_premium_per_unit: "ПРЕМИЯ / ЗА ЕД. ($)",
    architect_premium_once: "РАЗОВО ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "За каждую",
    architect_premium_mode_once: "Один раз",
    architect_icon_cta_add: "ДОБАВИТЬ АКТИВ",
    architect_icon_cta_update: "ОБНОВИТЬ",
    metals_market_screen_title: "РЫНОК МЕТАЛЛОВ",
    metal_name_gold: "ЗОЛОТО",
    metal_name_silver: "СЕРЕБРО",
    metal_name_platinum: "ПЛАТИНА",
    metal_name_palladium: "ПАЛЛАДИЙ",
    add_asset_screen_title: "ДОБАВИТЬ АКТИВ",
    asset_picker_custom_metal: "СВОЙ",
  },
  th: {
    action_clear: "ล้าง",
    architect_step_1_of_3: "ขั้นตอนที่ 1 จาก 3",
    architect_step_2_of_3: "ขั้นตอนที่ 2 จาก 3",
    architect_step_3_of_3: "ขั้นตอนที่ 3 จาก 3",
    architect_next_to_step: "ถัดไป: ขั้นตอนที่ %1$d จาก 3",
    architect_saving_in_progress: "กำลังบันทึก…",
    architect_premium_per_unit: "พรีเมียม/ชิ้น ($)",
    architect_premium_once: "ครั้งเดียว ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "ต่อหน่วย",
    architect_premium_mode_once: "หนึ่งครั้ง",
    architect_icon_cta_add: "เพิ่มสินทรัพย์",
    architect_icon_cta_update: "อัปเดต",
    metals_market_screen_title: "ตลาดโลหะมีค่า",
    metal_name_gold: "ทอง",
    metal_name_silver: "เงิน",
    metal_name_platinum: "แพลทินัม",
    metal_name_palladium: "พัลเลเดียม",
    add_asset_screen_title: "เพิ่มสินทรัพย์",
    asset_picker_custom_metal: "กำหนดเอง",
  },
  tr: {
    action_clear: "TEMİZLE",
    architect_step_1_of_3: "1/3. adım",
    architect_step_2_of_3: "2/3. adım",
    architect_step_3_of_3: "3/3. adım",
    architect_next_to_step: "İLERİ: %1$d/3. adım",
    architect_saving_in_progress: "Kaydediliyor…",
    architect_premium_per_unit: "PRİM / ADET ($)",
    architect_premium_once: "TEK SEFER ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Her biri için",
    architect_premium_mode_once: "Bir kez",
    architect_icon_cta_add: "VARLIK EKLE",
    architect_icon_cta_update: "GÜNCELLE",
    metals_market_screen_title: "METAL PİYASASI",
    metal_name_gold: "ALTIN",
    metal_name_silver: "GÜMÜŞ",
    metal_name_platinum: "PLATİN",
    metal_name_palladium: "PALADYUM",
    add_asset_screen_title: "VARLIK EKLE",
    asset_picker_custom_metal: "ÖZEL",
  },
  uk: {
    action_clear: "ОЧИСТИТИ",
    architect_step_1_of_3: "Крок 1 з 3",
    architect_step_2_of_3: "Крок 2 з 3",
    architect_step_3_of_3: "Крок 3 з 3",
    architect_next_to_step: "ДАЛІ: крок %1$d з 3",
    architect_saving_in_progress: "Збереження…",
    architect_premium_per_unit: "ПРЕМІЯ / ЗА ОД. ($)",
    architect_premium_once: "РАЗОВО ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "За кожну",
    architect_premium_mode_once: "Один раз",
    architect_icon_cta_add: "ДОДАТИ АКТИВ",
    architect_icon_cta_update: "ОНОВИТИ",
    metals_market_screen_title: "РИНОК МЕТАЛІВ",
    metal_name_gold: "ЗОЛОТО",
    metal_name_silver: "Срібло",
    metal_name_platinum: "ПЛАТИНА",
    metal_name_palladium: "ПАЛАДІЙ",
    add_asset_screen_title: "ДОДАТИ АКТИВ",
    asset_picker_custom_metal: "ВЛАСНИЙ",
  },
  vi: {
    action_clear: "XÓA",
    architect_step_1_of_3: "Bước 1/3",
    architect_step_2_of_3: "Bước 2/3",
    architect_step_3_of_3: "Bước 3/3",
    architect_next_to_step: "TIẾP: Bước %1$d/3",
    architect_saving_in_progress: "Đang lưu…",
    architect_premium_per_unit: "PHỤ PHÍ / MỖI CÁI ($)",
    architect_premium_once: "MỘT LẦN ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "Mỗi cái",
    architect_premium_mode_once: "Một lần",
    architect_icon_cta_add: "THÊM TÀI SẢN",
    architect_icon_cta_update: "CẬP NHẬT",
    metals_market_screen_title: "THỊ TRƯỜNG KIM LOẠI",
    metal_name_gold: "VÀNG",
    metal_name_silver: "BẠC",
    metal_name_platinum: "BẠCH KIM",
    metal_name_palladium: "PALADI",
    add_asset_screen_title: "THÊM TÀI SẢN",
    asset_picker_custom_metal: "TÙY CHỈNH",
  },
  "zh-rCN": {
    action_clear: "清除",
    architect_step_1_of_3: "第 1 步，共 3 步",
    architect_step_2_of_3: "第 2 步，共 3 步",
    architect_step_3_of_3: "第 3 步，共 3 步",
    architect_next_to_step: "下一步：第 %1$d 步，共 3 步",
    architect_saving_in_progress: "正在保存…",
    architect_premium_per_unit: "溢价/每件 ($)",
    architect_premium_once: "一次性 ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "每件",
    architect_premium_mode_once: "一次",
    architect_icon_cta_add: "添加资产",
    architect_icon_cta_update: "更新",
    metals_market_screen_title: "贵金属市场",
    metal_name_gold: "黄金",
    metal_name_silver: "白银",
    metal_name_platinum: "铂金",
    metal_name_palladium: "钯金",
    add_asset_screen_title: "添加资产",
    asset_picker_custom_metal: "自定义",
  },
  "zh-rTW": {
    action_clear: "清除",
    architect_step_1_of_3: "第 1 步（共 3 步）",
    architect_step_2_of_3: "第 2 步（共 3 步）",
    architect_step_3_of_3: "第 3 步（共 3 步）",
    architect_next_to_step: "下一步：第 %1$d 步（共 3 步）",
    architect_saving_in_progress: "正在儲存…",
    architect_premium_per_unit: "溢價/每件 ($)",
    architect_premium_once: "一次性 ($)",
    architect_premium_placeholder: "(+-) $0.00",
    architect_premium_mode_per: "每件",
    architect_premium_mode_once: "一次",
    architect_icon_cta_add: "新增資產",
    architect_icon_cta_update: "更新",
    metals_market_screen_title: "貴金屬市場",
    metal_name_gold: "黃金",
    metal_name_silver: "白銀",
    metal_name_platinum: "鉑金",
    metal_name_palladium: "鈀金",
    add_asset_screen_title: "新增資產",
    asset_picker_custom_metal: "自訂",
  },
};

function patchXml(xml, localeKey) {
  const t = T[localeKey];
  if (!t) throw new Error(`No translations for ${localeKey}`);

  const blockActionClear = `    <string name="action_clear">${esc(t.action_clear)}</string>\n`;

  if (!xml.includes('name="action_clear"')) {
    xml = xml.replace(
      /(<string name="action_delete">[^<]*<\/string>\r?\n)/,
      `$1${blockActionClear}`
    );
  }

  if (!xml.includes('name="architect_step_1_of_3"')) {
    xml = xml.replace(
      /(<string name="architect_stage_blueprint">[^<]*<\/string>\r?\n)(    <string name="architect_stage_live_card">)/,
      `$1    <string name="architect_step_1_of_3">${esc(t.architect_step_1_of_3)}</string>\n$2`
    );
  }

  if (!xml.includes('name="architect_step_2_of_3"')) {
    xml = xml.replace(
      /(<string name="architect_stage_live_card">[^<]*<\/string>\r?\n)(    <string name="architect_select_metal">)/,
      `$1    <string name="architect_step_2_of_3">${esc(t.architect_step_2_of_3)}</string>\n    <string name="architect_step_3_of_3">${esc(t.architect_step_3_of_3)}</string>\n$2`
    );
  }

  if (!xml.includes('name="architect_next_to_step"')) {
    xml = xml.replace(
      /(<string name="architect_select_unit">[^<]*<\/string>\r?\n)(    <string name="architect_proceed_to_card">)/,
      `$1    <string name="architect_next_to_step">${esc(t.architect_next_to_step)}</string>\n    <string name="architect_saving_in_progress">${esc(t.architect_saving_in_progress)}</string>\n$2`
    );
  }

  if (!xml.includes('name="architect_premium_per_unit"')) {
    xml = xml.replace(
      /(<string name="architect_premium">[^<]*<\/string>\r?\n)(    <string name="architect_estimated_value">)/,
      `$1    <string name="architect_premium_per_unit">${esc(t.architect_premium_per_unit)}</string>\n    <string name="architect_premium_once">${esc(t.architect_premium_once)}</string>\n    <string name="architect_premium_placeholder">${esc(t.architect_premium_placeholder)}</string>\n    <string name="architect_premium_mode_per">${esc(t.architect_premium_mode_per)}</string>\n    <string name="architect_premium_mode_once">${esc(t.architect_premium_mode_once)}</string>\n$2`
    );
  }

  if (!xml.includes('name="architect_icon_cta_add"')) {
    xml = xml.replace(
      /(<string name="architect_estimated_value">[^<]*<\/string>\r?\n)(    <string name="architect_finalize_vault">)/,
      `$1    <string name="architect_icon_cta_add">${esc(t.architect_icon_cta_add)}</string>\n    <string name="architect_icon_cta_update">${esc(t.architect_icon_cta_update)}</string>\n$2`
    );
  }

  if (!xml.includes('name="metals_market_screen_title"')) {
    xml = xml.replace(
      /(<string name="holdings_tab_metal">[^<]*<\/string>\r?\n)(    <string name="asset_custom_icon_section">)/,
      `$1    <string name="metals_market_screen_title">${esc(t.metals_market_screen_title)}</string>\n    <string name="metal_name_gold">${esc(t.metal_name_gold)}</string>\n    <string name="metal_name_silver">${esc(t.metal_name_silver)}</string>\n    <string name="metal_name_platinum">${esc(t.metal_name_platinum)}</string>\n    <string name="metal_name_palladium">${esc(t.metal_name_palladium)}</string>\n    <string name="add_asset_screen_title">${esc(t.add_asset_screen_title)}</string>\n    <string name="asset_picker_custom_metal">${esc(t.asset_picker_custom_metal)}</string>\n$2`
    );
  }

  return xml;
}

const dirs = fs.readdirSync(RES).filter((d) => d.startsWith("values-") && d !== "values-night");
let n = 0;
for (const d of dirs) {
  const f = path.join(RES, d, "strings.xml");
  if (!fs.existsSync(f)) continue;
  const localeKey = d.replace("values-", "");
  if (!T[localeKey]) {
    console.warn("Skip (no map):", d);
    continue;
  }
  let xml = fs.readFileSync(f, "utf8");
  const before = xml;
  xml = patchXml(xml, localeKey);
  if (xml !== before) {
    fs.writeFileSync(f, xml, "utf8");
    n++;
    console.log("Patched", d);
  } else {
    console.log("No changes", d);
  }
}
console.log("Done, patched files:", n);
