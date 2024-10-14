package com.example.crossword

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun InputPad(
    modifier: Modifier = Modifier,
    chars: String = "Counts",
    onInput: (String) -> Unit
) {
    var currentPosition by remember { mutableStateOf(Offset.Zero) }

    val path by remember { mutableStateOf(Path()) }
    val pathTemp by remember { mutableStateOf(Path()) }
    val collisionIndices = remember { mutableStateListOf<Key>() }

    val context = LocalContext.current
    val mTextMeasurer = rememberTextMeasurer()


    fun onDrag(change: PointerInputChange, dragAmount: Offset) {
        val prevSelectedKey = collisionIndices.lastOrNull()

        pathTemp.reset() // Reset the path to make it straight
        prevSelectedKey?.let {
            pathTemp.moveTo(it.position.x, it.position.y)
        }

        if (collisionIndices.isNotEmpty()) {
            currentPosition = change.position
            pathTemp.lineTo(currentPosition.x, currentPosition.y)
        }
    }

    fun onDragEnd() {
        val input = collisionIndices.joinToString(separator = "") { it.char }
        onInput.invoke(input)

        path.reset()
        pathTemp.reset()

        collisionIndices.clear()
        currentPosition = Offset.Zero
    }


    Canvas(
        modifier = modifier
            .background(Color(0xFFfbbbad), shape = CircleShape)
            .padding(10.dp)
            .requiredSize(265.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    currentPosition = down.position
                    path.moveTo(currentPosition.x, currentPosition.y)

                    if (waitForUpOrCancellation() != null) onDragEnd()
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = ::onDrag,
                    onDragEnd = ::onDragEnd
                )
            }
    ) {
        val radiusMain = size.minDimension / 2

        val letterPlotPoints by lazy {
            calculatePlotPoints(
                origin = center,
                orbitalRadius = radiusMain * 0.8F,
                numberOfSides = chars.length,
            )
        }

        drawInputPath(path = path, pathTemp = pathTemp)

        letterPlotPoints.forEachIndexed { index, offset ->
            val key = Key(index, offset, chars[index].uppercase())

            val isColliding = detectCollisions(
                touchPosition = currentPosition,
                center = offset,
                radiusMain * 0.23F
            )

            if (isColliding && !collisionIndices.contains(key)) {
                collisionIndices.add(key)
                path.lineTo(offset.x, offset.y)
            }

            drawInputKey(
                key,
                isSelected = collisionIndices.contains(key),
                radius = radiusMain * 0.23F,
                center = offset,
                textMeasurer = mTextMeasurer,
            )
        }
    }
}


private fun DrawScope.drawInputKey(
    key: Key,
    isSelected: Boolean,
    radius: Float = 80f,
    center: Offset,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333f58))
) {
    val textLayoutResult = textMeasurer.measure(key.char, textStyle)
    val textSize = textLayoutResult.size

    drawCircle(
        color = if (isSelected) Color(0XFFee8695) else Color.White.copy(alpha = .25F),
        radius = radius,
        center = center
    )

    val topLeft = Offset(
        x = center.x - textSize.width / 2,
        y = center.y - textSize.height / 2
    )

    drawText(
        textMeasurer = textMeasurer,
        text = key.char,
        topLeft = topLeft,
        style = textStyle
    )
}

private fun DrawScope.drawInputPath(
    stroke: Stroke = Stroke(
        width = 20f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Bevel,
    ),
    path: Path,
    pathTemp: Path,
) {
    drawPath(
        pathTemp,
        color = Color(0XFFee8695),
        style = stroke
    )

    drawPath(
        path,
        color = Color(0XFFee8695),
        style = stroke
    )
}


data class Key(
    val index: Int,
    val position: Offset,
    val char: String,
)

internal fun calculatePlotPoints(
    origin: Offset,
    orbitalRadius: Float,
    numberOfSides: Int,
): List<Offset> {
    val plotPointRadianSeparation = 2 * PI / numberOfSides
    val plotRadianFromOrigin = List(numberOfSides) { index ->
        index * plotPointRadianSeparation
    }

    return plotRadianFromOrigin.map { radian ->
        val x = orbitalRadius * cos(radian)
        val y = orbitalRadius * sin(radian)
        Offset(x.toFloat(), y.toFloat()) + origin
    }
}

private fun detectCollisions(
    touchPosition: Offset,
    center: Offset,
    r: Float
): Boolean {
    val distanceX = touchPosition.x - center.x
    val distanceY = touchPosition.y - center.y

    return (distanceX * distanceX) + (distanceY * distanceY) <= r * r;
}