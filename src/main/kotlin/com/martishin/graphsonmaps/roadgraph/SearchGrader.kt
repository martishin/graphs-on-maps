package com.martishin.graphsonmaps.roadgraph

import com.martishin.graphsonmaps.geography.GeographicPoint
import com.martishin.graphsonmaps.util.GraphLoader

class SearchGrader : Runnable {
    var feedback: String = ""
    var correct: Int = 0

    companion object {
        private const val TESTS = 12

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
            val grader = SearchGrader()

            // Infinite loop detection
            val thread = Thread(grader)
            thread.start()

            // Allow it to run for 10 seconds
            val endTime = System.currentTimeMillis() + 10000
            var infinite = false
            while (thread.isAlive) {
                if (System.currentTimeMillis() > endTime) {
                    // Stop the thread if it takes too long
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

        GraphLoader.loadRoadMap("data/graders/mod2/$file", graph)
        val corr = CorrectAnswer("data/graders/mod2/$file.answer", true)

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
        // Create a local immutable copy of corr.path
        val expectedPath = corr.path

        // Correct if same number of vertices
        feedback += appendFeedback(i * 3 - 2, "Testing vertex count")
        if (result.getNumVertices() != corr.vertices) {
            feedback += "FAILED. Expected ${corr.vertices}; got ${result.getNumVertices()}."
        } else {
            feedback += "PASSED."
            correct++
        }

        // Correct if same number of edges
        feedback += appendFeedback(i * 3 - 1, "Testing edge count")
        if (result.getNumEdges() != corr.edges) {
            feedback += "FAILED. Expected ${corr.edges}; got ${result.getNumEdges()}."
        } else {
            feedback += "PASSED."
            correct++
        }

        // Correct if paths are same size and have same elements
        feedback += appendFeedback(i * 3, "Testing BFS")
        val bfs = result.bfs(start, end)
        if (bfs == null) {
            if (expectedPath == null) {
                feedback += "PASSED."
                correct++
            } else {
                feedback += "FAILED. Your implementation returned null; expected \n${printBFSList(expectedPath)}."
            }
        } else if (expectedPath == null) {
            feedback += "FAILED. Your implementation returned \n${printBFSList(bfs)}; expected null."
        } else if (printBFSList(expectedPath) != printBFSList(bfs)) {
            feedback += "FAILED. Expected: \n${printBFSList(expectedPath)} Got: \n${printBFSList(bfs)}"
            if (bfs.size != expectedPath.size) {
                feedback += "Your result has size ${bfs.size}; expected ${expectedPath.size}."
            } else {
                feedback += "Correct size, but incorrect path."
            }
        } else {
            feedback += "PASSED."
            correct++
        }
    }

    /** Print a BFS path in readable form */
    fun printBFSList(bfs: List<GeographicPoint>): String = bfs.joinToString("\n")

    /** Run the grader */
    override fun run() {
        feedback = ""
        correct = 0

        try {
            runTest(1, "map1.txt", "Straight line (0->1->2->3->...)", GeographicPoint(0.0, 0.0), GeographicPoint(6.0, 6.0))

            runTest(2, "map2.txt", "Same as above (searching from 6 to 0)", GeographicPoint(6.0, 6.0), GeographicPoint(0.0, 0.0))

            runTest(3, "map3.txt", "Square graph - Each edge has 2 nodes", GeographicPoint(0.0, 0.0), GeographicPoint(1.0, 2.0))

            runTest(
                4,
                "ucsd.map",
                "UCSD MAP: Intersections around UCSD",
                GeographicPoint(32.8756538, -117.2435715),
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
