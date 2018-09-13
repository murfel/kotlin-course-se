package ru.hse.spb

/**
 * The solution object for the Codeforces problem C from the round #484 Div. 2
 * http://codeforces.com/contest/982/problem/C
 */
object Solution {
    /**
     * Reads one line of ints and return a list of them.
     */
    private fun readInts(separator: Char = ' ') = readLine()!!.split(separator).map(String::toInt)

    /**
     * Reads input specified as the number of vertices on the first line and n - 1 edges on the following n - 1 lines.
     * (Consult the problem statement for the precise specification.)
     */
    private fun readInput(): Array<Pair<Int, Int>> {
        val numVertices = readInts()[0]
        return Array(numVertices - 1) {
            val (firstVertex, secondVertex) = readInts()
            Pair(firstVertex, secondVertex)
        }
    }

    /**
     * Shifts the numeration of vertices by 1.
     *
     * This function is intended to be used to change the numeration of vertices from 1-notation to 0-notation.
     */
    fun decrementVertices(edgeList: Array<Pair<Int, Int>>): Array<Pair<Int, Int>> {
        return edgeList.map { Pair(it.first - 1, it.second - 1) }.toTypedArray()
    }

    /**
     * Converts an edge list of a graph to an adjacency list.
     */
    fun edgeListToAdjacencyList(numVertices: Int, edgeList: Array<Pair<Int, Int>>): Array<ArrayList<Int>> {
        val adjacencyList = Array(numVertices) { ArrayList<Int>() }
        for ((firstVertex, secondVertex) in edgeList) {
            adjacencyList[firstVertex].add(secondVertex)
            adjacencyList[secondVertex].add(firstVertex)
        }
        return adjacencyList
    }

    /**
     * Calculates subtree sizes of the tree rooted at the vertex [rootVertexNumber].
     *
     * A vertex is included in the calculation of its own subtree. Thus, the subtree size is at least 1 for every vertex.
     */
    fun calculateSubtreeSizes(adjacencyList: Array<ArrayList<Int>>, rootVertexNumber: Int): IntArray {
        val subtreeSizes = IntArray(adjacencyList.size) { -1 }
        fun dfs(vertexNumber: Int, parentVertexNumber: Int) {
            var subtreeSize = 1
            for (childVertexNumber in adjacencyList[vertexNumber]) {
                if (childVertexNumber == parentVertexNumber) {
                    continue
                }
                dfs(childVertexNumber, vertexNumber)
                subtreeSize += subtreeSizes[childVertexNumber]
            }
            subtreeSizes[vertexNumber] = subtreeSize
        }
        dfs(rootVertexNumber, -1)
        return subtreeSizes
    }

    /**
     * Calculates the maximum number of edges we can delete to keep each connected component with even number of
     * vertices.
     *
     * Uses the greedy solution based on the idea that if a vertex has an even subtree size, we can always delete the
     * edge to its parent.
     */
    fun calculateNumEdgesToDelete(subtreeSizes: IntArray): Int {
        var numEdges = 0
        if (subtreeSizes[0] % 2 == 1) {
            return -1
        }
        for (i in 1 until subtreeSizes.size) {
            if (subtreeSizes[i] % 2 == 0) {
                numEdges++
            }
        }
        return numEdges
    }

    /**
     * Returns the solution for the given input.
     */
    fun solve(edgeList: Array<Pair<Int, Int>>): Int {
        val decrementedEdgeList = decrementVertices(edgeList)
        val adjacencyList = edgeListToAdjacencyList(decrementedEdgeList.size + 1, decrementedEdgeList)
        return calculateNumEdgesToDelete(calculateSubtreeSizes(adjacencyList, 0))
    }

    /**
     * Reads input from the standard input, runs the solution, writes the answer to the standard output.
     */
    fun readSolveWrite() {
        print(solve(readInput()))
    }
}

fun main(args: Array<String>) {
    Solution.readSolveWrite()
}