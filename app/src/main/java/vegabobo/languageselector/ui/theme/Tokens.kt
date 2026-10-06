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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.unit.IntOffset
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
    const val ENTER_MS = 220
    const val EXIT_MS = 320
    const val SCALE_FROM = 0.95f

    val listSpring: FiniteAnimationSpec<IntOffset> = spring(dampingRatio = 0.8f, stiffness = 400f)
    val pressSpring: FiniteAnimationSpec<Float> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)

    val enter: EnterTransition =
        fadeIn(tween(ENTER_MS)) + scaleIn(tween(ENTER_MS), initialScale = SCALE_FROM)
    val exit: ExitTransition =
        fadeOut(tween(EXIT_MS)) + scaleOut(tween(EXIT_MS), targetScale = SCALE_FROM)
}

fun Modifier.animatedItem(scope: LazyItemScope): Modifier = with(scope) {
    this@animatedItem.animateItem(
        fadeInSpec = tween(Ui.ENTER_MS),
        placementSpec = Ui.listSpring,
        fadeOutSpec = tween(180)
    )
}

fun Modifier.pressClickable(
    shape: Shape,
    color: Color,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) Ui.PRESS_SCALE else 1f, Ui.pressSpring, label = "press")
    graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.shape = shape
        clip = true
    }
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
