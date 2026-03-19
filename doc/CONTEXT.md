# CONTEXT.md — Parent App (KMP + Compose Multiplatform)
> **LOCATION:** This file lives at `ParentAppMultiplatform/doc/CONTEXT.md`
> **AGENT INSTRUCTION:** Read this entire file before writing any code. Every
> decision here is final unless overridden in `doc/DECISIONS.md`. Do not
> re-derive architecture from scratch.

---

## 0. Repository & Documentation Structure

```
ParentAppMultiplatform/               ← Root of the git repository
│
├── doc/                              ← ALL documentation lives here
│   ├── CONTEXT.md                    ← THIS FILE — master reference
│   ├── DECISIONS.md                  ← Architecture decisions log
│   ├── DAILY_PROMPTS.md              ← Day-by-day agent task prompts
│   ├── Parent_App_Spec.docx          ← Original app specification
│   └── Parent_App_Sprint_Plan.docx   ← Original sprint plan
│
├── shared/                           ← KMP shared module (all logic + UI)
│   └── src/
│       ├── commonMain/
│       ├── androidMain/
│       └── iosMain/
│
├── androidApp/                       ← Android host application (entry only)
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── MainActivity.kt
│
└── iosApp/                           ← iOS Xcode project (entry only)
    └── iosApp/
        └── ContentView.swift
```

**Reference documents for full product requirements:**
- Full screen specs, API contracts, and wireframes → `doc/Parent_App_Spec.docx`
- Sprint schedule and day-by-day breakdown → `doc/Parent_App_Sprint_Plan.docx`

---

## 1. Project Identity

| Field | Value |
|---|---|
| App Name | Parent App |
| Product | Academic Monitoring & Management Platform for Parents |
| Package ID (Android) | `com.edmik.parentapp` |
| Bundle ID (iOS) | `com.edmik.parentapp` |
| Android Min SDK | API 26 (Android 8.0+) |
| iOS Minimum | iOS 16+ |
| Architecture | Compose Multiplatform — ALL UI + logic in `commonMain` |
| Dev API Base | `https://dev.indivue.in:8080` |
| Prod API Base | `https://kademinservices.indivue.in` |
| Auth | JWT Bearer token on every request header |
| Figma | https://www.figma.com/design/RzRLfAzFbVJD466Qzf99hh/Parent-App |
| Sprint | 2 weeks — Mar 18–31, 2026 |

---

## 2. Architecture Decision (NON-NEGOTIABLE)

**Compose Multiplatform (CMP)** — All UI and business logic live in `commonMain`.
Platform-specific code (`androidMain` / `iosMain`) is used **only** where no
multiplatform equivalent exists.

### Module Structure Inside `shared/`

```
shared/src/
├── commonMain/kotlin/com/edmik/parentapp/
│   ├── data/
│   │   ├── api/           ← Ktor service interfaces
│   │   ├── model/         ← @Serializable API request/response classes
│   │   ├── repository/    ← Repository implementations
│   │   ├── local/         ← SQLDelight queries, TokenManager
│   │   ├── payment/       ← PaymentLauncher expect/actual interface
│   │   ├── platform/      ← FilePicker, NetworkObserver expect/actual
│   │   └── sync/          ← OfflineSyncManager
│   ├── domain/
│   │   ├── model/         ← Pure Kotlin domain models
│   │   ├── repository/    ← Repository interfaces
│   │   └── usecase/       ← Use cases
│   ├── di/                ← Koin modules (NetworkModule, RepositoryModule, etc.)
│   ├── ui/
│   │   ├── theme/         ← Color.kt, Typography.kt, Shapes.kt, Theme.kt
│   │   ├── components/    ← ALL reusable composables (15 components)
│   │   ├── navigation/    ← Routes.kt, AppNavHost.kt
│   │   ├── login/
│   │   ├── dashboard/
│   │   ├── attendance/
│   │   ├── fees/
│   │   ├── leave/
│   │   ├── messages/
│   │   ├── analytics/
│   │   ├── calendar/
│   │   └── notifications/
│   └── util/              ← Strings.kt, Extensions, Constants
│
├── androidMain/kotlin/com/edmik/parentapp/
│   ├── actual/            ← BiometricAuth, NetworkObserver, FilePicker actuals
│   ├── payment/           ← Razorpay Android SDK actual
│   └── push/              ← FCM FirebaseMessagingService actual
│
└── iosMain/kotlin/com/edmik/parentapp/
    ├── actual/            ← BiometricAuth, NetworkObserver, FilePicker actuals
    ├── payment/           ← Razorpay iOS SDK actual (or stub)
    └── push/              ← APNs / Firebase iOS SDK actual
```

### Golden Rule for Agents
> If you are writing code that imports anything from `android.*`, `UIKit`, or
> `Foundation` **inside `commonMain`** — STOP. That code belongs in
> `androidMain` or `iosMain` behind an `expect/actual` interface.

---

## 3. Tech Stack (All Final — See `doc/DECISIONS.md` for rationale)

| Layer | Library | Notes |
|---|---|---|
| Language | Kotlin 2.0+ | |
| UI | Compose Multiplatform 1.7.x | JetBrains CMP stable |
| Navigation | Navigation Compose Multiplatform 2.8.x | Type-safe routes |
| DI | Koin 4.x | `koin-core` + `koin-compose` — works in `commonMain` |
| Networking | Ktor Client 3.x | `okhttp` engine (Android), `darwin` engine (iOS) |
| Serialization | Kotlinx Serialization 1.7+ | `@Serializable` on all models |
| Async | Kotlinx Coroutines 1.9+ | `StateFlow`, `ViewModel` |
| ViewModel | `lifecycle-viewmodel-compose` 2.8.x | KMP-compatible |
| Local DB | SQLDelight 2.x | Schema in `commonMain/sqldelight/` |
| Secure Storage | `multiplatform-settings` 1.2+ (encrypted) | EncryptedSharedPrefs / Keychain |
| Image Loading | Coil 3.x | Native CMP support |
| Charts | Canvas-based Compose (custom) | Donut, Gauge, Line — no third-party lib |
| Date/Time | Kotlinx DateTime 0.6+ | |
| Payment | Razorpay SDK (platform-specific via expect/actual) | |
| Push | FCM (Android) + APNs/Firebase iOS (via expect/actual) | |
| Biometric | `BiometricPrompt` (Android) + `LocalAuthentication` (iOS) | |

---

## 4. API Reference

### Base URLs
- Development: `https://dev.indivue.in:8080`
- Production:  `https://kademinservices.indivue.in`

> Full API contracts with request/response shapes are in `doc/Parent_App_Spec.docx`.
> The tables below are the canonical endpoint registry.

### Auth Pattern
```
Authorization: Bearer <access_token>
```
- Access token → secure storage + in-memory cache
- Refresh token → secure storage only
- Auto-refresh via Ktor `Auth` plugin `bearerTokens {}` on any 401

### All 36 Endpoints

#### Auth (3)
| Method | Path | Used By |
|---|---|---|
| POST | `/parent/login` | Login screen |
| POST | `/parent/forgot-password` | Forgot Password |
| POST | `/parent/refresh-token` | Ktor interceptor — all screens |

#### Dashboard & Students (3)
| Method | Path | Used By |
|---|---|---|
| GET | `/parent/dashboard?studentId={id}` | Dashboard |
| GET | `/parent/students` | Student Switcher |
| POST | `/parent/device/register` | App startup — FCM/APNs token |

#### Attendance (5)
| Method | Path | Used By |
|---|---|---|
| GET | `/parent/attendance/summary?studentId={id}` | Attendance Summary |
| GET | `/parent/attendance/subject/{subjectId}?studentId={id}` | Attendance Detail |
| GET | `/parent/attendance/calendar?studentId={id}&month={m}&year={y}` | Calendar View |
| GET | `/parent/attendance/trends?studentId={id}` | Trend View |
| GET | `/parent/attendance/today?studentId={id}` | Dashboard widget |

#### Fees (6)
| Method | Path | Used By |
|---|---|---|
| GET | `/parent/fees?studentId={id}` | Fee List |
| GET | `/parent/fees/{feeId}` | Fee Detail |
| POST | `/parent/fees/pay` | Payment init (get Razorpay orderId) |
| POST | `/parent/fees/pay/verify` | Payment verify after callback |
| GET | `/parent/fees/{feeId}/receipt` | Download Receipt |
| GET | `/parent/fees/{feeId}/history` | Fee payment history |

#### Leave (5)
| Method | Path | Used By |
|---|---|---|
| POST | `/parent/leave/apply` | Apply Leave |
| GET | `/parent/leave/history?studentId={id}` | Leave History |
| GET | `/parent/leave/{leaveId}` | Leave Detail |
| DELETE | `/parent/leave/{leaveId}` | Cancel Leave |
| POST | `/parent/leave/{leaveId}/document` | Upload attachment |

#### Messages & Announcements (6)
| Method | Path | Used By |
|---|---|---|
| GET | `/parent/messages?studentId={id}` | Conversations List |
| GET | `/parent/messages/{conversationId}` | Message Thread |
| POST | `/parent/messages` | Send Message |
| GET | `/parent/announcements?studentId={id}` | Announcements |
| GET | `/parent/announcements/{id}` | Announcement Detail |
| POST | `/parent/announcements/{id}/acknowledge` | Acknowledge |

#### Notifications (7)
| Method | Path | Used By |
|---|---|---|
| GET | `/parent/notifications?studentId={id}&page={p}&size={s}` | Notification Centre |
| PUT | `/parent/notifications/read-all` | Mark All Read |
| PUT | `/parent/notifications/{id}/read` | Mark Single Read |
| GET | `/parent/notifications/preferences` | Preferences screen |
| PUT | `/parent/notifications/preferences` | Save Preferences |
| POST | `/parent/device/register` | Register push token |
| DELETE | `/parent/device/{deviceId}` | Unregister on logout |

#### Analytics (2)
| Method | Path | Used By |
|---|---|---|
| GET | `/pulse/analytics/parent/dashboard?studentId={id}` | Analytics Dashboard ⚠️ uses `/pulse/` prefix |
| GET | `/parent/analytics/subject/{subjectId}?studentId={id}` | Subject Detail |

#### Calendar (1)
| Method | Path | Used By |
|---|---|---|
| GET | `/parent/calendar?studentId={id}&month={m}&year={y}` | Academic Calendar |

---

## 5. Design System

> Full Figma source at link in Section 1. Extracted tokens below are
> the implementation reference.

### Color Tokens
```kotlin
// commonMain/ui/theme/Color.kt
val PrimaryBlue          = Color(0xFF2196F3)
val PrimaryBlueDark      = Color(0xFF1976D2)
val BackgroundGradStart  = Color(0xFFE8F4FD)
val BackgroundGradEnd    = Color(0xFFFFFFFF)
val CardBackground       = Color(0xFFFFFFFF)
val ColorPresent         = Color(0xFF4CAF50)   // Green
val ColorAbsent          = Color(0xFFF44336)   // Red
val ColorLeave           = Color(0xFFFFC107)   // Amber
val ColorPending         = Color(0xFFFF9800)   // Orange-Amber
val AlertBannerBg        = Color(0xFFFFEBEE)
val TodayStatusBg        = Color(0xFFE8F5E9)
val TextPrimary          = Color(0xFF212121)
val TextSecondary        = Color(0xFF757575)
val BatchSelectorBg      = Color(0xFFE3F2FD)
val BackgroundLight      = Color(0xFFF5F5F5)
```

### Status Colour Semantics
| Colour | Hex | Meaning |
|---|---|---|
| 🟢 Green | `#4CAF50` | Present / Paid / Success / Approved |
| 🟡 Amber | `#FF9800` | Pending / Warning / Under Review |
| 🔴 Red | `#F44336` | Absent / Overdue / Rejected / Error |
| 🔵 Blue | `#1976D2` | Selected / Active / Primary action |
| ⚫ Grey | `#9E9E9E` | Holiday / Inactive / Read |

### Typography Scale
| Style | Size | Weight | Usage |
|---|---|---|---|
| Header | 18–20sp | Bold | Screen titles, card headers |
| Body | 14–16sp | Regular | Content text |
| Caption | 12sp | Light | Dates, labels, secondary info |
| Tab Label | 12sp | Regular | Bottom nav labels |

**Requirement:** Must not break layout at 150% system font scaling.

### Spacing & Touch Targets
| Token | Value |
|---|---|
| Min touch target | 44×44 dp |
| Card padding | 16 dp |
| List item min height | 56 dp |
| Bottom nav height | 56 dp |
| Standard horizontal margin | 16 dp |

---

## 6. Reusable Components (All in `commonMain/ui/components/`)

| Component | File | Used By |
|---|---|---|
| `BottomNavBar` | `BottomNavBar.kt` | All main screens |
| `StudentHeader` | `StudentHeader.kt` | Dashboard, Attendance, Fees |
| `BatchSelector` | `BatchSelector.kt` | Dashboard |
| `CalendarWidget` | `CalendarWidget.kt` | Dashboard, Attendance, Calendar |
| `DonutChart` | `DonutChart.kt` | Attendance Summary/Detail |
| `GaugeChart` | `GaugeChart.kt` | Attendance Summary |
| `LineChart` | `LineChart.kt` | Attendance Trends, Analytics |
| `AlertBanner` | `AlertBanner.kt` | Attendance low-alert, errors |
| `StatusBadge` | `StatusBadge.kt` | Fees, Leave, Notifications |
| `ActivityCard` | `ActivityCard.kt` | Dashboard feed |
| `SubjectCard` | `SubjectCard.kt` | Attendance Subject View |
| `LoadingOverlay` | `LoadingOverlay.kt` | All async screens |
| `ErrorState` | `ErrorState.kt` | All screens with API calls |
| `EmptyState` | `EmptyState.kt` | All list screens |
| `OfflineBanner` | `OfflineBanner.kt` | All screens when offline |
| `PullToRefresh` | `PullToRefresh.kt` | All list screens |

---

## 7. Navigation Routes

```kotlin
// commonMain/ui/navigation/Routes.kt
object Routes {
    const val LOGIN                  = "login"
    const val FORGOT_PASSWORD        = "forgot-password"
    const val DASHBOARD              = "dashboard"
    const val ATTENDANCE             = "attendance"
    const val ATTENDANCE_SUBJECT     = "attendance/subject/{subjectId}"
    const val FEES                   = "fees"
    const val FEE_DETAIL             = "fees/{feeId}"
    const val PAYMENT_RESULT         = "fees/payment-result"
    const val NOTIFICATIONS          = "notifications"
    const val NOTIFICATION_PREFS     = "notifications/preferences"
    const val LEAVE_APPLY            = "leave/apply"
    const val LEAVE_HISTORY          = "leave"
    const val LEAVE_DETAIL           = "leave/{leaveId}"
    const val MESSAGES               = "messages"
    const val MESSAGE_THREAD         = "messages/{conversationId}"
    const val ANNOUNCEMENTS          = "announcements"
    const val ANALYTICS              = "analytics"
    const val ANALYTICS_SUBJECT      = "analytics/subject/{subjectId}"
    const val CALENDAR               = "calendar"
}
```

### Bottom Navigation Tabs
- **Tab 1:** Home → `dashboard`
- **Tab 2:** Attendance → `attendance`
- **Tab 3:** Fees → `fees`
- **Tab 4:** Analytics → `analytics`
- **Tab 5:** More → ModalDrawer: Leave, Messages, Calendar, Settings

---

## 8. State Management Pattern

```
ViewModel (commonMain — extends androidx lifecycle ViewModel)
    ↓ emits
StateFlow<ScreenUiState>   ← sealed: Loading | Success(data) | Error(msg)
    ↓ collected by
Composable                 ← collectAsStateWithLifecycle()
```

### Global AppState (Koin singleton)
```kotlin
data class AppState(
    val currentStudentId: String = "",
    val currentStudentName: String = "",
    val currentStudentBatch: String = "",
    val linkedStudents: List<StudentSummary> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isOffline: Boolean = false
)
```

---

## 9. Offline Support Strategy

| Data | Cache | Stale Threshold |
|---|---|---|
| Dashboard | SQLDelight | 5 minutes |
| Attendance | SQLDelight | 60 minutes |
| Fees | SQLDelight (list only) | 60 minutes |
| Notifications | SQLDelight (last 50) | 10 minutes |
| Calendar | SQLDelight | 24 hours |
| Analytics | SQLDelight | 60 minutes |
| Leave apps | Queue in SQLDelight | Sync on reconnect |
| Messages | Queue in SQLDelight | Sync on reconnect |

**UI Rule:** `isOffline=true` → show `OfflineBanner` on every screen. Show
cached data below it. Never show a blank screen.

---

## 10. Screens Master List

### Sprint 1 — Days 1–5 (Mar 18–22)
| # | Screen | Route | Figma |
|---|---|---|---|
| 1 | Login | `/login` | No — spec wireframe |
| 2 | Forgot Password | `/forgot-password` | No — spec wireframe |
| 3 | Dashboard | `/dashboard` | ✅ Figma |
| 4 | Student Switcher | modal | No — bottom sheet |
| 5 | Attendance Summary | `/attendance` | ✅ Figma |
| 6 | Attendance Detail | `/attendance/subject/:id` | ✅ Figma |
| 7 | Fee List | `/fees` | No — spec wireframe |
| 8 | Fee Detail + Payment | `/fees/:id` | No — spec wireframe |
| 9 | Payment Result | `/fees/payment-result` | No — spec wireframe |
| 10 | Notification Centre | `/notifications` | No — spec wireframe |

### Sprint 2 — Days 6–10 (Mar 25–29)
| # | Screen | Route | Figma |
|---|---|---|---|
| 11 | Apply Leave | `/leave/apply` | No — spec wireframe |
| 12 | Leave History | `/leave` | No — spec wireframe |
| 13 | Leave Detail | `/leave/:id` | No — spec wireframe |
| 14 | Conversations | `/messages` | No — spec wireframe |
| 15 | Message Thread | `/messages/:id` | No — spec wireframe |
| 16 | Announcements | `/announcements` | No — spec wireframe |
| 17 | Analytics Dashboard | `/analytics` | No — spec wireframe |
| 18 | Analytics Subject | `/analytics/subject/:id` | No — spec wireframe |
| 19 | Academic Calendar | `/calendar` | Reuse CalendarWidget |
| 20 | Notification Prefs | `/notifications/preferences` | No — spec wireframe |

---

## 11. Explicitly Out of Scope

The agent must NOT build:
- Desktop or tablet-specific layouts
- Admin / teacher-facing screens
- Student login portal
- Web PWA
- Third-party analytics SDKs
- A/B testing or feature flags
- Any screen not in Section 10

---

## 12. Testing Checklist (Days 5 & 10)

- [ ] Login: valid → success, invalid → error, 5 failures → 30s lockout
- [ ] Token auto-refresh: 15 min idle → next API call succeeds silently
- [ ] Student switch: all screens reload with new `studentId`
- [ ] Fee payment Razorpay sandbox end-to-end
- [ ] Push notification deep-links all 6 types
- [ ] Offline: cached data + OfflineBanner on all main screens
- [ ] Leave offline queue: submits when reconnected
- [ ] 150% font scaling: no truncation or overflow
- [ ] Screen reader (TalkBack/VoiceOver): all interactive elements labelled
- [ ] Low-end Android (2GB RAM, 4G): no crash, acceptable performance

---

## 13. What Has Been Built (Updated After Each Day)

> Agent: read this section to understand what already exists before
> adding new code. Update after each day's session is complete.

### After Day 1 _(update when done)_
- [ ] KMP project configured in Android Studio — `com.edmik.parentapp`
- [ ] Dependencies added: Ktor, Koin, SQLDelight, CMP, Serialization, etc.
- [ ] Material 3 theme: `Color.kt`, `Typography.kt`, `Shapes.kt`, `Theme.kt`
- [ ] `TokenManager` — `getAccessToken()`, `setTokens()`, `clearTokens()`
- [ ] `ApiClient` — Ktor, JWT interceptor, auto-refresh on 401
- [ ] Koin modules — `NetworkModule`, `RepositoryModule`, `DatabaseModule`, `AppModule`
- [ ] `AppNavHost` — all 20 routes, `ComingSoonScreen` for unbuilt screens
- [ ] `LoginScreen` + `LoginViewModel` — integrated with `POST /parent/login`
- [ ] `ForgotPasswordScreen` — 3-step OTP flow

### After Day 2 _(update when done)_
- [ ] All 16 components in `commonMain/ui/components/`
- [ ] `AppStateManager` — `StateFlow<AppState>`, `switchStudent()`
- [ ] `NetworkConnectivityObserver` — expect/actual
- [ ] `DashboardScreen` + `DashboardViewModel` — cached to SQLDelight
- [ ] `StudentSwitcherBottomSheet`

### After Day 3 _(update when done)_
- [ ] `AttendanceSummaryScreen` + `AttendanceViewModel`
- [ ] `AttendanceDetailScreen` + `AttendanceDetailViewModel`
- [ ] `DonutChart`, `GaugeChart`, `LineChart` components

### After Day 4 _(update when done)_
- [ ] `FeeListScreen` + `FeeListViewModel`
- [ ] `FeeDetailScreen` + `FeeDetailViewModel`
- [ ] `PaymentResultScreen`
- [ ] `PaymentLauncher` expect/actual

### After Day 5 _(update when done)_
- [ ] All edge states (loading/error/empty) on all Sprint 1 screens
- [ ] Offline cache freshness banners
- [ ] iOS shared module: `compileKotlinIosSimulatorArm64` passing

### After Day 6 _(update when done)_
- [ ] `ApplyLeaveScreen`, `LeaveHistoryScreen`, `LeaveDetailScreen`
- [ ] `FilePicker` expect/actual
- [ ] Offline leave queue in SQLDelight

### After Day 7 _(update when done)_
- [ ] `ConversationsScreen`, `MessageThreadScreen`, `AnnouncementsScreen`
- [ ] Optimistic message sending + offline queue

### After Day 8 _(update when done)_
- [ ] `NotificationCentreScreen`, `NotificationPreferencesScreen`
- [ ] `PushNotificationManager` expect/actual + deep-link routing
- [ ] `AnalyticsDashboardScreen`

### After Day 9 _(update when done)_
- [ ] `AcademicCalendarScreen`, `AnalyticsSubjectDetailScreen`
- [ ] `OfflineSyncManager` — full leave + message queue sync

### After Day 10 _(update when done)_
- [ ] Signed release AAB generated
- [ ] All 20 screens regression-tested
- [ ] Submitted to Play Store internal track
