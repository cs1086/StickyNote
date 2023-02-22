package com.mouse.stickynote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mouse.stickynote.model.Note
import com.mouse.stickynote.model.Position
import com.mouse.stickynote.ui.note.BoardViewModel
import com.mouse.stickynote.ui.theme.StickyNoteTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

//todo 有位移的偏差bug
class MainActivity : ComponentActivity() {
    val viewModel by viewModels<BoardViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StickyNoteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        BoardView(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BoardView(boardViewModel: BoardViewModel) {
    val notes by boardViewModel.allNotes.collectAsState(initial = emptyList())
    Box(Modifier.fillMaxSize()) {
        notes.forEach { note ->
            //移動時callback
            val onNotePositionChanged: (Position) -> Unit = { delta ->
                GlobalScope.launch {
                    boardViewModel.moveNote(note.id, delta)
                }
            }
            //點下去時callback
            val onPositionDragDown: (Position) -> Unit = { delta ->
                GlobalScope.launch {
                    boardViewModel.dragDownPosition = delta
                }
            }
            StickyNote(
                Modifier.align(Alignment.Center),
                onNotePositionChanged,
                onPositionDragDown,
                note = note
            )
        }
    }
}

@Composable
fun StickyNote(
    modifier: Modifier = Modifier, // [1]
    onPositionChanged: (Position) -> Unit = {},
    onPositionDragDown: (Position) -> Unit = {},
    note: Note
) {
    Surface(
        elevation = 8.dp,
        color = Color(note.color.color),
        modifier = modifier
            .width(108.dp)
            .height(108.dp)
            .offset(note.position.x.dp, note.position.y.dp)

    ) {
        Column(modifier = Modifier
            .padding(16.dp)
            .pointerInput(note.id) {
                detectDragGestures() { change, dragAmount ->
                     change.consumeAllChanges()
                    println("@@@@consumeAllChanges.x=${dragAmount.x},y=${dragAmount.y}")
                    onPositionChanged(Position(dragAmount.x, dragAmount.y))
                }
            }
        ) {
            Text(note.text, style = MaterialTheme.typography.h5)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    StickyNoteTheme {
//        StickyNote("Android",10,10)
//        StickyNote("Android",80,60)
    }
}