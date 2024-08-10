package com.martishin.graphsonmaps.basicgraph

import com.martishin.graphsonmaps.util.GraphLoader
import java.io.File
import java.util.Scanner

class DegreeGrader {
    private var feedback: String = "" // Feedback from the grader
    private var correct: Int = 0 // Tests correctly passed

    companion object {
        private const val TESTS = 12 // Number of tests

        /**
         * Turn a list into a readable and printable string
         * @param lst  The list to process
         * @return The list items formatted as a printable string
         */
        fun printList(lst: List<Int>): String = lst.joinToString(" ")

        /**
         * Format readable feedback
         * @param score  The score received
         * @param feedback  The feedback message
         * @return A string where the feedback are score a formatted nicely
         */
        fun printOutput(
            score: Double,
            feedback: String,
        ): String = "Score: $score\nFeedback: $feedback"

        /**
         * Format test number and description
         * @param num  The test number
         * @param test The test description
         * @return A String with the test number and description neatly formatted.
         */
        fun appendFeedback(
            num: Int,
            test: String,
        ): String = "\n** Test #$num: $test..."

        /**
         * Run the grader
         * @param args Doesn't use command line parameters
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val grader = DegreeGrader()
            grader.run()
        }
    }

    /** Run a test case on an adjacency list and adjacency matrix.
     * @param i The graph number
     * @param desc A description of the graph
     */
    fun runTest(
        i: Int,
        desc: String,
    ) {
        val lst = GraphAdjList()
        val mat = GraphAdjMatrix()

        val file = "data/graders/mod1/graph$i.txt"
        val corr = readCorrect("$file.degrees")

        feedback += "\n\nGRAPH: $desc"
        feedback += appendFeedback(i * 2 - 1, "Testing adjacency list")

        // Load the graph, get the user's output, and compare with the right answer
        GraphLoader.loadGraph(file, lst)
        var result = lst.degreeSequence()
        judge(result, corr)

        feedback += appendFeedback(i * 2, "Testing adjacency matrix")
        GraphLoader.loadGraph(file, mat)
        result = mat.degreeSequence()
        judge(result, corr)
    }

    /** Run a road map/airplane route test case.
     * @param i The graph number
     * @param file The file to read the correct answer from
     * @param desc A description of the graph
     * @param type The type of graph to use
     */
    fun runSpecialTest(
        i: Int,
        file: String,
        desc: String,
        type: String,
    ) {
        val lst = GraphAdjList()
        val mat = GraphAdjMatrix()

        val filePath = "data/graders/mod1/$file"
        val corr = readCorrect("$filePath.degrees")

        feedback += "\n\n$desc"
        feedback += appendFeedback(i * 2 - 1, "Testing adjacency list")

        // Different method calls for different graph types
        when (type) {
            "road" -> {
                GraphLoader.loadRoadMap(filePath, lst)
                GraphLoader.loadRoadMap(filePath, mat)
            }
            "air" -> {
                GraphLoader.loadRoutes(filePath, lst)
                GraphLoader.loadRoutes(filePath, mat)
            }
        }

        var result = lst.degreeSequence()
        judge(result, corr)

        feedback += appendFeedback(i * 2, "Testing adjacency matrix")
        result = mat.degreeSequence()
        judge(result, corr)
    }

    /** Compare the user's result with the right answer.
     * @param result The list with the user's result
     * @param corr The list with the correct answer
     */
    fun judge(
        result: List<Int>?,
        corr: List<Int>,
    ) {
        if (result == null) {
            feedback += "FAILED. Result is NULL"
        } else if (printList(result) != printList(corr)) {
            feedback += "FAILED. Expected ${printList(corr)}, got ${printList(result)}. "
        } else {
            feedback += "PASSED."
            correct++
        }
    }

    /** Read a correct answer from a file.
     * @param file The file to read from
     * @return A list containing the correct answer
     */
    fun readCorrect(file: String): List<Int> {
        val ret = mutableListOf<Int>()
        try {
            Scanner(File(file)).use { scanner ->
                while (scanner.hasNextInt()) {
                    ret.add(scanner.nextInt())
                }
            }
        } catch (e: Exception) {
            feedback += "\nCould not open answer file! Please submit a bug report."
        }
        return ret
    }

    /** Run the grader. */
    fun run() {
        feedback = ""
        correct = 0

        try {
            runTest(1, "Straight line (0->1->2->3->...)")
            runTest(2, "Undirected straight line (0<->1<->2<->3<->...)")
            runTest(3, "Star graph - 0 is connected in both directions to all nodes except itself (starting at 0)")
            runTest(4, "Star graph - Each 'arm' consists of two undirected edges leading away from 0 (starting at 0)")
            runSpecialTest(5, "ucsd.map", "UCSD MAP: Intersections around UCSD", "road")
            runSpecialTest(6, "routesUA.dat", "AIRLINE MAP: Routes of airplanes around the world", "air")

            feedback =
                if (correct == TESTS) {
                    "All tests passed. Great job!$feedback"
                } else {
                    "Some tests failed. Check your code for errors, then try again:$feedback"
                }
        } catch (e: Exception) {
            feedback += "\nError during runtime: $e"
            e.printStackTrace()
        }

        println(printOutput(correct.toDouble() / TESTS, feedback))
    }
}
