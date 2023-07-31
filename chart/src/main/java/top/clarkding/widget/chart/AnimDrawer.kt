package top.clarkding.widget.chart

import android.graphics.*
import android.text.TextPaint
import androidx.annotation.ColorInt
import java.util.*

/***
 * 折线动画
 * @param lineColor：折线颜色(后期支持渐变)
 * @param lineDash：折线高度
 * @param gradientColor：折线下部渐变色
 * @param margin：坐标轴边距
 */
class AnimLine(@ColorInt val lineColor: Int,
               val lineDash: Float,
               @ColorInt val bgStartColor: Int? = null,
               @ColorInt val bgEndColor: Int? = null,
               val margin: DrawerMargin = DrawerMargin())

/**
 * 折线动效
 * @param baseRate：基线比例
 * @param mOriRight：线条方向：true： 从右到左；false：从左到右
 */
class LineAnimDrawer(val mSplit: BaseSplit,
                val mStyle: AnimLine,
                val mTvStyle: SplitTv? = null,
                val mRateValues: MutableList<Float>? = null,
                val mOriRight: Boolean = true): BaseAnimDrawer() {

    // 所有比例的容器
    private val mRateQueue = LinkedList<Float>()

    private var mLinesPos: FloatArray? = null
    private var mPathPos = mutableListOf<Float>()

    private val mBotPath = Path()

    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mGradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var mGradient: Shader? = null
    private val mGradMatrix = Matrix()

    // 控件总宽高
    private var mViewHeight = 0F
    private var mViewWidth = 0F

    private var mTagWidth = 0F

    // 表格宽高
    private var mMaxWidth = 0F
    private var mMaxHeight = 0F

    private var mAxisLeft = 0F
    private var mAxisRight = 0F

    init {
        mLinePaint.color = mStyle.lineColor
        mLinePaint.strokeWidth = mStyle.lineDash

        mRateValues?.let {

            // 初始装配
            if (mRateValues.size <= 20) {
                mRateQueue.addAll(mRateValues)
            } else {
                mRateQueue.addAll(mRateValues.subList(mRateValues.size - 21, mRateValues.size - 1))
            }
        }
    }

    /**
     * 涉及容器问题，放主线程
     */
    override fun update(curRate: Float) {

        if (mRateQueue.size >= mSplit.xSpiltNum) {
            mRateQueue.poll()
        }
        mRateQueue.add(curRate)

        reCalculate()
    }

    override fun draw(canvas: Canvas?) {
        // 绘制折线下方渐变区域
        canvas?.let {

            if (mPathPos.size % 8 == 0) {
                val pathIterator = mPathPos.iterator()
                var curIndex = 0
                while (pathIterator.hasNext()) {
                    mBotPath.reset()

                    val firstX = pathIterator.next()
                    val firstY = pathIterator.next()
                    val secondX = pathIterator.next()
                    val secondY = pathIterator.next()
                    val thirdX = pathIterator.next()
                    val thirdY = pathIterator.next()
                    val fourX = pathIterator.next()
                    val fourY = pathIterator.next()

                    mBotPath.moveTo(firstX, firstY)
                    mBotPath.lineTo(secondX, secondY)
                    mBotPath.lineTo(thirdX, thirdY)
                    mBotPath.lineTo(fourX, fourY)

                    mBotPath.close()

//                    val minHeight = secondY.coerceAtMost(thirdY).coerceAtMost(firstY).coerceAtMost(fourY)
                    // 通过Matrix可以控制渐变色的高度, 暂时屏蔽, 看UI有没有需求
//                    val scaleY = mMaxHeight / (mMaxHeight - minHeight)
//                    mGradMatrix.setScale(1F, scaleY)
//
//                    Log.d(WidgetTag, "Path scaleY: ${scaleY}, $maxHeight")
//
//                    mGradient?.setLocalMatrix(mGradMatrix)

                    mGradientPaint.shader = mGradient
                    it.drawPath(mBotPath, mGradientPaint)

                    curIndex++
                }
            }

            mLinesPos?.let { lines ->
                it.drawLines(lines, mLinePaint)
            }
        }
    }

    override fun calculateSize(width: Float, height: Float) {

        mSplit.setViewSize(width, height)

        mViewWidth = width
        mViewHeight = height

        mTagWidth = mTvStyle?.let {
            val maxText = mTvStyle.tagFormat?.format(mSplit.maxYValue.toFloat()) ?: "${mSplit.maxYValue}"
            val tvPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

            tvPaint.color = it.tvColor
            tvPaint.textSize = it.tvSize

            it.tvFamily?.let { family ->
                val typeface = Typeface.create(family, Typeface.NORMAL)
                tvPaint.typeface = typeface
            }

            tvPaint.measureText(maxText)
        } ?: 0F

        mAxisLeft = mStyle.margin.startMar + mTagWidth
        mAxisRight = mViewWidth - mStyle.margin.endMar

        mMaxWidth = mAxisRight - mAxisLeft
        mMaxHeight = mViewHeight - mStyle.margin.topMar - mStyle.margin.botMar - mSplit.getCellHeight(mSplit.ySpiltNum)

        mStyle.bgStartColor?.let { startColor ->
            mStyle.bgEndColor?.let { endColor ->

                mGradient = LinearGradient(
                    0f,
                    0f,
                    0f,
                    mMaxHeight * 2,
                    startColor,
                    endColor,
                    Shader.TileMode.CLAMP
                )

            } ?.run {
                mGradientPaint.color = startColor
            }
        }

        reCalculate()
    }

    /**
     * 重新计算点位数据
     */
    private fun reCalculate() {

        if (mRateQueue.size <= 1) {
            return
        }
        //TODO 按比例分割
        // 暂时只支持平均分割
        // 数据点起始于左右端点，因此分割的格子数应该小于数据点个数
        val cellNum = mSplit.xSpiltNum - 1
        val perCellWidth = mMaxWidth / cellNum

        // 判断数据是否可以填充满坐标轴
        mLinesPos = FloatArray((mRateQueue.size - 1) * 4)

        // 坐标轴x轴位置
        val pathBot = mViewHeight - mStyle.margin.botMar

        val tempPathList = mutableListOf<Float>()

        // 坐标轴x轴位置

        // 线条从右到左，倒序遍历
        val rateIterator = mRateQueue.listIterator(mRateQueue.size)
        if (mOriRight) {

            // 当前坐标
            var curIndex = 0
            while (rateIterator.hasPrevious()) {
                val curRate = rateIterator.previous()

                // 当前点坐标：
                // x：坐标轴右侧位置 - 当前单元格比例(目前只支持中间位置) - 右侧全部单元格
                // y：控件总高度 - 坐标轴底部边距 - 当前比例 - 线条的宽度
                val curPointX = mAxisRight - curIndex * perCellWidth
                val curY = pathBot - curRate * mMaxHeight - mStyle.lineDash

                mLinesPos?.let {
                    if (curIndex == 0) {
                        it[0] = curPointX
                        it[1] = curY

                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)
                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                    } else if (curIndex == mRateQueue.size - 1) {
                        it[it.size - 2] = curPointX
                        it[it.size - 1] = curY

                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)

                    } else {

                        val indexDiff = 2 + (curIndex - 1) * 4
                        it[indexDiff] = curPointX
                        it[indexDiff + 1] = curY
                        it[indexDiff + 2] = curPointX
                        it[indexDiff + 3] = curY

                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)

                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)
                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                    }
                }

                mPathPos.clear()
                mPathPos.addAll(tempPathList)

                curIndex++
            }

        } else {

            // 当前坐标
            var curIndex = 0
            while (rateIterator.hasPrevious()) {
                val curRate = rateIterator.previous()

                // 当前点坐标：
                // x：坐标轴左侧位置 + 当前单元格比例(目前只支持中间位置) + 左侧全部单元格
                // y：控件总高度 - 坐标轴底部边距 - 当前比例 - 线条的宽度
                val curPointX = mAxisLeft + curIndex * perCellWidth
                val curY = pathBot - curRate * mMaxHeight - mStyle.lineDash

                mLinesPos?.let {
                    if (curIndex == 0) {
                        it[0] = curPointX
                        it[1] = curY

                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)
                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                    } else if (curIndex == mRateQueue.size - 1) {
                        it[it.size - 2] = curPointX
                        it[it.size - 1] = curY

                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)

                    } else {

                        val indexDiff = 2 + (curIndex - 1) * 4
                        it[indexDiff] = curPointX
                        it[indexDiff + 1] = curY
                        it[indexDiff + 2] = curPointX
                        it[indexDiff + 3] = curY

                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)

                        tempPathList.add(curPointX)
                        tempPathList.add(curY)
                        tempPathList.add(curPointX)
                        tempPathList.add(pathBot)
                    }
                }

                mPathPos.clear()
                mPathPos.addAll(tempPathList)

                curIndex++
            }
        }
    }

}