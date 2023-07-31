package top.clarkding.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.Log
import android.view.ViewDebug.FlagToString
import androidx.annotation.ColorInt

/***
 * 分割线风格
 * @param lineColor：线条颜色(后期支持渐变)
 * @param lineDash：线条宽度
 * @param needLineFull：是否需要封顶线条，若填充满，线条数将比格子数多1
 * @param margin：边距
 */
class SplitLine(@ColorInt val lineColor: Int,
                val lineDash: Float,
                val needLineFull: Boolean = false,
                val margin: DrawerMargin = DrawerMargin()
)

/***
 * 圆点风格
 * @param dotColor：圆点颜色(后期支持渐变)
 * @param dotRadius：圆点半径
 */
class SplitDot(@ColorInt val dotColor: Int,
                val dotRadius: Float)

/***
 * 线状背景
 * @param mSplit：分割方式
 * @param splitLine：分割线风格
 * @param tagParam：标签风格，为空表示不设置标签
 */
class LineBgDrawer(val mSplit: BaseSplit,
                   val splitLine: SplitLine,
                   val tagParam: SplitTv? = null): BaseDrawer() {

    private val mMax = mSplit.maxYValue
    private val mNum = mSplit.ySpiltNum

    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTvPaint: TextPaint? = null

    private val mLineNum = if (splitLine.needLineFull) (mNum + 1) else mNum

    // 全部直线
    private val mLinePosition = FloatArray(mLineNum * 4)
    // 全部文字
    private var mTvParams: Array<TvDrawParam?>? = null

    init {
        mLinePaint.color = splitLine.lineColor
        mLinePaint.strokeWidth = splitLine.lineDash

        tagParam?.let {

            mTvPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

            mTvPaint?.color = it.tvColor
            mTvPaint?.textSize = it.tvSize

            it.tvFamily?.let { family ->
                val typeface = Typeface.create(family, Typeface.NORMAL)
                mTvPaint?.typeface = typeface
            }

            mTvParams = Array(mNum) {
                null
            }
        }
    }

    /**
     * 计算参数：当前不适配ltr布局
     * @param width：背景宽度
     * @param height：背景高度
     */
    override fun calculateSize(width: Float, height: Float) {
        // 计算每格占据的空间大小m
        mSplit.setViewSize(width, height)

        val maxTagWidth = tagParam?.let {
            val maxText = tagParam.tagFormat?.format(mSplit.maxYValue.toFloat()) ?: "${mSplit.maxYValue}"
            mTvPaint?.measureText(maxText) ?: 0F
        } ?: 0F

        Log.d(WidgetTag, "maxTagWidth: $maxTagWidth")

        val curStartX = maxTagWidth + splitLine.margin.startMar
        val curEndX = width - splitLine.margin.endMar
        var lastStartY = height

        val perValue = mMax.toFloat() / (mNum - 1)

        for (index in 0 until mLineNum) {

            val curYDiff = if (index == 0) {
                0F
            } else mSplit.getCellHeight(index - 1)

            val curY = lastStartY - splitLine.lineDash - curYDiff

            mLinePosition[index * 4] = curStartX
            mLinePosition[index * 4 + 1] = curY
            mLinePosition[index * 4 + 2] = curEndX
            mLinePosition[index * 4 + 3] = curY

            // 计算文字位置，y轴位置先基于分割线的位置写
            tagParam?.let {
                val curValue = perValue * index
                val curContent = tagParam.tagFormat?.format(curValue) ?: "$curValue"

                val tvRect = Rect()
                mTvPaint?.getTextBounds(curContent, 0, curContent.length, tvRect)

                val tvBot = mTvPaint?.fontMetricsInt?.bottom ?: 0

                // 基于线的位置写：文字的Y坐标(即baseline的位置)就是线的y坐标 - interline spacing（fontMetricsInt.bottom）
                val tvY = curY - tvBot

                mTvParams?.let {
                    it[index] = TvDrawParam(curContent, 0F, tvY)
                }
            }

            lastStartY -= curYDiff
        }
    }

    override fun draw(canvas: Canvas?) {

        canvas?.let {
            // 绘制全部线条
            canvas.drawLines(mLinePosition, mLinePaint)

            // 绘制全部文案
            mTvParams?.forEach { tvParam ->
                tvParam?.let {
                    mTvPaint?.let { paint ->
                        canvas.drawText(it.content, it.mTvX, it.mTvY, paint)
                    }
                }
            }
        }
    }
}

/***
 * 圆点背景
 * @param mSplit：分割方式
 * @param splitLine：分割点风格
 * @param tagParam：标签风格，为空表示不设置标签
 */
class DotBgDrawer(val mSplit: BaseSplit,
                val splitLine: SplitDot,
                val tagParam: SplitTv? = null): BaseDrawer() {

    private val mYMax = mSplit.maxYValue
    private val mYNum = mSplit.ySpiltNum

    private val mXMax = mSplit.maxXValue
    private val mXNum = mSplit.xSpiltNum

    private val mDotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTvPaint: TextPaint? = null

    private val mDotXNum = if (mSplit.needXFull) (mXNum + 1) else mXNum
    private val mDotYNum = if (mSplit.needYFull) (mYNum + 1) else mYNum

    // 全部点：单点的信息是两个float [x, y]
    private val mDotPosition = FloatArray(mDotXNum * mDotYNum * 2)

    init {
        mDotPaint.color = splitLine.dotColor
        mDotPaint.strokeWidth = splitLine.dotRadius
        mDotPaint.strokeCap = Paint.Cap.ROUND

        tagParam?.let {
            mTvPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            mTvPaint?.textSize = it.tvSize
            mTvPaint?.color = it.tvColor
        }
    }

    override fun calculateSize(width: Float, height: Float) {
        mSplit.setViewSize(width - splitLine.dotRadius * 2, height - splitLine.dotRadius * 2)

        var lastXValue = 0F
        Log.d(WidgetTag, "dotPos : ${mDotPosition.size}")

        var pointIndex = 0
        for (xIndex in 0 until mDotXNum) {

            val curXDiff = if (xIndex == 0) {
                splitLine.dotRadius
            } else mSplit.getCellWidth(xIndex - 1)

            var lastYValue = height

            for (yIndex in 0 until mDotYNum) {
                val curYDiff = if (yIndex == 0) {
                    splitLine.dotRadius
                } else mSplit.getCellHeight(yIndex - 1)

                val curY = lastYValue - curYDiff
                val curX = lastXValue + curXDiff

                mDotPosition[pointIndex++] = curX
                mDotPosition[pointIndex++] = curY

//                Log.d(WidgetTag, "index: [$xIndex, $yIndex], curX : $curX, curY: $curY, array1: $pointIndex")

                lastYValue -= curYDiff
            }

            lastXValue += curXDiff
        }
    }

    override fun draw(canvas: Canvas?) {
        canvas?.let {
            it.drawPoints(mDotPosition, mDotPaint)
        }
    }
}