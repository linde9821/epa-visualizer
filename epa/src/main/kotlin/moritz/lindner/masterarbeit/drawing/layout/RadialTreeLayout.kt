package moritz.lindner.masterarbeit.drawing.layout

interface RadialTreeLayout<T : Comparable<T>> : TreeLayout<T> {
    fun getCircleRadius(): Float
}
