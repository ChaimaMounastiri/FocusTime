package com.example.studysmart.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

object NavAnimations : DestinationStyle.Animated {

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(700)
        ) + fadeIn(animationSpec = tween(700))
    }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(700)
        ) + fadeOut(animationSpec = tween(700)) + scaleOut(targetScale = 0.9f, animationSpec = tween(700))
    }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(700)
        ) + fadeIn(animationSpec = tween(700))
    }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(700)
        ) + fadeOut(animationSpec = tween(700)) + scaleOut(targetScale = 0.9f, animationSpec = tween(700))
    }
}
