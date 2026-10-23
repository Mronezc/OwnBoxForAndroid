# OwnBox

<p align="center">
  <img src="docs/logo.png" width="128" height="128" alt="OwnBox Logo">
  <br>
  <b>A Modern Universal Proxy Toolchain & Network Diagnostics Client for Android</b>
  <br>
  <sub>基于 sing-box v1.15.0-alpha.12 原生内核构建的现代化全能网络代理与网络诊断客户端</sub>
</p>

<p align="center">
  <a href="https://github.com/Own716/OwnBoxForAndroid/releases/latest"><img src="https://img.shields.io/badge/Release-v3.2.0-blue.svg?style=flat-square" alt="Version"></a>
  <a href="https://github.com/SagerNet/sing-box"><img src="https://img.shields.io/badge/Sing--box-v1.15.0--alpha.12-blueviolet.svg?style=flat-square" alt="Sing-box"></a>
  <a href="https://android-arsenal.com/api?level=21"><img src="https://img.shields.io/badge/Android-5.0%2B%20(API%2021%2B)-brightgreen.svg?style=flat-square" alt="API"></a>
  <a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPL--3.0-orange.svg?style=flat-square" alt="License"></a>
  <a href="https://t.me/OwnBoxs"><img src="https://img.shields.io/badge/Telegram-@OwnBoxs-2CA5E0.svg?logo=telegram&style=flat-square" alt="Telegram"></a>
</p>

---

## 📖 Introduction / 项目简介

**OwnBox for Android** is a universal network proxy and network diagnostics client for Android, deeply customized on top of the official native **sing-box** core. It brings together full-protocol-stack support, customizable home screen icons with system light/dark auto-adaptation, lightning-fast real-connection TCP Ping latency testing, per-app routing presets, standalone latency testing per node, concurrent dialing, dual-network acceleration, WebDAV cloud backup and sync, and Quick Settings tiles.

**OwnBox for Android** 是一款基于官方原生 **sing-box** 最新内核深度定制的 Android 高性能通用网络代理与网络诊断客户端。集全协议栈支持、自适应深浅色图标、极速 TCP Ping、分应用精准路由预设、单节点独立测速、并发拨号、双网络加速、WebDAV 云端同步与快捷瓷贴于一体。

The latest **v3.2.0 Official Release** introduces instant connection establishment, 50ms tolerance hysteresis with consistent hashing load balancing to prevent disconnections, 300ms ultra-fast Anycast egress IP detection, a dedicated Network Diagnostics Center (supporting 13 major AI and streaming services connectivity tests), a modern compact navigation drawer with rounded visual controls, and incremental active-node retention for subscriptions.

最新 **v3.2.0 正式版** 带来秒级建立连接、50ms 科学容差防抖与一致性哈希负载均衡防断流，同时集成 300ms 极速 Anycast 落地 IP 嗅探、内置全功能网络诊断中心（支持 13 项主流 AI 与流媒体服务连通性检测）、现代化紧凑侧边栏与增量保留有效节点机制，兼具强悍吞吐性能与低功耗优雅体验。

---

## 📥 Downloads / 正式版全架构下载 (v3.2.0)

All packages are officially built and signed via GitHub Actions CI:  
所有安装包均通过 GitHub Actions 官方流水线签名打包：

| Architecture / 架构 | Target Devices / 适用机型 | Download / 下载 | Checksum / 校验 |
| :--- | :--- | :---: | :---: |
| 📱 **ARM64 (v8a)** *(Recommended / 推荐)* | 99% of mainstream 64-bit Android phones & tablets / 绝大多数主流 64 位安卓手机与平板 | [📥 APK Download](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-arm64-v8a-release.apk) | [sha256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 📱 **ARMeabi-v7a** | Older 32-bit Android phones or TV boxes / 较老款 32 位机型或部分电视盒子 | [📥 APK Download](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-armeabi-v7a-release.apk) | [sha256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 💻 **x86_64** | 64-bit Android emulators on PC / PC 端 64 位主流安卓模拟器 | [📥 APK Download](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-x86_64-release.apk) | [sha256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 💻 **x86** | 32-bit Android emulators on PC / 32 位电脑模拟器环境 | [📥 APK Download](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-x86-release.apk) | [sha256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 🌐 **Universal** | All-in-one universal package (larger size) / 全架构合包（自适应全机型） | [📥 APK Download](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-universal-release.apk) | [sha256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |

👉 **[View All Historical Releases / 查看历史发布记录](https://github.com/Own716/OwnBoxForAndroid/releases)**

---

## 📦 Architecture Notes / 架构选择说明

* **ARM64 (v8a)**: Suitable for the vast majority of mainstream Android phones and tablets; smaller package size and best performance / 适合绝大多数现代智能手机，体积更小、运行效率最高；
* **ARMeabi-v7a**: Suitable for older 32-bit devices or legacy Android TV boxes / 适合年代较久的老旧设备或部分智能电视盒子；
* **x86_64 / x86**: Suitable for Android emulators on Windows / macOS / Linux / 适合在电脑端安卓模拟器中使用；
* **Universal**: Auto-detects device architecture. Recommended if you are unsure of your device's CPU architecture / 如果您不确定自己设备的芯片架构，直接下载该通用包即可。

---

## 💬 Community & Feedback / 社区交流与反馈

* 📢 **Telegram Channel**: Join **[@OwnBoxs](https://t.me/OwnBoxs)** for announcements and discussions / 欢迎加入官方交流频道获取最新资讯
* 🐞 **Issues & Suggestions**: Feel free to submit an issue on **[GitHub Issues](https://github.com/Own716/OwnBoxForAndroid/issues)** / 欢迎提交使用反馈与功能建议
