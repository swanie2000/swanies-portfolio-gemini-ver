# Patches architect / funnel / crypto / asset / navigation strings (UTF-8 locales).
# Japanese and zh-rCN: edit strings.xml with a UTF-8-safe editor — do not add them here (PS default encoding mangles CJK).
# Run: powershell -NoProfile -ExecutionPolicy Bypass -File scripts/apply_locale_architect_funnel_batch.ps1
$ErrorActionPreference = 'Stop'
$root = Join-Path $PSScriptRoot '..\app\src\main\res' | Resolve-Path

$rows = @'
key|de|fr|it|ptBR|ja|zhCN|ar|ru
action_back|ZURUCK|RETOUR|INDIETRO|VOLTAR|戻る|返回|رجوع|НАЗАД
action_delete|LOSCHEN|SUPPRIMER|ELIMINA|EXCLUIR|削除|删除|حذف|УДАЛИТЬ
action_finalize|ABSCHLIESSEN|FINALISER|FINALIZA|FINALIZAR|確定|完成|إنهاء|ЗАВЕРШИТЬ
action_next|WEITER|SUIVANT|AVANTI|PROXIMO|次へ|下一步|التالي|ДАЛЕЕ
action_save|SPEICHERN|ENREGISTRER|SALVA|SALVAR|保存|保存|حفظ|СОХРАНИТЬ
action_save_changes|ANDERUNGEN SPEICHERN|ENREGISTRER LES MODIFICATIONS|SALVA MODIFICHE|SALVAR ALTERACOES|変更を保存|保存更改|حفظ التغييرات|СОХРАНИТЬ ИЗМЕНЕНИЯ
amount_entry_back|Zuruck|Retour|Indietro|Voltar|戻る|返回|رجوع|Назад
amount_entry_discard_body|Neues Vermogen wirklich verwerfen?|Abandonner ce nouvel actif ?|Scartare questo nuovo asset?|Descartar este novo ativo?|この新しい資産を破棄しますか?|确定放弃这项新资产?|هل تريد تجاهل هذا الأصل الجديد؟|Отменить этот новый актив?
amount_entry_discard_title|Vermogen verwerfen?|Abandonner l actif ?|Scartare asset?|Descartar ativo?|資産を破棄?|放弃资产?|تجاهل الأصل؟|Отменить актив?
amount_entry_label|Betrag fur %1$s|Saisir le montant pour %1$s|Importo per %1$s|Quantidade para %1$s|%1$s の数量を入力|输入 %1$s 的数量|أدخل الكمية لـ %1$s|Введите количество для %1$s
amount_entry_no|Nein|Non|No|Nao|いいえ|否|لا|Нет
amount_entry_save_asset|Vermogen speichern|Enregistrer l actif|Salva asset|Salvar ativo|資産を保存|保存资产|حفظ الأصل|Сохранить актив
amount_entry_yes|Ja|Oui|Si|Sim|はい|是|نعم|Да
architect_back_to_blueprint|Zuruck zum Entwurf|Retour au plan|Torna al progetto|Voltar ao plano|設計に戻る|返回蓝图|العودة إلى المخطط|К чертежу
architect_estimated_value|GESCHATZTER WERT|VALEUR ESTIMEE|VALORE STIMATO|VALOR ESTIMADO|見積もり価値|预估价值|القيمة المقدرة|ОЦЕНОЧНАЯ СТОИМОСТЬ
architect_finalize_vault|ABSCHLIESSEN UND VAULT|FINALISER ET COFFRE|FINALIZZA E VAULT|FINALIZAR E COFRE|確定して保管|完成并存入保管库|إنهاء والخزنة|ЗАВЕРШИТЬ И ХРАНИЛИЩЕ
architect_premium|AGIO ($)|PRIME ($)|PREMIO ($)|PREMIO ($)|プレミアム ($)|溢价 ($)|علاوة ($)|ПРЕМИЯ ($)
architect_proceed_to_card|WEITER ZUR KARTE|ALLER A LA CARTE|VAI ALLA SCHEDA|IR PARA O CARTAO|カードへ進む|进入卡片|متابعة إلى البطاقة|К КАРТОЧКЕ
architect_quantity|MENGE|QUANTITE|QUANTITA|QUANTIDADE|数量|数量|الكمية|КОЛИЧЕСТВО
architect_select_metal|METALL WAHLEN|CHOISIR LE METAL|SELEZIONA METALLO|SELECIONAR METAL|金属を選択|选择金属|اختر المعدن|ВЫБРАТЬ МЕТАЛЛ
architect_select_shape|FORM WAHLEN|CHOISIR LA FORME|SELEZIONA FORMA|SELECIONAR FORMA|形状を選択|选择形状|اختر الشكل|ВЫБРАТЬ ФОРМУ
architect_select_unit|EINHEIT WAHLEN|CHOISIR L UNITE|SELEZIONA UNITA|SELECIONAR UNIDADE|単位を選択|选择单位|اختر الوحدة|ВЫБРАТЬ ЕДИНИЦУ
architect_spot_price|SPOTPREIS|PRIX SPOT|PREZZO SPOT|PRECO SPOT|スポット価格|现货价格|سعر السبوت|СПОТ-ЦЕНА
architect_stage_blueprint|STUFE 1: ENTWURF|ETAPE 1 : PLAN|FASE 1: PROGETTO|ETAPA 1: PLANO|ステージ1: 設計|阶段1: 蓝图|المرحلة 1: المخطط|ЭТАП 1: ЧЕРТЕЖ
architect_stage_live_card|STUFE 2: LIVE-KARTE|ETAPE 2 : CARTE ACTIVE|FASE 2: SCHEDA LIVE|ETAPA 2: CARTAO AO VIVO|ステージ2: ライブカード|阶段2: 实时卡片|المرحلة 2: البطاقة الحية|ЭТАП 2: ЖИВАЯ КАРТОЧКА
asset_price_label_price|PREIS|PRIX|PREZZO|PRECO|価格|价格|السعر|ЦЕНА
asset_price_label_value|WERT|VALORE|VALORE|VALOR|価値|价值|القيمة|СТОИМОСТЬ
asset_total_value_label|GESAMTWERT|VALEUR TOTALE|VALORE TOTALE|VALOR TOTAL|合計価値|总价值|القيمة الإجمالية|ОБЩАЯ СТОИМОСТЬ
crypto_price_decimals|KURSNACHKOMMASTELLEN: %1$d|DECIMALES DU PRIX : %1$d|DECIMALI PREZZO: %1$d|DECIMAIS DO PRECO: %1$d|価格の小数桁: %1$d|价格小数位: %1$d|منازل عشرية للسعر: %1$d|ЗНАКОВ ПОСЛЕ ЗАПЯТОЙ: %1$d
crypto_quantity_held|GEHALTENE MENGE|QUANTITE DETENUE|QUANTITA DETENUTA|QUANTIDADE EM POSSE|保有数量|持有数量|الكمية المحتفظ بها|ДЕРЖИМОЕ КОЛИЧЕСТВО
crypto_settings_title|%1$s EINSTELLUNGEN|PARAMETRES %1$s|IMPOSTAZIONI %1$s|CONFIGURACOES %1$s|%1$s 設定|%1$s 设置|إعدادات %1$s|НАСТРОЙКИ %1$s
funnel_description_lines|BESCHREIBUNGSTEXTE|LIGNES DE DESCRIPTION|RIGHE DESCRIZIONE|LINHAS DE DESCRICAO|説明文の行|描述行|أسطر الوصف|СТРОКИ ОПИСАНИЯ
funnel_enter_name|Name eingeben...|Entrer le nom...|Inserisci nome...|Digite o nome...|名前を入力...|输入名称...|أدخل الاسم...|Введите имя...
funnel_enter_quantity|Menge eingeben...|Entrer la quantite...|Inserisci quantita...|Digite a quantidade...|数量を入力...|输入数量...|أدخل الكمية...|Введите количество...
funnel_icon|SYMBOL|ICONE|ICONA|ICONE|アイコン|图标|أيقونة|ИКОНКА
funnel_label_under_icon|LABEL UNTER SYMBOL|ETIQUETTE SOUS L ICONE|ETICHETTA SOTTO ICONA|ROTULO SOBRE O ICONE|アイコン下のラベル|图标下标签|تسمية تحت الأيقونة|ПОДПИСЬ ПОД ИКОНКОЙ
funnel_labels|ETIKETTEN|ETIQUETTES|ETICHETTE|ETIQUETAS|ラベル|标签|التسميات|МЕТКИ
funnel_line_1|Zeile 1...|Ligne 1...|Riga 1...|Linha 1...|行1...|第1行...|السطر 1...|Строка 1...
funnel_line_2|Zeile 2...|Ligne 2...|Riga 2...|Linha 2...|行2...|第2行...|السطر 2...|Строка 2...
funnel_name|NAME|NOM|NOME|NOME|名前|名称|الاسم|ИМЯ
funnel_premium|PRÄMIUM|PRIME|PREMIO|PREMIO|プレミアム|溢价|العلاوة|ПРЕМИЯ
funnel_quantity|MENGE|QUANTITE|QUANTITA|QUANTIDADE|数量|数量|الكمية|КОЛИЧЕСТВО
funnel_select_type|TYP WAHLEN|CHOISIR LE TYPE|SELEZIONA TIPO|SELECIONAR TIPO|タイプを選択|选择类型|اختر النوع|ВЫБРАТЬ ТИП
funnel_tap_photo|FOTO TIPPEN (ODER SCHWAN)|APPUYER SUR PHOTO (OU CYGNE)|TOCCA FOTO (O CIGNO)|TOQUE NA FOTO (OU CISNE)|写真をタップ（または白鳥）|点按照片（或天鹅）|اضغط على الصورة (أو البجعة)|НАЖМИТЕ НА ФОТО (ИЛИ ЛЕБЕДЯ)
funnel_unit_value|EINHEITSWERT|VALEUR UNITAIRE|VALORE UNITARIO|VALOR UNITARIO|単価|单价|قيمة الوحدة|ЦЕНА ЗА ЕДИНИЦУ
funnel_value|WERT|VALEUR|VALORE|VALOR|価値|价值|القيمة|СТОИМОСТЬ
sparkline_gathering_data|Daten werden geladen...|Collecte des donnees...|Raccolta dati...|Obtendo dados...|データ取得中...|正在获取数据...|جمع البيانات...|Загрузка данных...
portfolio_delete_body|%1$s dauerhaft löschen?|Supprimer definitivement %1$s ?|Eliminare definitivamente %1$s?|Apagar permanentemente %1$s?|%1$s を完全に削除しますか?|永久删除 %1$s？|حذف %1$s نهائيا؟|Удалить «%1$s» навсегда?
portfolio_delete_title|PORTFOLIO LOSCHEN?|SUPPRIMER LE PORTEFEUILLE ?|ELIMINARE PORTFOLIO?|EXCLUIR CARTEIRA?|ポートフォリオを削除?|删除投资组合？|حذف المحفظة؟|УДАЛИТЬ ПОРТФЕЛЬ?
portfolio_edit_title|PORTFOLIO BEARBEITEN|MODIFIER LE PORTEFEUILLE|MODIFICA PORTFOLIO|EDITAR CARTEIRA|ポートフォリオを編集|编辑投资组合|تعديل المحفظة|РЕДАКТИРОВАТЬ ПОРТФЕЛЬ
portfolio_fallback_name|PORTFOLIO|PORTEFEUILLE|PORTAFOGLIO|CARTEIRA|ポートフォリオ|投资组合|محفظة|ПОРТФЕЛЬ
portfolio_manager_title|PORTFOLIO-MANAGER|GESTIONNAIRE DE PORTEFEUILLE|GESTORE PORTFOLIO|GERENCIADOR DE CARTEIRAS|ポートフォリオ管理|投资组合管理|مدير المحافظ|МЕНЕДЖЕР ПОРТФЕЛЕЙ
portfolio_new_default_name|NEUES PORTFOLIO|NOUVEAU PORTEFEUILLE|NUOVO PORTFOLIO|NOVA CARTEIRA|新しいポートフォリオ|新建投资组合|محفظة جديدة|НОВЫЙ ПОРТФЕЛЬ
portfolio_startup_indicator|Start-Portfolio|Portefeuille de demarrage|Portfolio di avvio|Carteira inicial|起動時のポートフォリオ|启动用投资组合|محفظة البدء|Стартовый портфель
theme_apply_to_target|ANWENDEN AUF %1$s|APPLIQUER A %1$s|APPLICA A %1$s|APLICAR A %1$s|%1$s に適用|应用到 %1$s|تطبيق على %1$s|ПРИМЕНИТЬ К %1$s
theme_manager_title|THEMEN-MANAGER|GESTIONNAIRE DE THEMES|GESTORE TEMI|GERENCIADOR DE TEMAS|テーマ管理|主题管理|مدير السمات|МЕНЕДЖЕР ТЕМ
theme_reset_body|Uberschreibt Ihre HEX-Auswahl.|Remplace votre selection HEX.|Sostituisce la selezione HEX.|Substitui sua selecao HEX.|カスタムHEXの選択を上書きします。|将覆盖自定义十六进制选择。|سيستبدل اختيار HEX المخصص.|Заменит ваш выбор HEX.
theme_reset_title|Auf Standard zurucksetzen?|Reinitialiser par defaut ?|Ripristinare predefiniti?|Redefinir para o padrao?|初期値に戻しますか?|恢复默认设置？|إعادة التعيين؟|Сбросить к умолчанию?
widget_action_save_exit|SPEICHERN UND BEENDEN|ENREGISTRER ET QUITTER|SALVA ED ESCI|SALVAR E SAIR|保存して終了|保存并退出|حفظ وخروج|СОХРАНИТЬ И ВЫЙТИ
widget_back_to_setup|Zuruck zur Einrichtung|Retour a la configuration|Torna alla configurazione|Voltar a configuracao|設定に戻る|返回设置|العودة للإعداد|К настройке
widget_can_be_reordered|KANN NEU GEORDNET WERDEN|PEUT ETRE REORDONNE|RIORDINABILE|PODE SER REORDENADO|並べ替え可能|可重新排序|يمكن إعادة ترتيبه|МОЖНО МЕНЯТЬ ПОРЯДОК
widget_finish_save|FERTIG UND SPEICHERN|TERMINER ET ENREGISTRER|FINE E SALVA|CONCLUIR E SALVAR|完了して保存|完成并保存|إنهاء وحفظ|ГОТОВО И СОХРАНИТЬ
widget_link_portfolio_prompt|WIDGET MIT PORTFOLIO VERKNUPFEN|LIER LE WIDGET AU PORTEFEUILLE|COLLEGA WIDGET AL PORTFOLIO|VINCULAR WIDGET A CARTEIRA|ウィジェットをポートフォリオにリンク|将小组件关联到投资组合|ربط الودجت بالمحفظة|ПРИВЯЗАТЬ ВИДЖЕТ К ПОРТФЕЛЮ
widget_loading|Laden...|Chargement...|Caricamento...|Carregando...|読み込み中...|加载中...|جاري التحميل...|Загрузка...
widget_loading_preview|Vorschau wird geladen...|Chargement de l apercu...|Caricamento anteprima...|Carregando preview...|プレビューを読み込み中...|正在加载预览...|تحميل المعاينة...|Загрузка предпросмотра...
widget_manager_title|WIDGET-MANAGER|GESTIONNAIRE DE WIDGETS|GESTORE WIDGET|GERENCIADOR DE WIDGETS|ウィジェット管理|小组件管理|مدير الودجت|МЕНЕДЖЕР ВИДЖЕТОВ
widget_no_assets_available|Keine Vermogen verfugbar|Aucun actif disponible|Nessun asset disponibile|Nenhum ativo disponivel|利用可能な資産がありません|无可用资产|لا توجد أصول|Нет доступных активов
widget_portfolio_for_widget_label|PORTFOLIO FUR DIESES WIDGET|PORTEFEUILLE POUR CE WIDGET|PORTFOLIO PER QUESTO WIDGET|CARTEIRA DESTE WIDGET|このウィジェットのポートフォリオ|此小组件的投资组合|محفظة هذا الودجت|ПОРТФЕЛЬ ДЛЯ ВИДЖЕТА
widget_portfolio_label|PORTFOLIO|PORTEFEUILLE|PORTAFOGLIO|CARTEIRA|ポートフォリオ|投资组合|المحفظة|ПОРТФЕЛЬ
widget_preview_error|Vorschaufehler|Erreur d apercu|Errore anteprima|Erro de preview|プレビューエラー|预览错误|خطأ في المعاينة|Ошибка предпросмотра
widget_selected_assets|AUSGEWAHLTE VERMOGEN|ACTIFS SELECTIONNES|ASSET SELEZIONATI|ATIVOS SELECIONADOS|選択した資産|已选资产|الأصول المحددة|ВЫБРАННЫЕ АКТИВЫ
widget_set_draft_target|ENTWURF %1$s SETZEN|DEFINIR BROUILLON %1$s|IMPOSTA BOZZA %1$s|DEFINIR RASCUNHO %1$s|下書き %1$s を設定|设置草稿 %1$s|تعيين مسودة %1$s|ЧЕРНОВИК %1$s
widget_tab_assets|VERMOGEN|ACTIFS|ASSET|ATIVOS|資産|资产|الأصول|АКТИВЫ
widget_tab_preview|VORSCHAU|APERCU|ANTEPRIMA|PREVISAO|プレビュー|预览|معاينة|ПРЕДПРОСМОТР
widget_tab_setup|EINRICHTUNG|CONFIGURATION|CONFIGURAZIONE|CONFIGURACAO|セットアップ|设置|الإعداد|НАСТРОЙКА
widget_tab_style|STIL|APPARENCE|STILE|ESTILO|スタイル|样式|النمط|СТИЛЬ
'@

function Save-Xml([string]$path, [xml]$xml) {
    $settings = New-Object System.Xml.XmlWriterSettings
    $settings.Indent = $true
    $settings.IndentChars = '    '
    $settings.NewLineChars = "`n"
    $settings.NewLineHandling = 'Replace'
    $settings.Encoding = New-Object System.Text.UTF8Encoding($false)
    $writer = [System.Xml.XmlWriter]::Create($path, $settings)
    $xml.Save($writer)
    $writer.Close()
}

$lines = $rows -split "`n" | Where-Object { $_ -match '\|' }
$parsed = foreach ($line in $lines) {
    if ($line -match '^\s*key\s*\|') { continue }
    $parts = $line -split '\|'
    if ($parts.Count -lt 9) { continue }
    [pscustomobject]@{
        Key = $parts[0].Trim()
        De  = $parts[1].Trim()
        Fr  = $parts[2].Trim()
        It  = $parts[3].Trim()
        Pt  = $parts[4].Trim()
        Ja  = $parts[5].Trim()
        Zh  = $parts[6].Trim()
        Ar  = $parts[7].Trim()
        Ru  = $parts[8].Trim()
    }
}

foreach ($pair in @(
        @{ Folder = 'values-de';  Col = 'De' }
        @{ Folder = 'values-fr';  Col = 'Fr' }
        @{ Folder = 'values-it';  Col = 'It' }
        @{ Folder = 'values-pt-rBR'; Col = 'Pt' }
        @{ Folder = 'values-ar';  Col = 'Ar' }
        @{ Folder = 'values-ru';  Col = 'Ru' }
    )) {
    $p = Join-Path $root ($pair.Folder + '/strings.xml')
    [xml]$x = Get-Content -LiteralPath $p -Encoding UTF8
    foreach ($row in $parsed) {
        $val = $row.($pair.Col)
        $n = $x.resources.string | Where-Object { $_.name -eq $row.Key } | Select-Object -First 1
        if ($null -ne $n) { $n.InnerText = $val }
    }
    Save-Xml $p $x
}

Write-Output 'Applied architect/funnel/crypto/asset/widget/portfolio/theme batch (DE, FR, IT, pt-BR, AR, RU).'
