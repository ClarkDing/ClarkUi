package top.clarkding.widget.chart

import android.graphics.Canvas
import androidx.annotation.ColorInt

const val WidgetTag = "MOBU_WIDGET"

class WidgetPoint(var x: Float = 0F,
                  var y: Float = 0F)

class PathPoint(var first: WidgetPoint,
                var second: WidgetPoint,
                var third: WidgetPoint,
                var four: WidgetPoint)

abstract class BaseDrawer {

    abstract fun draw(canvas: Canvas?)

    abstract fun calculateSize(width: Float, height: Float)
}

abstract class BaseAnimDrawer: BaseDrawer() {

    abstract fun update(curRate: Float)
}

class DrawerMargin(val startMar: Float = 0F,
                   val endMar: Float = 0F,
                   val topMar: Float = 0F,
                   val botMar: Float = 0F)

/**
 * @param maxYValue：y轴最大值
 * @param ySpiltNum：y轴分割数
 * @param maxXValue：x轴最大值
 * @param xSpiltNum：x轴分割数
 * @param splitNum：背景分割比例，为空时平均分割
 * @param needXFull：顶部是否需要分割线
 * @param needYFull：End位置是否需要分割线
 */
abstract class BaseSplit(
    val maxYValue: Int,
    val ySpiltNum: Int,
    val maxXValue: Int,
    val xSpiltNum: Int,
    val needXFull: Boolean = false,
    val needYFull: Boolean = false) {

    /**
     * 获取当前单元格高度
     */
    abstract fun getCellHeight(index: Int): Float

    /**
     * 获取当前单元格宽度
     */
    abstract fun getCellWidth(index: Int): Float

    abstract fun setViewSize(width: Float, height: Float)
}

/**
 * 坐标轴标签标签
 * @param tvColor：文本颜色
 * @param tvSize：文字大小
 * @param tvFamily：字体
 * @param tagFormat：文字格式：如"%s%"
 */
class SplitTv(
    @ColorInt val tvColor: Int,
    val tvSize: Float,
    val tvFamily: String? = null,
    val tagFormat: String? = null)

class TvDrawParam(
    val content: String,
    val mTvX: Float,
    val mTvY: Float
)