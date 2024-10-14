package com.example.crossword

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WordSearchGame() {
    val gridSize = 8
    val grid = remember { generateGrid(gridSize) }
    val words = remember { listOf("HELLO", "WORLD", "JETPACK", "COMPOSE") }
    var selectedCells by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var foundWords by remember { mutableStateOf(setOf<String>()) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Word Search Game", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
        GridView(
            grid = grid,
            selectedCells = selectedCells,
            foundWords = foundWords,
            onDragStart = { selectedCells = listOf(it) },
            onDrag = { newCell ->
                if (newCell !in selectedCells) {
                    selectedCells = selectedCells + newCell
                }
            },
            onDragEnd = {
                val word = selectedCells.map { grid[it.first][it.second] }.joinToString("")
                if (word in words && word !in foundWords) {
                    foundWords = foundWords + word
                }
                selectedCells = listOf()
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Found Words: ${foundWords.joinToString(", ")}")
    }
}

@Composable
fun GridView(
    grid: List<List<Char>>,
    selectedCells: List<Pair<Int, Int>>,
    foundWords: Set<String>,
    onDragStart: (Pair<Int, Int>) -> Unit,
    onDrag: (Pair<Int, Int>) -> Unit,
    onDragEnd: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(16.dp)
            .background(Color.LightGray)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val cellSize = size.width / grid.size
                        val row = (offset.y / cellSize).toInt()
                        val col = (offset.x / cellSize).toInt()
                        onDragStart(row to col)
                    },
                    onDrag = { change, _ ->
                        val cellSize = size.width / grid.size
                        val row = (change.position.y / cellSize).toInt()
                        val col = (change.position.x / cellSize).toInt()
                        if (row in grid.indices && col in grid[0].indices) {
                            onDrag(row to col)
                        }
                    },
                    onDragEnd = { onDragEnd() }
                )
            }
    ) {
        grid.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, char ->
                val isSelected = Pair(rowIndex, colIndex) in selectedCells
                val isFoundWord = foundWords.any { word ->
                    selectedCells.map { grid[it.first][it.second] }.joinToString("") == word
                }
                CellView(
                    char = char,
                    isSelected = isSelected,
                    isFoundWord = isFoundWord,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .align(Alignment.TopStart)
                        .offset(
                            x = (colIndex * (100f / grid.size)).dp,
                            y = (rowIndex * (100f / grid.size)).dp
                        )
                )
            }
        }
    }
}

@Composable
fun CellView(char: Char, isSelected: Boolean, isFoundWord: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(1.dp)
            .background(
                when {
                    isFoundWord -> Color.Green
                    isSelected -> Color.Yellow
                    else -> Color.White
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(char.toString(), fontSize = 18.sp)
    }
}

fun generateGrid(size: Int): List<List<Char>> {
    return List(size) { List(size) { ('A'..'Z').random() } }
}