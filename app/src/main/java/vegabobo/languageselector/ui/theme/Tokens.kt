/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-manager
 *
 * Based on Morphe Manager (GPLv3), app/src/main/java/app/morphe/manager/ui/screen/shared/Animations.kt
 */

package vegabobo.languageselector.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

object Ui {
    val CardShape = RoundedCornerShape(16.dp)
    val CardPadding = 16.dp
    val ItemSpacing = 12.dp
    val ScreenPadding = 16.dp
    val IconSize = 40.dp
    val IconShape = RoundedCornerShape(10.dp)
    val HeaderIconSize = 64.dp
    val LabelShape = RoundedCornerShape(50)
    val EdgeFade = 8.dp
    val FabSize = 44.dp

    const val PRESS_SCALE = 0.97f
    const val ANIM_MS = 50

    fun <T> fast(): FiniteAnimationSpec<T> = tween(ANIM_MS, easing = LinearOutSlowInEasing)

    val enter: EnterTransition = fadeIn(fast())
    val exit: ExitTransition = fadeOut(fast())
}

fun Modifier.animatedItem(scope: LazyItemScope): Modifier = with(scope) {
    this@animatedItem.animateItem(fadeInSpec = null, placementSpec = Ui.fast(), fadeOutSpec = null)
}

fun Modifier.pressScale(source: InteractionSource, shape: Shape? = null): Modifier = composed {
    val pressed by source.collectIsPressedAsState()
    val scale = animateFloatAsState(if (pressed) Ui.PRESS_SCALE else 1f, Ui.fast(), label = "press")
    graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        if (shape != null) {
            this.shape = shape
            clip = true
        }
    }
}

fun Modifier.pressClickable(
    shape: Shape,
    color: Color,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    pressScale(source, shape)
        .background(color)
        .combinedClickable(
            interactionSource = source,
            indication = ripple(),
            onLongClick = onLongClick?.let {
                { haptic.performHapticFeedback(HapticFeedbackType.LongPress); it() }
            },
            onClick = { haptic.performHapticFeedback(HapticFeedbackType.VirtualKey); onClick() }
        )
}
