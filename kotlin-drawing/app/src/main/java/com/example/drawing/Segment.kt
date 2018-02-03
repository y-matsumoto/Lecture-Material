package com.example.drawing

import java.util.*

class Segment(c: Int) {

    internal val points = ArrayList<Point>()
    var color: Int = c

    fun addPoint(x: Int, y: Int) {
        val p = Point(x, y)
        points.add(p)
    }

    fun getPoints(): List<Point> {
        return points
    }
}
