package vegabobo.languageselector.ui.screen.appinfo

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.LocaleList
import android.provider.Settings
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.edit
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import vegabobo.languageselector.LocaleManager
import vegabobo.languageselector.service.UserServiceProvider
import vegabobo.languageselector.ui.screen.main.getAppIcon
import vegabobo.languageselector.ui.screen.main.getLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vegabobo.languageselector.BuildConfig
import java.util.Locale
import javax.inject.Inject

object PrefConstants {
    const val PINNED_LOCALES = "pinned_locales_v2"
}


@HiltViewModel
class AppInfoVm @Inject constructor(
    val app: Application,
    val localeManager: LocaleManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppInfoState())
    val uiState: StateFlow<AppInfoState> = _uiState.asStateFlow()

    lateinit var appInfo: ApplicationInfo

    private lateinit var searchIndex: Lazy<List<LocaleSearchEntry>>

    fun initFromAppId(appId: String) {
        appInfo =
            app.packageManager.getApplicationInfo(appId, PackageManager.ApplicationInfoFlags.of(0))
        _uiState.update {
            it.copy(
                appName = app.packageManager.getLabel(appInfo),
                appPackage = appInfo.packageName
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val px = (64 * app.resources.displayMetrics.density).toInt()
            val icon = app.packageManager.getAppIcon(appInfo).toBitmap(px, px).asImageBitmap()
            _uiState.update { it.copy(appIcon = icon) }
        }

        UserServiceProvider.run {
            val locales = systemLocales
            val suggested = (0 until locales.size()).map {
                SingleLocale(locales[it].capDisplayName(), locales[it].toLanguageTag())
            }
            _uiState.update { it.copy(listOfSuggestedLanguages = suggested) }
            updateCurrentLanguageState()
        }

        _uiState.update { it.copy(listOfAllLanguages = localeManager.localeList) }
        searchIndex = lazy { localeManager.localeList.toSearchIndex(Locale.getDefault()) }
        viewModelScope.launch(Dispatchers.Default) { searchIndex.value }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, searchResults = searchIndex.value.search(query)) }
    }

    fun updateCurrentLanguageState() {
        UserServiceProvider.run {
            val currentLocale = getApplicationLocales(appInfo.packageName)
            val locale = if (currentLocale.isEmpty) null else currentLocale.get(0)
            _uiState.update {
                it.copy(currentLanguage = locale?.capDisplayName().orEmpty(), currentTag = locale?.toLanguageTag().orEmpty())
            }
        }
    }

    fun onClickSingleLanguage(index: Int) {
        _uiState.update { it.copy(selectedLanguage = index) }
    }

    fun onBackWhenSelectedLang() {
        _uiState.update { it.copy(selectedLanguage = -1) }
    }

    fun onClickLocale(singleLocale: SingleLocale) {
        UserServiceProvider.run {
            setApplicationLocales(
                appInfo.packageName,
                LocaleList(singleLocale.toLocale())
            )
            updateCurrentLanguageState()
        }
    }

    fun onClickSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", appInfo.packageName, null)
        intent.setData(uri)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        app.startActivity(intent)
    }

    fun onClickOpen() {
        val launchIntent =
            app.packageManager.getLaunchIntentForPackage(appInfo.packageName)
        launchIntent?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: return
        app.startActivity(launchIntent)
    }

    fun onClickResetLang() {
        UserServiceProvider.run {
            setApplicationLocales(appInfo.packageName, LocaleList())
            _uiState.update { it.copy(currentLanguage = "", currentTag = "") }
        }
    }

    fun onClickRestart() {
        UserServiceProvider.run {
            forceStopPackage(appInfo.packageName)
            onClickOpen()
        }
    }

    fun getSp(): SharedPreferences =
        app.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    fun onPinLang(singleLocale: SingleLocale) {
        val pinned = getSp().loadPinned()
        if (pinned.none { it.languageTag == singleLocale.languageTag })
            savePinned(pinned + singleLocale)
        updatePinnedLangsFromSP()
    }

    fun onRemovePin(singleLocale: SingleLocale) {
        savePinned(getSp().loadPinned().filter { it.languageTag != singleLocale.languageTag })
        updatePinnedLangsFromSP()
    }

    fun updatePinnedLangsFromSP() {
        _uiState.update { it.copy(listOfPinnedLanguages = getSp().loadPinned()) }
    }

    private fun savePinned(list: List<SingleLocale>) =
        getSp().edit { putString(PrefConstants.PINNED_LOCALES, list.serializePinned()) }
}

val DEFAULT_PINNED: List<SingleLocale> = listOf("ru-RU", "en-US").map {
    val l = Locale.forLanguageTag(it)
    SingleLocale(l.capDisplayName(), l.toLanguageTag())
}

fun Locale.capDisplayName(): String {
    return this.getDisplayName(this).replaceFirstChar { it.uppercaseChar() }
}

fun String.parsePinned(): List<SingleLocale> = lineSequence().mapNotNull {
    val name = it.substringBeforeLast(",", "")
    val tag = it.substringAfterLast(",")
    if (name.isEmpty() || tag.isEmpty()) null else SingleLocale(name, tag)
}.toList()

fun SharedPreferences.loadPinned(): List<SingleLocale> =
    getString(PrefConstants.PINNED_LOCALES, null)?.parsePinned() ?: DEFAULT_PINNED

fun List<SingleLocale>.serializePinned(): String =
    joinToString("\n") { "${it.name},${it.languageTag}" }
