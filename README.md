<div align="center">
  <img src="docs/readme-banner.svg" alt="OwnBox for Android — Proxy & Network Diagnostics" width="100%" />
  <br />
  <h1>OwnBox for Android</h1>
  <p><strong>把连接、诊断与管理，收进一个简洁的 Android 客户端。</strong></p>
  <p>基于原生 sing-box 内核打造的多协议代理客户端与网络诊断工具。</p>

  <p>
    <a href="https://github.com/Own716/OwnBoxForAndroid/releases/latest"><img src="https://img.shields.io/github/v/release/Own716/OwnBoxForAndroid?display_name=tag&style=for-the-badge&color=14B8A6" alt="Latest release" /></a>
    <a href="https://github.com/Own716/OwnBoxForAndroid/stargazers"><img src="https://img.shields.io/github/stars/Own716/OwnBoxForAndroid?style=for-the-badge&color=0F172A" alt="GitHub stars" /></a>
    <a href="https://github.com/Own716/OwnBoxForAndroid/network/members"><img src="https://img.shields.io/github/forks/Own716/OwnBoxForAndroid?style=for-the-badge&color=334155" alt="GitHub forks" /></a>
    <a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/License-GPL--3.0-64748B?style=for-the-badge" alt="GPL-3.0 License" /></a>
  </p>

  <p>
    <a href="https://github.com/Own716/OwnBoxForAndroid/releases/latest"><img src="https://img.shields.io/badge/⬇%20下载最新版-14B8A6?style=for-the-badge&labelColor=0F172A" alt="Download latest" /></a>
    <a href="https://github.com/Own716/OwnBoxForAndroid/issues"><img src="https://img.shields.io/badge/反馈问题-GitHub%20Issues-334155?style=for-the-badge" alt="Report an issue" /></a>
    <a href="https://t.me/OwnBoxs"><img src="https://img.shields.io/badge/加入社区-Telegram-229ED9?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram community" /></a>
  </p>

  <p>
    <sub>Android 5.0+ · Kotlin + Go · sing-box v1.15.0-alpha.13 · Open Source</sub>
  </p>
</div>

---

## ✦ 项目简介

**OwnBox** 是一款面向 Android 的现代化代理客户端与网络诊断工具。项目基于原生 **sing-box** 内核进行适配与扩展，将多协议节点管理、真实连接延迟测试、分应用路由、网络服务连通性检测、WebDAV 备份与系统快捷操作整合到同一个应用中。

我们希望它不只是一个“能连接”的客户端，也是一套方便观察网络状态、管理配置并快速定位连接问题的日常工具。

> **项目定位：** OwnBox 是客户端软件，不内置或提供第三方代理订阅。实际连接速度、稳定性以及各网站/服务的可用状态，会受到设备、网络环境、节点质量和服务端策略等因素影响。

## ✨ 核心功能

<table>
  <tr>
    <td width="50%" valign="top">
      <h3>🚀 多协议连接</h3>
      <p>以 sing-box 内核为基础，支持多种代理协议与节点配置，覆盖日常连接、节点管理和订阅使用场景。</p>
    </td>
    <td width="50%" valign="top">
      <h3>📡 真实连接测试</h3>
      <p>提供 TCP Ping、节点延迟测试与连接状态反馈，帮助区分“配置已导入”和“目标网络实际可达”。</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>🧭 网络诊断中心</h3>
      <p>集中检测常见网站、AI 服务与流媒体平台的连通性，支持分类查看及单项重新检测。</p>
    </td>
    <td width="50%" valign="top">
      <h3>🎯 分应用路由</h3>
      <p>按应用组织代理流量，配合路由与 DNS 设置，让网络行为更容易管理与排查。</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>☁️ 备份与同步</h3>
      <p>支持 WebDAV 备份与同步配置，便于在更换设备或重装系统时管理自己的配置数据。</p>
    </td>
    <td width="50%" valign="top">
      <h3>📱 Android 系统集成</h3>
      <p>提供快捷设置磁贴、桌面小组件等便捷入口，并针对移动设备使用场景持续优化交互。</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>🎨 清爽的个性化体验</h3>
      <p>支持主题与应用图标个性化选项，兼顾浅色、深色模式与紧凑易读的界面布局。</p>
    </td>
    <td width="50%" valign="top">
      <h3>⚙️ 持续迭代的核心适配</h3>
      <p>持续跟进 sing-box 内核变化，围绕连接建立、节点切换、后台稳定性与资源占用进行适配和修复。</p>
    </td>
  </tr>
</table>

## 🧪 网络诊断支持范围

当前版本的网络诊断中心涵盖以下 13 项常见服务：

| 类别 | 检测目标 |
|:--|:--|
| 搜索与开发 | Google、GitHub |
| 视频与流媒体 | YouTube、Netflix、Disney+、Prime Video、Spotify、TikTok |
| AI 服务 | ChatGPT、Claude、Gemini |
| 社区与其他 | Discord、哔哩哔哩（Bilibili） |

检测结果用于了解测试时的网络连通性与响应情况，**不等于保证账号可用、内容可播放或服务一定解锁**。不同网络、地区、账号和服务策略可能产生不同结果。

## 📥 下载与安装

### 最新版本

<p align="center">
  <a href="https://github.com/Own716/OwnBoxForAndroid/releases/latest"><img src="https://img.shields.io/badge/前往最新版下载-查看所有安装包-14B8A6?style=for-the-badge&logo=github&logoColor=white" alt="Go to latest release" /></a>
</p>

建议优先打开 [**GitHub Releases 最新发布页**](https://github.com/Own716/OwnBoxForAndroid/releases/latest)，查看当前版本的说明、安装包与校验文件。

### 当前稳定版 v3.2.0 安装包

| 架构 | 适用设备 | 下载 | 校验 |
|:--|:--|:--:|:--:|
| **ARM64 (v8a)** · 推荐 | 大多数现代 Android 手机与平板 | [下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-arm64-v8a-release.apk) | [SHA-256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| **armeabi-v7a** | 较旧的 32 位 Android 设备 | [下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-armeabi-v7a-release.apk) | [SHA-256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| **x86_64** | 64 位 Android 模拟器 | [下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-x86_64-release.apk) | [SHA-256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |
| **x86** | 32 位 Android 模拟器 | [下载 APK](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/OwnBox-3.2.0-x86-release.apk) | [SHA-256](https://github.com/Own716/OwnBoxForAndroid/releases/download/v3.2.0/sha256sums.txt) |

> **如何选择？** 一般手机请选择 `ARM64 (v8a)`。如果不确定设备架构，请先查看设备信息；不要仅凭 Android 版本号判断 CPU 架构。

### 安装步骤

1. 打开最新发布页，下载与设备架构匹配的 APK。
2. 按系统提示完成安装；若系统要求，允许当前安装来源安装应用。
3. 首次使用时，按应用内指引授予 VPN 权限，再导入自己的节点或订阅配置。
4. 遇到连接异常时，可先运行节点延迟测试和网络诊断，再提交问题反馈。

## 🧩 技术栈

| 组件 | 说明 |
|:--|:--|
| Android | 原生 Android 客户端，最低支持 Android 5.0（API 21） |
| Kotlin | Android 应用主要开发语言 |
| Go / `libcore` | 集成与适配 sing-box 核心能力 |
| sing-box | 当前仓库配置版本为 `v1.15.0-alpha.13` |
| GitHub Actions | 自动化构建与发布流程 |

## 🔐 安全与签名说明

- 请优先从本仓库的 [**官方 Releases**](https://github.com/Own716/OwnBoxForAndroid/releases/latest) 下载当前发布版本，并核对发布页提供的 SHA-256 文件。
- 仓库的 [安全策略（SECURITY.md）](SECURITY.md) 明确说明：**2026 年 9 月之前历史提交中出现的旧 `release.keystore` 或凭据应视为已泄露且不可信**。不要将历史签名材料用于当前发布，也不要仅凭旧签名判断历史 APK 可信。
- 发现可复现的安全问题时，请参考 [SECURITY.md](SECURITY.md) 中的说明处理；普通功能问题与建议可提交至 [GitHub Issues](https://github.com/Own716/OwnBoxForAndroid/issues)。

## 🗺️ 项目文档

- [所有发布版本与更新说明](https://github.com/Own716/OwnBoxForAndroid/releases)
- [更新日志（CHANGELOG.md）](CHANGELOG.md)
- [发布说明（RELEASE_NOTES.md）](RELEASE_NOTES.md)
- [安全策略（SECURITY.md）](SECURITY.md)
- [GPL-3.0 许可证](LICENSE)

## 💬 社区、反馈与参与

- **Telegram 社区：** [@OwnBoxs](https://t.me/OwnBoxs)
- **问题反馈：** [提交 GitHub Issue](https://github.com/Own716/OwnBoxForAndroid/issues)
- **源码与贡献：** 欢迎阅读代码、反馈可复现的问题，或通过 Pull Request 参与改进。

提交问题时，建议附上应用版本、Android 版本、设备架构、网络类型、复现步骤，以及经过脱敏处理的相关日志。请勿公开分享订阅链接、节点密码、访问令牌、私钥或其他敏感信息。

## 📄 许可证

本项目采用 [GNU General Public License v3.0（GPL-3.0）](LICENSE) 许可证。使用、分发或修改代码前，请阅读许可证正文及相关上游组件的许可要求。

---

<div align="center">
  <p><strong>OwnBox · Connect clearly. Diagnose confidently.</strong></p>
  <p><sub>如果 OwnBox 对你有帮助，欢迎点亮 ⭐ Star、提交反馈，或加入 Telegram 社区。</sub></p>
</div>

<details>
  <summary><strong>English</strong> — Show the English overview</summary>

  <div align="center">
    <h2>OwnBox for Android</h2>
    <p><strong>A modern Android proxy client and network diagnostics toolkit.</strong></p>
    <p>Built around the native sing-box core, OwnBox brings together multi-protocol connections, node latency tests, per-app routing, service connectivity checks, WebDAV backup and Android system shortcuts.</p>
    <p>
      <a href="https://github.com/Own716/OwnBoxForAndroid/releases/latest">Download latest release</a> ·
      <a href="https://github.com/Own716/OwnBoxForAndroid/issues">Report an issue</a> ·
      <a href="https://t.me/OwnBoxs">Telegram community</a>
    </p>
  </div>

  ### Highlights

  - **Multi-protocol client:** based on sing-box with ongoing compatibility and stability improvements.
  - **Connection diagnostics:** TCP Ping, per-node latency tests and connectivity feedback.
  - **Service checks:** connectivity probes for Google, GitHub, YouTube, ChatGPT, Claude, Gemini, Netflix, Disney+, Prime Video, Spotify, TikTok, Discord and Bilibili.
  - **Everyday Android integration:** per-app routing, Quick Settings tile, home-screen widgets and WebDAV backup/sync.
  - **Personalization:** theme and app-icon options designed for a clean mobile experience.

  ### Downloads

  Visit the [latest Releases page](https://github.com/Own716/OwnBoxForAndroid/releases/latest) for current APKs, release notes and checksums. For most modern Android phones, choose **ARM64 (v8a)**. Other packages target older 32-bit devices or Android emulators.

  ### Important security note

  The repository's [SECURITY.md](SECURITY.md) states that historical `release.keystore` files and credentials appearing in commits before September 2026 are compromised and untrusted. Download current builds from the official Releases page and do not reuse historical signing materials.

  OwnBox is a client application and does not provide third-party proxy subscriptions. Connectivity, speed and service availability depend on the device, network, server and service-side policies.

  ### License & community

  Licensed under [GPL-3.0](LICENSE). Visit [GitHub Issues](https://github.com/Own716/OwnBoxForAndroid/issues) for bug reports and suggestions, or join [@OwnBoxs on Telegram](https://t.me/OwnBoxs).
</details>
