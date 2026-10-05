package vegabobo.languageselector

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.LocaleList
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import vegabobo.languageselector.service.UserServiceProvider
import vegabobo.languageselector.ui.screen.appinfo.SingleLocale
import vegabobo.languageselector.ui.screen.appinfo.capDisplayName
import vegabobo.languageselector.ui.screen.appinfo.loadPinned
import vegabobo.languageselector.ui.screen.main.getLabel


class QSTile : TileService() {

    private var isLoaded = false
    @Volatile
    private var locales = emptyList<SingleLocale>()

    private fun getNextSingleLocale(localeList: LocaleList): SingleLocale? =
        nextLocale(locales, if (localeList.isEmpty) null else localeList[0].toLanguageTag())

    private fun setDisabledTile() {
        qsTile.label = getString(R.string.app_name)
        qsTile.subtitle = getString(R.string.unavailable)
        qsTile.state = Tile.STATE_UNAVAILABLE
        qsTile.updateTile()
    }

    private fun IUserService.refreshTile(): ApplicationInfo? {
        val currentAppPackage = firstRunningTaskPackage
        val targetPackage =
            try {
                packageManager.getApplicationInfo(
                    currentAppPackage,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        if (
            targetPackage == null ||
            (targetPackage.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            targetPackage.packageName == BuildConfig.APPLICATION_ID
        ) {
            setDisabledTile()
            return null
        }
        var isCustomLocale = false
        val currentLocale =
            try {
                val appLocales = getApplicationLocales(currentAppPackage)
                if (!appLocales.isEmpty) {
                    isCustomLocale = true
                    appLocales[0].capDisplayName()
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }.ifBlank { getString(R.string.system_default) }
        qsTile.state = Tile.STATE_INACTIVE
        qsTile.updateTile()

        qsTile.label = currentLocale
        qsTile.subtitle = packageManager.getLabel(targetPackage)
        qsTile.state = if (isCustomLocale) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
        return targetPackage
    }

    fun loadLangs() {
        if (!isLoaded) {
            val pinned =
                getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE).loadPinned()
            locales = if (pinned.isEmpty()) emptyList() else listOf(SingleLocale("", "")) + pinned
            isLoaded = true
        }
    }

    override fun onTileAdded() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "QSTile onTileAdded()")
        super.onTileAdded()
    }

    override fun onStartListening() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "QSTile onStartListening()")

        super.onStartListening()

        loadLangs()
        if (locales.isEmpty()) {
            setDisabledTile()
            return
        }
        UserServiceProvider.run(onFail = ::setDisabledTile) { refreshTile() }
    }

    override fun onStopListening() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "QSTile onStopListening()")
        isLoaded = false
        locales = emptyList()
        super.onStopListening()
    }

    override fun onClick() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "QSTile onClick()")

        super.onClick()

        loadLangs()
        if (locales.isEmpty())
            return

        UserServiceProvider.run(onFail = ::setDisabledTile) {
            val targetPackage = refreshTile() ?: return@run
            val currentLocale = getApplicationLocales(targetPackage.packageName)
            val nextLocale = getNextSingleLocale(currentLocale) ?: return@run
            val localeList =
                if (nextLocale.languageTag.isEmpty())
                    LocaleList()
                else
                    LocaleList(nextLocale.toLocale())
            setApplicationLocales(targetPackage.packageName, localeList)
            refreshTile()
        }
    }

    override fun onTileRemoved() {
        if (BuildConfig.DEBUG)
            Log.d(BuildConfig.APPLICATION_ID, "QSTile onTileRemoved()")
        super.onTileRemoved()
    }
}

fun nextLocale(locales: List<SingleLocale>, currentTag: String?): SingleLocale? {
    if (locales.isEmpty()) return null
    val i = if (currentTag == null) 0 else locales.indexOfFirst { it.languageTag == currentTag }
    return if (i == -1) locales.first() else locales[(i + 1) % locales.size]
}
