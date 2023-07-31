package top.clarkding.widget.chart

/**
 * Y轴百分比分割
 * @param ySplitValue：y轴最大值
 * @param yNum：y轴分割数
 * @param splitYArray：背景分割比例，为空时平均分割
 */
class PercentYSplit(
    ySplitValue: Int,
    yNum: Int,
    xNum: Int,
    private val splitYArray: FloatArray? = null,
    private val splitXArray: FloatArray? = null): BaseSplit(ySplitValue, yNum, 0, xNum) {

    private var mHeight = 0F
    private var mAverageHeight = 0F

    @Volatile private var viewSizeSet = false

    init {
        if (splitYArray != null) {
            // splitPercentArray个数必须等于splitNum
            if (splitYArray.size != yNum)
                throw IllegalArgumentException("the size of splitPercentArray must equal to splitNum")
            // splitPercentArray总值必须小于等于1
            var totalPercent = 0F
            splitYArray.forEach {
                totalPercent += it
            }
            if (totalPercent > 1F)
                throw IllegalArgumentException("the total value of splitPercentArray must equal to 1")
        }

        if (splitXArray != null) {
            // splitPercentArray个数必须等于splitNum
            if (splitXArray.size != xNum)
                throw IllegalArgumentException("the size of splitPercentArray must equal to splitNum")
            // splitPercentArray总值必须小于等于1
            var totalPercent = 0F
            splitXArray.forEach {
                totalPercent += it
            }
            if (totalPercent > 1F)
                throw IllegalArgumentException("the total value of splitPercentArray must equal to 1")
        }
    }

    override fun getCellHeight(index: Int): Float {
        if (!viewSizeSet) {
            throw IllegalArgumentException("the size of view must be set before get height of cell")
        }
        return splitYArray?.get(index)?.let {
            it * mHeight
        }  ?: mAverageHeight
    }

    override fun getCellWidth(index: Int): Float = 0F

    override fun setViewSize(width: Float, height: Float) {
        viewSizeSet = true
        mHeight = height
        if (splitYArray == null) {
            mAverageHeight = height / ySpiltNum
        }
    }
}