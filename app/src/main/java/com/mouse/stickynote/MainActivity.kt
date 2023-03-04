package com.mouse.stickynote

import android.media.metrics.EditingSession
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.mouse.stickynote.model.Note
import com.mouse.stickynote.model.NoteType
import com.mouse.stickynote.model.Position
import com.mouse.stickynote.model.YBColor
import com.mouse.stickynote.ui.note.EditextViewModel
import com.mouse.stickynote.ui.note.EditorViewModel
import com.mouse.stickynote.ui.note.FilebaseRepository
import com.mouse.stickynote.ui.theme.StickyNoteTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KFunction1
import kotlin.reflect.KFunction2

sealed class Screen(val route: String) {
    object Board : Screen("board")
    object EditText : Screen("editText") {
        const val KEY_NOTE_ID = "noteId"
    }

}

//todo 有位移的偏差bug，因為flow的上傳與畫面刷新因為網路問題比較慢導致
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            StickyNoteTheme {
                // A surface container using the 'background' color from the theme
                NavHost(navController = navController, startDestination = Screen.Board.route) {
                    composable(Screen.Board.route) {
                        val viewModel by viewModel<EditorViewModel>()
                        EditorScreen(viewModel, openEditTextScreen = { note ->
                            println("!!!!note=$note")
                            val json = Uri.encode(Gson().toJson(note))
                            navController.navigate(Screen.EditText.route + "/" + json) // [4]
                        })
                    }
                    composable(Screen.EditText.route + "/" + "{${Screen.EditText.KEY_NOTE_ID}}",
                        arguments = listOf(
                            navArgument(Screen.EditText.KEY_NOTE_ID) {
                                type = NoteType()
                            }
                        )) { // [3]
                        val viewModel by it.viewModel<EditextViewModel>() {
                            parametersOf(it.arguments?.getParcelable(Screen.EditText.KEY_NOTE_ID))
                        }
                        EditextScreen(viewModel) {
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditorScreen(viewModel: EditorViewModel, openEditTextScreen: (Note) -> Unit) {
    Surface {
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput("Editor") {
                detectTapGestures { viewModel.blurNote() }
            }) {
            val notes by viewModel.allNotes.collectAsState(initial = emptyList())
            val selectingNote = notes.find { it.isSelected }
            BoardView(notes, viewModel::moveNote, viewModel::tapNote)
            AnimatedVisibility(
                visible = notes.firstOrNull { it.isSelected } != null, modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                FloatingActionButton(onClick = { viewModel.addNote() }) {
                    Icon(Icons.Rounded.Add, contentDescription = "顯示加號")
                }
            }
            AnimatedVisibility(
                visible = notes.firstOrNull { it.isSelected } != null,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                MenuView(
                    selectingNote,
                    viewModel::deleteNote,
                    viewModel::changeColor,
                    openEditTextScreen
                )
            }
        }
    }
}

@Composable
fun BoardView(
    notes: List<Note>,
    moveNote: KFunction2<String, Position, Unit>,
    tapNote: KFunction1<Note, Unit>
) {
    println("@@@@Mainactivity.boardView.nate=$notes")
    Box(Modifier.fillMaxSize()) {
        notes.forEach { note ->
            //移動時callback
            val onNotePositionChanged: (Position) -> Unit = { delta ->
                GlobalScope.launch {
                    moveNote(note.id, delta)
                }
            }
            val isSelected = note.isSelected
            StickyNote(
                Modifier.align(Alignment.Center),
                onNotePositionChanged,
                note = note,
                onNoteClick = tapNote,
                isSelected
            )
        }
    }
}

private val hilightBorder: @Composable Modifier.(Boolean) -> Modifier = { show ->
    if (show) {
        this.border(2.dp, Color.Black, MaterialTheme.shapes.medium)
    } else {
        this
    }.padding(8.dp)
}

@Composable
fun StickyNote(
    modifier: Modifier = Modifier, // [1]
    onPositionChanged: (Position) -> Unit = {},
    note: Note,
    onNoteClick: KFunction1<Note, Unit>,
    isSelected: Boolean
) {
    println("@@@@Mainactivity.StickyNote.nate=$note")
    Surface(
        elevation = 8.dp,
        color = Color(note.color.color),
        modifier = modifier
            .width(108.dp)
            .height(108.dp)
            .offset(note.position.x.dp, note.position.y.dp)
            .hilightBorder(isSelected)

    ) {
        Column(modifier = Modifier
            .padding(16.dp)
            .pointerInput(note.id) {
                detectDragGestures() { change, dragAmount ->
                    change.consumeAllChanges()
//                    println("@@@@consumeAllChanges.x=${dragAmount.x},y=${dragAmount.y}")
                    onPositionChanged(Position(dragAmount.x, dragAmount.y))
                }
            }
            .pointerInput(note.id) {
                detectTapGestures(onTap = {
                    onNoteClick(note)
                })
            }
        ) {
            Text(note.text, style = MaterialTheme.typography.h5)
        }
    }
}

@Composable
fun MenuView(
    selectNote: Note?, deleteNote: () -> Unit,
    changeColor: KFunction1<YBColor, Unit>,
    openEditTextScreen: (Note) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        color = MaterialTheme.colors.surface

    ) {
        Row {
            IconButton(onClick = { deleteNote() }) {
                Icon(Icons.Rounded.Delete, contentDescription = "刪除")
            }
            IconButton(onClick = { openEditTextScreen(selectNote!!) }) {
                Icon(Icons.Rounded.Edit, contentDescription = "修改")
            }
            IconButton(onClick = { expanded = true }) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(selectNote?.color?.color ?: 0x00000000))
                        .size(24.dp)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    YBColor.defaultColors.forEach {
                        DropdownMenuItem(onClick = { expanded = false;changeColor(it) }) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(it.color))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditextScreen(editextViewModel: EditextViewModel, onLeaveScreen: () -> Unit) {
    println("!!!!viewMdoe=${editextViewModel.text.collectAsState().value}")
    val text by editextViewModel.text.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        TextField(
            value = text,
            onValueChange = editextViewModel::onTextChange,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(fraction = 0.8f),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                textColor = Color.White
            ),
            textStyle = MaterialTheme.typography.h5
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopStart),
            onClick = onLeaveScreen
        ) {
            Icon(Icons.Rounded.Close, "cancel")
        }
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = editextViewModel::onConfirmClicked
        ) {
            Icon(Icons.Rounded.Check, "check")
        }
    }
//    IconButton()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val editextViewModel = EditextViewModel(Note.createRandomNote(), FilebaseRepository())
    EditextScreen(editextViewModel = editextViewModel) {}
}