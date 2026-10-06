package vegabobo.languageselector.ui.screen.appinfo

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.BackHeader
import vegabobo.languageselector.ui.components.HeaderCard
import vegabobo.languageselector.ui.components.LocaleItemList
import vegabobo.languageselector.ui.components.QuickTextButton
import vegabobo.languageselector.ui.components.ScrollToTopButton
import vegabobo.languageselector.ui.components.Title
import vegabobo.languageselector.ui.components.edgeFade
import vegabobo.languageselector.ui.components.languageCode
import vegabobo.languageselector.ui.theme.Ui
import vegabobo.languageselector.ui.theme.animatedItem

@Composable
fun AppInfoScreen(
    appId: String,
    navigateBack: () -> Unit,
    appInfoVm: AppInfoVm = hiltViewModel(),
) {
    val uiState by appInfoVm.uiState.collectAsState()
    val ctx = LocalContext.current
    val res = LocalResources.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    fun pinToast(locale: String) {
        val pinTxt =
            res.getString(R.string.pinned_ok).format(locale)
        Toast.makeText(ctx, pinTxt, Toast.LENGTH_SHORT).show()
    }

    fun unpinToast(locale: String) {
        val pinTxt =
            res.getString(R.string.unpinned).format(locale)
        Toast.makeText(ctx, pinTxt, Toast.LENGTH_SHORT).show()
    }

    @Composable
    fun PinnableLocale(locale: SingleLocale, modifier: Modifier, onClick: () -> Unit = { appInfoVm.onClickLocale(locale) }) {
        LocaleItemList(
            itemText = locale.name,
            modifier = modifier,
            code = languageCode(locale.languageTag),
            subtitle = locale.languageTag,
            onClick = onClick,
            onLongClick = {
                pinToast(locale.name)
                appInfoVm.onPinLang(locale)
            }
        )
    }

    LaunchedEffect(Unit) {
        appInfoVm.initFromAppId(appId)
        appInfoVm.updatePinnedLangsFromSP()
    }

    val background = MaterialTheme.colorScheme.background
    val bottom = WindowInsets.navigationBars.union(WindowInsets.ime).asPaddingValues().calculateBottomPadding()
    val query = uiState.searchQuery

    Box(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .edgeFade(listState, background),
            contentPadding = PaddingValues(
                start = Ui.ScreenPadding,
                end = Ui.ScreenPadding,
                bottom = bottom + Ui.ScreenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Ui.ItemSpacing)
        ) {
            item(key = "back") { BackHeader(stringResource(R.string.app_language), navigateBack) }

            item(key = "header") {
                HeaderCard(
                    icon = uiState.appIcon,
                    title = uiState.appName,
                    subtitle = uiState.appPackage,
                    label = uiState.currentLanguage.ifEmpty { stringResource(R.string.system_default) }
                )
            }

            item(key = "actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Ui.ItemSpacing)
                ) {
                    QuickTextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { appInfoVm.onClickOpen() },
                        icon = Icons.AutoMirrored.Outlined.OpenInNew,
                        text = stringResource(R.string.open),
                        primary = true
                    )
                    QuickTextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { appInfoVm.onClickRestart() },
                        icon = Icons.Outlined.Refresh,
                        text = stringResource(R.string.restart)
                    )
                    QuickTextButton(
                        modifier = Modifier.weight(1f),
                        onClick = { appInfoVm.onClickSettings() },
                        icon = Icons.Outlined.Settings,
                        text = stringResource(R.string.settings)
                    )
                }
            }

            if (uiState.selectedLanguage != -1) {
                val region = uiState.listOfAllLanguages[uiState.selectedLanguage]
                item(key = "t-region") {
                    Title("${stringResource(R.string.region)} – ${region.language}", Modifier.animatedItem(this))
                }
                items(region.locales, key = { "r-${it.languageTag}" }) { locale ->
                    PinnableLocale(locale, Modifier.animatedItem(this)) {
                        appInfoVm.onClickLocale(locale)
                        appInfoVm.onBackWhenSelectedLang()
                        coroutineScope.launch { listState.scrollToItem(0) }
                    }
                }
            } else {
                if (query.isEmpty()) {
                    if (uiState.listOfPinnedLanguages.isNotEmpty()) {
                        item(key = "t-pinned") { Title(stringResource(R.string.pinned), Modifier.animatedItem(this)) }
                        items(uiState.listOfPinnedLanguages, key = { "p-${it.languageTag}" }) { locale ->
                            LocaleItemList(
                                itemText = locale.name,
                                modifier = Modifier.animatedItem(this),
                                code = languageCode(locale.languageTag),
                                subtitle = locale.languageTag,
                                onClick = { appInfoVm.onClickLocale(locale) },
                                onLongClick = {
                                    unpinToast(locale.name)
                                    appInfoVm.onRemovePin(locale)
                                }
                            )
                        }
                    }

                    item(key = "t-user") { Title(stringResource(R.string.user_languages), Modifier.animatedItem(this)) }
                    item(key = "system") {
                        LocaleItemList(
                            itemText = stringResource(R.string.system_default),
                            modifier = Modifier.animatedItem(this)
                        ) { appInfoVm.onClickResetLang() }
                    }
                    items(uiState.listOfSuggestedLanguages, key = { "u-${it.languageTag}" }) { locale ->
                        PinnableLocale(locale, Modifier.animatedItem(this))
                    }

                    item(key = "t-all") { Title(stringResource(R.string.all_languages), Modifier.animatedItem(this)) }
                }

                stickyHeader(key = "search") {
                    Box(
                        Modifier
                            .background(background)
                            .padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { appInfoVm.onSearchQueryChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.search_languages)) },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                            trailingIcon = {
                                if (query.isNotEmpty())
                                    IconButton(onClick = { appInfoVm.onSearchQueryChange("") }) {
                                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.clear))
                                    }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() })
                        )
                    }
                }

                if (query.isEmpty()) {
                    items(uiState.listOfAllLanguages.size, key = { "a-${uiState.listOfAllLanguages[it].language}" }) { index ->
                        val language = uiState.listOfAllLanguages[index]
                        LocaleItemList(
                            itemText = language.language,
                            modifier = Modifier.animatedItem(this),
                            code = languageCode(language.locales.first().languageTag),
                            hasChildren = true
                        ) {
                            appInfoVm.onClickSingleLanguage(index)
                            coroutineScope.launch { listState.scrollToItem(0) }
                        }
                    }
                } else if (uiState.searchResults.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.no_languages_found),
                            modifier = Modifier.animatedItem(this)
                                .fillMaxWidth()
                                .padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.searchResults, key = { "s-${it.languageTag}" }) { locale ->
                        PinnableLocale(locale, Modifier.animatedItem(this)) {
                            keyboard?.hide()
                            appInfoVm.onClickLocale(locale)
                        }
                    }
                }
            }
        }

        ScrollToTopButton(
            state = listState,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Ui.ScreenPadding, bottom = bottom + Ui.ScreenPadding)
        )
    }

    BackHandler(enabled = uiState.selectedLanguage != -1 || query.isNotEmpty()) {
        if (uiState.selectedLanguage != -1) appInfoVm.onBackWhenSelectedLang()
        else appInfoVm.onSearchQueryChange("")
    }
}
