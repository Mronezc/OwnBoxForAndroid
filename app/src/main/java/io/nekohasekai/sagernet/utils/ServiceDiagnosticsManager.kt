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
import java.util.regex.Pattern

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
    RESTRICTED,
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

    private val claudeBlockedRegions = setOf("AF", "BY", "CN", "CU", "HK", "IR", "KP", "MO", "RU", "SY")
    private val geminiBlockedRegions = setOf("CHN", "RUS", "BLR", "CUB", "IRN", "PRK", "SYR", "HKG", "MAC")
    private val youtubeRegionPattern = Pattern.compile("(?i)(?:\"countryCode\"\\s*:\\s*\"|\"GL\"\\s*:\\s*\")([A-Za-z]{2})\"")
    private val tikTokRegionPattern = Pattern.compile("\"webapp\\.app-context\"\\s*:\\s*\\{[^{}]*?\"region\"\\s*:\\s*\"([^\"]*)\"")
    private val discordCountryPattern = Pattern.compile("\"country_code\"\\s*:\\s*\"([^\"]+)\"")

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
                testUrl = "https://ios.chat.openai.com/"
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
                testUrl = "https://www.youtube.com/premium?hl=en"
            ),
            ServiceStatusItem(
                id = "netflix",
                name = "Netflix",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_platform_netflix,
                testUrl = "https://www.netflix.com/title/81280792"
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
                testUrl = "https://www.primevideo.com"
            ),
            ServiceStatusItem(
                id = "spotify",
                name = "Spotify",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_platform_spotify,
                testUrl = "https://www.spotify.com/api/content/v1/country-selector?platform=web&format=json"
            ),
            ServiceStatusItem(
                id = "bilibili",
                name = "哔哩哔哩",
                category = ServiceCategory.STREAMING,
                iconRes = R.drawable.ic_baseline_stream_24,
                testUrl = "https://api.bilibili.com/pgc/player/web/playurl?avid=82846771&qn=0&type=&otype=json&ep_id=307247&fourk=1&fnver=0&fnval=16&module=bangumi"
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
                testUrl = "https://discord.com/api/v9/auth/location-metadata"
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

    private fun doRequest(url: String, ua: String): String? {
        var client: libcore.HTTPClient? = null
        return try {
            client = createHttpClient()
            val req = client.newRequest().apply {
                setURL(url)
                setUserAgent(ua)
            }
            val resp = req.execute()
            Util.getStringBox(resp.contentString)
        } catch (_: Throwable) {
            null
        } finally {
            runCatching { client?.close() }
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

            val success = withTimeoutOrNull(5000L) {
                var detectedRegion = ""
                var detectedState = ServiceTestState.AVAILABLE

                when (item.id) {
                    "google" -> {
                        val body = doRequest(item.testUrl, ua)
                        if (body == null) {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "github" -> {
                        val body = doRequest(item.testUrl, ua)
                        if (body == null) {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "chatgpt" -> {
                        // 对齐 FlClash checkChatGpt:
                        // 1. 请求 ios.chat.openai.com
                        val ios = doRequest("https://ios.chat.openai.com/", ua)
                        if (ios != null) {
                            val lower = ios.lowercase()
                            when {
                                lower.contains("disallowed isp") -> detectedState = ServiceTestState.RESTRICTED
                                lower.contains("sorry, you have been blocked") -> detectedState = ServiceTestState.UNAVAILABLE
                                lower.contains("request is not allowed") -> detectedState = ServiceTestState.AVAILABLE
                                else -> detectedState = ServiceTestState.AVAILABLE
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }

                        // 2. 请求 cdn-cgi/trace 提取地区
                        val trace = doRequest("https://chat.openai.com/cdn-cgi/trace", ua)
                        if (trace != null) {
                            for (line in trace.lines()) {
                                if (line.trim().startsWith("loc=")) {
                                    detectedRegion = line.trim().substring(4).trim().uppercase()
                                }
                            }
                        }

                        // 3. 校验 compliance
                        val web = doRequest("https://api.openai.com/compliance/cookie_requirements", ua)
                        if (web != null && web.contains("unsupported_country")) {
                            detectedState = ServiceTestState.RESTRICTED
                        }
                    }

                    "claude" -> {
                        // 对齐 FlClash checkClaude
                        val trace = doRequest("https://claude.ai/cdn-cgi/trace", ua)
                        if (trace != null) {
                            var loc = ""
                            for (line in trace.lines()) {
                                if (line.trim().startsWith("loc=")) {
                                    loc = line.trim().substring(4).trim().uppercase()
                                }
                            }
                            detectedRegion = loc
                            if (loc.isBlank()) {
                                detectedState = ServiceTestState.UNAVAILABLE
                            } else if (claudeBlockedRegions.contains(loc)) {
                                detectedState = ServiceTestState.RESTRICTED
                            } else {
                                detectedState = ServiceTestState.AVAILABLE
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "gemini" -> {
                        // 对齐 FlClash checkGemini: 扫描区域标记 ,2,1,200,"XXX"
                        val resp = doRequest("https://gemini.google.com/", ua)
                        if (resp != null) {
                            val marker = ",2,1,200,\""
                            val idx = resp.indexOf(marker)
                            if (idx >= 0 && idx + marker.length + 3 <= resp.length) {
                                val code = resp.substring(idx + marker.length, idx + marker.length + 3).uppercase()
                                if (geminiBlockedRegions.contains(code)) {
                                    detectedState = ServiceTestState.RESTRICTED
                                } else {
                                    detectedState = ServiceTestState.AVAILABLE
                                }
                            } else {
                                detectedState = ServiceTestState.AVAILABLE
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "youtube" -> {
                        // 对齐 FlClash checkYouTubePremium
                        val resp = doRequest("https://www.youtube.com/premium?hl=en", ua)
                        if (resp != null) {
                            val matcher = youtubeRegionPattern.matcher(resp)
                            if (matcher.find()) {
                                detectedRegion = matcher.group(1)?.uppercase().orEmpty()
                            }
                            val lower = resp.lowercase()
                            if (lower.contains("premium is not available in your country") ||
                                lower.contains("premium is not available in your region")
                            ) {
                                detectedState = ServiceTestState.RESTRICTED
                            } else {
                                detectedState = ServiceTestState.AVAILABLE
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "netflix" -> {
                        // 对齐 FlClash checkNetflix
                        val first = doRequest("https://www.netflix.com/title/81280792", ua)
                        val second = doRequest("https://www.netflix.com/title/70143836", ua)
                        if (first != null || second != null) {
                            detectedState = ServiceTestState.AVAILABLE
                        } else {
                            val fallback = doRequest("https://www.netflix.com/title/80018499", ua)
                            detectedState = if (fallback != null) ServiceTestState.AVAILABLE else ServiceTestState.UNAVAILABLE
                        }
                    }

                    "disney" -> {
                        val resp = doRequest(item.testUrl, ua)
                        if (resp == null) {
                            detectedState = ServiceTestState.UNAVAILABLE
                        } else {
                            detectedState = ServiceTestState.AVAILABLE
                        }
                    }

                    "prime" -> {
                        val resp = doRequest(item.testUrl, ua)
                        if (resp != null) {
                            if (resp.contains("isServiceRestricted")) {
                                detectedState = ServiceTestState.RESTRICTED
                            } else {
                                detectedState = ServiceTestState.AVAILABLE
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "spotify" -> {
                        val resp = doRequest(item.testUrl, ua)
                        if (resp != null) {
                            if (resp.contains("not available in your country")) {
                                detectedState = ServiceTestState.RESTRICTED
                            } else {
                                detectedState = ServiceTestState.AVAILABLE
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "tiktok" -> {
                        val resp = doRequest(item.testUrl, ua)
                        if (resp != null) {
                            val lower = resp.lowercase()
                            if (lower.contains("access denied") || lower.contains("not available in your region")) {
                                detectedState = ServiceTestState.RESTRICTED
                            } else {
                                detectedState = ServiceTestState.AVAILABLE
                                val matcher = tikTokRegionPattern.matcher(resp)
                                if (matcher.find()) {
                                    detectedRegion = matcher.group(1)?.uppercase().orEmpty()
                                }
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "discord" -> {
                        val resp = doRequest(item.testUrl, ua)
                        if (resp != null) {
                            detectedState = ServiceTestState.AVAILABLE
                            val matcher = discordCountryPattern.matcher(resp)
                            if (matcher.find()) {
                                detectedRegion = matcher.group(1)?.uppercase().orEmpty()
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }

                    "bilibili" -> {
                        val main = doRequest(item.testUrl, ua)
                        if (main != null && main.contains("\"code\":0")) {
                            detectedState = ServiceTestState.AVAILABLE
                            detectedRegion = "CN"
                        } else if (main != null && main.contains("-10403")) {
                            val hk = doRequest("https://api.bilibili.com/pgc/player/web/playurl?avid=18281381&cid=29892777&qn=0&type=&otype=json&ep_id=183799&fourk=1&fnver=0&fnval=16&module=bangumi", ua)
                            if (hk != null && hk.contains("\"code\":0")) {
                                detectedState = ServiceTestState.AVAILABLE
                                detectedRegion = "HK"
                            } else {
                                detectedState = ServiceTestState.RESTRICTED
                            }
                        } else {
                            detectedState = ServiceTestState.UNAVAILABLE
                        }
                    }
                }

                val cost = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                val cachedInfo = LandingIpManager.getCachedInfo()
                val ip = cachedInfo?.ip.orEmpty()
                val flag = if (detectedRegion.isNotBlank()) {
                    LandingIpManager.countryCodeToFlagEmoji(detectedRegion)
                } else {
                    cachedInfo?.countryFlag ?: "🌐"
                }

                item.latencyMs = cost
                item.outboundIp = ip
                item.countryFlag = flag
                item.state = detectedState
                item.subtitle = if (detectedState != ServiceTestState.UNAVAILABLE) {
                    if (ip.isNotBlank()) "$flag $ip".trim() else "$flag 可用"
                } else {
                    "-"
                }
                true
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
