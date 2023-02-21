package com.mouse.stickynote.model

import java.util.*
import kotlin.random.Random.Default.nextInt

data class Note(val id:String,
                val text:String,
                val position:Position,
val color: YBColor
) {
    companion object{
        fun createRandomNote():Note{
            val randomColorIndex= nextInt(YBColor.defaultColors.size)
            val randomPosition= Position(nextInt(-0,100).toFloat(),nextInt(0,100).toFloat())
            val randomId=UUID.randomUUID().toString()
            return Note(randomId,"王柏融",randomPosition,YBColor.defaultColors[randomColorIndex])
        }
    }
}
data class Position(val x: Float, val y: Float){
    operator fun plus(other:Position):Position{
        return Position(x+other.x,y+other.y)
    }
}
data class YBColor(val color:Long){
    companion object {
        val HotPink = YBColor(0xFFFF7EB9)
        val Aquamarine = YBColor(0xFF7AFCFF)
        val PaleCanary = YBColor(0xFFFEFF9C)
        val Gorse = YBColor(0xFFFFF740)

        val defaultColors = listOf(HotPink, Aquamarine, PaleCanary, Gorse)
    }
}