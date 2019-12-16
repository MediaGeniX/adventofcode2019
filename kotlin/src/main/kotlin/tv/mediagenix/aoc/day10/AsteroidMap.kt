package tv.mediagenix.aoc.day10

import java.lang.StrictMath.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class AsteroidMap(private val asteroids: Array<BooleanArray>) {
    operator fun get(x: Int, y: Int) = asteroids[y][x]
    val width = asteroids.getOrNull(0)?.size ?: 0
    val height = asteroids.size

    fun visibleAsteroids(x: Int, y: Int): Int {
        if (!this[x, y]) throw IllegalArgumentException("Not an asteroid: [$x, $y]")
        val vis = VisibilityMap.compute(this, x, y)
        return vis()
    }

    fun findBestSpot(): Spot = (0 until width * height).asSequence().map {
        val x = it / height
        val y = it % height
        Spot(x, y, if (this[x, y]) visibleAsteroids(x, y) else -1)
    }.max()!!

    fun find200th(fx: Int, fy: Int): Pair<Int, Int> {
        val visibilityMap = VisibilityMap.compute(this, fx, fy)
        if (visibilityMap() < 200) TODO("Not required for the challenge")
        return (0 until width * height).asSequence().map {
            (it / height) to (it % height)
        }.filter { (x, y) ->
            visibilityMap[x, y]
        }.sortedBy { (x, y) ->
            -atan2(y - fy.toDouble(), x - fx.toDouble())
        }.elementAt(199)
    }

    companion object {
        fun parse(input: String) = AsteroidMap(input.split('\n').map { row -> row.map { it == '#' }.toBooleanArray() }.toTypedArray())
    }
}

class VisibilityMap(private val visibleCells: Array<BooleanArray>) {
    operator fun get(x: Int, y: Int) = visibleCells[y][x]
    operator fun invoke() = visibleCells.sumBy { row -> row.count { it } }

    companion object { // TODO: optimize !
        fun compute(asteroids: AsteroidMap, x: Int, y: Int) = VisibilityMap(
                Array(asteroids.height) { iy ->
                    BooleanArray(asteroids.width) { ix ->
                        if (ix == x && iy == y) false
                        else {
                            if (asteroids[ix, iy]) {
                                val dx = ix - x
                                val dy = iy - y
                                if (abs(dx) in 0..1 && abs(dy) in 0..1) {
                                    return@BooleanArray true
                                }
                                val gcd = gcd(abs(dx), abs(dy))
                                val vx = dx / gcd
                                val vy = dy / gcd

                                var cx = x + vx
                                var cy = y + vy

                                val lowerX = min(x, ix)
                                val lowerY = min(y, iy)
                                val upperX = max(x, ix)
                                val upperY = max(y, iy)

                                while (cx in lowerX..upperX && cy in lowerY..upperY) {
                                    if ((cx != ix || cy != iy) && asteroids[cx, cy]) return@BooleanArray false
                                    cy += vy
                                    cx += vx
                                }

                                true
                            } else false
                        }
                    }
                })

    }
}

private tailrec fun gcd(n1: Int, n2: Int): Int = if (n2 == 0) n1 else gcd(n2, n1 % n2)

data class Spot(val x: Int, val y: Int, val visibleCount: Int) : Comparable<Spot> {
    override operator fun compareTo(other: Spot): Int = visibleCount.compareTo(other.visibleCount)
}