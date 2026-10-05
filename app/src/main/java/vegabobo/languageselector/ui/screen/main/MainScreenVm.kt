package vegabobo.languageselector.ui.screen.main

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Process
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.IUserService
import vegabobo.languageselector.dao.AppInfoDb
import vegabobo.languageselector.service.UserServiceProvider
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject


@HiltViewModel
class MainScreenVm @Inject constructor(
    val app: Application,
    appInfoDb: AppInfoDb
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainScreenState())
    val uiState: StateFlow<MainScreenState> = _uiState.asStateFlow()
    var lastSelectedApp: AppInfo? = null
    val dao = appInfoDb.appInfoDao()
    private val iconPx = (32 * app.resources.displayMetrics.density).toInt()
    private val icons = ConcurrentHashMap<String, ImageBitmap>()

    fun cachedIcon(pkg: String): ImageBitmap? = icons[pkg]

    suspend fun loadIcon(pkg: String): ImageBitmap = withContext(Dispatchers.IO) {
        icons.getOrPut(pkg) {
            val pm = app.packageManager
            val d = try {
                pm.getApplicationIcon(pkg)
            } catch (e: PackageManager.NameNotFoundException) {
                pm.defaultActivityIcon
            }
            d.toBitmap(iconPx, iconPx).asImageBitmap()
        }
    }

    fun getIndexFromAppInfoItem(): Int {
        return _uiState.value.homeApps.indexOfFirst { it.pkg == lastSelectedApp?.pkg }
    }

    fun loadOperationMode() {
        if (Shell.getShell().isAlive)
            Shell.getShell().close()
        Shell.getShell()
        if (Shell.isAppGrantedRoot() == true) {
            _uiState.update { it.copy(operationMode = OperationMode.ROOT) }
            UserServiceProvider.opMode = OperationMode.ROOT
            UserServiceProvider.bind()
            return
        }

        val isAvail = Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        if (isAvail) {
            _uiState.update { it.copy(operationMode = OperationMode.SHIZUKU) }
            return
        }

        _uiState.update { it.copy(operationMode = OperationMode.NONE) }
    }

    init {
        fillListOfApps()
        viewModelScope.launch {
            UserServiceProvider.connection.service.filterNotNull().collect {
                val s = _uiState.value
                if (s.operationMode == OperationMode.NONE && !s.isLoading)
                    fillListOfApps()
            }
        }
    }

    fun parseAppInfo(
        a: ApplicationInfo,
        service: IUserService = UserServiceProvider.getService()
    ): AppInfo {
        val isSystemApp = (a.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val languagePreferences = service.getApplicationLocales(a.packageName)
        val labels = arrayListOf<AppLabels>()
        if (isSystemApp)
            labels.add(AppLabels.SYSTEM_APP)
        if (!languagePreferences.isEmpty)
            labels.add(AppLabels.MODIFIED)
        return AppInfo(
            name = app.packageManager.getLabel(a),
            pkg = a.packageName,
            labels = labels
        )
    }

    fun fillListOfApps() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_uiState.value.operationMode == OperationMode.NONE)
                loadOperationMode()
            val service = try {
                UserServiceProvider.getService()
            } catch (e: Exception) {
                _uiState.update { it.copy(operationMode = OperationMode.NONE, isLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(operationMode = UserServiceProvider.opMode, isConnected = true) }
            val t0 = System.currentTimeMillis()
            val packageList = coroutineScope {
                getInstalledPackages().map {
                    async {
                        try {
                            parseAppInfo(it, service)
                        } catch (e: Exception) {
                            Log.w("MainScreenVm", "skipping ${it.packageName}", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
            Log.i("MainScreenVm", "parsed ${packageList.size} apps in ${System.currentTimeMillis() - t0} ms")
            var sortedList =
                packageList.sortedBy { it.name.lowercase() }.sortedBy { !it.isModified() }
            _uiState.update {
                it.copy(
                    listOfApps = sortedList,
                    homeApps = sortedList.homeFilter(it.isShowSystemAppsHome),
                    isLoading = false
                )
            }
            Log.i("MainScreenVm", "list shown in ${SystemClock.elapsedRealtime() - Process.getStartElapsedRealtime()} ms")
        }
    }

    fun getInstalledPackages(): List<ApplicationInfo> {
        return app.packageManager.getInstalledApplications(
            PackageManager.ApplicationInfoFlags.of(0)
        ).mapNotNull {
            if (!it.enabled || BuildConfig.APPLICATION_ID == it.packageName)
                null
            else
                it
        }
    }

    fun toggleDropdown() {
        val newDropdownVisibility = !uiState.value.isDropdownVisible
        _uiState.update { it.copy(isDropdownVisible = newDropdownVisibility) }
    }

    fun toggleSystemAppsVisibility() {
        val newShowSystemApps = !uiState.value.isShowSystemAppsHome
        _uiState.update {
            it.copy(
                isShowSystemAppsHome = newShowSystemApps,
                homeApps = it.listOfApps.homeFilter(newShowSystemApps)
            )
        }
        toggleDropdown()
    }

    fun onClickProceedShizuku() {
        if (Shizuku.pingBinder() &&
            Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED &&
            !Shizuku.shouldShowRequestPermissionRationale()
        )
            Shizuku.requestPermission(1)
        _uiState.update { it.copy(isLoading = true) }
        fillListOfApps()
    }

    fun onSearchTextFieldChange(newText: String) {
        _uiState.update { it.copy(searchTextFieldValue = newText) }
    }

    fun onSearchExpandedChange() {
        val isExpanded = !uiState.value.isExpanded
        _uiState.update { it.copy(isExpanded = isExpanded) }
        if (isExpanded)
            updateHistory()
        else
            _uiState.update { it.copy(searchTextFieldValue = "") }
    }

    fun onSelectedLabelChange(label: AppLabels) {
        _uiState.update {
            val lb = it.selectLabels
            it.copy(selectLabels = if (lb.contains(label)) lb - label else lb + label)
        }
    }

    fun updateHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val appInfoList = dao.getHistory().map { it.pkg }
            val history = appInfoList.mapNotNull { pkg ->
                val listOfApps = _uiState.value.listOfApps
                val idx = listOfApps.indexOfFirst { it.pkg == pkg }
                if (idx == -1)
                    null
                else
                    listOfApps[idx]
            }
            _uiState.update { it.copy(history = history) }
        }
    }

    fun addAppToHistory(ai: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.findByPkg(ai.pkg) == null) {
                dao.insert(ai.toAppInfoEntity())
            }
            dao.setLastSelected(ai.pkg, System.currentTimeMillis())
            updateHistory()
        }
    }

    fun onClickClear() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.cleanLastSelectedAll()
            updateHistory()
        }
    }

    fun reloadLastSelectedItem() {
        val pkg = lastSelectedApp?.pkg ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updatedAi = try {
                parseAppInfo(
                    app.packageManager.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
                )
            } catch (e: Exception) {
                return@launch
            }
            val apps = _uiState.value.listOfApps
            val idx = apps.indexOfFirst { it.pkg == updatedAi.pkg }
            if (idx != -1 && updatedAi.labels != apps[idx].labels) {
                val newList = apps.toMutableList().apply { set(idx, updatedAi) }
                    .sortedBy { it.name.lowercase() }.sortedBy { !it.isModified() }
                _uiState.update {
                    it.copy(
                        listOfApps = newList,
                        homeApps = newList.homeFilter(it.isShowSystemAppsHome),
                        snackBarDisplay = if (updatedAi.isModified()) SnackBarDisplay.MOVED_TO_TOP else SnackBarDisplay.MOVED_TO_BOTTOM
                    )
                }
            }
        }
    }

    fun resetSnackBarDisplay() = _uiState.update { it.copy(snackBarDisplay = SnackBarDisplay.NONE) }

    fun onClickApp(ai: AppInfo) {
        lastSelectedApp = ai
        addAppToHistory(ai)
    }
}
