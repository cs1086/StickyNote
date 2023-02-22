package com.mouse.stickynote.ui.note

import com.mouse.stickynote.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

interface NoteRepository {
//    val noteList:Flow<List<Note>>
    fun getAll(): Flow<List<Note>>
    fun putNote(newNote:Note):Flow<List<Note>>
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

    override fun putNote(newNote: Note) : Flow<List<Note>> {
        //println("@@@@putNote=${newNote.position}")
        noteMap[newNote.id] = newNote
        return  flow { emit(noteMap.elements().toList())}
    }

}