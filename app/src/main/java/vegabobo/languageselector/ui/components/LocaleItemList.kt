package vegabobo.languageselector.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.ui.theme.Ui

@Composable
fun LocaleItemList(
    itemText: String,
    modifier: Modifier = Modifier,
    code: String? = null,
    subtitle: String? = null,
    hasChildren: Boolean = false,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (selected) scheme.primaryContainer else scheme.surfaceContainer,
        Ui.fast(),
        label = "selected"
    )
    ListCard(
        modifier = modifier.semantics { this.selected = selected },
        color = container,
        onLongClick = onLongClick,
        onClick = onClick
    ) {
        LeadingBox {
            if (code != null)
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSecondaryContainer,
                    maxLines = 1
                )
            else
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = scheme.onSecondaryContainer
                )
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = itemText,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) scheme.onPrimaryContainer.copy(alpha = 0.8f) else scheme.onSurfaceVariant,
                    maxLines = 1
                )
        }
        if (selected)
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = scheme.onPrimaryContainer
            )
        if (hasChildren)
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = scheme.onSurfaceVariant
            )
    }
}

fun languageCode(tag: String): String = tag.substringBefore('-').uppercase()
