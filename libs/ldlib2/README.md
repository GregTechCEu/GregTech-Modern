# Patched LDLib2 Artifact

This directory vendors the patched LDLib2 NeoForge artifact required by the
GTCEu 26.1.2 migration.

- Artifact: `com.lowdragmc.ldlib2:ldlib2-neoforge:26.1.2-gtceu.1`
- Binary: `ldlib2-neoforge-26.1.2-gtceu.1.jar`
- Sources: `ldlib2-neoforge-26.1.2-gtceu.1-sources.jar`
- License: LGPL-3.0, copied in `LICENSE-LDLib2.txt`

The artifact is tracked because this patched `gtceu.1` build is not a normal
upstream published version. Replace it with a public Maven dependency once an
equivalent LDLib2 build is available.

LDLib2 depends on Taffy at compile time. GTCEu declares Taffy through the normal
Gradle version catalog instead of relying on this patched artifact's local Maven
metadata.
