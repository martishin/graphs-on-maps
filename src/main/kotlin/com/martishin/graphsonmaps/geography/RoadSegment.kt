package com.martishin.graphsonmaps.geography

data class RoadSegment(
    val point1: GeographicPoint,
    val point2: GeographicPoint,
    val geometryPoints: List<GeographicPoint>,
    val roadName: String,
    val roadType: String,
    val length: Double,
) {
    /** Return all of the points from start to end in that order
     * on this segment.
     * @param start
     * @param end
     * @return
     */
    fun getPoints(
        start: GeographicPoint,
        end: GeographicPoint,
    ): List<GeographicPoint> {
        val allPoints = mutableListOf<GeographicPoint>()
        when {
            point1 == start && point2 == end -> {
                allPoints.add(start)
                allPoints.addAll(geometryPoints)
                allPoints.add(end)
            }
            point2 == start && point1 == end -> {
                allPoints.add(end)
                allPoints.addAll(geometryPoints)
                allPoints.add(start)
                allPoints.reverse()
            }
            else -> {
                throw IllegalArgumentException("Start and end points do not match end points of segment")
            }
        }
        return allPoints
    }

    /** Two road segments are equal if they have the same start and end points
     *  and they have the same road name.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RoadSegment) return false

        val ptsEqual =
            (point1 == other.point1 && point2 == other.point2) ||
                (point1 == other.point2 && point2 == other.point1)

        return roadName == other.roadName && ptsEqual && length == other.length
    }

    override fun hashCode(): Int = point1.hashCode() + point2.hashCode()

    override fun toString(): String =
        buildString {
            append("$roadName, $roadType [")
            append(point1)
            for (p in geometryPoints) {
                append("; $p")
            }
            append("; $point2]")
        }

    // given one end, return the other.
    fun getOtherPoint(point: GeographicPoint): GeographicPoint? =
        when (point) {
            point1 -> point2
            point2 -> point1
            else -> {
                println("ERROR!! : in RoadSegment::getOtherPoint Neither point matched")
                null
            }
        }
}
