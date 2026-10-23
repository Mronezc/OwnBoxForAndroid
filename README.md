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

## 💬 社区与支持 (Support)

* 📢 欢迎加入官方交流频道：**[@OwnBoxs](https://t.me/OwnBoxs)**（获取最新发布资讯与使用技巧）
* 🐞 提交使用问题或功能建议：**[GitHub Issues](https://github.com/Own716/OwnBoxForAndroid/issues)**

---

## 📖 项目简介 (Introduction)

**OwnBox for Android** 是一款基于官方原生 **sing-box** 最新内核深度定制的 Android 高性能通用网络代理与网络诊断客户端。集全协议栈支持、自适应深浅色图标、极速 TCP Ping、分应用精准路由预设、单节点独立测速、并发拨号、双网络加速、WebDAV 云端同步、快捷瓷贴于一体。

最新 **v3.2.0 正式版** 带来秒级建立连接、50ms 科学容差防抖与一致性哈希负载均衡防断流，同时集成 300ms 极速 Anycast 落地 IP 嗅探、FlClash 级别网络诊断中心（支持 13 项主流 AI 与流媒体检测）、ChatGPT 风格现代化紧凑侧边栏与增量保留有效节点机制，兼具强悍吞吐性能与低功耗优雅体验。

---

## 📥 正式版全架构下载 (Downloads)

所有安装包均通过 GitHub Actions 官方流水线签名打包：

| 架构 / 类型 | 适用机型 / 设备 | 下载地址 | 文件校验 |
| :--- | :--- | :---: | :---: |
| 📱 **ARM64 (v8a)** *(推荐)* | 99% 的主流 64 位安卓手机、平板（高通/天玑/麒麟等） | [📥 立即下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-arm64-v8a-release.apk) | [校验值](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 📱 **ARMeabi-v7a** | 较老款 32 位安卓手机、部分老旧电视盒子 | [📥 立即下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-armeabi-v7a-release.apk) | [校验值](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 💻 **x86_64** | PC 端 64 位安卓模拟器（MuMu 12、雷电 9 等） | [📥 立即下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-x86_64-release.apk) | [校验值](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 💻 **x86** | 32 位老款 PC 模拟器 | [📥 立即下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-x86-release.apk) | [校验值](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| 🌐 **Universal (通用包)** | 包含所有 CPU 架构（体积较大，全设备自适应兼容） | [📥 立即下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-universal-release.apk) | [校验值](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |

👉 查看历史发布记录：**[GitHub Releases 官方页面](https://github.com/Own716/OwnBoxForAndroid/releases)**

---

## 📦 架构选择说明 (Architecture Notes)

* **ARM64 (v8a)**：适合绝大多数主流智能手机，体积更小、运行效率最高；
* **ARMeabi-v7a**：适合年代较久的老旧手机或部分特定 Android TV 设备；
* **x86_64 / x86**：适合在 Windows / macOS / Linux 电脑上的主流安卓模拟器中使用；
* **Universal**：如果您不确定自己设备的芯片架构，直接下载该通用包即可。

---

## 💬 问题反馈 (Issues & Feedback)

如果您在使用中遇到任何连接故障、崩溃异常或有改进想法，欢迎随时提交 Issue：

👉 **[前往 GitHub Issues 提交反馈](https://github.com/Own716/OwnBoxForAndroid/issues)**
