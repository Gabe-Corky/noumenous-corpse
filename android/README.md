# Android starter

This directory is the initial Android application scaffold for `com.Noumenous.CORPSE.Converter`. It coexists with the existing GitHub Pages simulators in the repository root.

## Build

Open this directory in Android Studio with JDK 17, Android SDK Platform 35, Android NDK, CMake 3.22.1, and an ARM64 emulator/device. Or run `gradle :app:assembleDebug` from this directory after installing a compatible Gradle distribution. A Gradle wrapper is not checked in yet.

The first milestone is intentionally modest: Compose host screen, a native page-size query, a demo `0xC7` marker check, and a WebView loading only packaged assets through `WebViewAssetLoader`. The marker check is not a checksum or security/integrity audit. The included viewport page is a placeholder; no existing root-level simulator is silently copied or executed by the app.

## Next

See [`../docs/IMPLEMENTATION_PLAN.md`](../docs/IMPLEMENTATION_PLAN.md) and [`../docs/ARCHITECTURE_REVIEW.md`](../docs/ARCHITECTURE_REVIEW.md) for milestones, risks, and corrections to the original proposal.
