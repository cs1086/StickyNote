package com.mouse.stickynote.ui.note

import android.media.CamcorderProfile.getAll
import androidx.lifecycle.ViewModel
import com.mouse.stickynote.model.Note
import com.mouse.stickynote.model.Position
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BoardViewModel(

): ViewModel() {

    private val noteRepository: NoteRepository=InMemoryrNoteRepository()
    val allNotes:MutableStateFlow<List<Note>> = MutableStateFlow(arrayListOf())
    init {
        GlobalScope.launch {
            noteRepository.getAll().collect{
                allNotes.emit(it)
            }
        }
    }
    suspend fun moveNote(noteId:String, delta:Position){
        val note=allNotes.value.find { it.id== noteId}
        if (note != null) {
            noteRepository.putNote(note.copy(position = note.position+delta))
            noteRepository.getAll().collect{
                allNotes.emit(it)
            }
        }

    }
}