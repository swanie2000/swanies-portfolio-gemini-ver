# One-off sync: insert new pro_gate / pro_plan strings into all values-* locales.
$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot

function Escape-XmlString([string]$s) { $s -replace "'", "\'" }

function New-ProBlock {
    param(
        [string]$ChoosePlan,
        [string]$ValueProps,
        [string]$Monthly,
        [string]$Yearly,
        [string]$Lifetime,
        [string]$BestValue
    )
    $ChoosePlan = Escape-XmlString $ChoosePlan
    $ValueProps = Escape-XmlString $ValueProps
    $Monthly = Escape-XmlString $Monthly
    $Yearly = Escape-XmlString $Yearly
    $Lifetime = Escape-XmlString $Lifetime
    $BestValue = Escape-XmlString $BestValue
    @"
    <string name="pro_gate_choose_plan">$ChoosePlan</string>
    <string name="pro_gate_value_props">$ValueProps</string>
    <string name="pro_gate_brand_line">Swanie\'s Portfolio</string>
    <string name="pro_plan_monthly">$Monthly</string>
    <string name="pro_plan_yearly">$Yearly</string>
    <string name="pro_plan_lifetime">$Lifetime</string>
    <string name="pro_plan_best_value">$BestValue</string>
    <string name="pro_plan_line_format">%1`$s  ·  %2`$s</string>
"@
}

$byLocale = @{
    "values-ar"    = New-ProBlock "اختر خطة أدناه لفتح Pro على هذا الجهاز." "محافظ متعددة · سمات · تحليلات · تخصيص الويدجت" "الخطة الشهرية" "الخطة السنوية" "الوصول مدى الحياة" "أفضل قيمة"
    "values-de"    = New-ProBlock "Wähle unten einen Plan, um Pro auf diesem Gerät freizuschalten." "Mehrere Portfolios · Themes · Analytics · Widget-Anpassung" "Monatsabo" "Jahresabo" "Lebenslanger Zugang" "BESTES ANGEBOT"
    "values-es"    = New-ProBlock "Elige un plan abajo para desbloquear Pro en este dispositivo." "Varios portfolios · Temas · Análisis · Personalización del widget" "Plan mensual" "Plan anual" "Acceso de por vida" "MEJOR VALOR"
    "values-fr"    = New-ProBlock "Choisissez un forfait ci-dessous pour débloquer Pro sur cet appareil." "Portefeuilles multiples · Thèmes · Analyses · Personnalisation du widget" "Forfait mensuel" "Forfait annuel" "Accès à vie" "MEILLEURE OFFRE"
    "values-hi"    = New-ProBlock "इस डिवाइस पर Pro अनलॉक करने के लिए नीचे एक प्लान चुनें।" "कई पोर्टफोलियो · थीम · एनालिटिक्स · विजेट अनुकूलन" "मासिक प्लान" "वार्षिक प्लान" "आजीवन एक्सेस" "सर्वोत्तम मूल्य"
    "values-in"    = New-ProBlock "Pilih paket di bawah untuk membuka Pro di perangkat ini." "Beberapa portofolio · Tema · Analitik · Kustomisasi widget" "Paket bulanan" "Paket tahunan" "Akses seumur hidup" "NILAI TERBAIK"
    "values-it"    = New-ProBlock "Scegli un piano qui sotto per sbloccare Pro su questo dispositivo." "Più portafogli · Temi · Analisi · Personalizzazione widget" "Piano mensile" "Piano annuale" "Accesso a vita" "MIGLIOR OFFERTA"
    "values-ja"    = New-ProBlock "このデバイスで Pro を解放するには、下からプランを選んでください。" "複数ポートフォリオ · テーマ · 分析 · ウィジェットのカスタマイズ" "月額プラン" "年額プラン" "生涯アクセス" "おすすめ"
    "values-ko"    = New-ProBlock "이 기기에서 Pro를 잠금 해제하려면 아래에서 요금제를 선택하세요." "여러 포트폴리오 · 테마 · 분석 · 위젯 사용자 지정" "월간 요금제" "연간 요금제" "평생 이용" "최고의 혜택"
    "values-nl"    = New-ProBlock "Kies hieronder een abonnement om Pro op dit apparaat te ontgrendelen." "Meerdere portfolios · Thema's · Analytics · Widget-aanpassing" "Maandabonnement" "Jaarabonnement" "Levenslange toegang" "BESTE DEAL"
    "values-pl"    = New-ProBlock "Wybierz plan poniżej, aby odblokować Pro na tym urządzeniu." "Wiele portfeli · Motywy · Analityka · Personalizacja widgetu" "Plan miesięczny" "Plan roczny" "Dostęp dożywotni" "NAJLEPSZA OFERTA"
    "values-pt-rBR" = New-ProBlock "Escolha um plano abaixo para desbloquear o Pro neste dispositivo." "Vários portfólios · Temas · Análises · Personalização do widget" "Plano mensal" "Plano anual" "Acesso vitalício" "MELHOR VALOR"
    "values-ru"    = New-ProBlock "Выберите план ниже, чтобы разблокировать Pro на этом устройстве." "Несколько портфелей · Темы · Аналитика · Настройка виджета" "Месячный план" "Годовой план" "Пожизненный доступ" "ЛУЧШЕЕ ПРЕДЛОЖЕНИЕ"
    "values-th"    = New-ProBlock "เลือกแพ็กเกจด้านล่างเพื่อปลดล็อก Pro บนอุปกรณ์นี้" "พอร์ตโฟลิโอหลายรายการ · ธีม · การวิเคราะห์ · ปรับแต่งวิดเจ็ต" "แพ็กเกจรายเดือน" "แพ็กเกจรายปี" "การเข้าถึงตลอดชีพ" "คุ้มที่สุด"
    "values-tr"    = New-ProBlock "Bu cihazda Pro'yu açmak için aşağıdan bir plan seçin." "Birden fazla portföy · Temalar · Analitik · Widget özelleştirme" "Aylık plan" "Yıllık plan" "Ömür boyu erişim" "EN İYİ DEĞER"
    "values-uk"    = New-ProBlock "Оберіть план нижче, щоб розблокувати Pro на цьому пристрої." "Кілька портфелів · Теми · Аналітика · Налаштування віджета" "Місячний план" "Річний план" "Доступ назавжди" "НАЙКРАЩА ПРОПОЗИЦІЯ"
    "values-vi"    = New-ProBlock "Chọn gói bên dưới để mở khóa Pro trên thiết bị này." "Nhiều danh mục · Chủ đề · Phân tích · Tùy chỉnh widget" "Gói tháng" "Gói năm" "Truy cập trọn đời" "GIÁ TRỊ TỐT NHẤT"
    "values-zh-rCN" = New-ProBlock "在下方选择方案，在此设备上解锁 Pro。" "多个投资组合 · 主题 · 分析 · 小组件自定义" "月度方案" "年度方案" "终身访问" "最超值"
    "values-zh-rTW" = New-ProBlock "在下方選擇方案，在此裝置上解鎖 Pro。" "多個投資組合 · 主題 · 分析 · 小工具自訂" "月度方案" "年度方案" "終身存取" "最超值"
}

foreach ($locale in $byLocale.Keys) {
    $path = Join-Path $repoRoot "app\src\main\res\$locale\strings.xml"
    if (-not (Test-Path $path)) { throw "Missing $path" }
    $text = Get-Content $path -Raw -Encoding UTF8
    if ($text -match 'pro_gate_choose_plan') {
        Write-Host "Skip $locale (already has pro_gate_choose_plan)"
        continue
    }
    $block = $byLocale[$locale]
    $newText = [regex]::Replace(
        $text,
        '(?m)(^\s*<string name="pro_gate_subtitle">.*</string>\r?\n)',
        "`${1}$block",
        1
    )
    if ($newText -eq $text) { throw "Could not insert into $locale" }
    [System.IO.File]::WriteAllText($path, $newText, [System.Text.UTF8Encoding]::new($false))
    Write-Host "Updated $locale"
}

Write-Host "Done."
