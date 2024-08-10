package com.martishin.graphsonmaps.roadgraph

import com.martishin.graphsonmaps.geography.GeographicPoint
import java.io.File
import java.util.Locale
import java.util.Scanner

class CorrectAnswer(
    file: String,
    hasEdges: Boolean,
) {
    var vertices: Int = 0
    var edges: Int = 0
    var path: List<GeographicPoint>? = null

    init {
        try {
            val scanner = Scanner(File(file))
            scanner.useLocale(Locale.ENGLISH)
            if (hasEdges) {
                vertices = scanner.nextInt()
                edges = scanner.nextInt()
            }
            if (scanner.hasNextDouble()) {
                path = ArrayList()
            }
            while (scanner.hasNextDouble()) {
                val x = scanner.nextDouble()
                val y = scanner.nextDouble()
                (path as ArrayList).add(GeographicPoint(x, y))
            }
        } catch (e: Exception) {
            System.err.println("Error reading correct answer!")
            e.printStackTrace()
        }
    }
}
