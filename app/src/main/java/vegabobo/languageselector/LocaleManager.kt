package vegabobo.languageselector

import vegabobo.languageselector.ui.screen.appinfo.LocaleRegion
import vegabobo.languageselector.ui.screen.appinfo.SingleLocale
import vegabobo.languageselector.ui.screen.appinfo.capDisplayName
import java.util.Locale

class LocaleManager {

    val localeList: List<LocaleRegion> = Locale.getAvailableLocales()
        .filter { it.language.isNotEmpty() }
        .groupBy { it.getDisplayLanguage(it).replaceFirstChar { c -> c.uppercaseChar() } }
        .map { (language, locales) ->
            LocaleRegion(language, locales.map { SingleLocale(it.capDisplayName(), it.toLanguageTag()) })
        }
        .sortedBy { it.language }

}
