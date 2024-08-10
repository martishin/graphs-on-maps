package com.martishin.graphsonmaps.mapmaker

import jakarta.json.JsonArray
import jakarta.json.JsonObject
import java.io.PrintWriter

class MapMaker(
    private val bounds: FloatArray,
) {
    private val nodes = HashMap<Int, Location>()

    fun parseData(filename: String): Boolean {
        val fetcher = DataFetcher(bounds)
        val data: JsonObject = fetcher.getData() ?: return false

        val elements: JsonArray = data.getJsonArray("elements")

        for (elem in elements.getValuesAs(JsonObject::class.java)) {
            if (elem.getString("type") == "node") {
                nodes[elem.getInt("id")] =
                    Location(
                        elem.getJsonNumber("lat").doubleValue(),
                        elem.getJsonNumber("lon").doubleValue(),
                    )
            }
        }

        val outfile: PrintWriter =
            try {
                PrintWriter(filename)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        for (elem in elements.getValuesAs(JsonObject::class.java)) {
            if (elem.getString("type") == "way") {
                val street = elem.getJsonObject("tags").getString("name", "")
                val type = elem.getJsonObject("tags").getString("highway", "")
                val oneway = elem.getJsonObject("tags").getString("oneway", "no")
                val nodelist = elem.getJsonArray("nodes").getValuesAs(JsonObject::class.java)

                for (i in 0 until nodelist.size - 1) {
                    val start = nodes[nodelist[i].getInt("id")] ?: continue
                    val end = nodes[nodelist[i + 1].getInt("id")] ?: continue
                    if (start.outsideBounds(bounds) || end.outsideBounds(bounds)) {
                        continue
                    }

                    outfile.println("$start$end\"$street\" $type")
                    if (oneway == "no") {
                        outfile.println("$end$start\"$street\" $type")
                    }
                }
            }
        }

        outfile.close()
        return true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 4) {
                println("Incorrect number of arguments.")
                println(args.size)
                return
            }

            val boundArr = FloatArray(4)
            try {
                for (i in args.indices) {
                    boundArr[i] = args[i].toFloat()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }

            val map = MapMaker(boundArr)
            map.parseData("ucsd.map")
        }
    }
}

class Location(
    private val lat: Double,
    private val lon: Double,
) {
    override fun toString(): String = "$lat $lon "

    /**
     * @param bounds [south, west, north, east]
     */
    fun outsideBounds(bounds: FloatArray): Boolean = lat < bounds[0] || lat > bounds[2] || lon < bounds[1] || lon > bounds[3]
}
