package ru.hse.spb

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SolutionTest {
    @Test
    fun decrementVertices() {
        assertArrayEquals(
                arrayOf(
                        Pair(1, 3),
                        Pair(3, 0),
                        Pair(2, 0)
                ),
                Solution.decrementVertices(arrayOf(
                        Pair(2, 4),
                        Pair(4, 1),
                        Pair(3, 1)
                ))
        )
    }

    @Test
    fun edgeListToAdjacencyList() {
        val expected = arrayOf(
                arrayListOf(2, 3),
                arrayListOf(3),
                arrayListOf(0),
                arrayListOf(0, 1)
        )
        val actual = Solution.edgeListToAdjacencyList(4, arrayOf(
                Pair(1, 3),
                Pair(3, 0),
                Pair(2, 0)
        )).map { it.sorted() }.toTypedArray()
        assertArrayEquals(expected, actual)
    }

    @Test
    fun calculateSubtreeSizes() {
        assertArrayEquals(intArrayOf(4, 1, 1, 2), Solution.calculateSubtreeSizes(arrayOf(
                arrayListOf(2, 3),
                arrayListOf(3),
                arrayListOf(0),
                arrayListOf(0, 1)
        ), 0))
    }

    @Test
    fun calculateNumEdgesToDelete() {
        assertEquals(1, Solution.calculateNumEdgesToDelete(intArrayOf(4, 1, 1, 2)))
    }

    @Test
    fun solveSample1() {
        assertEquals(1, Solution.solve(arrayOf(
                Pair(2, 4),
                Pair(4, 1),
                Pair(3, 1)
        )))
    }

    @Test
    fun solveSample2() {
        assertEquals(-1, Solution.solve(arrayOf(
                Pair(1, 2),
                Pair(1, 3)
        )))
    }

    @Test
    fun solveSample3() {
        assertEquals(4, Solution.solve(arrayOf(
                Pair(7, 1),
                Pair(8, 4),
                Pair(8, 10),
                Pair(4, 7),
                Pair(6, 5),
                Pair(9, 3),
                Pair(3, 5),
                Pair(2, 10),
                Pair(2, 5)
        )))
    }

    @Test
    fun solveSample4() {
        assertEquals(0, Solution.solve(arrayOf(Pair(1, 2))))
    }


}