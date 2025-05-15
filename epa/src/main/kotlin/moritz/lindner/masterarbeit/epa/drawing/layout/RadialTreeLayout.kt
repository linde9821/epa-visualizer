package moritz.lindner.masterarbeit.epa.drawing.layout

interface RadialTreeLayout<T : Comparable<T>> : TreeLayout<T> {
    fun getCircleRadius(): Float
}
