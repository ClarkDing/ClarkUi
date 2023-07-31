package top.clarkding.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobu.widget.DemoType.TYPE_CHART
import com.mobu.widget.chart.ChartActivity
import com.mobu.widget.databinding.ActivityDemoBinding
import com.mobu.widget.databinding.LayoutDemoBinding

private val DEMO_TAG = "MOBU_WIDGET"

object DemoType {

    val TYPE_CHART = 0
}

class DemoBean(val type: Int, val name: String)

class DemoAdapter(private val mCtx: Context,
                  private val mItems: List<DemoBean>,
                  private val mListener: (Int)->Unit): RecyclerView.Adapter<DemoAdapter.DemoHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoHolder {
        return DemoHolder(
            DataBindingUtil.inflate(
            LayoutInflater.from(mCtx),
            R.layout.layout_demo,
            parent,
            false), mListener)
    }

    override fun onBindViewHolder(holder: DemoHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    private fun getItem(position: Int): DemoBean? {
        return if (position >= mItems.size) {
            null
        } else mItems[position]
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    class DemoHolder(private val itemBinding: LayoutDemoBinding,
                     private val mListener: (Int)->Unit): RecyclerView.ViewHolder(itemBinding.root) {

        init {
            itemBinding.setItemClick {
                itemBinding.itemBean?.let { bean ->
                    mListener.invoke(bean.type)
                }
            }
        }

        fun bind(curBean: DemoBean) {
            itemBinding.apply {
                itemBean = curBean
                tvName.text = curBean.name
            }
        }
    }
}

class DemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDemoBinding
    private lateinit var adapter: DemoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DemoAdapter(this.applicationContext, buildData()) { type ->
            goTarget(type)
        }
//        binding.rlDemo.layoutMangarget = LinearLayoutManager(this)
        binding.rlDemo.adapter = adapter
    }

    private fun buildData(): List<DemoBean> = listOf(DemoBean(TYPE_CHART, "Chart Demo"))

    private fun goTarget(type: Int) {
        Log.d(DEMO_TAG, "goTarget: $type")
        val curIntent = when (type) {
            TYPE_CHART -> Intent(this, ChartActivity::class.java)
            else -> null
        }
        curIntent?.let {
            this@DemoActivity.startActivity(it)
        }
    }
}