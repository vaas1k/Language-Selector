package vegabobo.languageselector

import android.icu.util.ULocale
import vegabobo.languageselector.ui.screen.appinfo.LocaleRegion
import vegabobo.languageselector.ui.screen.appinfo.SingleLocale
import vegabobo.languageselector.ui.screen.appinfo.capDisplayName
import java.util.Locale

class LocaleManager {

    val localeList: List<LocaleRegion> = Locale.getAvailableLocales()
        .filter { it.language.isNotEmpty() }
        .groupBy { it.getDisplayLanguage(it).replaceFirstChar { c -> c.uppercaseChar() } }
        .map { (language, locales) ->
            // android.icu is a stub returning null in JVM unit tests
            val likely = ULocale.addLikelySubtags(ULocale(locales.first().language))?.country.orEmpty()
            LocaleRegion(language, locales.mainRegionFirst(likely).map { SingleLocale(it.capDisplayName(), it.toLanguageTag()) })
        }
        .sortedBy { it.language }

}

fun List<Locale>.mainRegionFirst(likelyCountry: String): List<Locale> = sortedWith(
    compareBy(
        { if (it.country.isEmpty() && it.script.isEmpty()) 0 else if (it.country == likelyCountry) 1 else 2 },
        { it.capDisplayName() },
    )
)
