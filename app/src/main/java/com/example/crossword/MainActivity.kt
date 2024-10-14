package com.example.crossword

import Answer
import Board
import Cell
import Direction
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.crossword.ui.theme.CrossWordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrossWordTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0XFFee8695)
                ) { innerPadding ->

                    val foundAnswers = remember { mutableStateListOf<Answer>() }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Board(foundAnswers = foundAnswers)
                        InputPad(
                            onInput = { input ->
                            answers.find { it.word.equals(input, ignoreCase = true) }?.let {
                                foundAnswers.add(it)
                            }
                        })
                    }
                }
            }
        }
    }
}


val answers = listOf(
    Answer(
        "Count", direction = Direction.DOWN,
        startPosition = Cell(0, 0)
    ),
    Answer(
        "Cut", direction = Direction.DOWN,
        startPosition = Cell(2, 1)
    ),
    Answer(
        "Out", direction = Direction.DOWN,
        startPosition = Cell(3, 1)
    ),
)


//FFfbbbad
//FFee8695
//FF4a7a96
//FF333f58
//FF292831
