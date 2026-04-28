---
title: Codebase Guide
---


# Codebase Guide

This section is a map for contributors who already know GregTech CEu Modern, the 1.20.1 branch, or Minecraft modding in
general, but need to understand how the 26.1.2 port is organized.

The pages here describe the codebase from the inside out:

- [Repository Map](Repository-Map.md) explains the source sets, package layout, resources, generated data, and common
  starting points.
- [Lifecycle and Registration](Lifecycle-and-Registration.md) follows the mod from construction through registry,
  common setup, client setup, dynamic packs, and data generation.
- [Systems and Extension Points](Systems-and-Extension-Points.md) points to the machine, recipe, material, UI,
  integration, storage, capability, and mixin systems most contributors touch.
- [NeoForge 26 Porting Notes](NeoForge-26-Porting-Notes.md) calls out the port-specific patterns that differ from older
  Forge or NeoForge branches.
- [Testing and Datagen](Testing-and-Datagen.md) describes the Gradle tasks, generated resources, and NeoForge 26.1.2
  GameTest bootstrap used by this repository.

!!! note "Local NeoForge reference"

    Before browsing for NeoForge API answers, check the vendored documentation snapshot under `docs/neoforged`.
    `docs/neoforged/GTCEU-UPSTREAM.md` records that the snapshot was taken from upstream NeoForged docs on
    2026-04-26 for docs version `26.1`.

!!! warning "Do not assume the 1.20.1 architecture"

    A lot of names still look familiar, but the port changed important foundations: ModDevGradle is used instead of the
    old build setup, item state moved toward data components, custom payloads use `StreamCodec`, capabilities are
    registered through NeoForge events, and runtime-generated assets/data are handled through GTCEu dynamic packs.
