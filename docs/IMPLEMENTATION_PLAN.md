# Implementation plan

This plan builds an Android host around the repository's existing browser-based architecture explorers without conflating the simulator with a functioning native substrate.

## Milestone 0 — Android foundation (this change)

- Preserve the three root-level HTML simulators and GitHub Pages behavior.
- Add an isolated `android/` Gradle project, Compose host, ARM64 C++20 JNI library, and packaged-asset WebView viewport.
- Keep the viewport on `WebViewAssetLoader`; do not enable broad file access, mixed content, external URL navigation, or a JavaScript-to-native bridge.
- Provide a small placeholder asset and plainly label demo-only native behavior.

Acceptance: Android Studio syncs and builds a debug ARM64 APK; host reports OS page size, marker control responds, and viewport loads the placeholder offline.

## Milestone 1 — Import one static web app safely

- Add Android Storage Access Framework document/tree selection (avoid all-files storage permission).
- Copy selected files into app-private storage with path traversal, symlink, file-count, size, and archive expansion limits.
- Stage and validate imports before replacing the active workspace; retain rollback and clear user-visible errors.
- Render a selected static app from a constrained app-private asset origin. Define supported entrypoints, MIME types, CSP expectations, and offline behavior.
- Add tests for malformed archives, oversized files, traversal names, missing entrypoints, and rollback.

Acceptance: import, preview, switch, and remove a small static HTML bundle without network permission or external storage access.

## Milestone 2 — Minimal loopback bridge

- Only after a concrete frontend needs HTTP/WebSocket APIs, implement a foreground service with an explicit lifecycle and a server bound exclusively to `127.0.0.1` (never `0.0.0.0`).
- Prefer a small audited HTTP implementation; expose a narrow versioned API and WebSocket origin checks, request limits, authentication/capability tokens where applicable, and graceful shutdown.
- Keep imported content isolated from privileged bridge operations. Do not expose arbitrary filesystem paths, shell commands, or generic JNI invocation.
- Add integration tests for bind address, lifecycle, malformed requests, oversized bodies, and stale-client behavior.

Acceptance: service starts/stops visibly and serves only the selected app's read-only static assets on loopback; no claim of zero latency or cross-process shared memory.

## Milestone 3 — Native primitives and measured memory policy

- Implement independently testable pure C++ functions (page-size-aware alignment, octet bitfields, bounded SPSC queue only if needed) with host and Android tests.
- Use OS-reported page size and checked arithmetic; never assume a device model maps to a page size or memory budget.
- Treat buffers as demand-allocated, bounded app-private memory. Establish conservative caps from runtime memory class and user settings; react to memory pressure.
- Add CRC/hash only with a documented format and test vectors. Use cryptographic hashes for archive integrity; checksum success is not authenticity.

Acceptance: unit/property tests cover boundaries and overflow, instrumentation tests report actual runtime capabilities, and measured memory stays within documented caps on representative devices.

## Milestone 4 — Backup/export

- Define a versioned archive manifest (paths, lengths, media types, SHA-256 digests, format version), safe extraction rules, and atomic export/import.
- Export user data through the Storage Access Framework. Do not claim to produce/sign an APK from inside the installed app in the first version; APK construction belongs in a controlled build pipeline.
- Integrate Zstandard only after dependency/license review, reproducible tests, and streaming resource limits.

Acceptance: round-trip tests preserve files and reject corrupt, truncated, traversal, and unsupported-version archives.

## Milestone 5 — UX, device compatibility, release

- Add DeX/keyboard/mouse/S Pen interaction testing; make viewport scaling and desktop user-agent behavior configurable per app.
- Validate Android 15+ 16 KB page-size compatibility for all native dependencies and packaged ELF segments using current Android tooling; do not hardcode a 64 KB maximum page size as a universal fix.
- Add CI for Gradle unit tests, Android lint, CMake build, native tests, dependency/license checks, and APK inspection.
- Document supported app types, limits, privacy model, data backup, release signing, and tested device matrix.
