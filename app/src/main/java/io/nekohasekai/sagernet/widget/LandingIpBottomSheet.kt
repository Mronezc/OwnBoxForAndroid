package io.nekohasekai.sagernet.widget

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.ktx.getColorAttr
import io.nekohasekai.sagernet.utils.LandingIpInfo
import io.nekohasekai.sagernet.utils.Theme

object LandingIpBottomSheet {

    fun show(
        activity: Activity,
        info: LandingIpInfo,
        onRefresh: () -> Unit,
    ) {
        val dialog = BottomSheetDialog(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.layout_landing_ip_details, null)
        dialog.setContentView(view)

        val tvLocation = view.findViewById<TextView>(R.id.tv_location)
        val tvFullIp = view.findViewById<TextView>(R.id.tv_full_ip)
        val tvIpVersion = view.findViewById<TextView>(R.id.tv_ip_version)
        val tvRiskBadge = view.findViewById<TextView>(R.id.tv_risk_badge)
        val tvNetworkType = view.findViewById<TextView>(R.id.tv_network_type)
        val tvThreatTags = view.findViewById<TextView>(R.id.tv_threat_tags)
        val tvSource = view.findViewById<TextView>(R.id.tv_source)
        val tvIsp = view.findViewById<TextView>(R.id.tv_isp)
        val tvOrg = view.findViewById<TextView>(R.id.tv_org)
        val tvAsn = view.findViewById<TextView>(R.id.tv_asn)
        val tvDuration = view.findViewById<TextView>(R.id.tv_duration)
        val btnCopyIp = view.findViewById<MaterialButton>(R.id.btn_copy_ip)
        val btnRetest = view.findViewById<MaterialButton>(R.id.btn_retest)
        val btnDismiss = view.findViewById<MaterialButton>(R.id.btn_dismiss)

        tvLocation.text = info.locationText
        tvFullIp.text = info.ip
        tvIpVersion.text = info.ipVersion
        tvRiskBadge.text = "${info.riskLevel.label} (${info.riskScore}%)"
        val density = activity.resources.displayMetrics.density
        val riskBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 6f * density
            setColor(Color.parseColor(info.riskLevel.colorHex))
        }
        tvRiskBadge.background = riskBg

        tvNetworkType.text = info.networkType
        tvThreatTags.text = if (info.threatTags.isNotEmpty()) info.threatTags.joinToString(" · ") else "无异常标记"
        tvSource.text = if (info.source.isNotBlank()) info.source else "权威 IP 智能库"
        tvIsp.text = if (info.isp.isNotBlank()) info.isp else activity.getString(R.string.unknown)
        tvOrg.text = if (info.org.isNotBlank()) info.org else activity.getString(R.string.unknown)
        tvAsn.text = if (info.asn.isNotBlank()) info.asn else activity.getString(R.string.unknown)
        tvDuration.text = "${info.durationMs} ms"

        val sheetContext = activity
        val isNight = Theme.usingNightMode(sheetContext)
        val isWhite = Theme.isWhiteTheme(sheetContext)

        val cardBgColor = when {
            isNight -> Color.parseColor("#1E1E1E")
            isWhite -> Color.parseColor("#F5F6F8")
            else -> null
        }
        val strokeColor = when {
            isNight -> Color.parseColor("#333333")
            isWhite -> Color.parseColor("#E0E0E0")
            else -> null
        }
        val primaryTextColor = when {
            isNight -> Color.parseColor("#F5F5F5")
            isWhite -> Color.parseColor("#1A1A1A")
            else -> null
        }
        val secondaryTextColor = when {
            isNight -> Color.parseColor("#B0B0B0")
            isWhite -> Color.parseColor("#5A5A5A")
            else -> null
        }

        if (cardBgColor != null) {
            view.findViewById<MaterialCardView>(R.id.card_ip)?.setCardBackgroundColor(cardBgColor)
            view.findViewById<MaterialCardView>(R.id.card_risk)?.setCardBackgroundColor(cardBgColor)
            view.findViewById<MaterialCardView>(R.id.card_details)?.setCardBackgroundColor(cardBgColor)
        }
        if (strokeColor != null) {
            view.findViewById<MaterialCardView>(R.id.card_ip)?.strokeColor = strokeColor
            view.findViewById<MaterialCardView>(R.id.card_risk)?.strokeColor = strokeColor
            view.findViewById<MaterialCardView>(R.id.card_details)?.strokeColor = strokeColor
            view.findViewById<View>(R.id.divider_risk_1)?.setBackgroundColor(strokeColor)
            view.findViewById<View>(R.id.divider_risk_2)?.setBackgroundColor(strokeColor)
            view.findViewById<View>(R.id.divider_risk_3)?.setBackgroundColor(strokeColor)
            view.findViewById<View>(R.id.divider1)?.setBackgroundColor(strokeColor)
            view.findViewById<View>(R.id.divider_details_org)?.setBackgroundColor(strokeColor)
            view.findViewById<View>(R.id.divider2)?.setBackgroundColor(strokeColor)
        }
        if (primaryTextColor != null) {
            view.findViewById<TextView>(R.id.tv_title)?.setTextColor(primaryTextColor)
            tvFullIp.setTextColor(primaryTextColor)
            tvNetworkType.setTextColor(primaryTextColor)
            tvThreatTags.setTextColor(primaryTextColor)
            tvSource.setTextColor(primaryTextColor)
            tvIsp.setTextColor(primaryTextColor)
            tvOrg.setTextColor(primaryTextColor)
            tvAsn.setTextColor(primaryTextColor)
            tvDuration.setTextColor(primaryTextColor)
        }
        if (secondaryTextColor != null) {
            tvLocation.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_ip)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_risk)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_network_type)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_threat_tags)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_source)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_isp)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_org)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_asn)?.setTextColor(secondaryTextColor)
            view.findViewById<TextView>(R.id.tv_label_duration)?.setTextColor(secondaryTextColor)
        }

        val primaryColor = activity.getColorAttr(R.attr.colorPrimary)
        val primaryStateList = ColorStateList.valueOf(primaryColor)

        btnCopyIp.setTextColor(primaryStateList)
        btnCopyIp.strokeColor = primaryStateList
        btnCopyIp.iconTint = primaryStateList

        btnRetest.setTextColor(primaryStateList)
        btnRetest.strokeColor = primaryStateList

        btnDismiss.backgroundTintList = primaryStateList
        btnDismiss.setTextColor(Color.WHITE)

        btnCopyIp.setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("IP", info.ip)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, activity.getString(R.string.copy_success), Toast.LENGTH_SHORT).show()
        }

        btnRetest.setOnClickListener {
            dialog.dismiss()
            onRefresh()
        }

        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
