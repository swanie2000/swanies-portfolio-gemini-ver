# Applies FR / DE / IT / pt-BR translations for Settings + Widget + Portfolio + Theme + AmountEntry keys
# that still matched English. Run from repo root: powershell -ExecutionPolicy Bypass -File scripts/apply_locale_batch_fr_de_it_pt.ps1
$ErrorActionPreference = 'Stop'
$root = Join-Path $PSScriptRoot '..\app\src\main\res' | Resolve-Path

$rows = @'
key|fr|de|it|ptBR
action_cancel|ANNULER|ABBRECHEN|ANNULLA|CANCELAR
action_delete|SUPPRIMER|LOSCHEN|ELIMINA|EXCLUIR
action_save|ENREGISTRER|SPEICHERN|SALVA|SALVAR
amount_entry_back|Retour|Zuruck|Indietro|Voltar
amount_entry_label|Saisir le montant pour %1$s|Betrag fur %1$s eingeben|Inserisci importo per %1$s|Informe a quantidade para %1$s
amount_entry_save_asset|Enregistrer l actif|Vermogen speichern|Salva asset|Salvar ativo
amount_entry_discard_title|Abandonner l actif?|Vermogen verwerfen?|Scartare asset?|Descartar ativo?
amount_entry_discard_body|Voulez-vous vraiment abandonner ce nouvel actif?|Neues Vermogen wirklich verwerfen?|Scartare questo nuovo asset?|Descartar este novo ativo?
amount_entry_yes|Oui|Ja|Si|Sim
amount_entry_no|Non|Nein|No|Nao
portfolio_delete_body|Supprimer definitivement %1$s ?|Dauerhaft %1$s loschen?|Eliminare definitivamente %1$s?|Apagar permanentemente %1$s?
portfolio_delete_title|SUPPRIMER LE PORTEFEUILLE?|PORTFOLIO LOSCHEN?|ELIMINARE PORTFOLIO?|EXCLUIR CARTEIRA?
portfolio_edit_title|MODIFIER LE PORTEFEUILLE|PORTFOLIO BEARBEITEN|MODIFICA PORTFOLIO|EDITAR CARTEIRA
portfolio_fallback_name|PORTEFEUILLE|PORTFOLIO|PORTFOLIO|CARTEIRA
portfolio_manager_title|GESTIONNAIRE DE PORTEFEUILLE|PORTFOLIO-MANAGER|GESTORE PORTFOLIO|GERENCIADOR DE CARTEIRAS
portfolio_new_default_name|NOUVEAU PORTEFEUILLE|NEUES PORTFOLIO|NUOVO PORTFOLIO|NOVA CARTEIRA
portfolio_startup_indicator|Indique le portefeuille de demarrage|Start-Portfolio kennzeichnen|Indica portfolio di avvio|Indica a carteira inicial
settings_background_gradient|Gradient de fond|Hintergrundverlauf|Gradiente di sfondo|Gradiente de fundo
settings_background_gradient_subtitle|Activer le changement dynamique de couleur|Dynamische Farbverschiebung aktivieren|Abilita cambio colore dinamico|Ativar mudanca dinamica de cor
settings_change_password_subtitle|Necessite le mot de passe actuel et la confirmation biometrique|Erfordert aktuelles Passwort und biometrische Bestatigung|Richiede password attuale e conferma biometrica|Exige senha atual e confirmacao biometrica
settings_change_password_title|Changer le mot de passe|Passwort andern|Cambia password|Alterar senha
settings_compact_cards|Cartes compactes|Kompakte Karten|Schede compatte|Cartoes compactos
settings_compact_cards_subtitle|Reduire les cartes pour afficher plus a l ecran|Karten verkleinern, um mehr anzuzeigen|Riduci le schede per mostrarne di piu|Encolher cartoes para mostrar mais na tela
settings_confirm_deletion|Confirmer la suppression|Loschung bestatigen|Conferma eliminazione|Confirmar exclusao
settings_confirm_deletion_subtitle|Afficher une confirmation avant de supprimer un actif|Bestatigung vor Entfernen eines Vermogens anzeigen|Mostra conferma prima di rimuovere un asset|Mostrar confirmacao antes de remover um ativo
settings_confirm_new_password|Confirmer le nouveau mot de passe|Neues Passwort bestatigen|Conferma nuova password|Confirmar nova senha
settings_current_password|Mot de passe actuel|Aktuelles Passwort|Password attuale|Senha atual
settings_current_timeout|Actuel : %1$s|Aktuell: %1$s|Attuale: %1$s|Atual: %1$s
settings_factory_default|PARAMETRES D USINE|WERKSZUSTAND|RIPRISTINO DI FABBRICA|PADRAO DE FABRICA
settings_factory_reset_body|Cela effacera entierement le coffre actuel, y compris tous les actifs et l historique. Cette action est definitive.|Dies loscht das aktuelle Depot inklusive aller Vermogen und Verlaufe. Nicht ruckgangig.|Cancella il vault corrente inclusi asset e cronologia. Azione irreversibile.|Apaga o cofre atual, ativos e historico. Acao permanente.
settings_factory_reset_title|REINITIALISER LE COFFRE A L USINE?|DEPOT AUF WERKSEINSTELLUNGEN?|RIPRISTINARE IL VAULT?|REDEFINIR COFRE PARA O PADRAO?
settings_gradient_intensity|Intensite du gradient|Verlaufsintensita|Intensita gradiente|Intensidade do gradiente
settings_high_visibility_cards|Cartes compactes haute visibilite|Kompakte Hochsichtbarkeits-Karten|Schede compatte ad alta visibilita|Cartoes compactos de alta visibilidade
settings_high_visibility_cards_subtitle|Rend les cartes plus lisibles avec un texte plus grand|Groseres, klareres Textlayout|Testo piu grande e chiaro|Texto maior e mais claro
settings_interface|INTERFACE|SCHNITTSTELLE|INTERFACCIA|INTERFACE
settings_login_timeout|DELAI DE CONNEXION|ANMELDE-TIMEOUT|TIMEOUT ACCESSO|TEMPO LIMITE DE LOGIN
settings_management|GESTION|VERWALTUNG|GESTIONE|GERENCIAMENTO
settings_new_password|Nouveau mot de passe|Neues Passwort|Nuova password|Nova senha
settings_password_bio_prompt_unavailable|Ouverture de l invite biometrique impossible|Biometrie-Prompt kann nicht geoffnet werden|Impossibile aprire il prompt biometrico|Nao foi possivel abrir o prompt biometrico
settings_password_complete_fields|Veuillez remplir tous les champs|Bitte alle Felder ausfullen|Compila tutti i campi|Preencha todos os campos
settings_password_mismatch|Le nouveau mot de passe ne correspond pas|Neues Passwort stimmt nicht|Le password non coincidem|Senha nova nao confere
settings_password_rule_error|8+ caracteres avec majuscule, chiffre et symbole|8+ Zeichen mit Grossbuchstabe, Zahl und Symbol|8+ caratteri con maiuscola, numero e simbolo|8+ caracteres com maiuscula, numero e simbolo
settings_password_update_failed|Impossible de mettre a jour le mot de passe|Passwort konnte nicht aktualisiert werden|Impossibile aggiornare la password|Falha ao atualizar senha
settings_password_updated|Mot de passe mis a jour|Passwort aktualisiert|Password aggiornata|Senha atualizada
settings_portfolio_manager|GESTIONNAIRE DE PORTEFEUILLE|PORTFOLIO-MANAGER|GESTORE PORTFOLIO|GERENCIADOR DE CARTEIRAS
settings_require_password_after_bio_fail|Exiger le mot de passe apres echec biometrique|Passwort nach Biometrie-Fehler verlangen|Richiedi password dopo fallimento biometrico|Exigir senha apos falha biometrica
settings_require_password_after_bio_fail_subtitle|Apres echec biometrique, demander le mot de passe avant nouvel essai|Nach Fehler Passwort vor erneutem Versuch|Dopo errore biometrico richiedi password prima di riprovare|Apos falha, exija senha antes de tentar biometria de novo
settings_reset_everything|TOUT REINITIALISER|ALLES ZURUCKSETZEN|REIMPOSTA TUDO|REDEFINIR TUDO
settings_reset_password|REINITIALISER LE MOT DE PASSE|PASSWORT ZURUCKSETZEN|REIMPOSTA PASSWORD|REDEFINIR SENHA
settings_system_actions|ACTIONS SYSTEME|SYSTEMAKTIONEN|AZIONI DI SISTEMA|ACOES DO SISTEMA
settings_theme_depth|THEME ET PROFONDEUR|THEME UND TIEFE|TEMA E PROFONDITA|TEMA E PROFUNDIDADE
settings_theme_manager|GESTIONNAIRE DE THEMES|THEMEN-MANAGER|GESTORE TEMI|GERENCIADOR DE TEMAS
settings_timeout_applies_desc|S applique au retour depuis l arriere-plan|Gilt beim Zuruckkehren aus dem Hintergrund|Si applica al ritorno dallo sfondo|Aplica ao voltar do segundo plano
settings_timeout_bg_desc|Redemander la connexion apres %1$s en arriere-plan|Erneut anmelden nach %1$s im Hintergrund|Richiedi login dopo %1$s in background|Solicitar login apos %1$s em segundo plano
settings_timeout_minutes|%1$d minutes|%1$d Minuten|%1$d minuti|%1$d minutos
settings_timeout_never|Jamais|Niemals|Mai|Nunca
settings_timeout_never_desc|Ne plus exiger de connexion pour le delai d arriere-plan|Kein erneutes Login durch Timeout|Nessun nuovo login per timeout|Nao exigir login por tempo em segundo plano
settings_timeout_seconds|%1$d secondes|%1$d Sekunden|%1$d secondi|%1$d segundos
settings_title|PARAMETRES|EINSTELLUNGEN|IMPOSTAZIONI|CONFIGURACOES
settings_translation_feedback_button|SIGNALER UN PROBLEME DE TRADUCTION|UBERSETZUNGSPROBLEM MELDEN|SEGNALA PROBLEMA DI TRADUZIONE|REPORTAR PROBLEMA DE TRADUCAO
settings_translation_feedback_chooser_title|Envoyer un retour de traduction|Ubersetzungsfeedback senden|Invia feedback traduzione|Enviar feedback de traducao
settings_translation_feedback_current_text|Texte actuel|Aktueller Text|Testo attuale|Texto atual
settings_translation_feedback_email_body|Langue: %1$s\nEcran: %2$s\nTexte actuel: %3$s\nTraduction proposee: %4$s\nNotes: %5$s|Sprache: %1$s\nBildschirm: %2$s\nAktueller Text: %3$s\nVorschlag: %4$s\nNotizen: %5$s|Lingua: %1$s\nSchermata: %2$s\nTesto attuale: %3$s\nSuggerimento: %4$s\nNote: %5$s|Idioma: %1$s\nTela: %2$s\nTexto atual: %3$s\nSugestao: %4$s\nNotas: %5$s
settings_translation_feedback_email_subject|Retour de traduction (%1$s)|Ubersetzungsfeedback (%1$s)|Feedback traduzione (%1$s)|Feedback de traducao (%1$s)
settings_translation_feedback_hint|Mauvaise traduction ? Envoyez le texte et votre correction.|Schlechte Ubersetzung? Text und Korrektur senden.|Traduzione errata? Invia testo e correzione.|Traducao ruim? Envie o texto e a correcao.
settings_translation_feedback_notes|Notes (optionnel)|Notizen (optional)|Note (opzionali)|Notas (opcional)
settings_translation_feedback_required|Saisissez votre traduction proposee|Bitte Ubersetzungsvorschlag eingeben|Inserisci la traduzione suggerida|Digite a traducao sugerida
settings_translation_feedback_screen|Ecran ou fonction|Bildschirm oder Funktion|Schermata o funzione|Tela ou recurso
settings_translation_feedback_suggested_text|Traduction proposee|Vorgeschlagene Ubersetzung|Traduzione suggerida|Traducao sugerida
settings_translation_feedback_submit|ENVOYER LE RAPPORT|BERICHT SENDEN|INVIA SEGNALAZAO|ENVIAR RELATORIO
settings_update|METTRE A JOUR|AKTUALISIEREN|AGGIORNA|ATUALIZAR
settings_updating|MISE A JOUR...|AKTUALISIERUNG...|AGGIORNAMENTO...|ATUALIZANDO...
theme_apply_to_target|APPLIQUER A %1$s|ANWENDEN AUF %1$s|APPLICA A %1$s|APLICAR A %1$s
theme_manager_title|GESTIONNAIRE DE THEMES|THEMEN-MANAGER|GESTORE TEMI|GERENCIADOR DE TEMAS
theme_reset_body|Cela remplacera votre selection HEX personnalisee.|Uberschreibt Ihre benutzerdefinierte HEX-Auswahl.|Sostituira la selezione HEX personalizzada.|Substituira sua selecao HEX personalizada.
theme_reset_title|Reinitialiser par defaut ?|Auf Standard zurucksetzen?|Ripristinare predefiniti?|Redefinir para o padrao?
widget_action_save_exit|ENREGISTRER ET QUITTER|SPEICHERN UND BEENDEN|SALVA ED ESCI|SALVAR E SAIR
widget_back_to_setup|Retour a la configuration|Zuruck zur Einrichtung|Torna alla configurazione|Voltar a configuracao
widget_can_be_reordered|PEUT ETRE REORDONNE|KANN NEU GEORDNET WERDEN|RIORDINABILE|PODE SER REORDENADO
widget_finish_save|TERMINER ET ENREGISTRER|FERTIG UND SPEICHERN|FINE E SALVA|CONCLUIR E SALVAR
widget_link_portfolio_prompt|LIER CE WIDGET A UN PORTEFEUILLE|WIDGET MIT PORTFOLIO VERKNUPFEN|COLLEGA WIDGET A UN PORTFOLIO|VINCULE ESTE WIDGET A UMA CARTEIRA
widget_loading|Chargement...|Laden...|Caricamento...|Carregando...
widget_loading_preview|Chargement de l apercu...|Vorschau wird geladen...|Caricamento anteprima...|Carregando preview...
widget_manager_title|GESTIONNAIRE DE WIDGETS|WIDGET-MANAGER|GESTORE WIDGET|GERENCIADOR DE WIDGETS
widget_no_assets_available|Aucun actif disponible|Keine Vermogen verfugbar|Nessun asset disponibile|Nenhum ativo disponivel
widget_portfolio_for_widget_label|PORTEFEUILLE POUR CE WIDGET|PORTFOLIO FUR DIESES WIDGET|PORTFOLIO PER QUESTO WIDGET|CARTEIRA DESTE WIDGET
widget_portfolio_label|PORTEFEUILLE|PORTFOLIO|PORTFOLIO|CARTEIRA
widget_preview_error|Erreur d apercu|Vorschaufehler|Errore anteprima|Erro de preview
widget_selected_assets|ACTIFS SELECTIONNES|AUSGEWAHLTE VERMOGEN|ASSET SELEZIONATI|ATIVOS SELECIONADOS
widget_set_draft_target|DEFINIR BROUILLON %1$s|ENTWURF %1$s SETZEN|IMPOSTA BOZZA %1$s|DEFINIR RASCUNHO %1$s
widget_tab_assets|ACTIFS|VERMOGEN|ASSET|ATIVOS
widget_tab_preview|APERCU|VORSCHAU|ANTEPRIMA|PREVISAO
widget_tab_setup|CONFIGURATION|EINRICHTUNG|CONFIGURAZAO|CONFIGURACAO
widget_tab_style|STYLE|STIL|STILE|ESTILO
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
$rowsData = foreach ($line in $lines | Select-Object -Skip 1) {
    $parts = $line -split '\|'
    if ($parts.Count -lt 5) { continue }
    [pscustomobject]@{
        Key = $parts[0].Trim()
        Fr  = $parts[1].Trim()
        De  = $parts[2].Trim()
        It  = $parts[3].Trim()
        Pt  = $parts[4].Trim()
    }
}

foreach ($locPair in @(
        @{ Folder = 'values-fr'; Prop = 'Fr' }
        @{ Folder = 'values-de'; Prop = 'De' }
        @{ Folder = 'values-it'; Prop = 'It' }
        @{ Folder = 'values-pt-rBR'; Prop = 'Pt' }
    )) {
    $p = Join-Path $root ($locPair.Folder + '/strings.xml')
    [xml]$x = Get-Content -LiteralPath $p
    foreach ($row in $rowsData) {
        $val = $row.($locPair.Prop)
        $n = $x.resources.string | Where-Object { $_.name -eq $row.Key } | Select-Object -First 1
        if ($null -ne $n) { $n.InnerText = $val }
    }
    Save-Xml $p $x
}

Write-Output 'Applied FR/DE/IT/pt-BR batch for audited keys.'
