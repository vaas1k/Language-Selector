package vegabobo.languageselector.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.ui.theme.Ui
import vegabobo.languageselector.ui.theme.pressClickable

@Composable
fun QuickTextButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    primary: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (primary) scheme.primaryContainer else scheme.secondaryContainer
    val content = if (primary) scheme.onPrimaryContainer else scheme.onSecondaryContainer
    Column(
        modifier = modifier
            .pressClickable(Ui.CardShape, container, onClick = onClick)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.5f), Ui.CardShape)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = icon,
            contentDescription = null,
            tint = content
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
