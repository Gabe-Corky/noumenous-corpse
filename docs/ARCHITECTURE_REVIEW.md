# Architecture review

## Summary

The repository currently contains three interactive HTML simulators and a README. They describe a possible Android product but are not an Android application, C++ runtime, daemon, VFS, GPU/NPU dispatch layer, or package builder. The `android/` scaffold added alongside this review is deliberately a working foundation, not a claim that those systems exist.

## Feasible first architecture

1. Compose provides a small host utility and launches a separately managed WebView viewport.
2. WebViewAssetLoader serves packaged static app assets using its app-assets HTTPS origin. JavaScript is enabled for frontend compatibility, but the starter has no JavaScript interface and no file/content access.
3. Kotlin calls a tiny JNI library for a page-size query and a fixed demonstration state marker.
4. SAF-based import, app-private staging, validation, and an optional loopback service are later milestones. A local server is not needed to render static content.

## Corrections and constraints

- **WebView is not a Vulkan/WebEngine wrapper.** Android WebView uses the platform Chromium renderer and GPU acceleration where supported; applications cannot force a Vulkan swapchain or bypass Android's rendering/security model.
- **A desktop viewport does not convert arbitrary desktop software.** Static HTML/CSS/JS and compatible WebAssembly may work in WebView; native desktop binaries, OS syscalls, desktop filesystem APIs, and arbitrary toolchains do not become Android apps by packaging them.
- **Loopback is not intrinsically secure.** A local server must bind only to loopback, have a narrow API, validate origins and inputs, constrain request sizes, and avoid giving untrusted imported pages filesystem or command access. Android cleartext policy and service lifecycle also matter.
- **`memfd_create`/mmap are not a VFS or persistent NVRAM.** They provide memory-backed file mappings subject to Android kernel/API availability, per-process limits, SELinux, and memory pressure. Mapping a large region does not reserve physical RAM, and eagerly touching hundreds of megabytes may cause severe pressure or process termination. Android user-space apps cannot lock a fixed 768 MB arena as a universal invariant.
- **Do not hardcode device tiers as hardware truth.** Query runtime capabilities and memory class, use conservative bounded allocations, and handle low-memory callbacks. Vendor NPUs require documented vendor SDKs and permissions; Vulkan/OpenCL/NEON support is not interchangeable or guaranteed for each workload.
- **Robolectric is a test framework, not an on-device daemon or AI agent.** Runtime adaptation must be ordinary application code with explicit, testable rules.
- **The `0xC7` value is a state marker, not a checksum.** A NEON vector sum of repeated bytes is not a file integrity proof. CRC32 detects some accidental corruption but is not cryptographic; use a versioned archive format and cryptographic hashes for content integrity, and signatures/keys for authenticity.
- **A 4095+1 layout does not itself provide atomic durable commits.** File-system write ordering, fsync/rename behavior, crash consistency, and a recovery protocol must be specified and tested.
- **APK generation/signing should not be represented as an on-device feature by default.** Builds should occur in a controlled toolchain/CI; installed apps do not gain a trusted signing identity by bundling a private key. Never embed production signing secrets.
- **16 KB compatibility requires verification, not arbitrary linker flags.** Check all native libraries and APK alignment with current Android/NDK tooling, including third-party libraries. A 64 KB max-page-size setting may increase file footprint and does not guarantee runtime compatibility by itself.
- **Minimize permissions.** SAF is preferable to `MANAGE_EXTERNAL_STORAGE`; the starter does not request storage or network permissions.

## Immediate risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Imported HTML runs active script | Stage and validate, isolate origin, no native JS bridge by default, block external navigation, clear data on removal |
| Untrusted archive path or decompression bomb | Canonicalize paths, reject traversal/symlinks, cap expanded bytes and entries, stream extraction |
| Local bridge abused by imported page | Avoid bridge until required; loopback-only bind, origin/token controls, narrow API, no shell or arbitrary paths |
| Device killed due to memory claims | No fixed multi-gigabyte allocation; measure and cap based on OS-reported limits |
| UI implies non-existent native features | Label simulations/demo behavior and keep implementation status visible |

## Scope of current foundation

Implemented in the starter: Android project scaffolding, a Compose host, native page-size query, demo marker test, and local packaged HTML placeholder. Not implemented: repository import, local HTTP/WebSocket daemon, WebSocket IPC, VFS/NVRAM slabs, hardware self-healing, GPU/NPU dispatch, Zstandard capsules, APK builder, or telemetry. See `IMPLEMENTATION_PLAN.md` for sequenced acceptance criteria.
