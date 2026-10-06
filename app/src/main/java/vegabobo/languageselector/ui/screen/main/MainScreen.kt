package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import android.net.Uri
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.AppListItem
import vegabobo.languageselector.ui.components.AppSearchBar
import vegabobo.languageselector.ui.components.ScrollToTopButton
import vegabobo.languageselector.ui.components.edgeFade
import vegabobo.languageselector.ui.theme.Ui
import vegabobo.languageselector.ui.theme.animatedItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainScreenVm: MainScreenVm = hiltViewModel(),
    navigateToAppScreen: (String) -> Unit,
    navigateToAbout: () -> Unit,
) {
    val uiState by mainScreenVm.uiState.collectAsState()
    val sb = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val movedUp = stringResource(R.string.moved_up)
    val movedDown = stringResource(R.string.moved_down)
    val navigate = stringResource(R.string.navigate)
    val locales = LocalConfiguration.current.locales.toLanguageTags()
    var lastLocales by rememberSaveable { mutableStateOf(locales) }

    LaunchedEffect(locales) {
        if (locales != lastLocales) {
            lastLocales = locales
            mainScreenVm.fillListOfApps()
        }
    }

    LaunchedEffect(Unit) {
        mainScreenVm.reloadLastSelectedItem()
        mainScreenVm.uiState.collectLatest {
            when (it.snackBarDisplay) {
                SnackBarDisplay.MOVED_TO_TOP -> {
                    val r = sb.showSnackbar(
                        message = movedUp,
                        actionLabel = navigate
                    )
                    val i = mainScreenVm.getIndexFromAppInfoItem()
                    if (r == SnackbarResult.ActionPerformed && i != -1)
                        lazyListState.animateScrollToItem(i + 1)
                }

                SnackBarDisplay.MOVED_TO_BOTTOM -> {
                    val r = sb.showSnackbar(
                        message = movedDown,
                        actionLabel = navigate
                    )
                    val i = mainScreenVm.getIndexFromAppInfoItem()
                    if (r == SnackbarResult.ActionPerformed && i != -1)
                        lazyListState.animateScrollToItem(i + 1)
                }

                else -> {}
            }
            mainScreenVm.resetSnackBarDisplay()
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(sb) }) { padding ->
        if (uiState.isLoading)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                if (!uiState.isConnected)
                    Text(stringResource(R.string.status_connecting))
                else {
                    Text(stringResource(if (uiState.operationMode == OperationMode.ROOT) R.string.status_root else R.string.status_shizuku))
                    Text(stringResource(R.string.status_loading_apps))
                }
            }
        else {
            Box(
                Modifier
                    .fillMaxSize()
                    .semantics { isTraversalGroup = true }) {
                AppSearchBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .semantics { traversalIndex = 0f },
                    placeholder = stringResource(R.string.search),
                    onUpdatedValue = { mainScreenVm.onSearchTextFieldChange(it) },
                    query = uiState.searchTextFieldValue,
                    onClickApp = { mainScreenVm.onClickApp(it); navigateToAppScreen(it.pkg) },
                    cachedIcon = mainScreenVm::cachedIcon,
                    loadIcon = mainScreenVm::loadIcon,
                    history = uiState.history,
                    apps = uiState.listOfApps,
                    isExpanded = uiState.isExpanded,
                    onExpandedChange = { mainScreenVm.onSearchExpandedChange() },
                    selectedLabels = uiState.selectLabels,
                    onSelectedLabelsChange = { mainScreenVm.onSelectedLabelChange(it) },
                    onClickClear = { mainScreenVm.onClickClear() },
                    actions = {
                        if (!uiState.isExpanded)
                            SearchBarActions(
                                isDropdownVisible = uiState.isDropdownVisible,
                                isShowingSystemApps = uiState.isShowSystemAppsHome,
                                onClickToggleDropdown = { mainScreenVm.toggleDropdown() },
                                onToggleDropdown = { mainScreenVm.toggleDropdown() },
                                onClickToggleSystemApps = { mainScreenVm.toggleSystemAppsVisibility() },
                                onClickAbout = { navigateToAbout() },
                                onClickOwnLanguage = {
                                    val uri = Uri.fromParts("package", context.packageName, null)
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_APP_LOCALE_SETTINGS, uri))
                                    } catch (e: ActivityNotFoundException) {
                                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
                                    }
                                }
                            )
                    })

                Box(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                        .background(MaterialTheme.colorScheme.background)
                        .zIndex(1f)
                )

                if (uiState.operationMode == OperationMode.NONE) {
                    ShizukuRequiredWarning { mainScreenVm.onClickProceedShizuku() }
                }

                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        start = Ui.ScreenPadding,
                        end = Ui.ScreenPadding,
                        bottom = padding.calculateBottomPadding() + Ui.ScreenPadding
                    ),
                    verticalArrangement = Arrangement.spacedBy(Ui.ItemSpacing),
                    modifier = Modifier
                        .semantics { traversalIndex = 1f }
                        .edgeFade(
                            lazyListState,
                            MaterialTheme.colorScheme.background,
                            WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                        )
                ) {
                    item {
                        Spacer(
                            Modifier
                                .statusBarsPadding()
                                .padding(top = 60.dp)
                        )
                    }
                    items(uiState.homeApps, key = { it.pkg }) { thisApp ->
                        AppListItem(
                            modifier = animatedItem(),
                            app = thisApp,
                            cachedIcon = mainScreenVm::cachedIcon,
                            loadIcon = mainScreenVm::loadIcon,
                            onClickApp = {
                                mainScreenVm.onClickApp(thisApp)
                                navigateToAppScreen(it)
                            }
                        )
                    }
                }

                ScrollToTopButton(
                    state = lazyListState,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = Ui.ScreenPadding, bottom = padding.calculateBottomPadding() + Ui.ScreenPadding)
                )
            }
        }
    }
}