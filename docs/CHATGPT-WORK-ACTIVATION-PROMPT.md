# ChatGPT / ChatGPT Work activation prompt

Copy this prompt into a ChatGPT Work task after connecting the correct GitHub repository. It does not grant permissions or automatically activate tools.

---
Before editing, read AGENTS.md, docs/AGENT-SKILLS-SETUP.md, and relevant skills under skills/ and .agents/skills/. Establish branch/commit, architecture, build/test commands, existing behavior, and CI baseline.

Preserve the existing UI and network-selection features unless explicitly asked otherwise. For network diagnostics, distinguish actual measured latency from estimates, identify the measurement endpoint/location, and never claim a system-wide ping reduction or game acceleration without reproducible measurements. Do not claim Android can alter another app's network stack or priority unless the implemented API and OS permissions genuinely allow it.

Use a separate branch and minimal changes. Review permissions, Accessibility Service/overlay behavior, privacy, battery usage, lifecycle, and background restrictions. Never commit signing keys, tokens, or private user data. Run available checks and report exact output; state clearly when an APK was not built or runtime-tested.

For security tasks, test only authorized scope. Separate confirmed findings, suspected risks, and untested controls. Do not attack third-party targets, perform destructive tests, alter production settings, or release/deploy without explicit approval.

Report statuses separately: proposed, coded, built, deployed, production-tested.
---