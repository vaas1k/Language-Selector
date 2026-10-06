/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 *
 * Based on Morphe Manager (GPLv3), app/src/main/java/app/morphe/manager/ui/screen/shared/AppFilterChip.kt
 */

package vegabobo.languageselector.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.ui.theme.Ui
import vegabobo.languageselector.ui.theme.pressScale

@Composable
fun FilterLabel(
    title: String,
    onClick: (Boolean) -> Unit,
    isSelected: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    FilterChip(
        selected = isSelected,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            onClick(isSelected)
        },
        label = { Text(title) },
        modifier = Modifier.pressScale(source),
        leadingIcon = if (isSelected) {
            { Icon(Icons.Outlined.Done, contentDescription = null, Modifier.size(16.dp)) }
        } else null,
        shape = Ui.LabelShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = scheme.surfaceContainerLowest,
            labelColor = scheme.onSurfaceVariant,
            selectedContainerColor = scheme.primaryContainer,
            selectedLabelColor = scheme.onPrimaryContainer,
            selectedLeadingIconColor = scheme.onPrimaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = scheme.outline.copy(alpha = 0.5f),
            selectedBorderColor = scheme.primary,
            selectedBorderWidth = 1.dp
        ),
        interactionSource = source
    )
}
