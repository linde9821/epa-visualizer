package moritz.lindner.masterarbeit.ui.common

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object BinCalculator {

    /**
     * Sturges' Rule: Good for normal distributions
     * bins = ceil(log2(n) + 1)
     */
    fun sturgesRule(n: Int): Int {
        return ceil(log2(n.toDouble()) + 1).toInt()
    }

    /**
     * Scott's Rule: Optimal for normally distributed data
     * binWidth = 3.5 * σ / n^(1/3)
     * bins = (max - min) / binWidth
     */
    fun scottsRule(data: List<Long>): Int {
        if (data.isEmpty()) return 1

        val n = data.size
        val stdDev = standardDeviation(data)
        val range = data.maxOrNull()!! - data.minOrNull()!!

        if (range == 0L || stdDev == 0.0) return 1

        val binWidth = 3.5 * stdDev / n.toDouble().pow(1.0/3.0)
        return max(1, ceil(range / binWidth).toInt())
    }

    /**
     * Freedman-Diaconis Rule: More robust to outliers
     * binWidth = 2 * IQR / n^(1/3)
     * bins = (max - min) / binWidth
     */
    fun freedmanDiaconisRule(data: List<Long>): Int {
        if (data.isEmpty()) return 1

        val n = data.size
        val sorted = data.sorted()
        val q1 = percentile(sorted, 25.0)
        val q3 = percentile(sorted, 75.0)
        val iqr = q3 - q1
        val range = sorted.last() - sorted.first()

        if (range == 0L || iqr == 0.0) return 1

        val binWidth = 2.0 * iqr / n.toDouble().pow(1.0/3.0)
        return max(1, ceil(range / binWidth).toInt())
    }

    /**
     * Square Root Rule: Simple and widely used
     * bins = ceil(sqrt(n))
     */
    fun squareRootRule(n: Int): Int {
        return ceil(sqrt(n.toDouble())).toInt()
    }

    /**
     * Doane's Rule: Extension of Sturges' for skewed data
     * bins = 1 + log2(n) + log2(1 + |g1| / σ_g1)
     * where g1 is skewness
     */
    fun doanesRule(data: List<Long>): Int {
        if (data.isEmpty()) return 1

        val n = data.size.toDouble()
        val skewness = abs(calculateSkewness(data))
        val sigmaG1 = sqrt(6.0 * (n - 2.0) / ((n + 1.0) * (n + 3.0)))

        return ceil(1 + log2(n) + log2(1 + skewness / sigmaG1)).toInt()
    }

    /**
     * Auto-select best rule based on data characteristics
     */
    fun autoBins(data: List<Long>): Int {
        if (data.size < 10) return min(data.size, 5)

        val skewness = abs(calculateSkewness(data))
        val hasOutliers = detectOutliers(data)

        return when {
            hasOutliers -> freedmanDiaconisRule(data) // Robust to outliers
            skewness > 1.0 -> doanesRule(data) // Handle skewed data
            data.size < 30 -> sturgesRule(data.size) // Small sample
            else -> scottsRule(data) // Default for normal-ish data
        }.coerceIn(10, 100) // Reasonable bounds
    }

    // Helper functions

    private fun standardDeviation(data: List<Long>): Double {
        val mean = data.average()
        val variance = data.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }

    private fun percentile(sortedData: List<Long>, p: Double): Double {
        val index = (p / 100.0) * (sortedData.size - 1)
        val lower = floor(index).toInt()
        val upper = ceil(index).toInt()
        val weight = index - lower

        return sortedData[lower] * (1 - weight) + sortedData[upper] * weight
    }

    private fun calculateSkewness(data: List<Long>): Double {
        val n = data.size
        val mean = data.average()
        val stdDev = standardDeviation(data)

        if (stdDev == 0.0) return 0.0

        val m3 = data.map { ((it - mean) / stdDev).pow(3) }.average()
        return m3 * n / ((n - 1) * (n - 2))
    }

    private fun detectOutliers(data: List<Long>): Boolean {
        val sorted = data.sorted()
        val q1 = percentile(sorted, 25.0)
        val q3 = percentile(sorted, 75.0)
        val iqr = q3 - q1

        val lowerBound = q1 - 1.5 * iqr
        val upperBound = q3 + 1.5 * iqr

        return data.any { it < lowerBound || it > upperBound }
    }
}