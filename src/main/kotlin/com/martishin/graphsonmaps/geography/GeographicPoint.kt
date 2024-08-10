package com.martishin.graphsonmaps.geography

import kotlin.math.*

data class GeographicPoint(
    val latitude: Double,
    val longitude: Double,
) {
    /**
     * Calculates the geographic distance in km between this point and
     * the other point.
     * @param other
     * @return The distance between this lat, lon point and the other point
     */
    fun distance(other: GeographicPoint): Double = getDist(latitude, longitude, other.latitude, other.longitude)

    fun getX() = latitude

    fun getY() = longitude

    private fun getDist(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val R = 6373.0 // radius of the earth in kilometres
        val lat1rad = Math.toRadians(lat1)
        val lat2rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a =
            sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1rad) * cos(lat2rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    override fun toString(): String = "Lat: $latitude, Lon: $longitude"
}
