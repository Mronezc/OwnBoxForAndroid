# OwnBox for Android v3.2.2 预览版 (v3.2.2-preview)

* **并发建连延迟极速优化 (RFC 8305 Happy Eyeballs)**：针对 sing-box v1.15.0-alpha.13 引入的 `Stagger connection attempts in parallel dial` 错峰拨号机制，将全局路由与出站回退延迟（`default_fallback_delay` / `fallback_delay`）调优至推荐标准值 `100ms`（上游默认 300ms）。彻底消除多 IP / 双栈环境下候选 IP 间 300ms~900ms 累积阶梯等待，大幅加快首包握手、冷启动与节点建连响应速度。
