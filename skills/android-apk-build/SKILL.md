---
name: android-apk-build
description: Build and verify an Android APK through Gradle or GitHub Actions without unverified success claims.
---

# Android APK Build

1. Inspect wrapper, JDK/Gradle/AGP versions, SDK levels, flavors, and Actions workflows.
2. Keep changes minimal and do not rewrite working build configuration unnecessarily.
3. Keep signing credentials and API secrets out of Git; use Actions secrets when required.
4. Run the repository-supported build task and record its exit status.
5. Confirm the APK exists at the expected artifact path and is uploaded by a successful workflow.
6. Report workflow run, artifact name, and what has not been tested on a physical device.
