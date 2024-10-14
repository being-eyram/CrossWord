import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


data class Cell(val x: Int, val y: Int)

@Composable
fun Board(modifier: Modifier = Modifier, foundAnswers: SnapshotStateList<Answer>) {

    val textStyle =
        TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333f58))
    val cells = convertStructureToCells()
    val maxX = cells.maxOf { it.x } + 1
    val maxY = cells.maxOf { it.y } + 1
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .background(color = Color(0xFFfbbbad))
            .padding(5.dp)
            .size(265.dp)
    ) {
        val squareWidth = size.width / maxOf(maxX, maxY)
        val cellSize = Size(squareWidth, squareWidth)

        cells.forEach { cell ->
            val topLeft = Offset(cell.x * squareWidth, cell.y * squareWidth)

            drawRect(
                color = Color(0XFFee8695),
                size = cellSize,
                topLeft = topLeft,
            )

            drawRect(
                color = Color(0xFF333f58),
                size = cellSize,
                topLeft = topLeft,
                style = Stroke(width = 10F)
            )

        }

        foundAnswers.forEach { ans ->
            ans.word.forEachIndexed { index, char ->

                val topLeft = when (ans.direction) {
                    Direction.DOWN -> Offset(
                        ans.startPosition.x * squareWidth,
                        (ans.startPosition.y + index) * squareWidth
                    )

                    Direction.ACROSS -> Offset(
                        (ans.startPosition.x + index) * squareWidth,
                        ans.startPosition.y * squareWidth
                    )
                }

                //Calculating the cell size of this character
                val rect = Rect(topLeft, cellSize)
                val textLayoutResult = textMeasurer.measure(char.uppercase(), textStyle)
                val textSize = textLayoutResult.size

                drawText(
                    textMeasurer = textMeasurer,
                    text = char.uppercase(),
                    topLeft = Offset(
                        x = rect.center.x - textSize.width / 2,
                        y = rect.center.y - textSize.height / 2
                    ),
                    style = textStyle
                )
            }
        }
    }
}


fun getStructure() = """
    #___
    #_##_
    #_##_
    #_##_
    #____
""".trimIndent()

/*
English test
1. DPad
2. Number of words
 */


fun convertStructureToCells(): List<Cell> {
    val cells = mutableListOf<Cell>()

    getStructure().lines().forEachIndexed { y, line ->
        line.forEachIndexed { x, char ->
            if (char == '#') {
                cells.add(Cell(x, y))
            }
        }
    }

    return cells
}


data class Answer(
    val word: String,
    val direction: Direction,
    val startPosition: Cell
)

enum class Direction { ACROSS, DOWN }


