package vegabobo.languageselector.ui.components

import vegabobo.languageselector.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import vegabobo.languageselector.ui.theme.Ui
import vegabobo.languageselector.ui.theme.animatedItem
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.ui.screen.main.AppInfo
import vegabobo.languageselector.ui.screen.main.AppLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchBar(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    query: String,
    onUpdatedValue: (String) -> Unit,
    apps: List<AppInfo> = emptyList(),
    history: List<AppInfo> = emptyList(),
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedLabels: List<AppLabels>,
    onSelectedLabelsChange: (AppLabels) -> Unit,
    onClickApp: (AppInfo) -> Unit,
    cachedIcon: (String) -> ImageBitmap?,
    loadIcon: suspend (String) -> ImageBitmap,
    onClickClear: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    SearchBar(
        modifier = Modifier
            .semantics { isTraversalGroup = true }
            .then(modifier),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.focusRequester(focusRequester),
                onSearch = { onUpdatedValue(it) },
                expanded = isExpanded,
                onExpandedChange = { onExpandedChange(it) },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Row { actions() }
                },
                query = query,
                onQueryChange = { onUpdatedValue(it) }
            )
        },
        expanded = isExpanded,
        onExpandedChange = { onExpandedChange(it) },
    ) {
        val results = remember(apps, query, selectedLabels) {
            apps.filterNot { filter(query, it, selectedLabels) }
        }
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier.edgeFade(listState, MaterialTheme.colorScheme.surfaceContainerHigh),
            contentPadding = PaddingValues(
                start = Ui.ScreenPadding,
                end = Ui.ScreenPadding,
                top = 8.dp,
                bottom = WindowInsets.navigationBars.union(WindowInsets.ime).asPaddingValues()
                    .calculateBottomPadding() + Ui.ScreenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Ui.ItemSpacing)
        ) {
            if (query.isNotBlank()) {
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterLabel(
                            title = stringResource(R.string.filter_show_system),
                            onClick = {
                                onSelectedLabelsChange(AppLabels.SYSTEM_APP)
                            },
                            isSelected = selectedLabels.contains(AppLabels.SYSTEM_APP)
                        )
                        FilterLabel(
                            title = stringResource(R.string.filter_show_modified),
                            onClick = { onSelectedLabelsChange(AppLabels.MODIFIED) },
                            isSelected = selectedLabels.contains(AppLabels.MODIFIED)
                        )
                    }
                }

                items(results, key = { it.pkg }) { app ->
                    AppListItem(
                        modifier = animatedItem(),
                        app = app,
                        cachedIcon = cachedIcon,
                        loadIcon = loadIcon,
                        onClickApp = { onClickApp(app) }
                    )
                }
            } else if (history.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Title(
                            title = stringResource(R.string.history),
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 8.dp)
                        )
                        TextButton(onClick = { onClickClear(); focusRequester.requestFocus() }) {
                            Text(text = stringResource(R.string.clear))
                        }
                    }
                }
                items(history, key = { it.pkg }) { app ->
                    AppListItem(
                        modifier = animatedItem(),
                        app = app,
                        cachedIcon = cachedIcon,
                        loadIcon = loadIcon,
                        onClickApp = { onClickApp(app) }
                    )
                }
            } else {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .alpha(0.6f),
                        text = stringResource(R.string.search_hint),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (query.isNotBlank())
        BackHandler {
            onUpdatedValue("")
        }
}

fun filter(query: String, app: AppInfo, cLabels: List<AppLabels>): Boolean {
    if (cLabels.contains(AppLabels.MODIFIED) && !app.labels.contains(AppLabels.MODIFIED))
        return true

    if (!cLabels.contains(AppLabels.SYSTEM_APP) && app.labels.contains(AppLabels.SYSTEM_APP))
        return true

    val lQuery = query.lowercase()
    return !(app.pkg.lowercase().contains(lQuery) || app.name.lowercase().contains(lQuery))
}