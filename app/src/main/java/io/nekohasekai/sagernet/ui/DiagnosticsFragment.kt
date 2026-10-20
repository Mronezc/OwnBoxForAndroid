package io.nekohasekai.sagernet.ui

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.ChipGroup
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.utils.LandingIpInfo
import io.nekohasekai.sagernet.utils.LandingIpManager
import io.nekohasekai.sagernet.utils.ServiceCategory
import io.nekohasekai.sagernet.utils.ServiceDiagnosticsManager
import io.nekohasekai.sagernet.utils.ServiceStatusItem
import io.nekohasekai.sagernet.utils.Theme
import io.nekohasekai.sagernet.widget.LandingIpBottomSheet
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DiagnosticsFragment : ToolbarFragment(R.layout.layout_diagnostics) {

    private lateinit var tvOverviewFlag: TextView
    private lateinit var tvOverviewIp: TextView
    private lateinit var tvOverviewIpVersion: TextView
    private lateinit var tvOverviewLocation: TextView
    private lateinit var tvOverviewNetworkType: TextView
    private lateinit var tvOverviewRiskBadge: TextView
    private lateinit var cardIpOverview: MaterialCardView
    private lateinit var btnOverviewFullDetails: MaterialButton
    private lateinit var btnOverviewRefreshIp: MaterialButton
    private lateinit var btnRetestAll: MaterialButton
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var recyclerServices: RecyclerView

    private lateinit var adapter: ServiceDiagnosticsAdapter
    private val serviceList = mutableListOf<ServiceStatusItem>()
    private var testAllJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isToolbarInitialized) {
            toolbar.setTitle(R.string.diagnostics_title)
        }

        tvOverviewFlag = view.findViewById(R.id.tv_overview_flag)
        tvOverviewIp = view.findViewById(R.id.tv_overview_ip)
        tvOverviewIpVersion = view.findViewById(R.id.tv_overview_ip_version)
        tvOverviewLocation = view.findViewById(R.id.tv_overview_location)
        tvOverviewNetworkType = view.findViewById(R.id.tv_overview_network_type)
        tvOverviewRiskBadge = view.findViewById(R.id.tv_overview_risk_badge)
        cardIpOverview = view.findViewById(R.id.card_ip_overview)
        btnOverviewFullDetails = view.findViewById(R.id.btn_overview_full_details)
        btnOverviewRefreshIp = view.findViewById(R.id.btn_overview_refresh_ip)
        btnRetestAll = view.findViewById(R.id.btn_retest_all)
        chipGroupCategory = view.findViewById(R.id.chip_group_category)
        recyclerServices = view.findViewById(R.id.recycler_services)

        // Theme adjustments
        val isNight = Theme.usingNightMode(requireContext())
        val isWhite = Theme.isWhiteTheme(requireContext())
        val cardBg = when {
            isNight -> Color.parseColor("#1E1E1E")
            isWhite -> Color.parseColor("#F5F6F8")
            else -> null
        }
        val strokeColor = when {
            isNight -> Color.parseColor("#333333")
            isWhite -> Color.parseColor("#E0E0E0")
            else -> null
        }
        if (cardBg != null) {
            cardIpOverview.setCardBackgroundColor(cardBg)
        }
        if (strokeColor != null) {
            cardIpOverview.strokeColor = strokeColor
        }

        // Initialize Services
        serviceList.clear()
        serviceList.addAll(ServiceDiagnosticsManager.createInitialList())

        adapter = ServiceDiagnosticsAdapter(serviceList) { item ->
            testSingleService(item)
        }
        recyclerServices.layoutManager = LinearLayoutManager(requireContext())
        recyclerServices.adapter = adapter

        // Setup Categories
        chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chip_ai -> adapter.filterByCategory(ServiceCategory.AI)
                R.id.chip_streaming -> adapter.filterByCategory(ServiceCategory.STREAMING)
                R.id.chip_common -> adapter.filterByCategory(ServiceCategory.COMMON)
                else -> adapter.filterByCategory(ServiceCategory.ALL)
            }
        }

        // IP Overview click listeners
        btnOverviewFullDetails.setOnClickListener {
            showFullIpDetails()
        }
        cardIpOverview.setOnClickListener {
            showFullIpDetails()
        }
        btnOverviewRefreshIp.setOnClickListener {
            refreshIpOverview(force = true)
        }

        // Retest All
        btnRetestAll.setOnClickListener {
            startTestAllServices()
        }

        // Initial Load
        loadIpOverview()
        startTestAllServices()
    }

    private fun loadIpOverview() {
        val cached = LandingIpManager.getCachedInfo()
        if (cached != null) {
            bindIpInfo(cached)
        } else {
            refreshIpOverview(force = false)
        }
    }

    private fun refreshIpOverview(force: Boolean) {
        if (!DataStore.serviceState.connected) {
            tvOverviewFlag.text = "🌐"
            tvOverviewIp.text = getString(R.string.landing_ip_failed)
            tvOverviewLocation.text = getString(R.string.diagnostics_not_connected)
            tvOverviewNetworkType.text = ""
            tvOverviewRiskBadge.visibility = View.GONE
            return
        }

        tvOverviewIp.text = getString(R.string.landing_ip_querying)
        tvOverviewLocation.text = "正在检测当前代理出站链路..."
        tvOverviewRiskBadge.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val result = LandingIpManager.queryLandingIp(DataStore.selectedProxy, forceRefresh = force)
            result.onSuccess { info ->
                bindIpInfo(info)
            }.onFailure { err ->
                tvOverviewFlag.text = "⚠️"
                tvOverviewIp.text = "检测失败"
                tvOverviewLocation.text = err.message ?: "无法获取当前出口 IP"
                tvOverviewNetworkType.text = ""
                tvOverviewRiskBadge.visibility = View.GONE
            }
        }
    }

    private fun bindIpInfo(info: LandingIpInfo) {
        tvOverviewFlag.text = info.countryFlag
        tvOverviewIp.text = info.ip
        tvOverviewIpVersion.text = info.ipVersion
        tvOverviewLocation.text = "${info.locationText} · ${info.asn}".trim()
        tvOverviewNetworkType.text = info.networkType

        val density = resources.displayMetrics.density
        val riskBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 4f * density
            setColor(Color.parseColor(info.riskLevel.colorHex))
        }
        tvOverviewRiskBadge.text = "${info.riskLevel.label} (${info.riskScore}%)"
        tvOverviewRiskBadge.background = riskBg
        tvOverviewRiskBadge.visibility = View.VISIBLE
    }

    private fun showFullIpDetails() {
        val cached = LandingIpManager.getCachedInfo()
        val act = activity ?: return
        if (cached != null) {
            LandingIpBottomSheet.show(act, cached) {
                refreshIpOverview(force = true)
            }
        } else {
            refreshIpOverview(force = true)
            Toast.makeText(requireContext(), getString(R.string.landing_ip_querying), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startTestAllServices() {
        if (!DataStore.serviceState.connected) {
            Toast.makeText(requireContext(), getString(R.string.diagnostics_not_connected), Toast.LENGTH_SHORT).show()
            return
        }

        testAllJob?.cancel()
        testAllJob = viewLifecycleOwner.lifecycleScope.launch {
            btnRetestAll.isEnabled = false
            for (item in serviceList) {
                launch {
                    ServiceDiagnosticsManager.testService(item) { updated ->
                        activity?.runOnUiThread {
                            adapter.updateItem(updated)
                        }
                    }
                }
            }
            btnRetestAll.isEnabled = true
        }
    }

    private fun testSingleService(item: ServiceStatusItem) {
        if (!DataStore.serviceState.connected) {
            Toast.makeText(requireContext(), getString(R.string.diagnostics_not_connected), Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            ServiceDiagnosticsManager.testService(item) { updated ->
                activity?.runOnUiThread {
                    adapter.updateItem(updated)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        testAllJob?.cancel()
    }
}
