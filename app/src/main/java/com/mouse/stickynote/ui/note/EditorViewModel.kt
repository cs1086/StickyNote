package com.mouse.stickynote.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mouse.stickynote.model.Note
import com.mouse.stickynote.model.Position
import com.mouse.stickynote.model.YBColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditorViewModel(val noteRepository: NoteRepository) : ViewModel() {

//    private val noteRepository: NoteRepository = FilebaseRepository()
    val allNotes: MutableStateFlow<List<Note>> = MutableStateFlow(arrayListOf())

    //val selectingNote=MutableStateFlow(Note.empty())//當前選擇的note
    init {
        GlobalScope.launch {
//            delay(1000)
            noteRepository.getAll().buffer().conflate().collect {
                println("@@@@ViewModel..getAll=$it")
                allNotes.emit(it)
//                it.find { note->note.isSelected }?.let { it1 -> selectingNote.emit(it1) }
            }
        }
    }

    fun moveNote(noteId: String, delta: Position) {
        val note = allNotes.value.find { it.id == noteId }
        if (note != null) {
            println("@@@@ViewModel.moveNote.position=${note.position + delta}")
            noteRepository.putNote(note.copy(position = note.position + delta))
//            noteRepository.getAll().collect {
////                println("@@@@moveNote.emit=${it.get(0).position}")
//                allNotes.emit(it)
//            }
        }
    }

    fun tapNote(note: Note) {
        viewModelScope.launch {
            blurNote()
            allNotes.value.find { note.id == it.id }?.let {
                noteRepository.putNote(it.copy(isSelected = true))//copy的方式才能觸發compose的變動
            }
            println("@@@@ViewModel.tapNote= $note")

        }

    }

    fun blurNote() {
        viewModelScope.launch {
            allNotes.value.find { note -> note.isSelected }?.let {
                noteRepository.putNote(it.copy(isSelected = false))//copy的方式才能觸發compose的變動
            }

        }

    }

    fun addNote() {
        noteRepository.createNote(Note.createRandomNote())
    }

    fun deleteNote() {
        noteRepository.deleteNote(allNotes.value.find { note -> note.isSelected }!!.id)
    }

    fun changeColor(color: YBColor) {
        val note = allNotes.value.find { it.isSelected }
        println("@@@@ViewModel..changeColor=firstColor= $note")
//        note?.color=color
        println("@@@@ViewModel..changeColor=$color to $note")
        noteRepository.putNote(note!!.copy(color = color))//copy的方式才能觸發compose的變動

    }
}