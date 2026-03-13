# DownNotice Mobile

> **DownNotice Mobile** — native Android app that monitors cloud provider status pages (RSS / Atom) and delivers real-time outage and degradation notifications.

This is the Android companion to [DownNotice Desktop](https://github.com/Yoshiofthewire/DownNotice) (Electron + React).

## Features

- **Feed monitoring** — polls RSS and Atom status feeds on a configurable interval (default 15 min)
- **Status-aware notification icon** — green 🟢 / yellow 🟡 / red 🔴 / black ⚫ reflects overall health at a glance
- **Two-tab dashboard**
  - *Feeds* — one row per provider with a colour-coded status dot, provider icon, and badge
  - *Notices* — all current incidents sorted by severity then date
- **Per-feed detail view** — tap a feed to see only that provider's notices
- **Native notifications** — OS-level alerts when new incidents appear, with deduplication
- **Auto-start on boot** — a `BOOT_COMPLETED` receiver re-schedules the background worker
- **Settings**
  - Add / remove / toggle RSS feeds (name, URL, icon)
  - Refresh interval (minutes)
  - History window (default 48 h)
  - Theme: Light / Dark / System
  - Notification toggle
- **About screen** — app name, version, build date, and scrollable GPL-2.0 licence text

## Default Feeds

| Provider | Feed URL |
|---|---|
| Microsoft Azure | `https://azure.status.microsoft/en-us/status/feed/` |
| Amazon Web Services | `https://status.aws.amazon.com/rss/all.rss` |
| Google Cloud Platform | `https://status.cloud.google.com/en/feed.atom` |
| GitHub | `https://www.githubstatus.com/history.rss` |
| Cloudflare | `https://www.cloudflarestatus.com/history.atom` |

## Status Classification

| Colour | Keywords |
|---|---|
| 🔴 Red (Down) | Disruption, Unavailable, Down, Interrupted, Outage |
| 🟡 Yellow (Degraded) | Degraded, Intermittent, Issues |
| 🟢 Green (Operational) | Resolved, Completed, Scheduled (+ future-dated items) |
| ⚫ Black (Error) | Feed fetch or parse failure |

## Requirements

- Android 8.0+ (API 26)
- JDK 17
- Android Studio Ladybug (2024.2) or newer recommended

## Building

Clone the repository and open the project in Android Studio, **or** build from the command line:

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease
```

The output APK is written to `app/build/outputs/apk/`.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (type-safe routes) |
| Networking | OkHttp 4 |
| Serialisation | kotlinx.serialization |
| Background work | WorkManager |
| Build | Gradle 8.8 / AGP 8.5, Version Catalog |

## Project Structure

```
app/src/main/java/com/downnotice/mobile/
├── DownNoticeApp.kt            # Application class
├── MainActivity.kt             # Single-activity entry point
├── MainViewModel.kt            # Shared ViewModel (feeds, settings, refresh)
├── data/
│   ├── model/                  # AppSettings, FeedConfig, FeedResult, …
│   ├── network/                # HTTP client
│   ├── parser/                 # RSS / Atom feed parser
│   └── repository/             # Feed & settings repositories
├── notification/               # NotificationHelper
├── service/                    # BootReceiver, WorkManager scheduler
└── ui/
    ├── about/                  # About screen (GPL-2.0 text)
    ├── components/             # StatusDot, StatusBadge, ProviderIcon, …
    ├── dashboard/              # Feeds + Notices tabs
    ├── detail/                 # Per-feed detail screen
    ├── navigation/             # Bottom-nav host & routes
    ├── settings/               # RSS feeds list & general settings tabs
    └── theme/                  # Colour palette & Material theme
```

## Contributing

Issues and pull requests are welcome. Please follow standard GitHub contribution workflow.

## License

This project is licensed under the [GNU General Public License v2.0](LICENSE).

