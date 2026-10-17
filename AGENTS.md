# Android Performance App Rules

- Baseline existing behavior before modifying UI, networking, VPN/DNS, ping targets, or overlay controls.
- Never promise lower game latency without reproducible measurements; distinguish network diagnostics from actual routing changes.
- Do not claim system-level performance controls that Android does not permit without root or privileged APIs.
- Preserve existing selectable regions/endpoints; do not silently remove options.
- Treat overlay, VPN, notification, and accessibility permissions as sensitive and explain their purpose.
- Inspect Gradle and Android SDK versions before changing build files. Never commit secrets or signing keys.
- Verify APK artifact existence before reporting a successful build.
- Do not publish or change release signing without approval.
