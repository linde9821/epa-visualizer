package moritz.lindner.masterarbeit.ui.components.epaview.components.tree.drawing.atlas

import moritz.lindner.masterarbeit.epa.ExtendedPrefixAutomaton
import moritz.lindner.masterarbeit.epa.api.EpaService
import moritz.lindner.masterarbeit.epa.construction.builder.EpaProgressCallback
import moritz.lindner.masterarbeit.epa.domain.State
import moritz.lindner.masterarbeit.epa.domain.Transition
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode

class DefaultConfig(
    extendedPrefixAutomaton: ExtendedPrefixAutomaton<Long>,
    private val stateSize: Float,
    private val minTransitionSize: Float,
    private val maxTransitionSize: Float,
    private val progressCallback: EpaProgressCallback? = null
) : AtlasConfig {

    // inspired seaborn https://seaborn.pydata.org/tutorial/color_palettes.html
    // and taken from seaborn by using seaborn_color_extractor.py to generate 64-samples of each color palette
    val heatmapByName = buildMap {

        put("crest", intArrayOf(
            Color.makeRGB(164, 204, 144),  // Position 0.00
            Color.makeRGB(160, 202, 144),  // Position 0.02
            Color.makeRGB(155, 200, 144),  // Position 0.03
            Color.makeRGB(151, 198, 145),  // Position 0.05
            Color.makeRGB(147, 196, 145),  // Position 0.06
            Color.makeRGB(142, 194, 145),  // Position 0.08
            Color.makeRGB(138, 192, 145),  // Position 0.10
            Color.makeRGB(133, 189, 145),  // Position 0.11
            Color.makeRGB(129, 187, 144),  // Position 0.13
            Color.makeRGB(124, 185, 144),  // Position 0.14
            Color.makeRGB(120, 183, 144),  // Position 0.16
            Color.makeRGB(116, 181, 144),  // Position 0.17
            Color.makeRGB(111, 179, 144),  // Position 0.19
            Color.makeRGB(107, 177, 144),  // Position 0.21
            Color.makeRGB(103, 175, 144),  // Position 0.22
            Color.makeRGB(100, 172, 144),  // Position 0.24
            Color.makeRGB(95, 169, 144),  // Position 0.25
            Color.makeRGB(92, 167, 144),  // Position 0.27
            Color.makeRGB(88, 165, 144),  // Position 0.29
            Color.makeRGB(85, 162, 144),  // Position 0.30
            Color.makeRGB(82, 160, 143),  // Position 0.32
            Color.makeRGB(79, 158, 143),  // Position 0.33
            Color.makeRGB(76, 155, 143),  // Position 0.35
            Color.makeRGB(74, 153, 143),  // Position 0.37
            Color.makeRGB(71, 151, 143),  // Position 0.38
            Color.makeRGB(68, 148, 142),  // Position 0.40
            Color.makeRGB(66, 146, 142),  // Position 0.41
            Color.makeRGB(63, 144, 142),  // Position 0.43
            Color.makeRGB(60, 141, 142),  // Position 0.44
            Color.makeRGB(58, 139, 141),  // Position 0.46
            Color.makeRGB(55, 136, 141),  // Position 0.48
            Color.makeRGB(53, 134, 141),  // Position 0.49
            Color.makeRGB(49, 131, 140),  // Position 0.51
            Color.makeRGB(47, 129, 140),  // Position 0.52
            Color.makeRGB(44, 126, 140),  // Position 0.54
            Color.makeRGB(42, 124, 140),  // Position 0.56
            Color.makeRGB(39, 122, 139),  // Position 0.57
            Color.makeRGB(37, 119, 139),  // Position 0.59
            Color.makeRGB(35, 117, 139),  // Position 0.60
            Color.makeRGB(33, 114, 138),  // Position 0.62
            Color.makeRGB(31, 112, 138),  // Position 0.63
            Color.makeRGB(30, 110, 138),  // Position 0.65
            Color.makeRGB(29, 107, 137),  // Position 0.67
            Color.makeRGB(28, 105, 137),  // Position 0.68
            Color.makeRGB(28, 102, 136),  // Position 0.70
            Color.makeRGB(28, 99, 136),  // Position 0.71
            Color.makeRGB(28, 97, 135),  // Position 0.73
            Color.makeRGB(29, 94, 134),  // Position 0.75
            Color.makeRGB(30, 91, 133),  // Position 0.76
            Color.makeRGB(31, 88, 132),  // Position 0.78
            Color.makeRGB(32, 85, 131),  // Position 0.79
            Color.makeRGB(33, 83, 130),  // Position 0.81
            Color.makeRGB(34, 80, 129),  // Position 0.83
            Color.makeRGB(35, 77, 128),  // Position 0.84
            Color.makeRGB(37, 74, 127),  // Position 0.86
            Color.makeRGB(38, 72, 125),  // Position 0.87
            Color.makeRGB(39, 69, 124),  // Position 0.89
            Color.makeRGB(40, 66, 122),  // Position 0.90
            Color.makeRGB(41, 63, 120),  // Position 0.92
            Color.makeRGB(42, 60, 119),  // Position 0.94
            Color.makeRGB(42, 57, 117),  // Position 0.95
            Color.makeRGB(43, 54, 116),  // Position 0.97
            Color.makeRGB(43, 51, 114),  // Position 0.98
            Color.makeRGB(44, 48, 113),  // Position 1.00
        ))

        put(
            "rocket_r", intArrayOf(
                Color.makeRGB(250, 234, 220),  // Position 0.00
                Color.makeRGB(249, 228, 211),  // Position 0.02
                Color.makeRGB(248, 222, 203),  // Position 0.03
                Color.makeRGB(248, 216, 194),  // Position 0.05
                Color.makeRGB(247, 210, 185),  // Position 0.06
                Color.makeRGB(247, 205, 176),  // Position 0.08
                Color.makeRGB(246, 199, 168),  // Position 0.10
                Color.makeRGB(246, 192, 159),  // Position 0.11
                Color.makeRGB(246, 186, 151),  // Position 0.13
                Color.makeRGB(246, 180, 142),  // Position 0.14
                Color.makeRGB(245, 174, 135),  // Position 0.16
                Color.makeRGB(245, 167, 127),  // Position 0.17
                Color.makeRGB(245, 161, 120),  // Position 0.19
                Color.makeRGB(245, 154, 113),  // Position 0.21
                Color.makeRGB(245, 147, 106),  // Position 0.22
                Color.makeRGB(244, 141, 100),  // Position 0.24
                Color.makeRGB(244, 132, 92),  // Position 0.25
                Color.makeRGB(243, 125, 86),  // Position 0.27
                Color.makeRGB(243, 118, 81),  // Position 0.29
                Color.makeRGB(242, 110, 75),  // Position 0.30
                Color.makeRGB(241, 103, 71),  // Position 0.32
                Color.makeRGB(240, 95, 67),  // Position 0.33
                Color.makeRGB(239, 87, 64),  // Position 0.35
                Color.makeRGB(237, 80, 62),  // Position 0.37
                Color.makeRGB(235, 72, 61),  // Position 0.38
                Color.makeRGB(232, 64, 62),  // Position 0.40
                Color.makeRGB(229, 57, 64),  // Position 0.41
                Color.makeRGB(225, 50, 66),  // Position 0.43
                Color.makeRGB(220, 44, 69),  // Position 0.44
                Color.makeRGB(216, 38, 71),  // Position 0.46
                Color.makeRGB(211, 33, 74),  // Position 0.48
                Color.makeRGB(205, 28, 77),  // Position 0.49
                Color.makeRGB(198, 24, 81),  // Position 0.51
                Color.makeRGB(192, 22, 83),  // Position 0.52
                Color.makeRGB(186, 22, 86),  // Position 0.54
                Color.makeRGB(179, 22, 87),  // Position 0.56
                Color.makeRGB(172, 23, 89),  // Position 0.57
                Color.makeRGB(166, 24, 90),  // Position 0.59
                Color.makeRGB(159, 26, 91),  // Position 0.60
                Color.makeRGB(152, 27, 91),  // Position 0.62
                Color.makeRGB(145, 28, 91),  // Position 0.63
                Color.makeRGB(138, 29, 91),  // Position 0.65
                Color.makeRGB(131, 30, 90),  // Position 0.67
                Color.makeRGB(125, 30, 89),  // Position 0.68
                Color.makeRGB(118, 30, 88),  // Position 0.70
                Color.makeRGB(111, 31, 87),  // Position 0.71
                Color.makeRGB(105, 31, 85),  // Position 0.73
                Color.makeRGB(98, 30, 83),  // Position 0.75
                Color.makeRGB(90, 30, 80),  // Position 0.76
                Color.makeRGB(84, 29, 78),  // Position 0.78
                Color.makeRGB(78, 29, 75),  // Position 0.79
                Color.makeRGB(71, 28, 72),  // Position 0.81
                Color.makeRGB(65, 27, 69),  // Position 0.83
                Color.makeRGB(59, 26, 65),  // Position 0.84
                Color.makeRGB(53, 24, 61),  // Position 0.86
                Color.makeRGB(47, 23, 57),  // Position 0.87
                Color.makeRGB(41, 21, 53),  // Position 0.89
                Color.makeRGB(35, 19, 49),  // Position 0.90
                Color.makeRGB(30, 17, 45),  // Position 0.92
                Color.makeRGB(24, 15, 41),  // Position 0.94
                Color.makeRGB(18, 12, 37),  // Position 0.95
                Color.makeRGB(12, 10, 33),  // Position 0.97
                Color.makeRGB(7, 7, 29),  // Position 0.98
                Color.makeRGB(2, 4, 25),  // Position 1.00)
            )
        )

        put(
            "mako_r", intArrayOf(
                Color.makeRGB(222, 244, 228),  // Position 0.00
                Color.makeRGB(214, 241, 222),  // Position 0.02
                Color.makeRGB(206, 238, 215),  // Position 0.03
                Color.makeRGB(197, 235, 208),  // Position 0.05
                Color.makeRGB(189, 231, 202),  // Position 0.06
                Color.makeRGB(180, 228, 195),  // Position 0.08
                Color.makeRGB(171, 226, 190),  // Position 0.10
                Color.makeRGB(161, 223, 185),  // Position 0.11
                Color.makeRGB(150, 220, 181),  // Position 0.13
                Color.makeRGB(139, 217, 178),  // Position 0.14
                Color.makeRGB(127, 215, 175),  // Position 0.16
                Color.makeRGB(115, 212, 173),  // Position 0.17
                Color.makeRGB(103, 209, 172),  // Position 0.19
                Color.makeRGB(93, 205, 172),  // Position 0.21
                Color.makeRGB(85, 201, 172),  // Position 0.22
                Color.makeRGB(78, 197, 172),  // Position 0.24
                Color.makeRGB(72, 191, 173),  // Position 0.25
                Color.makeRGB(67, 187, 173),  // Position 0.27
                Color.makeRGB(64, 183, 173),  // Position 0.29
                Color.makeRGB(61, 178, 172),  // Position 0.30
                Color.makeRGB(58, 174, 172),  // Position 0.32
                Color.makeRGB(56, 169, 172),  // Position 0.33
                Color.makeRGB(54, 165, 171),  // Position 0.35
                Color.makeRGB(53, 160, 170),  // Position 0.37
                Color.makeRGB(52, 156, 170),  // Position 0.38
                Color.makeRGB(52, 151, 169),  // Position 0.40
                Color.makeRGB(51, 147, 168),  // Position 0.41
                Color.makeRGB(51, 142, 167),  // Position 0.43
                Color.makeRGB(51, 138, 166),  // Position 0.44
                Color.makeRGB(52, 134, 165),  // Position 0.46
                Color.makeRGB(52, 129, 164),  // Position 0.48
                Color.makeRGB(52, 125, 163),  // Position 0.49
                Color.makeRGB(53, 119, 161),  // Position 0.51
                Color.makeRGB(53, 115, 160),  // Position 0.52
                Color.makeRGB(53, 110, 160),  // Position 0.54
                Color.makeRGB(54, 105, 159),  // Position 0.56
                Color.makeRGB(55, 101, 157),  // Position 0.57
                Color.makeRGB(56, 96, 156),  // Position 0.59
                Color.makeRGB(57, 91, 155),  // Position 0.60
                Color.makeRGB(59, 87, 153),  // Position 0.62
                Color.makeRGB(60, 82, 150),  // Position 0.63
                Color.makeRGB(62, 77, 146),  // Position 0.65
                Color.makeRGB(63, 72, 142),  // Position 0.67
                Color.makeRGB(64, 68, 136),  // Position 0.68
                Color.makeRGB(64, 64, 130),  // Position 0.70
                Color.makeRGB(64, 60, 123),  // Position 0.71
                Color.makeRGB(63, 57, 116),  // Position 0.73
                Color.makeRGB(62, 53, 108),  // Position 0.75
                Color.makeRGB(60, 49, 99),  // Position 0.76
                Color.makeRGB(59, 46, 92),  // Position 0.78
                Color.makeRGB(56, 42, 85),  // Position 0.79
                Color.makeRGB(54, 39, 79),  // Position 0.81
                Color.makeRGB(52, 36, 72),  // Position 0.83
                Color.makeRGB(49, 33, 65),  // Position 0.84
                Color.makeRGB(46, 30, 58),  // Position 0.86
                Color.makeRGB(43, 27, 52),  // Position 0.87
                Color.makeRGB(39, 24, 46),  // Position 0.89
                Color.makeRGB(35, 21, 39),  // Position 0.90
                Color.makeRGB(31, 18, 33),  // Position 0.92
                Color.makeRGB(27, 15, 27),  // Position 0.94
                Color.makeRGB(23, 12, 22),  // Position 0.95
                Color.makeRGB(19, 9, 16),  // Position 0.97
                Color.makeRGB(15, 6, 10),  // Position 0.98
                Color.makeRGB(11, 3, 5),  // Position 1.00
            )
        )

        put(
            "flare", intArrayOf(
                Color.makeRGB(236, 175, 128),  // Position 0.00
                Color.makeRGB(236, 171, 126),  // Position 0.02
                Color.makeRGB(236, 167, 123),  // Position 0.03
                Color.makeRGB(235, 164, 121),  // Position 0.05
                Color.makeRGB(235, 160, 118),  // Position 0.06
                Color.makeRGB(235, 156, 116),  // Position 0.08
                Color.makeRGB(234, 152, 114),  // Position 0.10
                Color.makeRGB(234, 148, 111),  // Position 0.11
                Color.makeRGB(233, 144, 109),  // Position 0.13
                Color.makeRGB(233, 140, 107),  // Position 0.14
                Color.makeRGB(232, 136, 105),  // Position 0.16
                Color.makeRGB(232, 132, 102),  // Position 0.17
                Color.makeRGB(231, 128, 100),  // Position 0.19
                Color.makeRGB(231, 124, 98),  // Position 0.21
                Color.makeRGB(230, 120, 97),  // Position 0.22
                Color.makeRGB(229, 117, 95),  // Position 0.24
                Color.makeRGB(228, 112, 94),  // Position 0.25
                Color.makeRGB(227, 108, 93),  // Position 0.27
                Color.makeRGB(226, 104, 92),  // Position 0.29
                Color.makeRGB(225, 100, 91),  // Position 0.30
                Color.makeRGB(223, 96, 91),  // Position 0.32
                Color.makeRGB(222, 92, 91),  // Position 0.33
                Color.makeRGB(220, 89, 92),  // Position 0.35
                Color.makeRGB(218, 85, 92),  // Position 0.37
                Color.makeRGB(216, 82, 93),  // Position 0.38
                Color.makeRGB(214, 79, 94),  // Position 0.40
                Color.makeRGB(211, 76, 95),  // Position 0.41
                Color.makeRGB(208, 73, 97),  // Position 0.43
                Color.makeRGB(206, 71, 98),  // Position 0.44
                Color.makeRGB(202, 69, 99),  // Position 0.46
                Color.makeRGB(199, 67, 101),  // Position 0.48
                Color.makeRGB(196, 65, 102),  // Position 0.49
                Color.makeRGB(191, 63, 104),  // Position 0.51
                Color.makeRGB(187, 62, 105),  // Position 0.52
                Color.makeRGB(184, 61, 106),  // Position 0.54
                Color.makeRGB(180, 60, 107),  // Position 0.56
                Color.makeRGB(176, 59, 108),  // Position 0.57
                Color.makeRGB(172, 58, 109),  // Position 0.59
                Color.makeRGB(168, 57, 109),  // Position 0.60
                Color.makeRGB(165, 56, 110),  // Position 0.62
                Color.makeRGB(161, 55, 111),  // Position 0.63
                Color.makeRGB(157, 54, 111),  // Position 0.65
                Color.makeRGB(154, 53, 111),  // Position 0.67
                Color.makeRGB(150, 52, 112),  // Position 0.68
                Color.makeRGB(146, 51, 112),  // Position 0.70
                Color.makeRGB(142, 50, 112),  // Position 0.71
                Color.makeRGB(139, 49, 112),  // Position 0.73
                Color.makeRGB(135, 48, 112),  // Position 0.75
                Color.makeRGB(130, 47, 112),  // Position 0.76
                Color.makeRGB(126, 46, 112),  // Position 0.78
                Color.makeRGB(123, 46, 111),  // Position 0.79
                Color.makeRGB(119, 45, 111),  // Position 0.81
                Color.makeRGB(115, 44, 110),  // Position 0.83
                Color.makeRGB(111, 43, 109),  // Position 0.84
                Color.makeRGB(108, 43, 108),  // Position 0.86
                Color.makeRGB(104, 42, 107),  // Position 0.87
                Color.makeRGB(100, 41, 106),  // Position 0.89
                Color.makeRGB(96, 40, 105),  // Position 0.90
                Color.makeRGB(93, 40, 104),  // Position 0.92
                Color.makeRGB(89, 39, 102),  // Position 0.94
                Color.makeRGB(85, 38, 101),  // Position 0.95
                Color.makeRGB(82, 37, 100),  // Position 0.97
                Color.makeRGB(78, 36, 99),  // Position 0.98
                Color.makeRGB(74, 34, 98),  // Position 1.00
            )
        )

        put(
            "magma_r", intArrayOf(
                Color.makeRGB(251, 252, 191),  // Position 0.00
                Color.makeRGB(252, 245, 183),  // Position 0.02
                Color.makeRGB(252, 238, 176),  // Position 0.03
                Color.makeRGB(252, 230, 168),  // Position 0.05
                Color.makeRGB(253, 223, 161),  // Position 0.06
                Color.makeRGB(253, 216, 154),  // Position 0.08
                Color.makeRGB(253, 209, 147),  // Position 0.10
                Color.makeRGB(254, 201, 141),  // Position 0.11
                Color.makeRGB(254, 194, 134),  // Position 0.13
                Color.makeRGB(254, 187, 128),  // Position 0.14
                Color.makeRGB(254, 179, 123),  // Position 0.16
                Color.makeRGB(254, 172, 117),  // Position 0.17
                Color.makeRGB(253, 164, 112),  // Position 0.19
                Color.makeRGB(253, 157, 107),  // Position 0.21
                Color.makeRGB(253, 149, 103),  // Position 0.22
                Color.makeRGB(252, 142, 99),  // Position 0.24
                Color.makeRGB(251, 132, 96),  // Position 0.25
                Color.makeRGB(249, 125, 93),  // Position 0.27
                Color.makeRGB(248, 117, 92),  // Position 0.29
                Color.makeRGB(246, 110, 91),  // Position 0.30
                Color.makeRGB(243, 103, 91),  // Position 0.32
                Color.makeRGB(240, 96, 93),  // Position 0.33
                Color.makeRGB(237, 89, 95),  // Position 0.35
                Color.makeRGB(232, 84, 97),  // Position 0.37
                Color.makeRGB(228, 78, 100),  // Position 0.38
                Color.makeRGB(222, 74, 103),  // Position 0.40
                Color.makeRGB(217, 70, 106),  // Position 0.41
                Color.makeRGB(211, 66, 109),  // Position 0.43
                Color.makeRGB(205, 63, 112),  // Position 0.44
                Color.makeRGB(198, 60, 115),  // Position 0.46
                Color.makeRGB(192, 58, 117),  // Position 0.48
                Color.makeRGB(185, 55, 120),  // Position 0.49
                Color.makeRGB(177, 53, 122),  // Position 0.51
                Color.makeRGB(171, 51, 124),  // Position 0.52
                Color.makeRGB(164, 48, 125),  // Position 0.54
                Color.makeRGB(158, 46, 126),  // Position 0.56
                Color.makeRGB(151, 44, 127),  // Position 0.57
                Color.makeRGB(145, 42, 128),  // Position 0.59
                Color.makeRGB(138, 40, 129),  // Position 0.60
                Color.makeRGB(132, 38, 129),  // Position 0.62
                Color.makeRGB(126, 36, 129),  // Position 0.63
                Color.makeRGB(119, 33, 129),  // Position 0.65
                Color.makeRGB(113, 31, 129),  // Position 0.67
                Color.makeRGB(107, 28, 128),  // Position 0.68
                Color.makeRGB(101, 26, 128),  // Position 0.70
                Color.makeRGB(94, 23, 127),  // Position 0.71
                Color.makeRGB(88, 21, 126),  // Position 0.73
                Color.makeRGB(82, 18, 124),  // Position 0.75
                Color.makeRGB(74, 16, 121),  // Position 0.76
                Color.makeRGB(67, 15, 117),  // Position 0.78
                Color.makeRGB(60, 15, 113),  // Position 0.79
                Color.makeRGB(53, 15, 106),  // Position 0.81
                Color.makeRGB(47, 16, 98),  // Position 0.83
                Color.makeRGB(40, 17, 89),  // Position 0.84
                Color.makeRGB(34, 17, 80),  // Position 0.86
                Color.makeRGB(28, 16, 70),  // Position 0.87
                Color.makeRGB(23, 15, 60),  // Position 0.89
                Color.makeRGB(18, 13, 51),  // Position 0.90
                Color.makeRGB(14, 10, 42),  // Position 0.92
                Color.makeRGB(10, 7, 34),  // Position 0.94
                Color.makeRGB(6, 5, 25),  // Position 0.95
                Color.makeRGB(3, 3, 17),  // Position 0.97
                Color.makeRGB(1, 1, 9),  // Position 0.98
                Color.makeRGB(0, 0, 3),  // Position 1.00
            )
        )
    }

    private val epaService = EpaService<Long>()
    private val cycleTimeByState = epaService.computeAllCycleTimes(
        extendedPrefixAutomaton = extendedPrefixAutomaton,
        minus = Long::minus,
        average = { cycleTimes ->
            if (cycleTimes.isEmpty()) {
                0f
            } else cycleTimes.average().toFloat()
        },
        progressCallback = progressCallback
    )
    private val minCycleTime = cycleTimeByState.values.min()
    private val maxCycleTime = cycleTimeByState.values.max()

    private val normalizedStateFrequency = epaService
        .getNormalizedStateFrequency(
            epa = extendedPrefixAutomaton,
            progressCallback = progressCallback
        )

    override fun toStateAtlasEntry(state: State): StateAtlasEntry {
        return StateAtlasEntry(
            size = stateSize,
            paint = toHeatmapPaint(
                value = cycleTimeByState[state]!!,
                min = minCycleTime,
                max = maxCycleTime
            )
        )
    }

    override fun toTransitionAtlasEntry(transition: Transition): TransitionAtlasEntry {
        val freq = normalizedStateFrequency.frequencyByState(transition.end)

        val width = linearProjectClamped(
            freq,
            normalizedStateFrequency.min(),
            normalizedStateFrequency.max(),
            minTransitionSize,
            maxTransitionSize
        )

        return TransitionAtlasEntry(
            paint = Paint().apply {
                color = Color.BLACK
                mode = PaintMode.STROKE
                strokeWidth = width
                isAntiAlias = true
            }
        )
    }

    fun linearProjectClamped(
        value: Float,
        oldMin: Float,
        oldMax: Float,
        newMin: Float,
        newMax: Float
    ): Float {
        val projected = newMin + (value - oldMin) * (newMax - newMin) / (oldMax - oldMin)
        return projected.coerceIn(newMin, newMax)
    }

    fun toHeatmapPaint(
        value: Float,
        min: Float,
        max: Float
    ): Paint {
        val clampedValue = ((value - min) / (max - min)).coerceIn(0.0f, 1.0f)

        // rocket_r mako_r flare magma_r crest
        val heatmap = heatmapByName["mako_r"] ?: heatmapByName.values.first()

        val colorPositions = FloatArray(heatmap.size) { i ->
            i.toFloat() / (heatmap.size - 1)
        }

        for (i in 0 until colorPositions.size - 1) {
            if (clampedValue >= colorPositions[i] && clampedValue <= colorPositions[i + 1]) {
                val range = colorPositions[i + 1] - colorPositions[i]
                val factor = (clampedValue - colorPositions[i]) / range

                val color = interpolateColor(heatmap[i], heatmap[i + 1], factor)
                return Paint().apply {
                    this.color = color
                }
            }
        }

        val color = heatmap.last()
        return Paint().apply {
            this.color = color
        }
    }

    /** Linearly interpolates between two colors */
    private fun interpolateColor(color1: Int, color2: Int, factor: Float): Int {
        val r1 = Color.getR(color1)
        val g1 = Color.getG(color1)
        val b1 = Color.getB(color1)
        val a1 = Color.getA(color1)

        val r2 = Color.getR(color2)
        val g2 = Color.getG(color2)
        val b2 = Color.getB(color2)
        val a2 = Color.getA(color2)

        val r = (r1 + (r2 - r1) * factor).toInt()
        val g = (g1 + (g2 - g1) * factor).toInt()
        val b = (b1 + (b2 - b1) * factor).toInt()
        val a = (a1 + (a2 - a1) * factor).toInt()

        return Color.makeARGB(a, r, g, b)
    }
}