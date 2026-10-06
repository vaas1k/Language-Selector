package vegabobo.languageselector.ui.components

import vegabobo.languageselector.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vegabobo.languageselector.ui.screen.main.AppInfo

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
    Row(
        modifier = Modifier
            .clickable { onClickApp(app.pkg) }
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bitmap = icon
        if (bitmap != null)
            Image(
                modifier = Modifier.size(32.dp),
                bitmap = bitmap,
                contentDescription = null
            )
        else
            Spacer(Modifier.size(32.dp))
        Spacer(modifier = Modifier.padding(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy((-4).dp)
        ) {
            Text(text = app.name, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(text = app.pkg, fontSize = 12.sp, maxLines = 1)
            Row {
                if (app.isSystemApp())
                    TextLabel(text = stringResource(R.string.label_system_app))
                if (app.isModified())
                    TextLabel(text = stringResource(R.string.label_modified))
            }
        }
    }
}

@Composable
fun TextLabel(text: String) {
    Box(Modifier.padding(top = 2.dp, end = 4.dp, bottom = 4.dp)) {
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.onPrimary)
        ) {
            Text(
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                text = text,
                maxLines = 1,
                lineHeight = 16.sp,
                fontSize = 10.sp
            )
        }
    }
}