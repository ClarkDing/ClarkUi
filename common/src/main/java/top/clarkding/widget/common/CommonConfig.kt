package top.clarkding.widget.common

import android.content.Context
import androidx.core.view.ViewCompat

/**
 * 是否是rtf语言
 */
fun Context.isRtf(): Boolean {
    return WidgetConfig.isRtf ?: (this.resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL)
}

object WidgetConfig {

    var isRtf: Boolean? = null
}