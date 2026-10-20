package io.nekohasekai.sagernet.utils

import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.ktx.USER_AGENT
import io.nekohasekai.sagernet.ktx.tryProxyOutbound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import libcore.Libcore
import moe.matsuri.nb4a.utils.Util
import org.json.JSONObject
import java.util.Locale

enum class IpRiskLevel(val label: String, val colorHex: String) {
    CLEAN("极低风险", "#10B981"),      // 绿色
    LOW("低风险", "#3B82F6"),         // 蓝色
    MEDIUM("中度风险", "#F59E0B"),      // 橙黄色
    HIGH("高风险", "#EF4444")          // 红色
}

data class LandingIpInfo(
    val ip: String,
    val country: String,
    val countryCode: String,
    val countryFlag: String,
    val city: String,
    val region: String,
    val isp: String,
    val org: String,
    val asn: String,
    val durationMs: Long,
    val queryTimestamp: Long = System.currentTimeMillis(),
    val ipVersion: String = if (ip.contains(":")) "IPv6" else "IPv4",
    val levelLabel: String = "普通",
    val typeLabel: String = "机房",
    val hitTag: String = "无",
    val source: String = "ipquery.io",
    val riskLevel: IpRiskLevel = IpRiskLevel.LOW,
    val riskScore: Int = 20,
    val networkType: String = typeLabel,
    val threatTags: List<String> = if (hitTag == "无") listOf(typeLabel) else listOf(hitTag),
    val isProxy: Boolean = false,
    val isVpn: Boolean = false,
    val isTor: Boolean = false,
    val isHosting: Boolean = true,
) {
    fun getMaskedIp(): String {
        return if (ip.contains(":")) {
            val parts = ip.split(":")
            if (parts.size >= 2) "${parts[0]}:${parts[1]}:****:****" else ip
        } else {
            val parts = ip.split(".")
            if (parts.size == 4) "${parts[0]}.${parts[1]}.*.*" else ip
        }
    }

    val briefText: String
        get() = if (durationMs > 0) {
            "$countryFlag $countryCode $ip · HTTP 握手 ${durationMs} 毫秒".trim()
        } else {
            "$countryFlag $countryCode $ip".trim()
        }

    val locationText: String
        get() {
            val parts = mutableListOf<String>()
            if (country.isNotBlank()) parts.add(country)
            if (city.isNotBlank() && city != country) parts.add(city)
            val base = parts.joinToString(" · ")
            return if (countryCode.isNotBlank()) "$countryFlag $base ($countryCode)" else "$countryFlag $base"
        }
}

object LandingIpManager {

    private const val CACHE_TTL_MS = 60_000L // 60 秒自动过期

    @Volatile
    private var currentCache: LandingIpInfo? = null

    @Volatile
    var cachedProfileId: Long = -1L
        private set

    @Volatile
    private var isQuerying: Boolean = false

    fun clearCache() {
        currentCache = null
        cachedProfileId = -1L
    }

    fun getCachedInfo(): LandingIpInfo? = currentCache

    fun isCurrentlyQuerying(): Boolean = isQuerying

    fun updateCachedDuration(duration: Long) {
        currentCache = currentCache?.copy(durationMs = duration)
    }

    fun countryCodeToFlagEmoji(countryCode: String?): String {
        if (countryCode == null || countryCode.length != 2) return "🌐"
        val code = countryCode.uppercase()
        if (!code[0].isLetter() || !code[1].isLetter()) return "🌐"
        val firstChar = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    private fun localizeCountry(countryCode: String, fallbackName: String): String {
        if (countryCode.length != 2) return fallbackName
        return runCatching {
            Locale("", countryCode.uppercase()).getDisplayCountry(Locale.getDefault()).takeIf { it.isNotBlank() }
        }.getOrNull() ?: fallbackName
    }

    fun parseProfileRegion(name: String): Pair<String, String> {
        if (name.isBlank()) return Pair("🌐", "节点出口")
        for (i in 0 until name.length - 1) {
            val cp1 = name.codePointAt(i)
            if (cp1 in 0x1F1E6..0x1F1FF) {
                val charCount1 = Character.charCount(cp1)
                if (i + charCount1 < name.length) {
                    val cp2 = name.codePointAt(i + charCount1)
                    if (cp2 in 0x1F1E6..0x1F1FF) {
                        val flag = String(Character.toChars(cp1)) + String(Character.toChars(cp2))
                        val c1 = (cp1 - 0x1F1E6 + 'A'.code).toChar()
                        val c2 = (cp2 - 0x1F1E6 + 'A'.code).toChar()
                        val code = "$c1$c2"
                        return Pair(flag, code)
                    }
                }
            }
        }
        val lower = name.lowercase()
        return when {
            lower.contains("香港") || lower.contains("hk") || lower.contains("hong kong") -> Pair("🇭🇰", "HK")
            lower.contains("日本") || lower.contains("jp") || lower.contains("japan") || lower.contains("东京") || lower.contains("大阪") -> Pair("🇯🇵", "JP")
            lower.contains("美国") || lower.contains("us") || lower.contains("united states") || lower.contains("洛杉矶") || lower.contains("硅谷") -> Pair("🇺🇸", "US")
            lower.contains("台湾") || lower.contains("tw") || lower.contains("taiwan") || lower.contains("台北") -> Pair("🇹🇼", "TW")
            lower.contains("新加坡") || lower.contains("sg") || lower.contains("singapore") || lower.contains("狮城") -> Pair("🇸🇬", "SG")
            lower.contains("韩国") || lower.contains("kr") || lower.contains("korea") || lower.contains("首尔") -> Pair("🇰🇷", "KR")
            lower.contains("英国") || lower.contains("uk") || lower.contains("gb") || lower.contains("united kingdom") || lower.contains("伦敦") -> Pair("🇬🇧", "GB")
            lower.contains("德国") || lower.contains("de") || lower.contains("germany") || lower.contains("法兰克福") -> Pair("🇩🇪", "DE")
            lower.contains("法国") || lower.contains("fr") || lower.contains("france") || lower.contains("巴黎") -> Pair("🇫🇷", "FR")
            lower.contains("加拿大") || lower.contains("ca") || lower.contains("canada") -> Pair("🇨🇦", "CA")
            lower.contains("澳大利亚") || lower.contains("au") || lower.contains("australia") || lower.contains("悉尼") -> Pair("🇦🇺", "AU")
            lower.contains("俄罗斯") || lower.contains("ru") || lower.contains("russia") || lower.contains("莫斯科") -> Pair("🇷🇺", "RU")
            lower.contains("土耳其") || lower.contains("tr") || lower.contains("turkey") -> Pair("🇹🇷", "TR")
            lower.contains("阿根廷") || lower.contains("ar") || lower.contains("argentina") -> Pair("🇦🇷", "AR")
            lower.contains("印度") || lower.contains("in") || lower.contains("india") -> Pair("🇮🇳", "IN")
            lower.contains("中国") || lower.contains("cn") || lower.contains("china") -> Pair("🇨🇳", "CN")
            else -> Pair("🌐", "节点出口")
        }
    }

    fun evaluateRiskAndType(
        isHosting: Boolean,
        isProxy: Boolean,
        isVpn: Boolean,
        isTor: Boolean,
        isMobile: Boolean,
        ispOrOrg: String,
        ip: String = "",
    ): Triple<IpRiskLevel, Int, Pair<String, List<String>>> {
        val tags = mutableListOf<String>()
        val lower = "$ispOrOrg $ip".lowercase()

        val isTorNode = isTor || lower.contains("tor-exit") || lower.contains("tor exit") || lower.contains("onion")
        val isCellular = isMobile || lower.contains("cellular") || lower.contains("wireless") ||
                lower.contains("mobile") || lower.contains("lte") || lower.contains("5g")

        val isVpnDatacenter = listOf(
            "m247", "datacamp", "cdn77", "choopa", "constant", "vultr", "quadranet", "cogent",
            "hostinger", "contabo", "colocrossing", "buyvm", "frantech", "ovh", "leaseweb",
            "psychz", "dedipath", "zenlayer", "sharktech", "linode", "kamatera", "ipvolume",
            "hivelocity", "scaleway", "ionos", "hetzner", "dmit", "bandwagon", "it7", "spartan",
            "v.ps", "misaka", "kurun", "xtom", "cherry servers", "clouvider", "serverius"
        ).any { lower.contains(it) }

        val isPublicCloud = listOf(
            "amazon", "aws", "google", "microsoft", "azure", "oracle", "digitalocean",
            "alibaba", "aliyun", "tencent", "huawei", "ucloud", "baidu cloud", "volcengine"
        ).any { lower.contains(it) }

        val isCdnEdge = listOf(
            "cloudflare", "fastly", "akamai", "edgecast", "limelight", "imperva", "incapsula"
        ).any { lower.contains(it) }

        val isResidentialIsp = listOf(
            "comcast", "at&t", "verizon", "charter", "spectrum", "cox", "lumen", "centurylink",
            "frontier", "xfinity", "optimum", "hkt", "pccw", "hkbn", "hong kong broadband",
            "hinet", "chunghwa", "taiwan mobile", "fareastone", "ntt", "kddi", "softbank",
            "ocn", "iij", "bbtec", "so-net", "singtel", "starhub", "m1 limited", "kt corp",
            "sk broadband", "lg uplus", "telekom", "vodafone", "orange", "british telecommunications",
            "virgin media", "telecom", "unicom"
        ).any { lower.contains(it) }

        val isGenericHosting = isHosting || lower.contains("hosting") || lower.contains("server") ||
                lower.contains("datacenter") || lower.contains("data center") || lower.contains("cloud") ||
                lower.contains("vps")

        val (baseType, baseTags, baseScore) = when {
            isTorNode -> Triple("Tor 洋葱路由网络", listOf("Tor 节点", "洋葱网络", "高匿出口"), 95)
            isCellular -> Triple("移动蜂窝网络 (Cellular)", listOf("移动蜂窝网络", "无线基站"), 12)
            isVpnDatacenter -> Triple("商业机房 / 数据中心 (Datacenter)", listOf("数据中心机房", "高频代理段", "商业托管"), 68)
            isPublicCloud -> Triple("商业机房 / 数据中心 (Datacenter)", listOf("数据中心机房", "公有云商", "服务器托管"), 50)
            isCdnEdge -> Triple("CDN 边缘网络 (Anycast Edge)", listOf("CDN边缘", "Anycast节点"), 28)
            isGenericHosting -> Triple("商业机房 / 数据中心 (Datacenter)", listOf("数据中心机房", "机房托管"), 55)
            isResidentialIsp -> Triple("家庭宽带 / 原生住宅 (Residential)", listOf("原生住宅", "家庭宽带", "干净信誉"), 9)
            else -> Triple("家庭宽带 / 原生住宅 (Residential)", listOf("住宅宽带", "普通用户段"), 18)
        }

        tags.addAll(baseTags)

        var scoreCalc = baseScore
        if (isProxy || isVpn) {
            scoreCalc = (scoreCalc + 28).coerceAtMost(98)
            if (isProxy) tags.add("代理标记")
            if (isVpn) tags.add("VPN 出口")
        }

        val hashVariance = if (ip.isNotBlank()) {
            val hash = kotlin.math.abs(ip.hashCode())
            (hash % 15) - 7
        } else {
            0
        }

        val finalScore = (scoreCalc + hashVariance).coerceIn(1, 99)

        val riskLevel = when {
            finalScore <= 20 -> IpRiskLevel.CLEAN
            finalScore <= 45 -> IpRiskLevel.LOW
            finalScore <= 75 -> IpRiskLevel.MEDIUM
            else -> IpRiskLevel.HIGH
        }

        return Triple(riskLevel, finalScore, Pair(baseType, tags.distinct()))
    }

    fun getProfileFallbackDisplay(profileId: Long): String {
        val profile = runCatching {
            io.nekohasekai.sagernet.database.ProfileManager.getProfile(profileId)
        }.getOrNull()
        val name = profile?.displayName().orEmpty()
        val (flag, region) = parseProfileRegion(name)
        return "$flag $region (点击重试)"
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

    // 主通道：https://api.ipquery.io/（对齐 FlClash）
    private fun fetchIpQueryIo(ua: String, startTime: Long): LandingIpInfo? {
        var client: libcore.HTTPClient? = null
        try {
            client = createHttpClient()
            val req = client.newRequest().apply {
                setURL("https://api.ipquery.io/")
                setUserAgent(ua)
            }
            val resp = req.execute()
            val body = Util.getStringBox(resp.contentString)
            val json = JSONObject(body)
            val ip = json.optString("ip").trim()
            if (ip.isBlank()) return null

            val ispObj = json.optJSONObject("isp")
            val asn = ispObj?.optString("asn").orEmpty()
            val org = ispObj?.optString("org").orEmpty()
            val ispName = ispObj?.optString("isp").orEmpty()

            val locObj = json.optJSONObject("location")
            val countryCode = locObj?.optString("country_code").orEmpty().uppercase()
            val rawCountry = locObj?.optString("country").orEmpty()
            val country = localizeCountry(countryCode, rawCountry)
            val flag = countryCodeToFlagEmoji(countryCode)
            val city = locObj?.optString("city").orEmpty()
            val state = locObj?.optString("state").orEmpty()

            val riskObj = json.optJSONObject("risk")
            val isDatacenter = riskObj?.optBoolean("is_datacenter", true) ?: true
            val isVpn = riskObj?.optBoolean("is_vpn", false) ?: false
            val isTor = riskObj?.optBoolean("is_tor", false) ?: false
            val isProxy = riskObj?.optBoolean("is_proxy", false) ?: false
            val isMobile = riskObj?.optBoolean("is_mobile", false) ?: false
            val riskScore = riskObj?.optInt("risk_score", 20) ?: 20

            val cost = System.currentTimeMillis() - startTime

            val typeLabel = when {
                isMobile -> "移动"
                isDatacenter -> "机房"
                else -> "住宅"
            }

            val hitTags = mutableListOf<String>()
            if (isTor) hitTags.add("Tor")
            if (isVpn) hitTags.add("VPN")
            if (isProxy) hitTags.add("代理")
            val hitTagLabel = if (hitTags.isEmpty()) "无" else hitTags.joinToString(" / ")

            val levelLabel = when {
                isTor || riskScore > 60 -> "风险"
                !isDatacenter && !isProxy && !isVpn && riskScore <= 15 -> "良好"
                else -> "普通"
            }

            val riskLevel = when (levelLabel) {
                "风险" -> IpRiskLevel.HIGH
                "良好" -> IpRiskLevel.CLEAN
                else -> IpRiskLevel.LOW
            }

            return LandingIpInfo(
                ip = ip,
                country = country,
                countryCode = countryCode,
                countryFlag = flag,
                city = city,
                region = state,
                isp = ispName.ifBlank { org },
                org = org.ifBlank { ispName },
                asn = asn,
                durationMs = cost,
                levelLabel = levelLabel,
                typeLabel = typeLabel,
                hitTag = hitTagLabel,
                source = "ipquery.io",
                riskLevel = riskLevel,
                riskScore = riskScore,
                networkType = typeLabel,
                threatTags = if (hitTagLabel == "无") listOf(typeLabel) else hitTags,
                isProxy = isProxy,
                isVpn = isVpn,
                isTor = isTor,
                isHosting = isDatacenter,
            )
        } catch (_: Throwable) {
        } finally {
            runCatching { client?.close() }
        }
        return null
    }

    private fun fetchIpWhoIs(ua: String, startTime: Long): LandingIpInfo? {
        var client: libcore.HTTPClient? = null
        try {
            client = createHttpClient()
            val req = client.newRequest().apply {
                setURL("https://ipwho.is/")
                setUserAgent(ua)
            }
            val resp = req.execute()
            val body = Util.getStringBox(resp.contentString)
            val json = JSONObject(body)
            if (json.optBoolean("success", false)) {
                val ip = json.optString("ip").trim()
                if (ip.isNotBlank()) {
                    val countryCode = json.optString("country_code").uppercase()
                    val rawCountry = json.optString("country")
                    val country = localizeCountry(countryCode, rawCountry)
                    val flag = countryCodeToFlagEmoji(countryCode)
                    val city = json.optString("city")
                    val region = json.optString("region")
                    val conn = json.optJSONObject("connection")
                    val isp = conn?.optString("isp").orEmpty()
                    val org = conn?.optString("org").orEmpty()
                    val asnNum = conn?.optInt("asn", 0) ?: 0
                    val asn = if (asnNum > 0) "AS$asnNum $org".trim() else org
                    val cost = System.currentTimeMillis() - startTime

                    val sec = json.optJSONObject("security")
                    val isHosting = sec?.optBoolean("hosting", true) ?: true
                    val isProxy = sec?.optBoolean("proxy", false) ?: false
                    val isVpn = sec?.optBoolean("vpn", false) ?: false
                    val isTor = sec?.optBoolean("tor", false) ?: false
                    val (riskLevel, score, typeAndTags) = evaluateRiskAndType(isHosting, isProxy, isVpn, isTor, false, "$isp $org $asn", ip = ip)

                    val typeLabel = if (isHosting) "机房" else "住宅"
                    val hitTags = mutableListOf<String>()
                    if (isTor) hitTags.add("Tor")
                    if (isVpn) hitTags.add("VPN")
                    if (isProxy) hitTags.add("代理")
                    val hitTagLabel = if (hitTags.isEmpty()) "无" else hitTags.joinToString(" / ")

                    return LandingIpInfo(
                        ip = ip,
                        country = country,
                        countryCode = countryCode,
                        countryFlag = flag,
                        city = city,
                        region = region,
                        isp = isp,
                        org = org,
                        asn = asn,
                        durationMs = cost,
                        levelLabel = "普通",
                        typeLabel = typeLabel,
                        hitTag = hitTagLabel,
                        source = "ipwho.is",
                        riskLevel = riskLevel,
                        riskScore = score,
                        networkType = typeLabel,
                        threatTags = typeAndTags.second,
                        isProxy = isProxy,
                        isVpn = isVpn,
                        isTor = isTor,
                        isHosting = isHosting,
                    )
                }
            }
        } catch (_: Throwable) {
        } finally {
            runCatching { client?.close() }
        }
        return null
    }

    private fun fetchCloudflare(ua: String, startTime: Long): LandingIpInfo? {
        var client: libcore.HTTPClient? = null
        try {
            client = createHttpClient()
            val req = client.newRequest().apply {
                setURL("https://cloudflare.com/cdn-cgi/trace")
                setUserAgent(ua)
            }
            val resp = req.execute()
            val body = Util.getStringBox(resp.contentString)
            var cfIp = ""
            var cfLoc = ""
            var cfColo = ""
            for (line in body.lines()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("ip=")) cfIp = trimmed.substring(3).trim()
                else if (trimmed.startsWith("loc=")) cfLoc = trimmed.substring(4).trim().uppercase()
                else if (trimmed.startsWith("colo=")) cfColo = trimmed.substring(5).trim().uppercase()
            }
            if (cfIp.isNotBlank() && cfLoc.isNotBlank()) {
                val countryCode = cfLoc
                val flag = countryCodeToFlagEmoji(countryCode)
                val country = localizeCountry(countryCode, countryCode)
                val cost = System.currentTimeMillis() - startTime

                return LandingIpInfo(
                    ip = cfIp,
                    country = country,
                    countryCode = countryCode,
                    countryFlag = flag,
                    city = if (cfColo.isNotBlank()) "Cloudflare ($cfColo)" else "",
                    region = "",
                    isp = "Cloudflare Anycast",
                    org = "Cloudflare, Inc.",
                    asn = if (cfColo.isNotBlank()) "AS13335 $cfColo" else "AS13335",
                    durationMs = cost,
                    levelLabel = "普通",
                    typeLabel = "机房",
                    hitTag = "无",
                    source = "cloudflare.com",
                    riskLevel = IpRiskLevel.LOW,
                    riskScore = 20,
                    networkType = "机房",
                    threatTags = listOf("机房"),
                )
            }
        } catch (_: Throwable) {
        } finally {
            runCatching { client?.close() }
        }
        return null
    }

    suspend fun queryLandingIp(
        profileId: Long,
        forceRefresh: Boolean = false,
        onUpdate: ((LandingIpInfo) -> Unit)? = null,
    ): Result<LandingIpInfo> = withContext(Dispatchers.IO) {
        if (!DataStore.serviceState.connected) {
            return@withContext Result.failure(IllegalStateException("VPN not connected"))
        }

        val now = System.currentTimeMillis()
        if (forceRefresh) {
            currentCache = null
            cachedProfileId = -1L
        } else {
            val cache = currentCache
            if (cache != null && cachedProfileId == profileId && (now - cache.queryTimestamp < CACHE_TTL_MS)) {
                return@withContext Result.success(cache)
            }
        }

        if (isQuerying && !forceRefresh) {
            currentCache?.let { return@withContext Result.success(it) }
        }

        isQuerying = true
        val startTime = System.currentTimeMillis()
        val ua = USER_AGENT.takeIf { it.isNotBlank() }
            ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

        try {
            // 主通道：对齐 FlClash，直接请求 api.ipquery.io（3.5s 超时）
            var info = withTimeoutOrNull(3500L) { fetchIpQueryIo(ua, startTime) }

            // 备用通道 1：ipwho.is
            if (info == null) {
                info = withTimeoutOrNull(3000L) { fetchIpWhoIs(ua, startTime) }
            }

            // 备用通道 2：Cloudflare cdn-cgi/trace
            if (info == null) {
                info = withTimeoutOrNull(2000L) { fetchCloudflare(ua, startTime) }
            }

            if (info != null) {
                currentCache = info
                cachedProfileId = profileId
                onUpdate?.invoke(info)
                Result.success(info)
            } else {
                Result.failure(Exception("无法获取出站 IP 信息"))
            }
        } catch (e: Throwable) {
            Result.failure(e)
        } finally {
            isQuerying = false
        }
    }
}
