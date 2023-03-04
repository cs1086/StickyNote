package com.mouse.stickynote.model

import android.os.Bundle
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.*
import kotlin.random.Random.Default.nextInt

@Parcelize
data class Note(
    val id: String,
    val text: String,
    val position: Position,
    var color: YBColor,
    var isSelected: Boolean
) : Parcelable {
    companion object {
        fun createRandomNote(): Note {
            val randomColorIndex = nextInt(YBColor.defaultColors.size)
            val randomPosition = Position(nextInt(-0, 100).toFloat(), nextInt(0, 100).toFloat())
            val randomId = UUID.randomUUID().toString()
            return Note(
                randomId,
                "王柏融",
                randomPosition,
                YBColor.defaultColors[randomColorIndex],
                false
            )
        }

        fun empty(): Note {
            return Note("", "", Position(0f, 0f), YBColor.Aquamarine, false)
        }
    }
}

@Parcelize
data class Position(val x: Float, val y: Float) : Parcelable {
    operator fun plus(other: Position): Position {
        return Position(x + other.x, y + other.y)
    }
}

@Parcelize
data class YBColor(val color: Long) : Parcelable {
    companion object {
        val HotPink = YBColor(0xFFFF7EB9)
        val Aquamarine = YBColor(0xFF7AFCFF)
        val PaleCanary = YBColor(0xFFFEFF9C)
        val Gorse = YBColor(0xFFFFF740)

        val defaultColors = listOf(HotPink, Aquamarine, PaleCanary, Gorse)
    }
}

class NoteType : NavType<Note>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Note? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): Note {
        return Gson().fromJson(value, Note::class.java)
    }

    override fun put(bundle: Bundle, key: String, value: Note) {
        bundle.putParcelable(key, value)
    }
}