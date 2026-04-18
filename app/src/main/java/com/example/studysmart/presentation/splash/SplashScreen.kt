package com.example.studysmart.presentation.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studysmart.R
import com.example.studysmart.presentation.destinations.DashboardScreenRouteDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay

@RootNavGraph(start = true)
@Destination
@Composable
fun SplashScreen(
    navigator: DestinationsNavigator
) {
    val logoScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, easing = LinearEasing),
        label = "logo_alpha"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = LinearEasing),
        label = "title_alpha"
    )

    LaunchedEffect(Unit) {
        delay(2000)
        navigator.navigate(DashboardScreenRouteDestination) {
            popUpTo(DashboardScreenRouteDestination.route) { inclusive = true }
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val infiniteTransition = rememberInfiniteTransition(label = "splash_gradient")
    val blend by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blend"
    )

    val topColor = lerp(primary, tertiary, blend)
    val bottomColor = lerp(secondary, primary, blend)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            var showSlogan by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(800)
                showSlogan = true
            }

            if (showSlogan) {
                Text(
                    text = stringResource(id = R.string.splash_tagline),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.animateContentSize()
                )
            }
        }
    }
}
