# CORPSE native contract checks

The first native diagnostic milestone loads explicit numeric rules from `assets/config/substrate_rules.json` and passes them into a bounded C++ contract check. The checker verifies the baseline octet and field split, byte rotations, the runtime page size, the 12 × 64 MiB geometry, and the CRC-32C `123456789` reference vector.

The `VERIFY` action reports pass/fail in the host UI. CRC-32C here is a primitive reference test, not a cryptographic integrity/authenticity guarantee. The slab arithmetic is a topology assertion only; it does not allocate or lock 768 MiB. No model, daemon, downloaded patches, automatic correction, or live substrate mutation is part of this milestone.

For host-side tests, configure CMake with `-DCORPSE_BUILD_NATIVE_TESTS=ON` and run CTest. Android Studio builds the JNI library with the tests disabled by default.
