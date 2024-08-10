package com.martishin.graphsonmaps.mapmaker

import jakarta.json.Json
import jakarta.json.JsonObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DataFetcher(
    bounds: FloatArray,
) {
    private val HIGHWAYS =
        arrayOf(
            "motorway",
            "trunk",
            "primary",
            "secondary",
            "tertiary",
            "unclassified",
            "residential",
            "motorway_link",
            "trunk_link",
            "primary_link",
            "secondary_link",
            "tertiary_link",
            "living_street",
        )

    private val query: String = constructQuery(bounds)

    fun getData(): JsonObject? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL("http://overpass-api.de/api/interpreter")
            conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.requestMethod = "POST"
            conn.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.7")

            DataOutputStream(conn.outputStream).use { wr ->
                wr.writeBytes(query)
            }

            conn.inputStream.use { it ->
                Json.createReader(it).readObject()
            }
        } catch (e: Exception) {
            println(e)
            null
        }
    }

    private fun constructQuery(boundsArray: FloatArray): String {
        val bounds =
            buildString {
                append("(")
                for (i in boundsArray.indices) {
                    append(boundsArray[i])
                    if (i < boundsArray.size - 1) {
                        append(",")
                    } else {
                        append(")")
                    }
                }
            }

        return buildString {
            append("[out:json];(")
            for (s in HIGHWAYS) {
                append("way[\"highway\"=\"$s\"]$bounds;")
            }
            append("); (._;>;); out;")
        }
    }
}
