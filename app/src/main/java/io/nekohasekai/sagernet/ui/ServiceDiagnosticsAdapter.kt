package io.nekohasekai.sagernet.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.utils.ServiceCategory
import io.nekohasekai.sagernet.utils.ServiceStatusItem
import io.nekohasekai.sagernet.utils.ServiceTestState
import io.nekohasekai.sagernet.utils.Theme

class ServiceDiagnosticsAdapter(
    private val allItems: MutableList<ServiceStatusItem>,
    private val onItemClick: (ServiceStatusItem) -> Unit,
) : RecyclerView.Adapter<ServiceDiagnosticsAdapter.ViewHolder>() {

    private var currentCategory: ServiceCategory = ServiceCategory.ALL
    private var displayedItems: List<ServiceStatusItem> = allItems.toList()

    fun filterByCategory(category: ServiceCategory) {
        currentCategory = category
        displayedItems = if (category == ServiceCategory.ALL) {
            allItems.toList()
        } else {
            allItems.filter { it.category == category }
        }
        notifyDataSetChanged()
    }

    fun updateItem(item: ServiceStatusItem) {
        val index = displayedItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun updateAll() {
        displayedItems = if (currentCategory == ServiceCategory.ALL) {
            allItems.toList()
        } else {
            allItems.filter { it.category == currentCategory }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_diagnostics, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(displayedItems[position])
    }

    override fun getItemCount(): Int = displayedItems.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rootCard = itemView.findViewById<MaterialCardView>(R.id.card_service_root)
        private val icon = itemView.findViewById<ImageView>(R.id.service_icon)
        private val tvName = itemView.findViewById<TextView>(R.id.tv_service_name)
        private val tvCategory = itemView.findViewById<TextView>(R.id.tv_service_category)
        private val tvDetail = itemView.findViewById<TextView>(R.id.tv_service_detail)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tv_service_status)
        private val pbTesting = itemView.findViewById<ProgressBar>(R.id.pb_service_testing)
        private val tvLatency = itemView.findViewById<TextView>(R.id.tv_service_latency)

        fun bind(item: ServiceStatusItem) {
            tvName.text = item.name
            tvCategory.text = item.category.label
            icon.setImageResource(item.iconRes)

            val context = itemView.context
            val isNight = Theme.usingNightMode(context)
            val isWhite = Theme.isWhiteTheme(context)
            val density = context.resources.displayMetrics.density

            val cardBg = when {
                isNight -> Color.parseColor("#1E1E1E")
                isWhite -> Color.parseColor("#F5F6F8")
                else -> null
            }
            if (cardBg != null) {
                rootCard.setCardBackgroundColor(cardBg)
            }
            val strokeColor = when {
                isNight -> Color.parseColor("#333333")
                isWhite -> Color.parseColor("#E0E0E0")
                else -> null
            }
            if (strokeColor != null) {
                rootCard.strokeColor = strokeColor
            }

            when (item.state) {
                ServiceTestState.TESTING -> {
                    pbTesting.visibility = View.VISIBLE
                    tvStatus.visibility = View.GONE
                    tvDetail.text = if (item.detailMessage.isNotBlank()) item.detailMessage else "正在握手探测..."
                    tvLatency.text = "检测中..."
                }
                ServiceTestState.AVAILABLE -> {
                    pbTesting.visibility = View.GONE
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = item.statusSummary.ifBlank { "正常可用" }
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 4f * density
                        setColor(Color.parseColor("#10B981")) // 绿色
                    }
                    tvStatus.background = bg
                    tvDetail.text = item.detailMessage.ifBlank { "服务响应正常" }
                    tvLatency.text = "${item.latencyMs} ms"
                }
                ServiceTestState.RESTRICTED -> {
                    pbTesting.visibility = View.GONE
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = item.statusSummary.ifBlank { "地区受限" }
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 4f * density
                        setColor(Color.parseColor("#F59E0B")) // 橙色
                    }
                    tvStatus.background = bg
                    tvDetail.text = item.detailMessage.ifBlank { "当前节点机房被屏蔽" }
                    tvLatency.text = if (item.latencyMs > 0) "${item.latencyMs} ms" else "-"
                }
                ServiceTestState.UNAVAILABLE -> {
                    pbTesting.visibility = View.GONE
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = item.statusSummary.ifBlank { "不可用" }
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 4f * density
                        setColor(Color.parseColor("#EF4444")) // 红色
                    }
                    tvStatus.background = bg
                    tvDetail.text = item.detailMessage.ifBlank { "连接超时或拒绝" }
                    tvLatency.text = if (item.latencyMs > 0) "${item.latencyMs} ms" else "-"
                }
                ServiceTestState.IDLE -> {
                    pbTesting.visibility = View.GONE
                    tvStatus.visibility = View.VISIBLE
                    tvStatus.text = "未测试"
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 4f * density
                        setColor(Color.parseColor("#6B7280")) // 灰色
                    }
                    tvStatus.background = bg
                    tvDetail.text = "点击卡片立即开始测试"
                    tvLatency.text = "-"
                }
            }

            rootCard.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
