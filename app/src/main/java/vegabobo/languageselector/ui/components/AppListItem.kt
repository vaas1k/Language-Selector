package vegabobo.languageselector.ui.components

import vegabobo.languageselector.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.ui.screen.main.AppInfo
import vegabobo.languageselector.ui.theme.Ui

@Composable
fun AppListItem(
    modifier: Modifier = Modifier,
    app: AppInfo,
    cachedIcon: (String) -> ImageBitmap?,
    loadIcon: suspend (String) -> ImageBitmap,
    onClickApp: (String) -> Unit
) {
    var icon by remember(app.pkg) { mutableStateOf(cachedIcon(app.pkg)) }
    LaunchedEffect(app.pkg) { if (icon == null) icon = loadIcon(app.pkg) }
    ListCard(modifier = modifier, onClick = { onClickApp(app.pkg) }) {
        val bitmap = icon
        if (bitmap != null)
            Image(
                modifier = Modifier.size(Ui.IconSize),
                bitmap = bitmap,
                contentDescription = null
            )
        else
            Spacer(Modifier.size(Ui.IconSize))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.pkg,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (app.isSystemApp() || app.isModified())
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (app.isSystemApp())
                        TextLabel(
                            text = stringResource(R.string.label_system_app),
                            container = MaterialTheme.colorScheme.tertiaryContainer,
                            content = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    if (app.isModified())
                        TextLabel(
                            text = stringResource(R.string.label_modified),
                            container = MaterialTheme.colorScheme.primaryContainer,
                            content = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                }
        }
    }
}

@Composable
fun TextLabel(text: String, container: Color, content: Color) {
    Text(
        modifier = Modifier
            .background(container, Ui.LabelShape)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = content,
        maxLines = 1
    )
}
