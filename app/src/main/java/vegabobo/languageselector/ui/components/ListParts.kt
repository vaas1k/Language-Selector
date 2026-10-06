package vegabobo.languageselector.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.theme.Ui
import vegabobo.languageselector.ui.theme.pressClickable

@Composable
fun ListCard(
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressClickable(Ui.CardShape, MaterialTheme.colorScheme.surfaceContainer, onLongClick, onClick)
            .padding(Ui.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.ItemSpacing),
        content = content
    )
}

@Composable
fun HeaderCard(icon: ImageBitmap?, title: String, subtitle: String, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer, Ui.CardShape)
            .padding(Ui.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.CardPadding)
    ) {
        if (icon != null)
            Image(modifier = Modifier.size(Ui.HeaderIconSize), bitmap = icon, contentDescription = null)
        else
            Spacer(Modifier.size(Ui.HeaderIconSize))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(Modifier.padding(top = 4.dp)) {
                TextLabel(
                    text = label,
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun LeadingBox(content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(Ui.IconSize)
            .background(MaterialTheme.colorScheme.secondaryContainer, Ui.IconShape),
        contentAlignment = Alignment.Center
    ) { content() }
}

fun Modifier.edgeFade(state: LazyListState, color: Color, top: Dp = 0.dp): Modifier =
    drawWithContent {
        drawContent()
        val h = Ui.EdgeFade.toPx()
        val t = top.toPx()
        if (state.canScrollBackward)
            drawRect(
                Brush.verticalGradient(listOf(color, Color.Transparent), t, t + h),
                Offset(0f, t),
                Size(size.width, h)
            )
        if (state.canScrollForward)
            drawRect(
                Brush.verticalGradient(listOf(Color.Transparent, color), size.height - h, size.height),
                Offset(0f, size.height - h),
                Size(size.width, h)
            )
    }

@Composable
fun ScrollToTopButton(state: LazyListState, modifier: Modifier = Modifier, afterItems: Int = 8) {
    val visible by remember(state) { derivedStateOf { state.firstVisibleItemIndex >= afterItems } }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(Ui.ENTER_MS)) + scaleIn(tween(Ui.ENTER_MS), initialScale = 0.85f),
        exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.85f)
    ) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                scope.launch { state.animateScrollToItem(0) }
            },
            modifier = Modifier.size(Ui.FabSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.KeyboardArrowUp, contentDescription = stringResource(R.string.scroll_to_top))
            }
        }
    }
}
