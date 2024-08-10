package com.martishin.graphsonmaps.roadgraph

import com.martishin.graphsonmaps.geography.GeographicPoint
import com.martishin.graphsonmaps.util.GraphLoader

class AStarGrader : Runnable {
    var feedback: String = ""
    var correct: Int = 0

    companion object {
        private const val TESTS = 4

        /** Format readable feedback */
        fun printOutput(
            score: Double,
            feedback: String,
        ): String = "Score: $score\nFeedback: $feedback"

        /** Format test number and description */
        fun appendFeedback(
            num: Int,
            test: String,
        ): String = "\n** Test #$num: $test..."

        @JvmStatic
        fun main(args: Array<String>) {
            val grader = AStarGrader()

            // Infinite loop detection
            val thread = Thread(grader)
            thread.start()
            val endTime = System.currentTimeMillis() + 10000
            var infinite = false
            while (thread.isAlive) {
                // Stop after 10 seconds
                if (System.currentTimeMillis() > endTime) {
                    thread.stop()
                    infinite = true
                    break
                }
            }
            if (infinite) {
                println(printOutput(grader.correct.toDouble() / TESTS, grader.feedback + "\nYour program entered an infinite loop."))
            }
        }
    }

    /** Run a test case on an adjacency list and adjacency matrix. */
    fun runTest(
        i: Int,
        file: String,
        desc: String,
        start: GeographicPoint,
        end: GeographicPoint,
    ) {
        val graph = MapGraph()

        feedback += "\n\n$desc"

        GraphLoader.loadRoadMap("data/graders/mod3/$file", graph)
        val corr = CorrectAnswer("data/graders/mod3/$file.answer", false)

        judge(i, graph, corr, start, end)
    }

    /** Compare the user's result with the right answer. */
    fun judge(
        i: Int,
        result: MapGraph,
        corr: CorrectAnswer,
        start: GeographicPoint,
        end: GeographicPoint,
    ) {
        // Correct if paths are same length and have the same elements
        feedback += appendFeedback(i, "Running A* from (${start.getX()}, ${start.getY()}) to (${end.getX()}, ${end.getY()})")
        val path = result.aStarSearch(start, end)

        if (path == null) {
            if (corr.path == null) {
                feedback += "PASSED."
                correct++
            } else {
                feedback += "FAILED. Your implementation returned null; expected \n${printPath(corr.path!!)}."
            }
        } else {
            val correctPath = corr.path
            if (correctPath == null) {
                feedback += "FAILED. Your implementation returned \n${printPath(path)}; expected null."
            } else if (path.size != correctPath.size || !correctPath.containsAll(path)) {
                feedback += "FAILED. Expected: \n${printPath(correctPath)} Got: \n${printPath(path)}"
                if (path.size != correctPath.size) {
                    feedback += "Your result has size ${path.size}; expected ${correctPath.size}."
                } else {
                    feedback += "Correct size, but incorrect path."
                }
            } else {
                feedback += "PASSED."
                correct++
            }
        }
    }

    /** Print a search path in readable form */
    fun printPath(path: List<GeographicPoint>): String = path.joinToString("\n")

    /** Run the grader */
    override fun run() {
        feedback = ""
        correct = 0

        try {
            runTest(
                1,
                "map1.txt",
                "MAP: Straight line (-3 <- -2 <- -1 <- 0 -> 1 -> 2-> 3 ->...)",
                GeographicPoint(0.0, 0.0),
                GeographicPoint(6.0, 6.0),
            )

            runTest(2, "map2.txt", "MAP: Example map from the writeup", GeographicPoint(7.0, 3.0), GeographicPoint(4.0, -1.0))

            runTest(3, "map3.txt", "MAP: Right triangle (with a little detour)", GeographicPoint(0.0, 0.0), GeographicPoint(0.0, 4.0))

            runTest(
                4,
                "ucsd.map",
                "UCSD MAP: Intersections around UCSD",
                GeographicPoint(32.8709815, -117.2434254),
                GeographicPoint(32.8742087, -117.2381344),
            )

            if (correct == TESTS) {
                feedback = "All tests passed. Great job!" + feedback
            } else {
                feedback = "Some tests failed. Check your code for errors, then try again:" + feedback
            }
        } catch (e: Exception) {
            feedback += "\nError during runtime: $e"
            e.printStackTrace()
        }

        println(printOutput(correct.toDouble() / TESTS, feedback))
    }
}
