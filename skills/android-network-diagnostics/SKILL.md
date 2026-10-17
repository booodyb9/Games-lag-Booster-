---
name: android-network-diagnostics
description: Diagnose Android network and ping features while preserving endpoint selection and avoiding unsupported game-acceleration claims.
---

# Android Network Diagnostics

1. Trace endpoint configuration, DNS, ping measurement, VPN service, and UI selection paths.
2. Preserve user-selectable regions/servers if present; verify selection affects the intended endpoint.
3. Treat ICMP, DNS resolution, TCP checks, and HTTP timing as different measurements.
4. Do not label a server as a game server unless its identity is verified.
5. Explain Android permission and VPN implications; never add traffic interception or certificate bypass without a legitimate, explicit need.
6. Test no network, timeout, invalid endpoint, permission denied, and region change.
7. Report measurements honestly; do not guarantee reduced game ping.
