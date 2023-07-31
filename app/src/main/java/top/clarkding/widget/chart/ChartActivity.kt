package top.clarkding.widget.chart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import top.clarkding.widget.databinding.ActivityChartBinding

class ChartActivity: AppCompatActivity() {

    private lateinit var binding: ActivityChartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fillChart()
    }

    private fun fillChart() {
        val percentYSplit = PercentYSplit(100, 6, xNum = 16)
        val percentYMargin = DrawerMargin(startMar = 10F)
        val percentYTvStyle = SplitTv(this.getColor(top.clarkding.widget.R.color.purple_200), 20F, tagFormat = "%.0f%%")

        val mRateValue = mutableListOf<Float>()
        mRateValue.add(1F)
        mRateValue.add(0.5F)
        mRateValue.add(0.7F)
        mRateValue.add(0.2F)
        mRateValue.add(0.1F)
        mRateValue.add(0.3F)

        binding.vElectrocard.setDrawer(LineBgDrawer(
            percentYSplit,
            SplitLine(this.getColor(top.clarkding.widget.R.color.purple_200), 5F, false, percentYMargin),
            percentYTvStyle
        ))

        binding.vElectrocard2.setDrawer(DotBgDrawer(
            PercentSplit(100, 5, 100, 15),
            SplitDot(this.getColor(top.clarkding.widget.R.color.purple_200), 5F),
            null
        ))

        binding.vElectrocardAnim.setDrawer(LineAnimDrawer(
            percentYSplit,
            mStyle = AnimLine(
                this.getColor(top.clarkding.widget.R.color.teal_200),
                5F,
                this.getColor(top.clarkding.widget.R.color.shadow_start),
                this.getColor(top.clarkding.widget.R.color.shadow_end),
                percentYMargin),
            mTvStyle = percentYTvStyle,
            mRateValues = mRateValue
        ))
    }
}