package io.nekohasekai.sagernet

import io.nekohasekai.sagernet.utils.IpRiskLevel
import io.nekohasekai.sagernet.utils.LandingIpInfo
import io.nekohasekai.sagernet.utils.LandingIpManager
import io.nekohasekai.sagernet.utils.ServiceDiagnosticsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LandingIpRiskEvaluationTest {

    @Test
    fun testRiskEvaluationTor() {
        val (risk, score, typeAndTags) = LandingIpManager.evaluateRiskAndType(
            isHosting = true,
            isProxy = true,
            isVpn = false,
            isTor = true,
            isMobile = false,
            ispOrOrg = "Tor Exit Node",
            ip = "185.220.101.5",
        )
        assertEquals(IpRiskLevel.HIGH, risk)
        assertTrue(score >= 90)
        assertTrue(typeAndTags.first.contains("Tor"))
        assertTrue(typeAndTags.second.contains("Tor 节点"))
    }

    @Test
    fun testRiskEvaluationVpnDatacenter() {
        val (risk, score, typeAndTags) = LandingIpManager.evaluateRiskAndType(
            isHosting = true,
            isProxy = false,
            isVpn = false,
            isTor = false,
            isMobile = false,
            ispOrOrg = "M247 Ltd Singapore",
            ip = "194.59.251.10",
        )
        // M247 is a known heavy commercial VPN provider -> Medium/High risk
        assertTrue(risk == IpRiskLevel.MEDIUM || risk == IpRiskLevel.HIGH)
        assertTrue(score >= 55)
        assertTrue(typeAndTags.first.contains("Datacenter") || typeAndTags.first.contains("数据中心"))
        assertTrue(typeAndTags.second.contains("高频代理段"))
    }

    @Test
    fun testRiskEvaluationPublicCloud() {
        val (risk, score, typeAndTags) = LandingIpManager.evaluateRiskAndType(
            isHosting = true,
            isProxy = false,
            isVpn = false,
            isTor = false,
            isMobile = false,
            ispOrOrg = "Amazon.com, Inc. AWS EC2",
            ip = "13.229.170.194",
        )
        // Public cloud like AWS -> Medium risk
        assertEquals(IpRiskLevel.MEDIUM, risk)
        assertTrue(score in 40..65)
        assertTrue(typeAndTags.first.contains("Datacenter") || typeAndTags.first.contains("数据中心"))
        assertTrue(typeAndTags.second.contains("公有云商"))
    }

    @Test
    fun testRiskEvaluationCdn() {
        val (risk, score, typeAndTags) = LandingIpManager.evaluateRiskAndType(
            isHosting = true,
            isProxy = false,
            isVpn = false,
            isTor = false,
            isMobile = false,
            ispOrOrg = "Cloudflare, Inc. Anycast",
            ip = "104.21.56.78",
        )
        // Cloudflare Anycast -> Low risk
        assertEquals(IpRiskLevel.LOW, risk)
        assertTrue(score in 20..42)
        assertTrue(typeAndTags.first.contains("CDN") || typeAndTags.first.contains("Anycast"))
        assertTrue(typeAndTags.second.contains("CDN边缘"))
    }

    @Test
    fun testRiskEvaluationResidential() {
        val (risk, score, typeAndTags) = LandingIpManager.evaluateRiskAndType(
            isHosting = false,
            isProxy = false,
            isVpn = false,
            isTor = false,
            isMobile = false,
            ispOrOrg = "HKT Broadband Hong Kong",
            ip = "218.102.15.8",
        )
        // Genuine residential broadband -> Clean
        assertEquals(IpRiskLevel.CLEAN, risk)
        assertTrue(score in 2..20)
        assertTrue(typeAndTags.first.contains("Residential") || typeAndTags.first.contains("原生住宅"))
        assertTrue(typeAndTags.second.contains("原生住宅"))
    }

    @Test
    fun testLandingIpInfoDefaultsAndIpVersion() {
        val infoV4 = LandingIpInfo(
            ip = "104.21.56.78",
            country = "United States",
            countryCode = "US",
            countryFlag = "🇺🇸",
            city = "San Jose",
            region = "CA",
            isp = "Cloudflare",
            org = "Cloudflare Anycast",
            asn = "AS13335",
            durationMs = 120,
        )
        assertEquals("IPv4", infoV4.ipVersion)
        assertEquals(IpRiskLevel.LOW, infoV4.riskLevel)
        assertTrue(infoV4.briefText.contains("104.21.56.78"))

        val infoV6 = LandingIpInfo(
            ip = "2606:4700::6811:384e",
            country = "United States",
            countryCode = "US",
            countryFlag = "🇺🇸",
            city = "San Jose",
            region = "CA",
            isp = "Cloudflare",
            org = "Cloudflare Anycast",
            asn = "AS13335",
            durationMs = 150,
        )
        assertEquals("IPv6", infoV6.ipVersion)
    }

    @Test
    fun testInitialServiceListCountAndIds() {
        val list = ServiceDiagnosticsManager.createInitialList()
        assertEquals(13, list.size)
        val ids = list.map { it.id }.toSet()
        val expected = setOf(
            "google", "github", "youtube", "chatgpt", "claude", "gemini",
            "netflix", "disney", "prime", "spotify", "tiktok", "discord", "bilibili"
        )
        assertEquals(expected, ids)
    }
}
