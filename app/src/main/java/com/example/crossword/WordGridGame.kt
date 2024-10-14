package com.example.crossword

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


@Composable
fun WordGridGame() {
    val grid = remember { generateGrid() }
    val selectedWord = remember { mutableStateOf("") }
    val correctWords = listOf("WORD", "GAME", "TEST", "KOTLIN", "COMPOSE")
    var highlightedCells by remember { mutableStateOf(emptyList<Pair<Int, Int>>()) }
    var isCorrectWord by remember { mutableStateOf(false) }

    // Gesture detection for swiping
    var startPosition by remember { mutableStateOf<Offset?>(null) }
    var endPosition by remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BasicText(
            text = "Selected Word: ${selectedWord.value}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),

        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)  // Keeps the grid square
                .padding(16.dp)
                .background(Color.LightGray)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            startPosition = offset
                        },
                        onDrag = { change, dragAmount ->
                            //change.consume()
                            endPosition = change.position
                            // Highlight the cells while dragging
                            highlightedCells = getHighlightedCells(startPosition!!, endPosition!!,40.dp.toPx())
                        },
                        onDragEnd = {
                            if (startPosition != null && endPosition != null) {
                                val selected = getSelectedWord(grid, startPosition!!, endPosition!!, 40.dp.toPx())
                                selectedWord.value = selected
                                isCorrectWord = correctWords.contains(selected)
                            }
                        }
                    )
                }
        ) {
            DrawGrid(grid, highlightedCells, isCorrectWord)
        }
    }
}

@Composable
fun DrawGrid(grid: List<List<Char>>, highlightedCells: List<Pair<Int, Int>>, isCorrectWord: Boolean) {
    val cellSize = 40.dp
    Column {
        for (i in grid.indices) {
            Row {
                for (j in grid[i].indices) {
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .zIndex(if (highlightedCells.contains(i to j)) 1f else 0f)
                            .background(
                                when {
                                    highlightedCells.contains(i to j) && isCorrectWord -> Color.Yellow
                                    highlightedCells.contains(i to j) -> Color(0xFFFFCDD2) // Shade of red for incorrect words
                                    else -> Color.White
                                }
                            )
                            .border(1.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = grid[i][j].toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawGrid(grid: List<List<Char>>, highlightedCells: List<Pair<Int, Int>>) {
    val cellSize = 40.dp
    Column {
        for (i in grid.indices) {
            Row {
                for (j in grid[i].indices) {
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .zIndex(if (highlightedCells.contains(i to j)) 1f else 0f)
                            .background(if (highlightedCells.contains(i to j)) Color.Yellow else Color.White)
                            .border(1.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = grid[i][j].toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun generateGrid(): List<List<Char>> {
    val letters = ('A'..'Z').toList()
    return List(8) { List(8) { letters.random() } }
}

fun getSelectedWord(grid: List<List<Char>>, start: Offset, end: Offset, gridSize: Float): String {


    val startX = (start.x / gridSize).toInt()
    val startY = (start.y / gridSize).toInt()
    val endX = (end.x / gridSize).toInt()
    val endY = (end.y / gridSize).toInt()

    return if (startX == endX) {
        // Vertical swipe
        (min(startY, endY)..max(startY, endY)).map { y -> grid[y][startX] }.joinToString("")
    } else if (startY == endY) {
        // Horizontal swipe
        (min(startX, endX)..max(startX, endX)).map { x -> grid[startY][x] }.joinToString("")
    } else if (abs(endX - startX) == abs(endY - startY)) {
        // Diagonal swipe
        (0..abs(endX - startX)).map { i -> grid[min(startY, endY) + i][min(startX, endX) + i] }
            .joinToString("")
    } else {
        ""
    }
}

fun getHighlightedCells(start: Offset, end: Offset, gridSize: Float): List<Pair<Int, Int>> {
    val startX = (start.x / gridSize).toInt()
    val startY = (start.y / gridSize).toInt()
    val endX = (end.x / gridSize).toInt()
    val endY = (end.y / gridSize).toInt()

//    return if (startX == endX) {
//        // Vertical swipe
//        (minOf(startY, endY)..maxOf(startY, endY)).map { y -> startX to y }
//    } else if (startY == endY) {
//        // Horizontal swipe
//        (minOf(startX, endX)..maxOf(startX, endX)).map { x -> x to startY }
//    } else {
//        emptyList() // Diagonal or other directions can be handled here
//    }

    return if (startX == endX) {
        // Vertical swipe
        (min(startY, endY)..max(startY, endY)).map { y -> startX to y }
    } else if (startY == endY) {
        // Horizontal swipe
        (min(startX, endX)..max(startX, endX)).map { x -> x to startY }
    } else if (abs(endX - startX) == abs(endY - startY)) {
        // Diagonal swipe
        (0..abs(endX - startX)).map { i -> min(startX, endX) + i to min(startY, endY) + i }
    } else {
        emptyList()
    }
}
