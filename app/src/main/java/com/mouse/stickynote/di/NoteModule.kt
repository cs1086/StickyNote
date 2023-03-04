package com.mouse.stickynote.di
import com.mouse.stickynote.model.Note
import com.mouse.stickynote.ui.note.EditextViewModel
import com.mouse.stickynote.ui.note.EditorViewModel
import com.mouse.stickynote.ui.note.FilebaseRepository
import com.mouse.stickynote.ui.note.NoteRepository
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel
fun getNoteModule() = module {
    viewModel{
        EditorViewModel(get())
    }
    viewModel{(note: Note)->
        EditextViewModel(note=note,get())
    }
    single<NoteRepository>{
        FilebaseRepository()
    }
}