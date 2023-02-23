package com.mouse.stickynote.ui.note

import com.mouse.stickynote.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

interface NoteRepository {
    fun getAll(): Flow<List<Note>>
    fun putNote(newNote:Note)
}

class InMemoryrNoteRepository : NoteRepository {
    private val noteMap = ConcurrentHashMap<String, Note>()
//    override val noteList:Flow<List<Note>> = getAll()
    init {
        val initNote = Note.createRandomNote()
        noteMap[initNote.id] = initNote
    }

//    private val allNotes = listOf(Note.createRandomNote())
    override fun getAll(): Flow<List<Note>> {
        return flow { emit(noteMap.elements().toList())}
    }

    override fun putNote(newNote: Note) {
        //println("@@@@putNote=${newNote.position}")
        noteMap[newNote.id] = newNote
    }

}