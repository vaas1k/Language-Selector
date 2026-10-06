package vegabobo.languageselector.ui.screen.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.components.BackHeader
import vegabobo.languageselector.ui.components.ListCard
import vegabobo.languageselector.ui.components.TextLabel
import vegabobo.languageselector.ui.components.edgeFade
import vegabobo.languageselector.ui.theme.Ui

@Composable
fun LibrariesScreen(navigateBack: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val libraries = remember { Libs.Builder().withJson(context, R.raw.aboutlibraries).build().libraries }
    val listState = rememberLazyListState()
    val background = MaterialTheme.colorScheme.background

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .statusBarsPadding()
            .edgeFade(listState, background),
        contentPadding = PaddingValues(
            start = Ui.ScreenPadding,
            end = Ui.ScreenPadding,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + Ui.ScreenPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Ui.ItemSpacing)
    ) {
        item { BackHeader(stringResource(R.string.deps_libs), navigateBack) }
        items(libraries, key = { it.uniqueId }) { library ->
            val url = library.website ?: library.scm?.url
            val author = library.developers.mapNotNull { it.name }.joinToString()
                .ifEmpty { library.organization?.name.orEmpty() }
            ListCard(onClick = { if (url != null) uriHandler.openUri(url) }) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (author.isNotEmpty())
                        Text(
                            text = author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        library.licenses.forEach {
                            TextLabel(
                                text = it.name,
                                container = MaterialTheme.colorScheme.secondaryContainer,
                                content = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                if (url != null)
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }
        }
    }
}
