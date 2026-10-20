package io.nekohasekai.sagernet.utils

import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.USER_AGENT
import io.nekohasekai.sagernet.ktx.tryProxyOutbound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import libcore.Libcore
import moe.matsuri.nb4a.utils.Util
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ServiceCategory(val label: String) {
    ALL("全部"),
    AI("AI 工具"),
    STREAMING("流媒体"),
    COMMON("常用服务")
}

enum class ServiceTestState {
    IDLE,
    TESTING,
    AVAILABLE,
    UNAVAILABLE
}

data class ServiceStatusItem(
    val id: String,
    val name: String,
    val category: ServiceCategory,
    val iconRes: Int,
    val testUrl: String,
    var state: ServiceTestState = ServiceTestState.IDLE,
    var latencyMs: Long = 0L,
    var outboundIp: String = "",
    var countryFlag: String = "",
    var subtitle: String = "-",
    var testTimestamp: String = "-",
)

object ServiceDiagnosticsManager {

    private val concurrencySemaphore = Semaphore(6) // 最大并发数为 6
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun createInitialList(): List<ServiceStatusItem> {
        return listOf(
            // 常用服务
            ServiceStatusItem(
                id = "google",
                name = "Google",
                category = ServiceCategory.COMMON,
                iconRes = R.drawable.ic_baseline_search_24,
                testUrl = "https://www.google.com/generate_204"
            ),
            ServiceStatusItem(
                id = "github",
                name = "GitHub",
                category = ServiceCategory.COMMON,
                iconRes = R.drawable.ic_device_developer_mode,
                testUrl = "https://github.com/"
            ),
            // AI 工具
            ServiceStatusItem(
                id = "chatgpt",
                name = "ChatGPT",
                category = ServiceCategory.AI,
                iconRes = R.drawable.ic_platform_chatgpt,
                testUrl = "https://chatgpt.com/cdn-cgi/trace"
            ),
            ServiceStatusItem(
                id = "claude",
                name = "Claude",
                category = ServiceCategory.AI,
                iconRes = R.drawable.ic_platform_claude,
                testUrl = "https://claude.ai/cdn-cgi/trace"
            ),
            ServiceStatusItem(
                id = "gemini",
                name = "Gemini",
                category = ServiceCategory.AI,
                iconRes = R.drawable.ic_platform_gemini,
                testUrl = "https://gemini.google.com/"
            ),
            // 流媒体
            ServiceStatusItem(
                id = "youtube",
                name = "YouTube",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_platform_youtube,
                testUrl = "https://www.youtube.com/generate_204"
            ),
            ServiceStatusItem(
                id = "netflix",
                name = "Netflix",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_platform_netflix,
                testUrl = "https://www.netflix.com/title/80018499"
            ),
            ServiceStatusItem(
                id = "disney",
                name = "Disney+",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_platform_disney,
                testUrl = "https://www.disneyplus.com/"
            ),
            ServiceStatusItem(
                id = "prime",
                name = "Prime Video",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_platform_prime,
                testUrl = "https://www.primevideo.com/"
            ),
            ServiceStatusItem(
                id = "spotify",
                name = "Spotify",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_platform_spotify,
                testUrl = "https://www.spotify.com/"
            ),
            ServiceStatusItem(
                id = "bilibili",
                name = "哔哩哔哩",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_baseline_stream_24,
                testUrl = "https://www.bilibili.com/"
            ),
            // 更多常用服务
            ServiceStatusItem(
                id = "tiktok",
                name = "TikTok",
                category = ServiceCategory.COMMON,
                iconRes = R.drawable.ic_platform_tiktok,
                testUrl = "https://www.tiktok.com/"
            ),
            ServiceStatusItem(
                id = "discord",
                name = "Discord",
                category = ServiceCategory.COMMON,
                iconRes = R.drawable.ic_social_emoji_symbols,
                testUrl = "https://discord.com/"
            )
        )
    }

    private fun createHttpClient(): libcore.HTTPClient {
        return Libcore.newHttpClient().apply {
            modernTLS()
            val mixedPort = DataStore.mixedPort
            if (mixedPort > 0) {
                trySocks5(mixedPort.toInt(), "", "")
            } else {
                tryProxyOutbound()
            }
        }
    }

    suspend fun testService(
        item: ServiceStatusItem,
        onUpdate: ((ServiceStatusItem) -> Unit)? = null,
    ): ServiceStatusItem = withContext(Dispatchers.IO) {
        if (!DataStore.serviceState.connected) {
            item.state = ServiceTestState.UNAVAILABLE
            item.subtitle = "-"
            item.latencyMs = 0L
            item.testTimestamp = timeFormat.format(Date())
            onUpdate?.invoke(item)
            return@withContext item
        }

        item.state = ServiceTestState.TESTING
        item.subtitle = "检测中..."
        onUpdate?.invoke(item)

        concurrencySemaphore.withPermit {
            val startTime = System.currentTimeMillis()
            val ua = USER_AGENT.takeIf { it.isNotBlank() }
                ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

            val success = withTimeoutOrNull(4000L) {
                var client: libcore.HTTPClient? = null
                try {
                    client = createHttpClient()
                    val req = client.newRequest().apply {
                        setURL(item.testUrl)
                        setUserAgent(ua)
                    }
                    val resp = req.execute()
                    val cost = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                    val body = runCatching { Util.getStringBox(resp.contentString) }.getOrDefault("")

                    // 检查 Cloudflare trace 端点
                    var parsedIp = ""
                    var parsedLoc = ""
                    if (item.testUrl.contains("cdn-cgi/trace")) {
                        for (line in body.lines()) {
                            val trimmed = line.trim()
                            if (trimmed.startsWith("ip=")) parsedIp = trimmed.substring(3).trim()
                            else if (trimmed.startsWith("loc=")) parsedLoc = trimmed.substring(4).trim().uppercase()
                        }
                    }

                    val cachedInfo = LandingIpManager.getCachedInfo()
                    val ip = parsedIp.ifBlank { cachedInfo?.ip.orEmpty() }
                    val flag = if (parsedLoc.isNotBlank()) {
                        LandingIpManager.countryCodeToFlagEmoji(parsedLoc)
                    } else {
                        cachedInfo?.countryFlag ?: "🌐"
                    }

                    item.latencyMs = cost
                    item.outboundIp = ip
                    item.countryFlag = flag
                    item.state = ServiceTestState.AVAILABLE
                    item.subtitle = if (ip.isNotBlank()) "$flag $ip".trim() else "可用"
                    true
                } catch (_: Throwable) {
                    false
                } finally {
                    runCatching { client?.close() }
                }
            } ?: false

            if (!success) {
                item.state = ServiceTestState.UNAVAILABLE
                item.subtitle = "-"
                item.latencyMs = 0L
            }
            item.testTimestamp = timeFormat.format(Date())
            onUpdate?.invoke(item)
            item
        }
    }
}
