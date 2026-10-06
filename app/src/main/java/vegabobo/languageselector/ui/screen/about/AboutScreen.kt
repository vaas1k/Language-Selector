package vegabobo.languageselector.ui.screen.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import vegabobo.languageselector.BuildConfig
import vegabobo.languageselector.R
import vegabobo.languageselector.service.UserServiceProvider
import vegabobo.languageselector.ui.components.BackHeader
import vegabobo.languageselector.ui.components.HeaderCard
import vegabobo.languageselector.ui.components.LeadingBox
import vegabobo.languageselector.ui.components.ListCard
import vegabobo.languageselector.ui.components.Title
import vegabobo.languageselector.ui.components.edgeFade
import vegabobo.languageselector.ui.screen.main.OperationMode
import vegabobo.languageselector.ui.screen.main.getAppIcon
import vegabobo.languageselector.ui.theme.Ui

@Composable
fun AboutScreen(
    navigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val libraries = remember { Libs.Builder().withJson(context, R.raw.aboutlibraries).build().libraries }
    val appIcon = remember {
        context.packageManager.getAppIcon(context.applicationInfo).toBitmap().asImageBitmap()
    }
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
        item { BackHeader(stringResource(R.string.about), navigateBack) }
        item {
            HeaderCard(
                icon = appIcon,
                title = stringResource(R.string.app_name),
                subtitle = stringResource(R.string.version).format(
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                ),
                label = when {
                    !UserServiceProvider.isConnected() -> stringResource(R.string.mode_none)
                    UserServiceProvider.opMode == OperationMode.ROOT -> stringResource(R.string.mode_root)
                    else -> stringResource(R.string.mode_shizuku, UserServiceProvider.uid)
                }
            )
        }
        item { Title(stringResource(id = R.string.app)) }
        item {
            LinkItem(
                icon = Icons.Outlined.Code,
                title = stringResource(R.string.ghrepo),
                description = stringResource(R.string.view_source)
            ) { uriHandler.openUri("https://github.com/vaas1k/Language-Selector") }
        }
        item {
            LinkItem(
                icon = Icons.Outlined.History,
                title = stringResource(R.string.original_project),
                description = "github.com/VegaBobo/Language-Selector"
            ) { uriHandler.openUri("https://github.com/VegaBobo/Language-Selector") }
        }
        item { Title(stringResource(R.string.deps_libs)) }
        item {
            Column(
                Modifier
                    .clip(Ui.CardShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                libraries.forEachIndexed { index, library ->
                    if (index > 0)
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = Ui.CardPadding),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                        )
                    val url = library.website.orEmpty()
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable(enabled = url.isNotEmpty()) { uriHandler.openUri(url) }
                            .padding(horizontal = Ui.CardPadding, vertical = 12.dp)
                    ) {
                        Text(text = library.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = library.licenses.joinToString { it.name },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    ListCard(onClick = onClick) {
        LeadingBox {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
