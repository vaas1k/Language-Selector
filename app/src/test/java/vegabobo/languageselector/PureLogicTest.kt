package vegabobo.languageselector

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vegabobo.languageselector.ui.components.filter
import vegabobo.languageselector.ui.screen.appinfo.SingleLocale
import vegabobo.languageselector.ui.screen.appinfo.parsePinned
import vegabobo.languageselector.ui.screen.appinfo.serializePinned
import vegabobo.languageselector.ui.screen.main.AppInfo
import vegabobo.languageselector.ui.screen.main.AppLabels
import vegabobo.languageselector.ui.screen.main.homeFilter

private fun app(pkg: String, name: String = pkg, vararg labels: AppLabels) =
    AppInfo(name, pkg, labels.toList())

class PureLogicTest {

    private val sys = SingleLocale("", "")
    private val en = SingleLocale("English (United States)", "en-US")
    private val ru = SingleLocale("Русский (Россия)", "ru-RU")

    @Test
    fun nextLocale_cyclesThroughPinnedAndWrapsToSystemDefault() {
        val l = listOf(sys, en, ru)
        assertEquals(en, nextLocale(l, null))
        assertEquals(ru, nextLocale(l, "en-US"))
        assertEquals(sys, nextLocale(l, "ru-RU"))
        assertEquals(sys, nextLocale(l, "de-DE")) // unknown current → restart
    }

    @Test
    fun nextLocale_handlesEmptyAndSingleEntry() {
        assertNull(nextLocale(emptyList(), null))
        assertEquals(sys, nextLocale(listOf(sys), null))
    }

    @Test
    fun parsePinned_keepsOrderAndCommasInName_skipsGarbage() {
        val parsed = "Русский (Россия),ru-RU\nChinese (Traditional, Hong Kong SAR China),zh-Hant-HK\ngarbage\n,".parsePinned()
        assertEquals(
            listOf(
                SingleLocale("Русский (Россия)", "ru-RU"),
                SingleLocale("Chinese (Traditional, Hong Kong SAR China)", "zh-Hant-HK"),
            ),
            parsed
        )
    }

    @Test
    fun serializePinned_roundTrips() {
        val list = listOf(ru, en)
        assertEquals(list, list.serializePinned().parsePinned())
        assertEquals(emptyList<SingleLocale>(), "".parsePinned())
    }

    @Test
    fun homeFilter_hidesUnmodifiedSystemAppsUnlessRequested() {
        val user = app("a.user")
        val system = app("b.sys", labels = arrayOf(AppLabels.SYSTEM_APP))
        val modifiedSystem = app("c.sys", labels = arrayOf(AppLabels.SYSTEM_APP, AppLabels.MODIFIED))
        val all = listOf(user, system, modifiedSystem)
        assertEquals(listOf(user, modifiedSystem), all.homeFilter(false))
        assertEquals(all, all.homeFilter(true))
    }

    @Test
    fun searchFilter_matchesNameOrPackageAndHonoursLabels() {
        val tg = app("org.telegram", "Telegram")
        val sysTg = app("com.oem.telegram", "OEM", AppLabels.SYSTEM_APP)
        // filter() returns true when the app must be HIDDEN
        assertFalse(filter("tele", tg, emptyList()))
        assertFalse(filter("TELEGRAM", tg, emptyList()))
        assertTrue(filter("signal", tg, emptyList()))
        assertTrue(filter("tele", sysTg, emptyList())) // system hidden by default
        assertFalse(filter("tele", sysTg, listOf(AppLabels.SYSTEM_APP)))
        assertTrue(filter("tele", tg, listOf(AppLabels.MODIFIED))) // not modified
    }

    @Test
    fun localeManager_groupsEveryLocaleAndSkipsRoot() {
        val regions = LocaleManager().localeList
        assertTrue(regions.none { it.language.isEmpty() })
        val english = regions.first { it.locales.any { l -> l.languageTag == "en-US" } }
        assertTrue(english.locales.any { it.languageTag == "en" }) // first locale of a group not dropped
        val total = regions.sumOf { it.locales.size }
        assertEquals(java.util.Locale.getAvailableLocales().count { it.language.isNotEmpty() }, total)
    }
}
