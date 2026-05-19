package com.kavyakanaja.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    val baseX: Float,
    val baseY: Float,
    val size: Float,
    val alpha: Float,
    val speed: Float,
    val angle: Float,
    var time: Float = Random.nextFloat() * 2f * PI.toFloat()
)

@Composable
fun FireflyCanvas(modifier: Modifier = Modifier) {
    val particleCount = 35
    val particles = remember {
        List(particleCount) {
            val x = Random.nextFloat()
            val y = Random.nextFloat()
            Particle(
                x = x, y = y,
                baseX = x, baseY = y,
                size = Random.nextFloat() * 5f + 2f,
                alpha = Random.nextFloat() * 0.6f + 0.2f,
                speed = Random.nextFloat() * 0.008f + 0.002f,
                angle = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "firefly")
    val tick by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tick"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            p.time += p.speed * 0.5f
            val dx = sin(p.time * 0.8f + p.angle) * 0.04f
            val dy = cos(p.time * 0.6f + p.angle * 1.3f) * 0.04f
            p.x = (p.baseX + dx + tick * 0.0001f).mod(1f)
            p.y = (p.baseY + dy).coerceIn(0f, 1f)

            val pulseAlpha = p.alpha * (0.5f + 0.5f * sin(p.time * 2.0f + p.angle))

            drawFirefly(
                x = p.x * w,
                y = p.y * h,
                radius = p.size,
                alpha = pulseAlpha
            )
        }
    }
}

private fun DrawScope.drawFirefly(x: Float, y: Float, radius: Float, alpha: Float) {
    // Glow outer ring
    drawCircle(
        color = Color(0xFFFFE082).copy(alpha = alpha * 0.3f),
        radius = radius * 3f,
        center = Offset(x, y)
    )
    // Glow mid
    drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = alpha * 0.5f),
        radius = radius * 1.8f,
        center = Offset(x, y)
    )
    // Core bright dot
    drawCircle(
        color = Color(0xFFFFF9C4).copy(alpha = alpha),
        radius = radius,
        center = Offset(x, y)
    )
}
