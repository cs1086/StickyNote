package com.mouse.stickynote.ui.note

import androidx.lifecycle.ViewModel
import com.mouse.stickynote.model.Note
import kotlinx.coroutines.flow.MutableStateFlow

class EditextViewModel(
    val note: Note,
    val noteRepository: NoteRepository,
) : ViewModel() {
    var text = MutableStateFlow("")

    init {
        onTextChange(note.text)
    }

    fun onTextChange(text: String) {
        this.text.value = text
    }

    fun onCancelClicked() {

    }

    fun onConfirmClicked() {
        noteRepository.putNote(note.copy(text = text.value))
    }
}