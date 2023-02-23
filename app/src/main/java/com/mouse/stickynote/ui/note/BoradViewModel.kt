package com.mouse.stickynote.ui.note

import android.media.CamcorderProfile.getAll
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mouse.stickynote.model.Note
import com.mouse.stickynote.model.Position
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BoardViewModel(

) : ViewModel() {

    private val noteRepository: NoteRepository = FilebaseRepository()
    val allNotes: MutableStateFlow<List<Note>> = MutableStateFlow(arrayListOf())

    init {
        GlobalScope.launch {
//            delay(1000)
            noteRepository.getAll().buffer().conflate().collect {
                println("@@@@noteRepository.getAll=$it")
                allNotes.emit(it)
            }
        }
    }

    suspend fun moveNote(noteId: String, delta: Position) {
        val note = allNotes.value.find { it.id == noteId }
        if (note != null) {
            println("@@@@moveNote.position=${note.position + delta}")
            noteRepository.putNote(note.copy(position = note.position + delta))
            noteRepository.getAll().collect {
//                println("@@@@moveNote.emit=${it.get(0).position}")
                allNotes.emit(it)
            }
        }
    }
}