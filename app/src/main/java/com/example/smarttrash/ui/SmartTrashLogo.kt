package com.example.smarttrash.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarttrash.R
import com.example.smarttrash.ui.theme.SmartTrashTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SmartTrashLogo(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.smart_trash_logo_content_description)
    Box(
        modifier = modifier
            .size(200.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(48.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF5AB12D),
                        Color(0xFF1E5D11)
                    )
                )
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            val w = size.width
            val h = size.height

            val swooshPath = Path().apply {
                moveTo(w * 0.1f, h * 0.85f)
                cubicTo(
                    w * 0.2f, h * 0.95f,
                    w * 0.8f, h * 0.85f,
                    w * 0.95f, h * 0.65f
                )
            }
            drawPath(
                path = swooshPath,
                color = Color(0xFFFDB913),
                style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
            )

            val binPath = Path().apply {
                moveTo(w * 0.25f, h * 0.35f)
                lineTo(w * 0.75f, h * 0.35f)
                lineTo(w * 0.7f, h * 0.85f)
                lineTo(w * 0.3f, h * 0.85f)
                close()
            }
            drawPath(path = binPath, color = Color(0xFF0E430A))

            drawRoundRect(
                color = Color(0xFF0E430A),
                topLeft = Offset(w * 0.22f, h * 0.3f),
                size = Size(w * 0.56f, h * 0.06f),
                cornerRadius = CornerRadius(w * 0.01f, w * 0.01f)
            )

            val handlePath = Path().apply {
                moveTo(w * 0.42f, h * 0.3f)
                cubicTo(
                    w * 0.42f, h * 0.22f,
                    w * 0.58f, h * 0.22f,
                    w * 0.58f, h * 0.3f
                )
            }
            drawPath(
                path = handlePath,
                color = Color.White,
                style = Stroke(width = w * 0.03f, cap = StrokeCap.Round)
            )

            val stripePath = Path().apply {
                moveTo(w * 0.55f, h * 0.35f)
                cubicTo(w * 0.6f, h * 0.5f, w * 0.55f, h * 0.7f, w * 0.55f, h * 0.85f)
                lineTo(w * 0.65f, h * 0.85f)
                cubicTo(w * 0.65f, h * 0.7f, w * 0.7f, h * 0.5f, w * 0.65f, h * 0.35f)
                close()
            }
            drawPath(path = stripePath, color = Color(0xFF2E7D32).copy(alpha = 0.3f))

            val pixelColor = Color.White.copy(alpha = 0.8f)
            val pSize = w * 0.04f
            drawRect(pixelColor, Offset(w * 0.15f, h * 0.4f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.21f, h * 0.42f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.18f, h * 0.46f), Size(pSize, pSize))
            
            drawRect(pixelColor, Offset(w * 0.28f, h * 0.45f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.32f, h * 0.48f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.26f, h * 0.52f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.34f, h * 0.55f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.29f, h * 0.6f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.35f, h * 0.65f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.31f, h * 0.7f), Size(pSize, pSize))
            drawRect(pixelColor, Offset(w * 0.38f, h * 0.72f), Size(pSize, pSize))

            val starPath = Path().apply {
                val centerX = w * 0.82f
                val centerY = h * 0.25f
                val outerRadius = w * 0.12f
                val innerRadius = w * 0.05f
                val numPoints = 5
                var angle = -Math.PI / 2
                val angleStep = Math.PI / numPoints
                
                for (i in 0 until numPoints * 2) {
                    val r = if (i % 2 == 0) outerRadius else innerRadius
                    val x = (centerX + r * cos(angle)).toFloat()
                    val y = (centerY + r * sin(angle)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                    angle += angleStep
                }
                close()
            }
            drawPath(path = starPath, color = Color(0xFFFDB913))

            val circleCenter = Offset(w * 0.78f, h * 0.55f)
            val circleRadius = w * 0.12f
            drawCircle(
                color = Color(0xFFFDB913),
                radius = circleRadius,
                center = circleCenter
            )
            drawCircle(
                color = Color.White,
                radius = circleRadius,
                center = circleCenter,
                style = Stroke(width = w * 0.015f)
            )

            val checkPath = Path().apply {
                moveTo(circleCenter.x - circleRadius * 0.4f, circleCenter.y)
                lineTo(circleCenter.x - circleRadius * 0.1f, circleCenter.y + circleRadius * 0.3f)
                lineTo(circleCenter.x + circleRadius * 0.4f, circleCenter.y - circleRadius * 0.3f)
            }
            drawPath(
                path = checkPath,
                color = Color.White,
                style = Stroke(width = w * 0.04f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SmartTrashLogoPreview() {
    SmartTrashTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            SmartTrashLogo()
        }
    }
}
