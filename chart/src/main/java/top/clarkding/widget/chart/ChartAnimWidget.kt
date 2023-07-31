package top.clarkding.widget.chart

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class ChartAnimWidget: View {

    private var mBgDrawer: BaseAnimDrawer? = null

    constructor(context: Context):
            this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?):
            this(context, attributeSet, 0)

    constructor(context: Context, attrs: AttributeSet?, defAttr: Int):
            super(context, attrs, defAttr) {
    }

    fun setDrawer(bgDrawer: BaseAnimDrawer) {
        mBgDrawer = bgDrawer
        invalidate()
    }

    fun update(curValue: Float) {
        mBgDrawer?.update(curValue)
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mBgDrawer?.calculateSize((right - left).toFloat(), (bottom - top).toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        mBgDrawer?.draw(canvas)
    }
}