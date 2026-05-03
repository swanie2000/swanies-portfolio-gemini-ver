package com.swanie.portfolio.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.swanie.portfolio.R

/** Native display name for each supported app locale (uses `language_name_*_native` resources). */
@Composable
fun languageDisplayNameForOption(code: String): String = when (code) {
    "en" -> stringResource(R.string.language_name_english_native)
    "es" -> stringResource(R.string.language_name_spanish_native)
    "pt-BR" -> stringResource(R.string.language_name_portuguese_brazil_native)
    "fr" -> stringResource(R.string.language_name_french_native)
    "de" -> stringResource(R.string.language_name_german_native)
    "ja" -> stringResource(R.string.language_name_japanese_native)
    "ko" -> stringResource(R.string.language_name_korean_native)
    "zh-CN" -> stringResource(R.string.language_name_chinese_simplified_native)
    "hi" -> stringResource(R.string.language_name_hindi_native)
    "ar" -> stringResource(R.string.language_name_arabic_native)
    "zh-TW" -> stringResource(R.string.language_name_chinese_traditional_native)
    "it" -> stringResource(R.string.language_name_italian_native)
    "ru" -> stringResource(R.string.language_name_russian_native)
    "tr" -> stringResource(R.string.language_name_turkish_native)
    "id" -> stringResource(R.string.language_name_indonesian_native)
    "vi" -> stringResource(R.string.language_name_vietnamese_native)
    "th" -> stringResource(R.string.language_name_thai_native)
    "pl" -> stringResource(R.string.language_name_polish_native)
    "nl" -> stringResource(R.string.language_name_dutch_native)
    "uk" -> stringResource(R.string.language_name_ukrainian_native)
    else -> stringResource(R.string.language_name_english_native)
}
