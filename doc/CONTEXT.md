# CONTEXT.md — Parent App (KMP + Compose Multiplatform)
> **LOCATION:** `ParentAppMultiplatform/doc/CONTEXT.md`
> **LAST UPDATED:** 2026-03-20
>
> **AGENT INSTRUCTION:** Read this file in full before writing a single line of code.
> Every decision here is final. Do not re-derive the architecture. Do not substitute
> libraries. If something is unclear, check `doc/DECISIONS.md` before asking.

---

## 0. Quick Reference Card

| Field | Value |
|---|---|
| App Name | Parent App |
| Package ID | `com.edmik.parentapp` |
| Repo Root | `ParentAppMultiplatform/` |
| Android Min SDK | API 26 (Android 8.0+) |
| iOS Minimum | iOS 16+ |
| Dev API | `https://dev.indivue.in:8080` |
| Prod API | `https://kademinservices.indivue.in` |
| Auth | JWT Bearer — every request |
| Figma | https://www.figma.com/design/RzRLfAzFbVJD466Qzf99hh/Parent-App |
| Sprint 1 | Mar 18–24 · 10 screens |
| Sprint 2 | Mar 25–31 · 10 screens |

---

## 1. Project Overview

**What it is:** A mobile-only Academic Monitoring & Management Platform for parents to track their child's attendance, fees, exams, homework, analytics, leave management, and teacher communication.

**Target users:** Parents aged 28–55. Primarily Android (70–80% of users). Moderate digital literacy. The app must be simple, fast, and trustworthy.

**Who builds it:** 1 frontend developer, full-time, 10 working days.

**Backend strategy:** Backend pushes APIs same-day. Frontend integrates immediately — no mock APIs needed.

---

## 2. Architecture (NON-NEGOTIABLE)

**MVVM + MVI + Clean Architecture on Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP)**

The spec doc lists Android-native Hilt/Retrofit/Room. Those decisions are overridden by the KMP decision (see `doc/DECISIONS.md` D-001 through D-007). All code lives in `commonMain` unless a platform API has no KMP equivalent.

### Layer Responsibilities

```
┌───────────────────────────────────────────────────────────────────┐
│  PRESENTATION  (presentation/)                                    │
│  Screen.kt · ViewModel.kt · State.kt · Event.kt                  │
│  - Screen observes StateFlow<State> from ViewModel               │
│  - User actions sent as Event via onEvent()                      │
│  - ViewModel calls UseCases ONLY — never Repository directly     │
│  - State contains domain models ONLY — never DTOs                │
└───────────────────────┬───────────────────────────────────────────┘
                        │ calls UseCase
┌───────────────────────▼───────────────────────────────────────────┐
│  DOMAIN  (domain/)                                                │
│  model/ · repository/ · usecase/                                 │
│  - Pure Kotlin — zero framework dependencies                     │
│  - Repository interfaces return domain models                    │
│  - UseCase = one class, one operation, operator fun invoke()     │
│  - Domain models have NO @Serializable annotation                │
└───────────────────────┬───────────────────────────────────────────┘
                        │ implements
┌───────────────────────▼───────────────────────────────────────────┐
│  DATA  (data/)                                                    │
│  remote/api · remote/dto · mapper/ · local/ · repository/       │
│  - DTOs are @Serializable — they never leave the data layer      │
│  - data/mapper/ converts DTO → domain model                      │
│  - RepositoryImpl imports from mapper/, returns domain models    │
└───────────────────────────────────────────────────────────────────┘
```

### Dependency Arrow (never reverse this)
```
Presentation → Domain ← Data
```

### Hard Stop Rules — if you are about to break these, stop
- `import com.edmik.parentapp.data.*` inside `presentation/` → **VIOLATION**
- `@Serializable` class inside `State.kt` or `Event.kt` → **VIOLATION**
- ViewModel injects a `Repository` instead of a `UseCase` → **VIOLATION**
- `toDomain()` written inside `RepositoryImpl` instead of `data/mapper/` → **VIOLATION**
- `actual` file in a flat `actual/` folder instead of mirroring the package path → **VIOLATION**

---

## 3. Tech Stack (All Final — Do Not Substitute)

| Layer | Library | Version | Notes |
|---|---|---|---|
| Language | Kotlin | 2.0+ | |
| UI | Compose Multiplatform | 1.7.x | JetBrains CMP |
| Navigation | Navigation Compose CMP | 2.8.x | type-safe routes |
| DI | **Koin** | 4.x | `koin-core` + `koin-compose` — NOT Hilt |
| Networking | **Ktor Client** | 3.x | NOT Retrofit · OkHttp engine (Android), Darwin (iOS) |
| Serialization | Kotlinx Serialization | 1.7+ | NOT Moshi · DTOs only |
| Async | Kotlinx Coroutines | 1.9+ | `StateFlow`, `SharedFlow`, `ViewModel` |
| ViewModel | lifecycle-viewmodel-compose | 2.8.x | KMP-compatible |
| Local DB | **SQLDelight** | 2.x | NOT Room · `.sq` files in `data/local/entity/` |
| Secure Storage | multiplatform-settings (encrypted) | 1.2+ | NOT EncryptedSharedPreferences |
| Image Loading | Coil | 3.x | native CMP support |
| Charts | Canvas (custom Compose) | — | no third-party chart lib |
| Date/Time | Kotlinx DateTime | 0.6+ | NOT java.time |
| Payment | Razorpay (expect/actual) | — | `com.razorpay:checkout:1.6.33` Android |
| Push | FCM + APNs (expect/actual) | — | Firebase Cloud Messaging |
| Biometric | BiometricPrompt / LocalAuthentication | — | expect/actual |

---

## 4. Full Project Folder Structure

```
ParentAppMultiplatform/
├── androidApp/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/com/edmik/parentapp/
│           └── MainActivity.kt              ← setContent { App() } only
│
├── iosApp/
│   └── iosApp/
│       ├── iOSApp.swift
│       └── ContentView.swift               ← ComposeUIViewController wrapper
│
├── shared/
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/
│       │   ├── kotlin/com/edmik/parentapp/
│       │   │   │
│       │   │   ├── data/                                  ← DATA LAYER
│       │   │   │   ├── remote/
│       │   │   │   │   ├── api/                           ← Ktor service interfaces
│       │   │   │   │   │   ├── AuthApiService.kt
│       │   │   │   │   │   ├── DashboardApiService.kt
│       │   │   │   │   │   ├── AttendanceApiService.kt
│       │   │   │   │   │   ├── FeeApiService.kt
│       │   │   │   │   │   ├── LeaveApiService.kt
│       │   │   │   │   │   ├── MessagingApiService.kt
│       │   │   │   │   │   ├── NotificationApiService.kt
│       │   │   │   │   │   ├── AnalyticsApiService.kt
│       │   │   │   │   │   └── HttpClientFactory.kt       ← Ktor client + JWT plugin
│       │   │   │   │   └── dto/                           ← @Serializable DTOs only
│       │   │   │   │       ├── AuthDto.kt
│       │   │   │   │       ├── DashboardDto.kt
│       │   │   │   │       ├── AttendanceDto.kt
│       │   │   │   │       ├── FeeDto.kt
│       │   │   │   │       ├── LeaveDto.kt
│       │   │   │   │       ├── MessageDto.kt
│       │   │   │   │       ├── NotificationDto.kt
│       │   │   │   │       └── AnalyticsDto.kt
│       │   │   │   ├── mapper/                            ← DTO → Domain converters
│       │   │   │   │   ├── DashboardMapper.kt
│       │   │   │   │   ├── AttendanceMapper.kt
│       │   │   │   │   ├── FeeMapper.kt
│       │   │   │   │   ├── LeaveMapper.kt
│       │   │   │   │   ├── MessageMapper.kt
│       │   │   │   │   ├── NotificationMapper.kt
│       │   │   │   │   └── AnalyticsMapper.kt
│       │   │   │   ├── local/
│       │   │   │   │   ├── database/                      ← SQLDelight DB + secure storage
│       │   │   │   │   │   ├── AppDatabase.kt
│       │   │   │   │   │   └── TokenManager.kt            ← multiplatform-settings
│       │   │   │   │   └── entity/                        ← SQLDelight .sq table definitions
│       │   │   │   │       ├── Dashboard.sq
│       │   │   │   │       ├── Attendance.sq
│       │   │   │   │       ├── Fees.sq
│       │   │   │   │       ├── Leave.sq                   ← pending_leave_queue table
│       │   │   │   │       ├── Messages.sq                ← pending_message_queue table
│       │   │   │   │       ├── Calendar.sq
│       │   │   │   │       └── Analytics.sq
│       │   │   │   ├── repository/                        ← Impls (import mapper/, return domain)
│       │   │   │   │   ├── AuthRepositoryImpl.kt
│       │   │   │   │   ├── DashboardRepositoryImpl.kt
│       │   │   │   │   ├── AttendanceRepositoryImpl.kt
│       │   │   │   │   ├── FeeRepositoryImpl.kt
│       │   │   │   │   ├── LeaveRepositoryImpl.kt
│       │   │   │   │   ├── MessagingRepositoryImpl.kt
│       │   │   │   │   ├── NotificationRepositoryImpl.kt
│       │   │   │   │   └── AnalyticsRepositoryImpl.kt
│       │   │   │   ├── platform/                          ← expect declarations
│       │   │   │   │   ├── NetworkConnectivityObserver.kt
│       │   │   │   │   ├── FilePicker.kt
│       │   │   │   │   ├── BiometricAuthManager.kt
│       │   │   │   │   └── PushNotificationManager.kt
│       │   │   │   ├── payment/
│       │   │   │   │   └── PaymentLauncher.kt             ← expect declaration
│       │   │   │   └── sync/
│       │   │   │       └── OfflineSyncManager.kt          ← calls UseCases to drain queues
│       │   │   │
│       │   │   ├── domain/                                ← DOMAIN LAYER — pure Kotlin
│       │   │   │   ├── model/                             ← NO @Serializable
│       │   │   │   │   ├── Student.kt
│       │   │   │   │   ├── Dashboard.kt
│       │   │   │   │   ├── Attendance.kt
│       │   │   │   │   ├── Fee.kt
│       │   │   │   │   ├── Leave.kt
│       │   │   │   │   ├── Message.kt
│       │   │   │   │   ├── Announcement.kt
│       │   │   │   │   ├── Notification.kt
│       │   │   │   │   ├── Analytics.kt
│       │   │   │   │   ├── CalendarEvent.kt
│       │   │   │   │   └── AnalyticsSubject.kt
│       │   │   │   ├── repository/                        ← interfaces, return domain models
│       │   │   │   │   ├── AuthRepository.kt
│       │   │   │   │   ├── DashboardRepository.kt
│       │   │   │   │   ├── AttendanceRepository.kt
│       │   │   │   │   ├── FeeRepository.kt
│       │   │   │   │   ├── LeaveRepository.kt
│       │   │   │   │   ├── MessagingRepository.kt
│       │   │   │   │   ├── NotificationRepository.kt
│       │   │   │   │   └── AnalyticsRepository.kt
│       │   │   │   └── usecase/                           ← 1 class = 1 operation
│       │   │   │       ├── auth/
│       │   │   │       │   ├── LoginUseCase.kt
│       │   │   │       │   └── ForgotPasswordUseCase.kt
│       │   │   │       ├── dashboard/
│       │   │   │       │   ├── GetDashboardUseCase.kt
│       │   │   │       │   └── GetLinkedStudentsUseCase.kt
│       │   │   │       ├── attendance/
│       │   │   │       │   ├── GetAttendanceSummaryUseCase.kt
│       │   │   │       │   ├── GetAttendanceCalendarUseCase.kt
│       │   │   │       │   ├── GetAttendanceTrendsUseCase.kt
│       │   │   │       │   └── GetSubjectAttendanceUseCase.kt
│       │   │   │       ├── fees/
│       │   │   │       │   ├── GetFeeListUseCase.kt
│       │   │   │       │   ├── GetFeeDetailUseCase.kt
│       │   │   │       │   ├── InitiatePaymentUseCase.kt
│       │   │   │       │   └── VerifyPaymentUseCase.kt
│       │   │   │       ├── leave/
│       │   │   │       │   ├── SubmitLeaveUseCase.kt
│       │   │   │       │   ├── GetLeaveHistoryUseCase.kt
│       │   │   │       │   ├── GetLeaveDetailUseCase.kt
│       │   │   │       │   └── CancelLeaveUseCase.kt
│       │   │   │       ├── messages/
│       │   │   │       │   ├── GetConversationsUseCase.kt
│       │   │   │       │   ├── GetMessageThreadUseCase.kt
│       │   │   │       │   ├── SendMessageUseCase.kt
│       │   │   │       │   ├── GetAnnouncementsUseCase.kt
│       │   │   │       │   └── AcknowledgeAnnouncementUseCase.kt
│       │   │   │       ├── notifications/
│       │   │   │       │   ├── GetNotificationsUseCase.kt
│       │   │   │       │   ├── MarkNotificationReadUseCase.kt
│       │   │   │       │   ├── MarkAllReadUseCase.kt
│       │   │   │       │   ├── GetNotificationPreferencesUseCase.kt
│       │   │   │       │   └── SaveNotificationPreferencesUseCase.kt
│       │   │   │       ├── analytics/
│       │   │   │       │   ├── GetAnalyticsDashboardUseCase.kt
│       │   │   │       │   └── GetAnalyticsSubjectUseCase.kt
│       │   │   │       └── calendar/
│       │   │   │           └── GetCalendarUseCase.kt
│       │   │   │
│       │   │   ├── di/                                    ← Koin modules — wiring only
│       │   │   │   ├── NetworkModule.kt
│       │   │   │   ├── RepositoryModule.kt
│       │   │   │   ├── UseCaseModule.kt
│       │   │   │   ├── DatabaseModule.kt
│       │   │   │   └── AppModule.kt
│       │   │   │
│       │   │   ├── presentation/                          ← PRESENTATION LAYER
│       │   │   │   ├── app/                               ← app-level state (NOT in di/)
│       │   │   │   │   ├── AppState.kt
│       │   │   │   │   ├── AppStateManager.kt
│       │   │   │   │   └── App.kt                         ← top-level @Composable
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt
│       │   │   │   │   ├── Typography.kt
│       │   │   │   │   ├── Shapes.kt
│       │   │   │   │   └── Theme.kt
│       │   │   │   ├── components/                        ← 16 shared Composables
│       │   │   │   │   ├── BottomNavBar.kt
│       │   │   │   │   ├── StudentHeader.kt
│       │   │   │   │   ├── BatchSelector.kt
│       │   │   │   │   ├── CalendarWidget.kt
│       │   │   │   │   ├── DonutChart.kt
│       │   │   │   │   ├── GaugeChart.kt
│       │   │   │   │   ├── LineChart.kt
│       │   │   │   │   ├── AlertBanner.kt
│       │   │   │   │   ├── StatusBadge.kt
│       │   │   │   │   ├── ActivityCard.kt
│       │   │   │   │   ├── SubjectCard.kt
│       │   │   │   │   ├── LoadingOverlay.kt
│       │   │   │   │   ├── ErrorState.kt
│       │   │   │   │   ├── EmptyState.kt
│       │   │   │   │   ├── OfflineBanner.kt
│       │   │   │   │   └── PullToRefresh.kt
│       │   │   │   ├── navigation/
│       │   │   │   │   ├── Routes.kt
│       │   │   │   │   └── AppNavHost.kt
│       │   │   │   └── screens/                           ← one folder per screen group
│       │   │   │       ├── login/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   └── OtpInputRow.kt
│       │   │   │       │   ├── LoginScreen.kt
│       │   │   │       │   ├── LoginViewModel.kt
│       │   │   │       │   ├── LoginState.kt
│       │   │   │       │   └── LoginEvent.kt
│       │   │   │       ├── forgot_password/
│       │   │   │       │   ├── ForgotPasswordScreen.kt
│       │   │   │       │   ├── ForgotPasswordViewModel.kt
│       │   │   │       │   ├── ForgotPasswordState.kt
│       │   │   │       │   └── ForgotPasswordEvent.kt
│       │   │   │       ├── home/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   └── StatCard.kt
│       │   │   │       │   ├── DashboardScreen.kt
│       │   │   │       │   ├── DashboardViewModel.kt
│       │   │   │       │   ├── DashboardState.kt
│       │   │   │       │   ├── DashboardEvent.kt
│       │   │   │       │   └── StudentSwitcherBottomSheet.kt
│       │   │   │       ├── attendance/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   ├── SubjectProgressRow.kt
│       │   │   │       │   │   └── AttendanceDayCell.kt
│       │   │   │       │   ├── AttendanceSummaryScreen.kt
│       │   │   │       │   ├── AttendanceSummaryViewModel.kt
│       │   │   │       │   ├── AttendanceSummaryState.kt
│       │   │   │       │   ├── AttendanceSummaryEvent.kt
│       │   │   │       │   ├── AttendanceDetailScreen.kt
│       │   │   │       │   ├── AttendanceDetailViewModel.kt
│       │   │   │       │   ├── AttendanceDetailState.kt
│       │   │   │       │   └── AttendanceDetailEvent.kt
│       │   │   │       ├── fees/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   ├── FeeItemCard.kt
│       │   │   │       │   │   └── FeeBreakdownRow.kt
│       │   │   │       │   ├── FeeListScreen.kt
│       │   │   │       │   ├── FeeListViewModel.kt
│       │   │   │       │   ├── FeeListState.kt
│       │   │   │       │   ├── FeeListEvent.kt
│       │   │   │       │   ├── FeeDetailScreen.kt
│       │   │   │       │   ├── FeeDetailViewModel.kt
│       │   │   │       │   ├── FeeDetailState.kt
│       │   │   │       │   ├── FeeDetailEvent.kt
│       │   │   │       │   └── PaymentResultScreen.kt
│       │   │   │       ├── leave/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   ├── LeaveStatusTimeline.kt
│       │   │   │       │   │   └── LeaveTypeDropdown.kt
│       │   │   │       │   ├── ApplyLeaveScreen.kt
│       │   │   │       │   ├── ApplyLeaveViewModel.kt
│       │   │   │       │   ├── ApplyLeaveState.kt
│       │   │   │       │   ├── ApplyLeaveEvent.kt
│       │   │   │       │   ├── LeaveHistoryScreen.kt
│       │   │   │       │   ├── LeaveHistoryViewModel.kt
│       │   │   │       │   ├── LeaveHistoryState.kt
│       │   │   │       │   ├── LeaveHistoryEvent.kt
│       │   │   │       │   ├── LeaveDetailScreen.kt
│       │   │   │       │   ├── LeaveDetailViewModel.kt
│       │   │   │       │   ├── LeaveDetailState.kt
│       │   │   │       │   └── LeaveDetailEvent.kt
│       │   │   │       ├── messages/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   ├── MessageBubble.kt
│       │   │   │       │   │   ├── ConversationCard.kt
│       │   │   │       │   │   └── MessageInputBar.kt
│       │   │   │       │   ├── ConversationsScreen.kt
│       │   │   │       │   ├── ConversationsViewModel.kt
│       │   │   │       │   ├── ConversationsState.kt
│       │   │   │       │   ├── ConversationsEvent.kt
│       │   │   │       │   ├── MessageThreadScreen.kt
│       │   │   │       │   ├── MessageThreadViewModel.kt
│       │   │   │       │   ├── MessageThreadState.kt
│       │   │   │       │   ├── MessageThreadEvent.kt
│       │   │   │       │   ├── AnnouncementsScreen.kt
│       │   │   │       │   ├── AnnouncementsViewModel.kt
│       │   │   │       │   ├── AnnouncementsState.kt
│       │   │   │       │   └── AnnouncementsEvent.kt
│       │   │   │       ├── analytics/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   ├── SubjectPerformanceCard.kt
│       │   │   │       │   │   └── ClassAvgBars.kt
│       │   │   │       │   ├── AnalyticsDashboardScreen.kt
│       │   │   │       │   ├── AnalyticsDashboardViewModel.kt
│       │   │   │       │   ├── AnalyticsDashboardState.kt
│       │   │   │       │   ├── AnalyticsDashboardEvent.kt
│       │   │   │       │   ├── AnalyticsSubjectDetailScreen.kt
│       │   │   │       │   ├── AnalyticsSubjectDetailViewModel.kt
│       │   │   │       │   ├── AnalyticsSubjectDetailState.kt
│       │   │   │       │   └── AnalyticsSubjectDetailEvent.kt
│       │   │   │       ├── calendar/
│       │   │   │       │   ├── components/
│       │   │   │       │   │   └── EventListBottomSheet.kt
│       │   │   │       │   ├── AcademicCalendarScreen.kt
│       │   │   │       │   ├── AcademicCalendarViewModel.kt
│       │   │   │       │   ├── AcademicCalendarState.kt
│       │   │   │       │   └── AcademicCalendarEvent.kt
│       │   │   │       └── notifications/
│       │   │   │           ├── components/
│       │   │   │           │   └── NotificationItemRow.kt
│       │   │   │           ├── NotificationCentreScreen.kt
│       │   │   │           ├── NotificationCentreViewModel.kt
│       │   │   │           ├── NotificationCentreState.kt
│       │   │   │           ├── NotificationCentreEvent.kt
│       │   │   │           ├── NotificationPreferencesScreen.kt
│       │   │   │           ├── NotificationPreferencesViewModel.kt
│       │   │   │           ├── NotificationPreferencesState.kt
│       │   │   │           └── NotificationPreferencesEvent.kt
│       │   │   │
│       │   │   └── util/
│       │   │       ├── Strings.kt
│       │   │       ├── DateFormatter.kt
│       │   │       ├── Extensions.kt
│       │   │       └── Constants.kt
│       │   │
│       │   └── sqldelight/parentapp/               ← SQLDelight source set
│       │       └── (mirrors data/local/entity/ .sq files)
│       │
│       ├── androidMain/kotlin/com/edmik/parentapp/
│       │   ├── data/payment/PaymentLauncher.android.kt
│       │   ├── data/platform/
│       │   │   ├── NetworkConnectivityObserver.android.kt
│       │   │   ├── FilePicker.android.kt
│       │   │   ├── BiometricAuthManager.android.kt
│       │   │   └── PushNotificationManager.android.kt
│       │   └── di/AndroidDatabaseModule.kt
│       │
│       └── iosMain/kotlin/com/edmik/parentapp/
│           ├── data/payment/PaymentLauncher.ios.kt
│           ├── data/platform/
│           │   ├── NetworkConnectivityObserver.ios.kt
│           │   ├── FilePicker.ios.kt
│           │   ├── BiometricAuthManager.ios.kt
│           │   └── PushNotificationManager.ios.kt
│           ├── di/IosDatabaseModule.kt
│           └── presentation/app/MainViewController.kt
│
├── gradle/libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

> **KMP expect/actual rule:** `actual` files must be in the same **package** as their `expect`.
> File path in `androidMain`/`iosMain` mirrors the `commonMain` package path exactly.
> `expect class PaymentLauncher` in `data.payment` → actual at `.../data/payment/PaymentLauncher.android.kt`

---

## 5. MVI Pattern — Every Screen Follows This

### State.kt — current screen snapshot (data class)
```kotlin
// presentation/screens/home/DashboardState.kt
data class DashboardState(
    val isLoading: Boolean = false,
    val data: DashboardData? = null,      // domain model ✅ — never DashboardDto
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val cacheAgeMessage: String? = null   // "Last updated 7 min ago" or null
)
```

### Event.kt — user intentions dispatched to ViewModel
```kotlin
// presentation/screens/home/DashboardEvent.kt
sealed class DashboardEvent {
    object Load : DashboardEvent()
    object Refresh : DashboardEvent()
    data class SwitchStudent(val studentId: String) : DashboardEvent()
    object BellTapped : DashboardEvent()
}
```

### ViewModel — dispatcher + StateFlow
```kotlin
// presentation/screens/home/DashboardViewModel.kt
class DashboardViewModel(
    private val getDashboard: GetDashboardUseCase,    // UseCase — NOT Repository
    private val appState: AppStateManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun onEvent(event: DashboardEvent) = when (event) {
        DashboardEvent.Load, DashboardEvent.Refresh -> load()
        is DashboardEvent.SwitchStudent -> appState.switchStudent(event.studentId)
        DashboardEvent.BellTapped -> { /* navigation in Screen */ }
    }

    private fun load() { viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        getDashboard(appState.state.value.currentStudentId)
            .onSuccess { data -> _state.update { it.copy(isLoading = false, data = data) } }
            .onFailure { e -> _state.update { it.copy(isLoading = false, errorMessage = e.message) } }
    }}
}
```

### Screen — collects state, dispatches events, handles navigation
```kotlin
@Composable
fun DashboardScreen(vm: DashboardViewModel = koinViewModel(), onNavigate: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.onEvent(DashboardEvent.Load) }
    DashboardContent(state = state, onEvent = vm::onEvent, onNavigate = onNavigate)
}
```

### Mapper — standalone file in data/mapper/
```kotlin
// data/mapper/DashboardMapper.kt
fun DashboardDto.toDomain() = DashboardData(
    student = student.toDomain(),
    attendancePercentage = attendancePercentage,
    pendingFeesAmount = pendingFeesAmount,
    // ...
)
fun StudentInfoDto.toDomain() = Student(id, name, batch, profilePhotoUrl)
```

```kotlin
// data/repository/DashboardRepositoryImpl.kt
import com.edmik.parentapp.data.mapper.toDomain  // ← import from mapper

override suspend fun getDashboard(studentId: String): Result<DashboardData> =
    runCatching { api.getDashboard(studentId).toDomain() }
```

---

## 6. Global AppState

```kotlin
// presentation/app/AppState.kt
data class AppState(
    val currentStudentId: String = "",
    val currentStudentName: String = "",
    val currentStudentBatch: String = "",
    val linkedStudents: List<Student> = emptyList(),  // domain model
    val unreadNotificationCount: Int = 0,
    val isOffline: Boolean = false
)
// AppStateManager is a Koin singleton living in presentation/app/ — NOT in di/
```

---

## 7. All 36 API Endpoints

### Base URLs
```
Dev  : https://dev.indivue.in:8080
Prod : https://kademinservices.indivue.in
Auth : Authorization: Bearer <access_token>
```
Token auto-refresh via Ktor `bearerTokens { }` plugin on HTTP 401.

### Endpoints by Feature

```
AUTH (3)
  POST  /parent/login
  POST  /parent/forgot-password
  POST  /parent/refresh-token

DASHBOARD & STUDENTS (3)
  GET   /parent/dashboard?studentId={id}
  GET   /parent/students
  POST  /parent/device/register

ATTENDANCE (5)
  GET   /parent/attendance/summary?studentId={id}
  GET   /parent/attendance/subject/{subjectId}?studentId={id}
  GET   /parent/attendance/calendar?studentId={id}&month={m}&year={y}
  GET   /parent/attendance/trends?studentId={id}
  GET   /parent/attendance/today?studentId={id}

FEES (6)
  GET   /parent/fees?studentId={id}
  GET   /parent/fees/{feeId}
  POST  /parent/fees/pay
  POST  /parent/fees/pay/verify
  GET   /parent/fees/{feeId}/receipt
  GET   /parent/fees/{feeId}/history

LEAVE (5)
  POST  /parent/leave/apply            ← multipart/form-data (file optional)
  GET   /parent/leave/history?studentId={id}
  GET   /parent/leave/{leaveId}
  DELETE /parent/leave/{leaveId}
  POST  /parent/leave/{leaveId}/document

MESSAGES & ANNOUNCEMENTS (6)
  GET   /parent/messages?studentId={id}
  GET   /parent/messages/{conversationId}
  POST  /parent/messages
  GET   /parent/announcements?studentId={id}
  GET   /parent/announcements/{id}
  POST  /parent/announcements/{id}/acknowledge

NOTIFICATIONS (7)
  GET   /parent/notifications?studentId={id}&page={p}&size={s}
  PUT   /parent/notifications/read-all
  PUT   /parent/notifications/{id}/read
  GET   /parent/notifications/preferences
  PUT   /parent/notifications/preferences
  POST  /parent/device/register
  DELETE /parent/device/{deviceId}

ANALYTICS (2)
  GET   /pulse/analytics/parent/dashboard?studentId={id}   ⚠️ /pulse/ prefix
  GET   /parent/analytics/subject/{subjectId}?studentId={id}

CALENDAR (1)
  GET   /parent/calendar?studentId={id}&month={m}&year={y}
```

---

## 8. Design System

### Colors
```kotlin
// presentation/theme/Color.kt
val PrimaryBlue          = Color(0xFF2196F3)   // active tabs, links
val PrimaryBlueDark      = Color(0xFF1976D2)   // active tab fill, buttons
val BackgroundGradStart  = Color(0xFFE8F4FD)   // screen gradient start
val BackgroundGradEnd    = Color(0xFFFFFFFF)   // screen gradient end
val CardBackground       = Color(0xFFFFFFFF)   // cards
val BackgroundLight      = Color(0xFFF5F5F5)   // page background
val ColorPresent         = Color(0xFF4CAF50)   // present / paid / success / approved
val ColorAbsent          = Color(0xFFF44336)   // absent / overdue / rejected / error
val ColorLeave           = Color(0xFFFFC107)   // leave / warning
val ColorPending         = Color(0xFFFF9800)   // pending / attention
val AlertBannerBg        = Color(0xFFFFEBEE)   // low-attendance alert background
val TodayStatusBg        = Color(0xFFE8F5E9)   // "Today: Present" card bg
val BatchSelectorBg      = Color(0xFFE3F2FD)   // batch pill background
val TextPrimary          = Color(0xFF212121)   // headings, primary text
val TextSecondary        = Color(0xFF757575)   // captions, date labels
```

### Status Colour Semantics
| Colour | Meaning |
|---|---|
| `#4CAF50` Green | Present / Paid / Approved / Success |
| `#FF9800` Amber | Pending / Under Review / Warning |
| `#F44336` Red | Absent / Overdue / Rejected / Error |
| `#1976D2` Blue | Selected / Active / Primary action |
| `#9E9E9E` Grey | Holiday / Inactive / Read |

### Typography
```
Header  : 18–20sp Bold      (screen titles)
Body    : 14–16sp Regular   (content)
Caption : 12sp Light        (dates, labels, tab text)
```

### Spacing & Touch Targets
```
Min touch target : 44×44 dp
Card padding     : 16 dp
List item min h  : 56 dp
Bottom nav h     : 56 dp
Font scaling     : Must not break at 150% system font size
```

### Key Components (from Figma)
| Component | Description |
|---|---|
| `BottomNavBar` | 5 tabs: Home, Attendance, Fees, Analytics, More(drawer) — blue active, grey inactive |
| `StudentHeader` | 40dp avatar circle + name bold + batch caption + notification bell with badge |
| `BatchSelector` | Rounded pill, graduation cap icon, batch name, "Tap to change Batch", chevron |
| `CalendarWidget` | 7-col SUN–SAT grid, today=blue outline, event dots (blue/red/orange/green/grey), nav arrows |
| `DonutChart` | 3 segments (present=green, absent=red, leave=yellow), center=total count, legend right |
| `GaugeChart` | Semi-circle progress indicator showing attendance % |
| `AlertBanner` | `#FFEBEE` bg, warning triangle icon, bold title + body text |
| `ActivityCard` | Coloured circle icon + title + date + type tag (Exam=red, Homework=blue, Fees=teal) |
| `SubjectCard` | Side-by-side cards — subject name + Present/Absent badge + time |

---

## 9. Navigation Routes

```kotlin
// presentation/navigation/Routes.kt
object Routes {
    const val LOGIN                = "login"
    const val FORGOT_PASSWORD      = "forgot-password"
    const val DASHBOARD            = "dashboard"
    const val ATTENDANCE           = "attendance"
    const val ATTENDANCE_SUBJECT   = "attendance/subject/{subjectId}"
    const val FEES                 = "fees"
    const val FEE_DETAIL           = "fees/{feeId}"
    const val PAYMENT_RESULT       = "fees/payment-result"
    const val LEAVE_APPLY          = "leave/apply"
    const val LEAVE_HISTORY        = "leave"
    const val LEAVE_DETAIL         = "leave/{leaveId}"
    const val MESSAGES             = "messages"
    const val MESSAGE_THREAD       = "messages/{conversationId}"
    const val ANNOUNCEMENTS        = "announcements"
    const val ANALYTICS            = "analytics"
    const val ANALYTICS_SUBJECT    = "analytics/subject/{subjectId}"
    const val CALENDAR             = "calendar"
    const val NOTIFICATIONS        = "notifications"
    const val NOTIFICATION_PREFS   = "notifications/preferences"
}
```

---

## 10. All 20 Screens — Full Specifications

### Sprint 1 (Days 1–5 · Mar 18–22)

| # | Screen | Route | Figma | Day |
|---|---|---|---|---|
| 1 | Login | `/login` | spec wireframe | Day 1 |
| 2 | Forgot Password | `/forgot-password` | spec wireframe | Day 1 |
| 3 | Dashboard | `/dashboard` | ✅ Figma | Day 2 |
| 4 | Student Switcher | modal (bottom sheet) | spec wireframe | Day 2 |
| 5 | Attendance Summary | `/attendance` | ✅ Figma | Day 3 |
| 6 | Attendance Detail | `/attendance/subject/:id` | ✅ Figma | Day 3 |
| 7 | Fee List | `/fees` | spec wireframe | Day 4 |
| 8 | Fee Detail + Payment | `/fees/:id` | spec wireframe | Day 4 |
| 9 | Payment Result | `/fees/payment-result` | spec wireframe | Day 4 |
| 10 | Notification Centre | `/notifications` | spec wireframe | Day 4–5 |

### Sprint 2 (Days 6–10 · Mar 25–29)

| # | Screen | Route | Figma | Day |
|---|---|---|---|---|
| 11 | Apply Leave | `/leave/apply` | spec wireframe | Day 6 |
| 12 | Leave History | `/leave` | spec wireframe | Day 6 |
| 13 | Leave Detail | `/leave/:id` | spec wireframe | Day 6 |
| 14 | Conversations | `/messages` | spec wireframe | Day 7 |
| 15 | Message Thread | `/messages/:id` | spec wireframe | Day 7 |
| 16 | Announcements | `/announcements` | spec wireframe | Day 7 |
| 17 | Analytics Dashboard | `/analytics` | spec wireframe | Day 8 |
| 18 | Analytics Subject Detail | `/analytics/subject/:id` | spec wireframe | Day 9 |
| 19 | Academic Calendar | `/calendar` | reuse CalendarWidget | Day 9 |
| 20 | Notification Preferences | `/notifications/preferences` | spec wireframe | Day 8 |

---

### Screen 1: Login
**API:** `POST /parent/login`
- App logo centred, Student ID field, Password field (show/hide toggle)
- "Remember Me" checkbox, Login button (primary, full-width), "Forgot Password?" link
- Biometric login button — shown only after first successful login
- On success: store JWT tokens (access in memory + secure storage, refresh in secure storage), navigate to Dashboard
- Rate limit: disable Login button 30 seconds after 5 failed attempts

### Screen 2: Forgot Password
**API:** `POST /parent/forgot-password`
- Enter registered mobile/email → Send OTP button
- 6-digit OTP input (shown after OTP sent)
- New password + Confirm password fields → Reset button

### Screen 3: Dashboard
**API:** `GET /parent/dashboard?studentId={id}`
- StudentHeader (avatar + name + notification bell badge)
- BatchSelector (batch pill, tap to open Student Switcher)
- 4 stat cards in a row: Attendance %, Pending Fees (₹), Upcoming Exams, Homework Due
- CalendarWidget (monthly, lecture=blue, exam=red, homework=orange, fees=green)
- Recent Activity feed (scrollable, ActivityCard per item)
- BottomNavBar (Home | Attendance | Fees | Analytics | More drawer)

### Screen 4: Student Profile Switcher
**API:** `GET /parent/students`
- ModalBottomSheet: list of linked students with photo, name, batch
- Currently active student highlighted (blue checkmark)
- "Link Another Student" button at bottom
- Tapping switches `AppState.currentStudentId` → reloads Dashboard

### Screen 5: Attendance Summary
**API:** `GET /parent/attendance/summary?studentId={id}`
- GaugeChart showing overall percentage + "Including Leave" toggle switch (green when ON)
- Present / Absent / Leave count
- Subject-wise list: name + LinearProgressIndicator + % badge
  - ≥85%: green · 75–84%: amber · <75%: red
- AlertBanner (visible when any subject <75%): "English: 68% — Attend N more classes to reach 75%"
  - Formula: `N = ceil((0.75 × totalClasses - presentCount) / 0.25)`
- Toggle: Calendar View / Trend View

**Calendar View:** `GET /parent/attendance/calendar?studentId={id}&month={m}&year={y}`
- Monthly grid: Present=green, Absent=red, Holiday=grey, Leave=blue

**Trend View:** `GET /parent/attendance/trends?studentId={id}`
- LineChart: weekly attendance % over last 3 months

### Screen 6: Attendance Detail (Subject)
**API:** `GET /parent/attendance/subject/{subjectId}?studentId={id}`
- Subject name + overall %
- DonutChart (present/absent/leave segments for this subject)
- Day-by-day table: Date | Period | Status (Present=green, Absent=red, Leave=blue)
- Monthly trend LineChart

### Screen 7: Fee List
**API:** `GET /parent/fees?studentId={id}`
- Summary card: Total / Paid / Pending (Indian format ₹1,20,000)
- Fee cards with StatusBadge: PAID=green, PENDING=amber, OVERDUE=red
- "Pay Now" button on PENDING/OVERDUE; "Download Receipt" on PAID

### Screen 8: Fee Detail + Payment
**API:** `GET /parent/fees/{feeId}` · `POST /parent/fees/pay` · `POST /parent/fees/pay/verify`
- Breakdown: base amount, tax, late fee, total
- Payment history list
- "Pay Now" button → Razorpay checkout
- Payment flow:
  1. `POST /parent/fees/pay` → get `razorpayOrderId`
  2. Launch Razorpay SDK
  3. On success callback → `POST /parent/fees/pay/verify`
  4. Navigate to Payment Result

### Screen 9: Payment Result
- **Success:** green checkmark (animated), transaction ID, "Download Receipt" button
- **Failure:** red ✕, error message, "Retry" button

### Screen 10: Notification Centre
**API:** `GET /parent/notifications?studentId={id}&page=0&size=20`
- Paginated list, newest first — icon by category, title, body, timestamp, unread indicator
- Tap → deep-link to relevant screen
- "Mark All Read" in header
- Pull-to-refresh
- Unread badge on BottomNavBar bell

### Screen 11: Apply Leave
**API:** `POST /parent/leave/apply` (multipart/form-data)
- Leave type dropdown: Sick / Family Emergency / Personal / Other
- Date range picker (start + end date, start must not be in past)
- Reason textarea (min 10 chars, char counter)
- Attach document button (optional, max 5 MB, PDF/JPG/PNG)
- Submit button (disabled until all fields valid)
- Confirmation dialog before submission

### Screen 12: Leave History
**API:** `GET /parent/leave/history?studentId={id}`
- Filter tabs: All / Pending / Approved / Rejected (client-side filter, no re-fetch)
- Leave cards: type + dates + StatusBadge + submission date

### Screen 13: Leave Detail
**API:** `GET /parent/leave/{leaveId}`
- Full details: type, dates, reason, attachment link
- Status timeline: Submitted → Under Review → Approved/Rejected
- Reviewer name + remarks (if reviewed)
- Cancel button: visible ONLY when `status == SUBMITTED`

### Screen 14: Conversations List
**API:** `GET /parent/messages?studentId={id}`
- Conversation cards: participant name/role + last message preview + timestamp + unread badge
- "New Message" FAB, search bar (client-side filter)

### Screen 15: Message Thread
**API:** `GET /parent/messages/{conversationId}` · `POST /parent/messages`
- Chat bubbles: parent=right (blue `#1976D2`), teacher/admin=left (grey `#F5F5F5`)
- Date separators between different days
- Text input + Send button (disabled when empty) + attach icon
- Read receipts (double tick)
- Auto-scroll to latest; `LazyColumn(reverseLayout = true)`
- Optimistic send: message appears immediately, state: SENDING → SENT / FAILED

### Screen 16: Announcements
**API:** `GET /parent/announcements?studentId={id}` · `POST /parent/announcements/{id}/acknowledge`
- List: title + date + 2-line preview + "NEW" badge when unacknowledged
- Detail: full body + attachment links (open in browser)
- "Acknowledge" button → changes to "✓ Acknowledged" on success

### Screen 17: Analytics Dashboard
**API:** `GET /pulse/analytics/parent/dashboard?studentId={id}` ⚠️ `/pulse/` prefix
- Overall performance summary text
- Subject cards (LazyRow): subject name + score % + trend arrow (UP=green↑, DOWN=red↓, STABLE=grey→)
- Weak areas list
- Progress trend LineChart (weekly scores)

### Screen 18: Analytics Subject Detail
**API:** `GET /parent/analytics/subject/{subjectId}?studentId={id}`
- Score history table: test name | date | score/maxScore | %
- Score trend LineChart
- Class average comparison (student bar vs class avg bar)
- Teacher remarks card (visible only when data present)

### Screen 19: Academic Calendar
**API:** `GET /parent/calendar?studentId={id}&month={m}&year={y}`
- Full-screen CalendarWidget
- Event dot legend: Lecture=blue, Exam=red, Homework=orange, Fees=green, Holiday=grey
- Tap a day → ModalBottomSheet listing events for that day

### Screen 20: Notification Preferences
**API:** `GET /parent/notifications/preferences` · `PUT /parent/notifications/preferences`
- Per-category toggles: Fee Reminders, Attendance Alerts, Exam Schedule, Homework Due, Leave Updates, Announcements
- Quiet hours section: master switch + start/end time pickers (enabled only when switch ON)
- "Save" button → show success Snackbar

---

## 11. Offline Strategy

| Data | Cache Location | Stale Threshold | Offline Behaviour |
|---|---|---|---|
| Dashboard | SQLDelight `cached_dashboard` | 5 minutes | Show cache + `OfflineBanner` |
| Attendance | SQLDelight `cached_attendance_*` | 60 minutes | Show cache + `OfflineBanner` |
| Fees | SQLDelight `cached_fee_list` | 60 minutes | Show cache + `OfflineBanner` + disable Pay Now |
| Notifications | SQLDelight `cached_notifications` | 10 minutes | Show cache + `OfflineBanner` |
| Calendar | SQLDelight `cached_calendar` | 24 hours | Show cache + `OfflineBanner` |
| Analytics | SQLDelight `cached_analytics_*` | 60 minutes | Show cache + `OfflineBanner` |
| Leave submissions | `pending_leave_queue` | sync on reconnect | Save locally + Snackbar |
| Messages sent | `pending_message_queue` | sync on reconnect | Optimistic UI + sync |

**Banner rule:** `AppState.isOffline == true` → `OfflineBanner` shown on every screen. Never show a blank screen when cached data exists.

**Stale cache rule:** `(now - cachedAt) > threshold && isOnline` → auto-refresh silently + show `"Last updated X min ago"` banner until fresh data arrives.

---

## 12. Push Notifications

**Setup:**
1. Create Firebase project (or use existing Edmik Firebase project)
2. Add Android + iOS apps in Firebase console
3. `google-services.json` → `androidApp/`, `GoogleService-Info.plist` → `iosApp/`
4. On first launch: request permission → get FCM token
5. Register: `POST /parent/device/register { deviceToken, platform }`
6. Tap handler → deep-link routing in `AppNavHost.kt`

**Deep-link routing by notification type:**
| Type | Navigate to |
|---|---|
| `FEE_DUE` | `Routes.FEE_DETAIL` (with feeId) |
| `ATTENDANCE_LOW` | `Routes.ATTENDANCE` |
| `LEAVE_STATUS` | `Routes.LEAVE_DETAIL` (with leaveId) |
| `NEW_MESSAGE` | `Routes.MESSAGE_THREAD` (with conversationId) |
| `ANNOUNCEMENT` | `Routes.ANNOUNCEMENTS` |
| `EXAM_SCHEDULE` | `Routes.CALENDAR` |

---

## 13. Payment Integration

**Razorpay Android:** `com.razorpay:checkout:1.6.33` via expect/actual `PaymentLauncher`

```
data class PaymentRequest(orderId, amount, currency, description, studentName, studentEmail)
sealed class PaymentOutcome {
  data class Success(paymentId, orderId, signature)
  data class Failure(errorCode, errorMessage)
  object Cancelled
}
expect class PaymentLauncher {
  suspend fun launch(request: PaymentRequest): PaymentOutcome
}
```

iOS stub until Razorpay iOS SDK is integrated (see `doc/DECISIONS.md` Q-003).

---

## 14. Accessibility Requirements (WCAG 2.1 AA)

- Contrast ratio ≥ 4.5:1 for all body text
- All interactive elements have `contentDescription` for screen readers
- Focus indicators visible
- Error messages announced to screen readers
- 150% font scaling: no text truncation or layout overflow
- Minimum touch target: 44×44 dp on all tappable elements

---

## 15. Testing Checklist (Sprint End)

- [ ] Login: valid credentials → Dashboard; invalid → error toast
- [ ] 5 failed logins → 30s countdown lockout
- [ ] Token auto-refresh: 15 min idle → next API call succeeds silently
- [ ] Multi-student switch: ALL screens reload with new `studentId`
- [ ] Fee payment: Razorpay sandbox end-to-end on Android device
- [ ] Payment failure/cancel: correct result screen shown
- [ ] Push notifications received and deep-link correctly (all 6 types)
- [ ] Offline mode: cached data + OfflineBanner on all main screens
- [ ] Leave offline queue: submits automatically when reconnected
- [ ] Message offline queue: sends automatically when reconnected
- [ ] 150% font scaling: no overflow on any of 20 screens
- [ ] Screen reader: all interactive elements labelled
- [ ] Low-end Android (2 GB RAM, 4G): no crash
- [ ] Both compile checks pass:
  - `./gradlew :shared:compileKotlinAndroid`
  - `./gradlew :shared:compileKotlinIosSimulatorArm64`
- [ ] `grep -r "import android\." shared/src/commonMain/` → empty
- [ ] `grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/` → empty

---

## 16. What Has Been Built (Update After Each Day)

### Day 1 — Sprint 1 _(check when done)_
- [ ] Project configured — `com.edmik.parentapp`, all KMP targets
- [ ] `libs.versions.toml` — all dependencies declared
- [ ] Material 3 theme in `presentation/theme/`
- [ ] `HttpClientFactory.kt` in `data/remote/api/` — Ktor + JWT bearerTokens
- [ ] `TokenManager.kt` in `data/local/database/` — multiplatform-settings
- [ ] Koin modules — `NetworkModule`, `RepositoryModule`, `UseCaseModule`, `DatabaseModule`, `AppModule`
- [ ] `AppNavHost.kt` in `presentation/navigation/` — all 20 routes, `ComingSoonScreen` for unbuilt
- [ ] `screens/login/` — `LoginScreen`, `LoginViewModel`, `LoginState`, `LoginEvent`
- [ ] `LoginUseCase` + `AuthRepository` interface + `AuthRepositoryImpl` + `AuthDto.kt`
- [ ] `screens/forgot_password/` — `ForgotPasswordScreen`, State, Event

### Day 2 — Sprint 1 _(check when done)_
- [ ] `domain/model/Student.kt` + `domain/model/Dashboard.kt`
- [ ] `AppState.kt` + `AppStateManager.kt` in `presentation/app/`
- [ ] `NetworkConnectivityObserver` expect/actual in `data/platform/`
- [ ] All 16 shared components in `presentation/components/`
- [ ] `GetDashboardUseCase` + `GetLinkedStudentsUseCase`
- [ ] `DashboardRepository` interface + `DashboardRepositoryImpl`
- [ ] `DashboardMapper.kt` in `data/mapper/`
- [ ] `DashboardDto.kt` in `data/remote/dto/`
- [ ] `DashboardApiService.kt` in `data/remote/api/`
- [ ] `Dashboard.sq` in `data/local/entity/`
- [ ] `screens/home/` — `DashboardScreen`, `DashboardViewModel`, `DashboardState`, `DashboardEvent`, `StudentSwitcherBottomSheet`
- [ ] `screens/home/components/StatCard.kt`

### Day 3 — Sprint 1 _(check when done)_
- [ ] `domain/model/Attendance.kt` — AttendanceSummary, AttendanceStatus enum
- [ ] `AttendanceDto.kt` + `AttendanceMapper.kt` + `AttendanceApiService.kt`
- [ ] 4 Attendance UseCases + `AttendanceRepository` interface + Impl
- [ ] `Attendance.sq` in `data/local/entity/`
- [ ] `screens/attendance/` — all screens, ViewModels, State, Event
- [ ] `screens/attendance/components/` — `SubjectProgressRow`, `AttendanceDayCell`

### Day 4 — Sprint 1 _(check when done)_
- [ ] `domain/model/Fee.kt` — FeeListData, FeeStatus enum
- [ ] `FeeDto.kt` + `FeeMapper.kt` + `FeeApiService.kt`
- [ ] 4 Fee UseCases + `FeeRepository` interface + Impl
- [ ] `PaymentLauncher` expect/actual in `data/payment/`
- [ ] `Fees.sq` in `data/local/entity/`
- [ ] `screens/fees/` — all screens, ViewModels, State, Event
- [ ] `screens/fees/components/` — `FeeItemCard`, `FeeBreakdownRow`
- [ ] `NotificationCentreScreen` + State + Event (basic — no push yet)

### Day 5 — Sprint 1 polish _(check when done)_
- [ ] All loading/error/empty states on Sprint 1 screens
- [ ] Pull-to-refresh on all list screens
- [ ] OfflineBanner on all main screens
- [ ] `Strings.kt` in `util/` — no hardcoded strings in Composables
- [ ] `@Immutable` on all domain model data classes
- [ ] 150% font scaling verified
- [ ] iOS compile: `compileKotlinIosSimulatorArm64` passing

### Day 6 — Sprint 2 _(check when done)_
- [ ] `domain/model/Leave.kt` — LeaveType enum, LeaveStatus enum
- [ ] `LeaveDto.kt` + `LeaveMapper.kt` + `LeaveApiService.kt`
- [ ] 4 Leave UseCases + `LeaveRepository` interface + Impl
- [ ] `FilePicker` expect/actual in `data/platform/`
- [ ] `Leave.sq` in `data/local/entity/` — includes `pending_leave_queue`
- [ ] `screens/leave/` — all screens, ViewModels, State, Event
- [ ] `screens/leave/components/` — `LeaveStatusTimeline`, `LeaveTypeDropdown`

### Day 7 — Sprint 2 _(check when done)_
- [ ] `domain/model/Message.kt` + `domain/model/Announcement.kt`
- [ ] `MessageDto.kt` + `MessageMapper.kt` + `MessagingApiService.kt`
- [ ] 5 Messaging UseCases + `MessagingRepository` interface + Impl
- [ ] `Messages.sq` — includes `pending_message_queue`
- [ ] `screens/messages/` — all screens, ViewModels, State, Event
- [ ] `screens/messages/components/` — `MessageBubble`, `ConversationCard`, `MessageInputBar`

### Day 8 — Sprint 2 _(check when done)_
- [ ] `domain/model/Notification.kt` + `domain/model/Analytics.kt`
- [ ] `NotificationDto.kt` + `AnalyticsDto.kt` + mappers + API services
- [ ] 6 Notification + Analytics UseCases
- [ ] `PushNotificationManager` expect/actual + device registration
- [ ] `screens/notifications/` — all files
- [ ] `screens/analytics/` — Analytics Dashboard screen

### Day 9 — Sprint 2 _(check when done)_
- [ ] `domain/model/CalendarEvent.kt` + `domain/model/AnalyticsSubject.kt`
- [ ] `GetCalendarUseCase` + `GetAnalyticsSubjectUseCase`
- [ ] `OfflineSyncManager` fully wired — calls UseCases to sync queues
- [ ] `screens/calendar/` — all files
- [ ] `screens/analytics/` — Analytics Subject Detail screen
- [ ] Cache freshness logic on all 7 caching ViewModels
- [ ] `Calendar.sq` + `Analytics.sq` in `data/local/entity/`

### Day 10 — Sprint 2 final _(check when done)_
- [ ] All 20 screens regression-tested
- [ ] Signed release AAB generated
- [ ] ProGuard rules: `domain.model.**` + `data.remote.dto.**` + Koin + Ktor + Razorpay
- [ ] Play Store listing assets (5 screenshots + description)
- [ ] `v1.0.0` git tag pushed
