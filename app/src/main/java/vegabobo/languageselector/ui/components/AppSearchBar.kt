package vegabobo.languageselector.ui.components

import vegabobo.languageselector.R
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        LazyColumn {
            if (query.isNotBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .padding(
                                start = 23.dp,
                                top = 8.dp,
                                bottom = 8.dp,
                                end = 8.dp
                            )
                            .horizontalScroll(rememberScrollState())
                    ) {
                        FilterLabel(
                            title = stringResource(R.string.filter_show_system),
                            onClick = {
                                onSelectedLabelsChange(AppLabels.SYSTEM_APP)
                            },
                            isSelected = selectedLabels.contains(AppLabels.SYSTEM_APP)
                        )
                        Spacer(Modifier.padding(8.dp))
                        FilterLabel(
                            title = stringResource(R.string.filter_show_modified),
                            onClick = { onSelectedLabelsChange(AppLabels.MODIFIED) },
                            isSelected = selectedLabels.contains(AppLabels.MODIFIED)
                        )
                    }
                }

                items(results, key = { it.pkg }) { app ->
                    AppListItem(
                        modifier = Modifier.padding(
                            start = 23.dp,
                            end = 23.dp,
                            top = 4.dp,
                            bottom = 4.dp
                        ),
                        app = app,
                        cachedIcon = cachedIcon,
                        loadIcon = loadIcon,
                        onClickApp = { onClickApp(app) }
                    )
                }
            } else if (history.isNotEmpty()) {
                item {
                    Row(
                        Modifier.padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.history).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = modifier
                                .padding(start = 18.dp)
                                .padding(bottom = 8.dp)
                                .padding(top = 8.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { onClickClear(); focusRequester.requestFocus() }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = stringResource(R.string.clear))
                            }
                        }
                        Spacer(modifier = Modifier.padding(6.dp))
                    }
                }
                items(history, key = { it.pkg }) { app ->
                    AppListItem(
                        modifier = Modifier.padding(
                            start = 23.dp,
                            end = 23.dp,
                            top = 4.dp,
                            bottom = 4.dp
                        ),
                        app = app,
                        cachedIcon = cachedIcon,
                        loadIcon = loadIcon,
                        onClickApp = { onClickApp(app) }
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.weight(1f))

                    }
                }
            } else {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .alpha(0.4f),
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