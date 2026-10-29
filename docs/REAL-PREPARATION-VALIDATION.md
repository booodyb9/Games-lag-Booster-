# Validation and physical-device checklist

Regression coverage: empty history, deleting last session, missing-value averages, no invented RAM, full stability range, opacity updates without starting service, profile/settings persistence, uncapped TCP jitter, unreachable probes, measured cache deletion, preserving user data, disabled cleanup, and no false process-termination success.

On a physical Android 14–16 device:
1. Enable HUD via overlay permission, close it from the HUD, verify the main switch turns off. Change opacity while off: no service/notification should start.
2. Deny and grant Do Not Disturb policy access. Enable game mode, verify actual notification behavior, then disable it and verify restoration. Check Android 15+ app-owned Zen rule behavior and other existing DND rules.
3. Save a game profile, reopen it and restart the app. Apply FPS/resolution inside the game manually; verify no system-level FPS claim is shown.
4. Test Wi-Fi/mobile/offline, blocked TCP port, failed target, region switch during a test, and selecting the fastest measured target.
5. Clean cache twice: second cleanup may correctly report zero. User documents/game data must remain.
6. Disable each preparation option and verify the corresponding action is skipped when launching a game.
7. Record a session with unavailable CPU/GPU/FPS: show unavailable, never a guessed value.

No physical-device game FPS or in-match latency improvement is claimed by automated build results.

## Follow-up: rootless capability and selected diagnostics

- The saved public TCP destination now drives dashboard, overlay and preparation probes. Every live timing identifies its actual host and port.
- Background telemetry samples TCP at most once per five seconds per monitor, while system memory/temperature can continue updating each second.
- Warm-up controls that cannot change other apps' network paths have been removed. Network actions retest the selected endpoint or open actual Android network/DNS controls.
- RAM management opens Android's installed-app settings even when Android exposes no running processes. The user decides which app to stop; this is not automatic RAM cleanup.
- The overlay's RAM tile opens app management, its network tile opens wireless settings, and app-cache deletion remains explicitly labeled.
- Cache results show available RAM before and after, without hiding decreases. The cache-only button does not wait for unrelated network probes.
- No system RAM purge, third-party process termination, external game FPS boost, VPN tunnel, game-server selection or lower in-match ping is claimed. Shizuku/root support and a real VPN backend require a separate enabled capability; neither is bundled or silently activated.
