package vegabobo.languageselector.ui.screen.appinfo

import androidx.compose.ui.graphics.ImageBitmap
import java.util.Locale

data class LocaleRegion(
    val language: String,
    val locales: List<SingleLocale>
)

data class SingleLocale(
    val name: String,
    val languageTag: String
) {
    fun toLocale(): Locale {
        return Locale.forLanguageTag(languageTag)
    }
}

data class AppInfoState(
    val appIcon: ImageBitmap? = null,
    val appName: String = "",
    val appPackage: String = "",
    val currentLanguage: String = "",
    val currentTag: String? = null,
    val listOfSuggestedLanguages: List<SingleLocale> = emptyList(),
    val listOfPinnedLanguages: List<SingleLocale> = emptyList(),
    val selectedLanguage: Int = -1,
    val listOfAllLanguages: List<LocaleRegion> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<SingleLocale> = emptyList(),
)

data class LocaleSearchEntry(val locale: SingleLocale, val key: String)

fun List<LocaleRegion>.toSearchIndex(uiLocale: Locale): List<LocaleSearchEntry> =
    flatMap { region ->
        region.locales.map {
            val key = listOf(region.language, it.name, it.languageTag, it.toLocale().getDisplayName(uiLocale))
                .joinToString("\n")
                .lowercase()
            LocaleSearchEntry(it, key)
        }
    }

fun List<LocaleSearchEntry>.search(query: String): List<SingleLocale> {
    val q = query.trim().replace('_', '-').lowercase()
    if (q.isEmpty()) return emptyList()
    return filter { q in it.key }
        .sortedBy { !it.key.startsWith(q) && !it.key.contains("\n$q") }
        .map { it.locale }
}