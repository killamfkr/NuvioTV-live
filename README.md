<div align="center">

# Nuvio Live

**Nuvio for movies and series, Live TV and IPTV, and a full phone & tablet experience — one app for Google TV, Android TV, Fire TV, phones, and tablets.**

[![Development branch](https://img.shields.io/badge/branch-dev-8A2BE2?style=for-the-badge)](https://github.com/killamfkr/NuvioTV-live/tree/dev)
[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=for-the-badge)](LICENSE)
[![Based on Nuvio](https://img.shields.io/badge/core-NuvioMedia%2FNuvioTV-8A2BE2?style=for-the-badge)](https://github.com/NuvioMedia/NuvioTV)

[Lineage & fork status](#-lineage-and-fork-status) · [What changed here](#-what-changed-in-nuvio-live) · [Build & install](#-build-and-install) · [Contributing](#-contributing)

</div>

---

## 📛 Name: Nuvio Live

This repository is developed and released as **Nuvio Live**. The launcher label, sidebar title, and documentation use that name.

**Development branch:** all ongoing work happens on **`dev`**. Treat `dev` as the main line for this project going forward.

---

## 🧭 Lineage and fork status

Nuvio Live did not appear from scratch. It stacks three layers of history:

| Layer | Project | Role |
|--------|---------|------|
| **Core** | [NuvioMedia/NuvioTV](https://github.com/NuvioMedia/NuvioTV) | Official Nuvio app (addons, library, player, accounts, etc.). This tree is based on **Nuvio 1.1.0-beta.4**. |
| **IPTV fork** | [homelessbrian/NuvioTV](https://github.com/homelessbrian/NuvioTV) (“Nuvio + IPTV”) | Live TV guide, M3U/Xtream, catch-up, On Demand from providers, Google Drive sync for IPTV, and related UI. |
| **This repo** | [killamfkr/NuvioTV-live](https://github.com/killamfkr/NuvioTV-live) → **Nuvio Live** | Continued product work: **mobile & touch**, handheld layouts, and fixes that are not tracked upstream or on the homelessbrian fork. |

### When this stopped being a fork of “Nuvio + IPTV”

**As of the Nuvio Live rename on `dev` (commit introducing this README and `app_name` = “Nuvio Live”), this repository is an independent continuation.** It does **not** aim to stay in sync with [homelessbrian/NuvioTV](https://github.com/homelessbrian/NuvioTV) releases.

- **Last shared IPTV-fork alignment:** code inherited from the homelessbrian-style Live TV integration on top of Nuvio **1.1.0-beta.4** (see `versionName` in `app/build.gradle.kts`).
- **Branch where independence is declared:** **`dev`** — use this branch for builds, PRs, and releases for Nuvio Live.
- **Relationship to official Nuvio:** still a **GPL-3.0 derivative** of NuvioMedia/NuvioTV. Report core Nuvio-only bugs upstream when they also occur in stock Nuvio; report Nuvio Live–specific issues in this repo.

---

## ✨ What changed in Nuvio Live

Below is a practical changelog for **this fork** relative to “stock Nuvio + homelessbrian IPTV,” focused on what maintainers of this repo have added on **`dev`**.

### 📱 Mobile and touch (phones & tablets)

- **Handheld navigation** — bottom/sidebar behavior tuned for touch; portrait and compact-width layouts.
- **Touch on TV Material controls** — `TouchTvButton`, `TouchTvCard`, `TouchTvSurface`, `TouchTvIconButton`, and related helpers so settings, addons, plugins, library, search, account flows, and the VOD player control bar respond to tap as well as D-pad.
- **Stream picker after Play** — dedicated mobile stream picker; full-width rows; reliable tap on addon streams.
- **VOD player** — tap to show controls / play–pause; portrait-friendly layout; progress bar **tap and drag to seek** on touch devices.
- **Detail & home** — play, continue watching, tabs, and row titles improved for small screens.

### 📺 Live TV & IPTV (from upstream fork layer)

Retained from the IPTV integration (see upstream README on homelessbrian for feature detail):

- TiviMate-style **guide**, overlay mode, catch-up, reminders, channel management.
- **M3U / Xtream**, multiple EPG sources, On Demand from providers, parental PIN, optional home row, Google Drive sync for IPTV settings.

### 🔧 Engineering notes

- **Package / updates:** `applicationId` variants and updater `GITHUB_OWNER` / `GITHUB_REPO` default to **`killamfkr`** / **`NuvioTV-live`** in `app/build.gradle.kts` (overridable via `local.properties`).
- **CI:** PR checks include template validation and `fullDebug` build on pull requests to `dev`.

For commit-level history, see the [log on `dev`](https://github.com/killamfkr/NuvioTV-live/commits/dev).

---

## 📥 Build and install

### From source (recommended for this fork)

1. Clone and check out **`dev`**:
   ```bash
   git clone https://github.com/killamfkr/NuvioTV-live.git
   cd NuvioTV-live
   git checkout dev
   ```
2. Open in Android Studio (or use the project’s Gradle tasks).
3. Build **`fullDebug`** or **`fullRelease`** as needed for your device ABI.

### APK variants (typical)

| Device | ABI hint |
|--------|-----------|
| Most phones, Google TV, Android TV (2018+) | `arm64-v8a` |
| Fire TV, older boxes | `armeabi-v7a` |
| Universal | `universal` |

Requires **Android 7.0+** (Fire OS 6+). This fork may use a different `applicationId` suffix than Play Store Nuvio so it can install **alongside** official Nuvio where configured.

---

## 🚀 Getting started (app)

1. **Addons / accounts:** configure as in Nuvio (Stremio-style addons, Trakt, debrid, etc.).
2. **Live TV:** **Settings → Live TV** — add M3U or Xtream, optional EPG; open **Live TV** or **On Demand** from the menu.
3. **Phone / tablet:** use touch for menus; in playback, tap the video for controls and use the progress bar to seek.

---

## 🤝 Contributing

- **Base branch:** open PRs against **`dev`**.
- **Scope:** Nuvio Live–specific fixes and mobile/touch/Live TV improvements in this repo.
- **Upstream:** respect [NuvioMedia/NuvioTV](https://github.com/NuvioMedia/NuvioTV) and GPL-3.0 obligations; do not imply endorsement by NuvioMedia.

---

## 🙏 Credits and legal

- **[Nuvio](https://github.com/NuvioMedia/NuvioTV)** — NuvioMedia team; core app and GPL-3.0 license.
- **Live TV fork lineage** — community IPTV work popularized as “Nuvio + IPTV” ([homelessbrian/NuvioTV](https://github.com/homelessbrian/NuvioTV)); guide patterns inspired by TiviMate.
- **Nuvio Live** — maintained on **`dev`** in this repository; **not affiliated with or endorsed by NuvioMedia**.

This app is a **client-side player**. It does not host media. Only use sources you are allowed to access. See Nuvio’s [legal / DMCA](https://github.com/NuvioMedia/NuvioTV#legal--dmca) guidance, which applies to the underlying core.

Licensed under **GNU GPL v3.0** — see [LICENSE](LICENSE).
