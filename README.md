# SafeGuard Kids — Family Safety & Digital Wellbeing Platform

SafeGuard Kids is a transparent, ethical parental-control and digital-wellbeing system inspired by modern guardian applications such as FlashGet Kids and Google Family Link.

---

## Core Philosophy & Ethical Safety Principles

SafeGuard Kids is strictly engineered as a **transparent family-safety tool** rather than a covert surveillance application:

1. **Zero-Bypass Policy**: Monitoring is never hidden from the child or teen. A prominent status indicator and persistent notifications inform the child whenever protective features or location sharing are active.
2. **No Covert Surveillance**: Strictly does **not** implement keylogging, secret microphone/camera activation, call recording, SMS reading, or credential interception.
3. **OS Security Respect**: Does not bypass lock screens, PINs, passwords, biometric authenticators, or operating system sandboxing.
4. **End-to-End Encryption**: All telemetry logs, location coordinates, and parental commands are encrypted using AES-256-GCM and verified with SHA-256 integrity checksums.
5. **Clear Privacy Rights**: Features a dedicated Privacy Center where both parents and children can inspect all collected data points, export data in standardized JSON, and revoke device links at any time.

---

## System Architecture

```
                    ┌───────────────────────────────────┐
                    │      Parent Guardian Device       │
                    │   (Dashboard, Rules, Geofences)   │
                    └─────────────────┬─────────────────┘
                                      │ E2EE (AES-256-GCM / TLS 1.3)
                                      ▼
        ┌───────────────────────────────────────────────────────────┐
        │        Cloud Backend / Local Room Persistence Layer       │
        │  • PostgreSQL / Firestore Schemas                         │
        │  • Role-Based Access Control (RBAC)                       │
        │  • Secure 6-Digit Pairing Flow                            │
        │  • Real-Time Notification & SOS Webhooks                  │
        └─────────────────────────────┬─────────────────────────────┘
                                      │
                                      ▼
                    ┌───────────────────────────────────┐
                    │    Child Companion Device         │
                    │ (Screen Time, SOS, Time Requests) │
                    └───────────────────────────────────┘
```

---

## Key Features

### 1. Parent Guardian Dashboard
* **Child Profile Switcher**: Switch effortlessly between family profiles (e.g. Leo (12), Maya (15)) or add new profiles.
* **Device Telemetry**: Real-time battery %, online/idle/offline status, device hardware model, and last active timestamp.
* **Screen Time Gauge**: Dynamic progress ring showing used vs daily limit with color feedback (Safe Green, Warning Amber, Overlimit Rose).
* **Instant Quick Controls**:
  * **Family Focus Pause**: Instantly pause child device access for family dinner or focus time.
  * **Study Mode**: Lock all distracting apps and whitelist educational tools (Duolingo, Khan Academy).
  * **Bedtime Downtime**: Automatic sleep schedules locking devices while keeping Phone/Emergency dialer functional.
  * **Transparent Screen Viewing**: Official Android `MediaProjection` consent-based stream with immediate revocation controls.

### 2. Screen-Time Management
* Custom daily screen time limits with smooth sliders.
* Automated weekly and weekend schedules.
* School hours downtime (Mon–Fri 08:30 – 15:00).
* Weekly activity pattern visualizer (Jetpack Compose custom Canvas chart).
* Day-specific rules list (Mon–Sun).

### 3. App & Website Controls
* Installed apps audit with category tagging (Social, Gaming, Education, Entertainment, Utility).
* Per-app daily limits (e.g., 30m for YouTube, 45m for Minecraft).
* App blocking & "Always Allowed" whitelist.
* SafeSearch enforcement (Google, Bing, YouTube Restricted Mode).
* Study-only browsing mode.
* Custom domain whitelist and blacklist management.

### 4. Consent-Based Location & Geofencing
* Live GPS coordinates with address lookup.
* Canvas-rendered radar map with safe-zone radius rings.
* Customizable Safe Places (Home, School, Skate Park) with radius configuration (50m–500m).
* Instant Arrival & Departure notifications.
* Location breadcrumb trail with timestamps.

### 5. Safety Alerts & Emergency SOS
* Severity classifications (Critical, Warning, Info).
* Triggers for low battery (<20%), excessive screen time, blocked site access, and safe zone departures.
* **Emergency SOS**: Large, high-visibility distress button on the child companion app. When pressed, immediately sends critical alerts with live GPS coordinates to the parent dashboard.

### 6. Child Request & Approval System
* Children can request extra screen time (+15m, +30m, +1h), app unlock, or website access with custom reasons.
* Parents receive alerts and can approve or decline with notes.

### 7. Transparency & Privacy Center
* Complete disclosure of what data is collected and why.
* List of strictly prohibited covert surveillance practices.
* Data export in standardized JSON format.
* Revoke companion device link and purge stored logs.

---

## Backend API Specification (REST / GraphQL)

### Authentication & Pairing
* `POST /auth/register` — Register parent account.
* `POST /auth/login` — Authenticate and issue short-lived JWT + refresh token.
* `POST /devices/pair` — Verify 6-digit one-time pairing code and bind child device.

### Profiles & Rules
* `GET /children` — List children profiles in family.
* `POST /children` — Create new child profile.
* `GET /screen-time/rules?childId=:id` — Fetch daily and weekly schedules.
* `POST /screen-time/rules` — Update limits, study mode, or bedtime.
* `GET /apps/rules?childId=:id` — Get app limits and block status.
* `POST /apps/rules` — Set app block/limit.
* `GET /websites/rules?childId=:id` — Get web domain rules.
* `POST /websites/rules` — Add custom web rule.

### Location & Alerts
* `GET /locations/live?childId=:id` — Get live GPS coordinates and geofence state.
* `POST /locations/ping` — Child device updates location event (E2EE encrypted payload).
* `POST /geofences` — Add or modify parent safe zones.
* `GET /alerts` — Fetch safety alerts list.
* `POST /emergency/sos` — Child triggers urgent distress signal.
* `GET /requests` — Get pending access requests.
* `PATCH /requests/:id` — Approve or decline request.

---

## iOS Companion Architecture

For iOS devices, SafeGuard Kids employs Apple's official **Screen Time API** and **Family Controls Framework**:
1. `FamilyControls.AuthorizationCenter`: Requests explicit parent/child authorization without MDM profiles.
2. `ManagedSettings.ManagedSettingsStore`: Applies app shielding, web restrictions, and downtime.
3. `DeviceActivity.DeviceActivityMonitor`: Schedules study hours, bedtimes, and calculates daily thresholds without accessing personal screen content.
4. `CoreLocation`: Provides consent-based geofencing (`CLCircularRegion`) with `locationManager.startMonitoring(for: region)`.

---

## Build & Test Instructions

### Android Build
```bash
# Verify compilation
gradle :app:assembleDebug

# Run Robolectric unit tests
gradle :app:testDebugUnitTest
```
