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
    var statusSummary: String = "未测试",
    var detailMessage: String = "",
)

object ServiceDiagnosticsManager {

    private val concurrencySemaphore = Semaphore(4) // 控制最大并发数为 4

    fun createInitialList(): List<ServiceStatusItem> {
        return listOf(
            // AI 工具
            ServiceStatusItem(
                id = "chatgpt",
                name = "ChatGPT",
                category = ServiceCategory.AI,
                iconRes = R.drawable.ic_platform_chatgpt,
                testUrl = "https://chatgpt.com/"
            ),
            ServiceStatusItem(
                id = "claude",
                name = "Claude",
                category = ServiceCategory.AI,
                iconRes = R.drawable.ic_platform_claude,
                testUrl = "https://claude.ai/login"
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
                testUrl = "https://www.youtube.com/premium"
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
                testUrl = "https://api.bilibili.com/x/web-interface/nav"
            ),
            // 常用与开发
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
                testUrl = "https://api.github.com/zen"
            ),
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
                testUrl = "https://discord.com/api/v9/experiments"
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
            item.statusSummary = "未连接代理"
            item.detailMessage = "请先开启连接"
            item.latencyMs = 0L
            onUpdate?.invoke(item)
            return@withContext item
        }

        item.state = ServiceTestState.TESTING
        item.statusSummary = "检测中..."
        item.detailMessage = "正在建立 TLS 握手..."
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
                    val body = runCatching { Util.getStringBox(resp.contentString) }.getOrDefault("")
                    val cost = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                    item.latencyMs = cost

                    when (item.id) {
                        "chatgpt" -> {
                            val lower = body.lowercase()
                            if (lower.contains("cf-mitigated") || lower.contains("attention required") || lower.contains("1020")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "CF 盾牌拦截"
                                item.detailMessage = "触发 Cloudflare 验证或 OpenAI IP 限制"
                            } else if (lower.contains("country not supported") || lower.contains("unavailable in your country")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "地区未开放"
                                item.detailMessage = "当前节点机房被列为未开放服务区"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "正常可用"
                                item.detailMessage = "握手耗时 ${cost}ms · 认证网关畅通"
                            }
                        }
                        "claude" -> {
                            val lower = body.lowercase()
                            if (lower.contains("app unavailable in your region") || lower.contains("403 forbidden")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "地区未授权"
                                item.detailMessage = "当前节点所在地区尚未开通 Claude"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "正常可用"
                                item.detailMessage = "握手耗时 ${cost}ms · 区域授权正常"
                            }
                        }
                        "gemini" -> {
                            val lower = body.lowercase()
                            if (lower.contains("not supported in your country") || lower.contains("unavailable in your territory")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "地区未开放"
                                item.detailMessage = "Google Gemini 暂未对该机房 IP 开放"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "正常可用"
                                item.detailMessage = "握手耗时 ${cost}ms · 模型服务畅通"
                            }
                        }
                        "youtube" -> {
                            val matcher = Pattern.compile("\"countryCode\"\\s*:\\s*\"([A-Za-z]{2})\"").matcher(body)
                            val countryCode = if (matcher.find()) matcher.group(1)?.uppercase().orEmpty() else ""
                            val flag = if (countryCode.isNotBlank()) LandingIpManager.countryCodeToFlagEmoji(countryCode) else ""
                            if (body.contains("Premium is not available in your country")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "地区未开通"
                                item.detailMessage = "YouTube Premium 在当前地区未开放"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = if (flag.isNotBlank()) "已解锁 $flag $countryCode" else "正常可用"
                                item.detailMessage = "握手耗时 ${cost}ms · 视频与 Premium 正常"
                            }
                        }
                        "netflix" -> {
                            if (body.contains("title/80018499") || body.contains("watch") || body.contains("House of Cards")) {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "原生全解锁"
                                item.detailMessage = "握手耗时 ${cost}ms · 完整支持非自制版权剧"
                            } else {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "仅限自制剧"
                                item.detailMessage = "非自制剧受限，仅可观看自制剧"
                            }
                        }
                        "disney" -> {
                            val lower = body.lowercase()
                            if (lower.contains("not available in your region") || lower.contains("restricted") || lower.contains("unavailable")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "地区限制"
                                item.detailMessage = "当前地区不在 Disney+ 官方服务范围内"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "原生支持"
                                item.detailMessage = "握手耗时 ${cost}ms · 流媒体已授权"
                            }
                        }
                        "prime" -> {
                            val lower = body.lowercase()
                            if (lower.contains("georestricted") || lower.contains("not-available-in-your-country")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "地区限制"
                                item.detailMessage = "当前节点受 Prime Video 地域限制"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "正常可用"
                                item.detailMessage = "握手耗时 ${cost}ms · 流媒体版权库畅通"
                            }
                        }
                        "spotify" -> {
                            val lower = body.lowercase()
                            if (lower.contains("not available in your country")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "未开放"
                                item.detailMessage = "出口 IP 所在地区暂未开放 Spotify"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "正常可用"
                                item.detailMessage = "握手耗时 ${cost}ms · 音乐流媒体正常"
                            }
                        }
                        "tiktok" -> {
                            if (body.contains("tiktok-verify-page")) {
                                item.state = ServiceTestState.RESTRICTED
                                item.statusSummary = "风控拦截"
                                item.detailMessage = "触发 TikTok 验证码"
                            } else {
                                item.state = ServiceTestState.AVAILABLE
                                item.statusSummary = "正常可用"
                                item.detailMessage = "握手耗时 ${cost}ms · 短视频接入正常"
                            }
                        }
                        "bilibili" -> {
                            item.state = ServiceTestState.AVAILABLE
                            item.statusSummary = "正常连通"
                            item.detailMessage = "握手耗时 ${cost}ms · 接口响应正常"
                        }
                        else -> {
                            item.state = ServiceTestState.AVAILABLE
                            item.statusSummary = "正常可用"
                            item.detailMessage = "握手耗时 ${cost}ms · 服务连通正常"
                        }
                    }
                    true
                } catch (e: Throwable) {
                    val msg = e.message.orEmpty()
                    val cost = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
                    item.latencyMs = cost
                    if (msg.contains("403") || msg.contains("cf-mitigated") || msg.contains("Attention Required") || msg.contains("restricted")) {
                        item.state = ServiceTestState.RESTRICTED
                        item.statusSummary = "访问受限"
                        item.detailMessage = "HTTP 403 权限阻断或人机验证"
                        true
                    } else {
                        false
                    }
                } finally {
                    runCatching { client?.close() }
                }
            }

            if (success != true) {
                item.state = ServiceTestState.UNAVAILABLE
                item.statusSummary = "连接超时"
                item.detailMessage = "超过 4000ms 未响应"
                item.latencyMs = 0L
            }

            onUpdate?.invoke(item)
            item
        }
    }
}
