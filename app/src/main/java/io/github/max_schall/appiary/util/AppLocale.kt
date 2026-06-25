package io.github.max_schall.appiary.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/** Supported in-app languages. [tag] empty = follow the system language. */
enum class AppLanguage(val tag: String, val displayName: String) {
    SYSTEM("", "System default"),
    ENGLISH("en", "English"),
    GERMAN("de", "Deutsch"),
}

/**
 * Thin wrapper over AndroidX per-app locales (`AppCompatDelegate`). Works on all
 * supported versions and persists the choice via the AppCompat locales service.
 */
object AppLocale {

    fun current(): AppLanguage {
        val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return when {
            tag.startsWith("de") -> AppLanguage.GERMAN
            tag.startsWith("en") -> AppLanguage.ENGLISH
            else -> AppLanguage.SYSTEM
        }
    }

    fun set(language: AppLanguage) {
        val locales = if (language.tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    /** The effective ISO language code right now ("de"/"en"/…), resolving SYSTEM. */
    fun effectiveLanguageCode(): String {
        val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return when {
            tag.isNotEmpty() -> tag.substringBefore('-').lowercase()
            else -> Locale.getDefault().language.lowercase()
        }
    }
}
