package com.mouse.stickynote.ui.note

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.mouse.stickynote.model.Note
import com.mouse.stickynote.model.Position
import com.mouse.stickynote.model.YBColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FilebaseRepository :NoteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val query = firestore.collection(COLLECTION_NOTES)
        .limit(100)
    private var allNotes= emptyList<Note>()
    init {
        query.addSnapshotListener { result, e ->
            println("@@@@取道db資料")
            result?.let { onSnapshotUpdated(it) }
        }
    }

    override fun getAll(): Flow<List<Note>> {
        return flow { emit(allNotes)}
    }

    override fun putNote(note: Note): Flow<List<Note>> {
        setNoteDocument(note)
        return getAll()

    }

    private fun onSnapshotUpdated(snapshot: QuerySnapshot) {
        allNotes = snapshot
            .map { document -> documentToNotes(document) }.let{
                if (it.isEmpty()) listOf(Note.createRandomNote()) else it
            }
        println("@@@@onSnapshotUpdated.allNotes=$allNotes")
    }

    private fun setNoteDocument(note: Note) {
        val noteData = hashMapOf(
            FIELD_TEXT to note.text,
            FIELD_COLOR to note.color.color,
            FIELD_POSITION_X to note.position.x,
            FIELD_POSITION_Y to note.position.y
        )

        firestore.collection(COLLECTION_NOTES)
            .document(note.id)
            .set(noteData)
    }

    private fun documentToNotes(document: QueryDocumentSnapshot): Note {
        val data: Map<String, Any> = document.data

        val text = data[FIELD_TEXT] as String
        val color = YBColor(data[FIELD_COLOR] as Long)
        println("@@@@documentToNotes.data=${data[FIELD_POSITION_X]}")
        val positionX = (data[FIELD_POSITION_X]) as Double? ?: 0F
        val positionY = data[FIELD_POSITION_Y] as Double? ?: 0F
        val position = Position(positionX.toFloat(), positionY.toFloat())
        return Note(document.id, text, position, color)
    }

    companion object {
        const val COLLECTION_NOTES = "Notes"
        const val FIELD_TEXT = "text"
        const val FIELD_COLOR = "color"
        const val FIELD_POSITION_X = "positionX"
        const val FIELD_POSITION_Y = "positionY"
    }
}