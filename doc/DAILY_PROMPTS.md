# DAILY_PROMPTS.md — Parent App Agent Task Prompts
> **LOCATION:** `ParentAppMultiplatform/doc/DAILY_PROMPTS.md`

---

## How to Use This File

### Every Day — 3 Steps Before Pasting Any Prompt

```
STEP 1 — Open a brand new chat session with your AI agent

STEP 2 — Attach BOTH of these files from the doc/ folder:
            ParentAppMultiplatform/doc/CONTEXT.md
            ParentAppMultiplatform/doc/DECISIONS.md

STEP 3 — Find today's day below, copy everything inside the
         ═══ block, fill in [USER INPUT] sections, then paste
         as your first message
```

### How to Handle Changes

| What changed | Where to update before pasting |
|---|---|
| New Figma screens | In `UI REFERENCE` section → add "USE ATTACHED IMAGES" + attach files |
| API endpoint path changed | In `API CONFIGURATION` → update the `Path:` line |
| API request body changed | In `API CONFIGURATION` → update the `Request:` JSON block |
| API response shape changed | In `API CONFIGURATION` → update the `Response:` JSON block |
| Bug from a prior day | Paste into `BUGS TO FIX` section at top of Day 5 or Day 10 |
| New decision made | Add a row to `doc/DECISIONS.md` before next day's session |

### After Each Day
1. Mark the checkboxes in `doc/CONTEXT.md → Section 13 "What Has Been Built"`
2. Resolve any Open Questions in `doc/DECISIONS.md` that were answered
3. Add any new decisions made by the agent to `doc/DECISIONS.md`

---
---

## DAY 1 — Configure Project + Theme + Ktor + Koin + Navigation + Login

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 1 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.
All architecture decisions, tech stack, API endpoints, and file paths
are defined there. Do not re-derive them.

-----------------------------------------------------------------------
STARTING POINT (IMPORTANT — READ CAREFULLY)
-----------------------------------------------------------------------
The KMP project has already been created using the Android Studio
"Kotlin Multiplatform" project wizard with:

  Package ID  : com.edmik.parentapp
  Project name: ParentAppMultiplatform
  Modules     : shared, androidApp, iosApp (standard KMP layout)

The wizard creates a working skeleton. Your job today is NOT to create
a new project — it is to configure the existing one correctly:
replace the wizard's default dependencies, set up the architecture
layers, add all required libraries, and build the Login screen.

EXISTING WIZARD FILES (do not delete, do modify):
  shared/build.gradle.kts          ← add all KMP dependencies
  androidApp/build.gradle.kts      ← add Android-specific deps
  shared/src/commonMain/           ← add all source files here
  androidApp/src/main/MainActivity.kt ← update to call shared App()
  iosApp/                          ← leave Xcode project as-is

-----------------------------------------------------------------------
SECTION 1 — PROJECT IDENTITY (from doc/CONTEXT.md §1)
-----------------------------------------------------------------------
Package ID      : com.edmik.parentapp
Android Min SDK : 26  (Android 8.0+)
iOS Minimum     : 16+
Dev API Base    : https://dev.indivue.in:8080
Full screen + API specs → doc/Parent_App_Spec.docx
Sprint schedule         → doc/Parent_App_Sprint_Plan.docx

-----------------------------------------------------------------------
SECTION 2 — TECH STACK (from doc/CONTEXT.md §3 — all versions final)
-----------------------------------------------------------------------
Compose Multiplatform : 1.7.x
Navigation CMP        : 2.8.x
Koin                  : 4.x  (NOT Hilt — see doc/DECISIONS.md D-003)
Ktor Client           : 3.x  (NOT Retrofit — see D-004)
Kotlinx Serialization : 1.7+
SQLDelight            : 2.x  (NOT Room — see D-005)
multiplatform-settings: 1.2+ (encrypted)
Kotlinx Coroutines    : 1.9+
lifecycle-viewmodel   : 2.8.x (KMP-compatible)
Coil                  : 3.x
Kotlinx DateTime      : 0.6+

ZERO android.* imports allowed inside commonMain — see doc/CONTEXT.md §2.

-----------------------------------------------------------------------
SECTION 3 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL]
Login screen has no Figma design. If you have a screenshot or updated
design to provide, attach it and write "USE ATTACHED IMAGES" here.
Otherwise the spec wireframe below is used.

SPEC WIREFRAME — LOGIN SCREEN:
┌─────────────────────────────────┐
│                                 │
│         [APP LOGO / ICON]       │  ← centered, top 30% of screen
│         Parent App              │
│                                 │
│  ┌───────────────────────────┐  │
│  │ Student ID                │  │  ← OutlinedTextField
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ Password            [👁]  │  │  ← password field, eye toggle
│  └───────────────────────────┘  │
│  ☐ Remember Me                  │  ← Checkbox
│  ┌───────────────────────────┐  │
│  │         LOGIN             │  │  ← full width, #1976D2
│  └───────────────────────────┘  │
│       Forgot Password?          │  ← TextButton
│  [🔐 Login with Biometric]      │  ← only after first login
└─────────────────────────────────┘

SPEC WIREFRAME — FORGOT PASSWORD SCREEN:
  Step 1: mobile/email input + "Send OTP" button
  Step 2: 6-digit OTP input boxes + "Verify" button
  Step 3: new password + confirm password + "Reset" button
  All 3 steps on one screen; show/hide by ViewModel step state.

DESIGN TOKENS (full list → doc/CONTEXT.md §5):
  Primary Blue Dark : #1976D2   (buttons, active states)
  Background        : #E8F4FD → #FFFFFF gradient
  Text Primary      : #212121
  Text Secondary    : #757575
  Error             : #F44336
  Card BG           : #FFFFFF
  Min touch target  : 44×44 dp

-----------------------------------------------------------------------
SECTION 4 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT — Login
  Method  : POST
  Path    : /parent/login
  Base URL: https://dev.indivue.in:8080
  Request : { "studentId": "string", "password": "string" }
  Response: {
    "accessToken": "string",
    "refreshToken": "string",
    "student": {
      "id": "string",
      "name": "string",
      "batch": "string",
      "profilePhotoUrl": "string | null"
    }
  }
  Errors  : 401 = Invalid credentials | 429 = Rate limited

ENDPOINT — Forgot Password
  Method  : POST
  Path    : /parent/forgot-password
  Request : { "identifier": "string" }
  Response: { "message": "string", "otpSentTo": "string" }

ENDPOINT — Token Refresh (Ktor interceptor — not called manually)
  Method  : POST
  Path    : /parent/refresh-token
  Request : { "refreshToken": "string" }
  Response: { "accessToken": "string", "refreshToken": "string" }

-----------------------------------------------------------------------
SECTION 5 — TASKS (complete in order)
-----------------------------------------------------------------------

TASK 1.1 — Update shared/build.gradle.kts
Modify the wizard's existing file (do not recreate from scratch).
Ensure these are declared:
  - kotlin("multiplatform") plugin
  - Targets: androidTarget(), iosArm64(), iosSimulatorArm64(), iosX64()
  - compose plugin + CMP BOM
  - commonMain dependencies: all libraries from Section 2
  - androidMain dependencies: Ktor OkHttp engine, Android actuals
  - iosMain dependencies: Ktor Darwin engine, iOS actuals
  - SQLDelight plugin + schema source directory config

TASK 1.2 — Update androidApp/build.gradle.kts
  - compileSdk = 34, minSdk = 26, targetSdk = 34
  - Add google-services plugin placeholder (commented out until Day 8)
  - Add Razorpay dependency: com.razorpay:checkout:1.6.33

TASK 1.3 — Update androidApp/src/main/MainActivity.kt
  Replace wizard's default content with:
  ```kotlin
  class MainActivity : ComponentActivity() {
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          setContent { ParentAppTheme { App() } }
      }
  }
  ```
  App() is the top-level composable defined in commonMain.

TASK 1.4 — Create Material 3 Theme
  Files: commonMain/ui/theme/
  - Color.kt   → all tokens from doc/CONTEXT.md §5
  - Typography.kt → scale from doc/CONTEXT.md §5
  - Shapes.kt  → small=8dp, medium=12dp, large=16dp
  - Theme.kt   → ParentAppTheme { } wrapping MaterialTheme

TASK 1.5 — Create TokenManager
  File: commonMain/data/local/TokenManager.kt
  Use multiplatform-settings (encrypted). Methods:
    getAccessToken(): String?
    getRefreshToken(): String?
    setTokens(access: String, refresh: String)
    clearTokens()
    hasLoggedInBefore(): Boolean

TASK 1.6 — Create Ktor ApiClient
  File: commonMain/data/api/ApiClient.kt
  - Base URL: https://dev.indivue.in:8080
  - Bearer token injected on every request from TokenManager
  - Ktor bearerTokens { } plugin for auto-refresh on 401
  - ContentNegotiation with Kotlinx JSON
  - Timeouts: connect=15s, request=30s, socket=30s
  - HttpLogging: ALL in debug, NONE in release (BuildConfig.DEBUG check)

TASK 1.7 — Create Koin DI Modules
  Files: commonMain/di/
  - NetworkModule.kt   → provides ApiClient, API service interfaces
  - RepositoryModule.kt → provides repository implementations
  - DatabaseModule.kt  → provides SQLDelight driver (expect/actual) + DB instance
  - AppModule.kt       → provides TokenManager, AppStateManager (stub for now)

  SQLDelight driver expect/actual:
    commonMain: expect fun createDriver(schema, name): SqlDriver
    androidMain: actual → AndroidSqliteDriver
    iosMain:    actual → NativeSqliteDriver

TASK 1.8 — Create Navigation Scaffold
  Files: commonMain/ui/navigation/
  - Routes.kt     → all 20 constants from doc/CONTEXT.md §7
  - AppNavHost.kt → NavHost wiring all routes; screens not yet built
                     use ComingSoonScreen(screenName: String)
  - Start destination: Routes.LOGIN if TokenManager.getAccessToken()==null
                       else Routes.DASHBOARD

  App.kt (top-level entry, called from MainActivity):
  ```kotlin
  @Composable
  fun App() {
      ParentAppTheme {
          val navController = rememberNavController()
          AppNavHost(navController = navController)
      }
  }
  ```

TASK 1.9 — Create Login Screen + ViewModel
  Files: commonMain/ui/login/

  LoginScreen.kt — per wireframe in Section 3:
    - Logo placeholder (Box with brand blue bg + "P" text, 80dp)
    - Student ID: OutlinedTextField
    - Password: OutlinedTextField with PasswordVisualTransformation,
      trailing icon toggles visibility
    - Remember Me: Row { Checkbox(...) + Text("Remember Me") }
    - Login button: Button, full width, disabled during Loading state
    - Forgot Password: TextButton, navigates to Routes.FORGOT_PASSWORD
    - Biometric button: visible only if hasLoggedInBefore() == true

  LoginViewModel.kt:
    sealed class LoginUiState {
      object Idle : LoginUiState()
      object Loading : LoginUiState()
      object Success : LoginUiState()
      data class Error(val message: String) : LoginUiState()
      data class RateLimited(val secondsLeft: Int) : LoginUiState()
    }
    val uiState: StateFlow<LoginUiState>
    fun login(studentId: String, password: String)
    Rate limit: track failureCount + lastFailureTime in multiplatform-settings
                After 5 failures → emit RateLimited(30) with countdown ticker

  AuthRepository.kt + LoginUseCase.kt in their respective domain layers.
  On success: TokenManager.setTokens() → navController.navigate(DASHBOARD)

TASK 1.10 — Create Forgot Password Screen
  File: commonMain/ui/login/ForgotPasswordScreen.kt
  sealed class ForgotPasswordStep { object EnterContact, EnterOtp, ResetPassword }
  StateFlow<ForgotPasswordStep> drives which form section is visible.
  Show inline field-level validation errors below each input.

-----------------------------------------------------------------------
SECTION 6 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ ./gradlew :shared:compileKotlinAndroid → zero errors
□ ./gradlew :shared:compileKotlinIosSimulatorArm64 → zero errors
□ Android emulator: app launches, Login screen renders with brand colors
□ Xcode: project opens and builds for iOS Simulator without errors
□ Login with valid credentials → tokens saved → "Coming soon" Dashboard
□ Login with invalid credentials → error message shown
□ 5 failed logins → button disabled → "Try again in Xs" countdown
□ "Forgot Password?" → navigates to 3-step forgot password screen
□ Step progression on Forgot Password works (1→2→3)
□ ZERO android.* imports anywhere inside commonMain/ (run grep check)
□ Theme colors match doc/CONTEXT.md §5 design tokens exactly
□ Password field show/hide toggle works

-----------------------------------------------------------------------
SECTION 7 — OUTPUT FORMAT
-----------------------------------------------------------------------
Provide in this exact order:
  1. List of every file created/modified with full path from repo root
  2. Complete code for each file — no truncation, no "..." placeholders
  3. Exact Gradle dependency strings with versions used
  4. List of assumptions made (especially API response field names)
  5. Flagged items requiring backend or design input
  6. Update checklist for doc/CONTEXT.md §13 "After Day 1"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 2 — Dashboard + Student Switcher + All Reusable Components

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 2 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.

STARTING POINT:
  Day 1 complete. The following exist and compile:
  - Ktor ApiClient with JWT interceptor and auto-refresh
  - Koin modules: NetworkModule, RepositoryModule, DatabaseModule, AppModule
  - TokenManager (multiplatform-settings)
  - Material 3 theme (Color.kt, Typography.kt, Shapes.kt, Theme.kt)
  - AppNavHost with all 20 routes (unbuilt screens = ComingSoonScreen)
  - LoginScreen + LoginViewModel (POST /parent/login integrated)
  - ForgotPasswordScreen (3-step flow)
  Check doc/CONTEXT.md §13 "After Day 1" for exact interface list.

TODAY'S GOAL:
  Build the Dashboard screen (matching Figma), the Student Switcher
  bottom sheet, and ALL 16 reusable components. These components must
  be importable by Day 3 onwards without any modification.

-----------------------------------------------------------------------
SECTION 1 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL]
Figma link: https://www.figma.com/design/RzRLfAzFbVJD466Qzf99hh/Parent-App
If you have updated Figma exports or screenshots for Dashboard, attach
them and write "USE ATTACHED IMAGES" here. Otherwise Figma designs are
live at the link above — match them closely.

SPEC WIREFRAME — DASHBOARD:
┌─────────────────────────────────┐
│ [Avatar] Rahul Sharma     [🔔5] │  ← StudentHeader
│ Class 11-A | CBSE Science  [▼] │  ← BatchSelector pill
├─────────────────────────────────┤
│  87.5%   ₹15K    3      2      │  ← 4 quick-stat cards
│  Attend  Fees   Exams   HW     │
├─────────────────────────────────┤
│        [CalendarWidget]         │  ← monthly grid, event dots
├─────────────────────────────────┤
│ Recent Activity                 │
│ ● Physics Test — 78/100  Mar15 │  ← ActivityCard
│ ● Fee Due: ₹15,000       Mar20 │
├─────────────────────────────────┤
│ [🏠] [📅] [💰] [📊] [☰]       │  ← BottomNavBar
└─────────────────────────────────┘

STUDENT SWITCHER (ModalBottomSheet):
┌─────────────────────────────────┐
│ Switch Student            [✕]  │
│ ✓ [Avatar] Rahul Sharma        │  ← active (blue checkmark)
│      Class 11-A | CBSE         │
│ ○ [Avatar] Priya Sharma        │
│      Class 8-B | CBSE          │
│ [+ Link Another Student]        │
└─────────────────────────────────┘

Full design tokens → doc/CONTEXT.md §5.
Event dot colours: Lecture=#2196F3, Exam=#F44336, HW=#FF9800,
                   Fees=#4CAF50, Holiday=#9E9E9E

-----------------------------------------------------------------------
SECTION 2 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT — Dashboard
  Method  : GET
  Path    : /parent/dashboard?studentId={studentId}
  Base URL: https://dev.indivue.in:8080
  Response: {
    "student": { "id","name","batch","profilePhotoUrl" },
    "attendancePercentage": 87.5,
    "pendingFeesAmount": 15000,
    "upcomingExamsCount": 3,
    "pendingHomeworkCount": 2,
    "recentActivities": [
      { "id","title","date":"2026-03-15",
        "type":"EXAM|HOMEWORK|FEES|ATTENDANCE|ANNOUNCEMENT" }
    ],
    "calendarEvents": [
      { "date":"2026-03-20",
        "type":"LECTURE|EXAM|HOMEWORK|FEES|HOLIDAY","title":"string" }
    ],
    "unreadNotificationCount": 5
  }

ENDPOINT — Linked Students
  Method  : GET
  Path    : /parent/students
  Response: [ { "id","name","batch","profilePhotoUrl" } ]

-----------------------------------------------------------------------
SECTION 3 — REUSABLE COMPONENTS TO BUILD
-----------------------------------------------------------------------
All files → commonMain/ui/components/
Full spec for each → doc/CONTEXT.md §6

Build all 16. Minimum spec per component:

  BottomNavBar.kt   5 tabs (Home/Attendance/Fees/Analytics/More).
                    Active=PrimaryBlueDark+white icon. More→ModalDrawer.
                    Params: currentRoute:String, onTabSelected:(String)->Unit

  StudentHeader.kt  Left:40dp avatar circle (initials or Coil AsyncImage)
                    Center:name bold 16sp + batch caption.
                    Right:bell icon + blue badge (count, hidden if 0, "99+" cap).

  BatchSelector.kt  Rounded pill #E3F2FD bg. Graduation cap icon + batch
                    name + "Tap to change Batch" caption + chevron.
                    Param: onTap:()->Unit (caller handles bottom sheet)

  CalendarWidget.kt 7-col grid SUN–SAT. Today=blue circle outline.
                    Selected=filled blue circle. Up to 3 event dots/day.
                    Prev/next month arrows + "Month Year" header.
                    Params: events:List<CalendarEvent>,
                             onDayClick:(LocalDate)->Unit,
                             onMonthChange:(month,year)->Unit

  ActivityCard.kt   Coloured circle icon by type + title + date + type pill.
                    EXAM=red, HOMEWORK=blue, FEES=teal, ATTENDANCE=green.
                    Min height 56dp.

  StatusBadge.kt    Coloured pill. PAID/APPROVED=green, PENDING/SUBMITTED=amber,
                    OVERDUE/REJECTED=red. White text 12sp bold.

  DonutChart.kt     Canvas drawArc. Proportional segments. Center=count+"Total".
                    Legend right side. data class DonutSegment(value,color,label)

  GaugeChart.kt     Canvas half-circle arc. Threshold colour: ≥85% green,
                    75–84% amber, <75% red. Center="{pct}%"+caption.

  AlertBanner.kt    Full-width card. BG=#FFEBEE. Warning icon (amber).
                    Bold title + body. Param: visible:Boolean.

  LineChart.kt      Canvas. X=labels, Y=0–100. Connected line + dot per point.
                    Optional dashed second line for class average.
                    data class ChartPoint(label:String, value:Float)

  LoadingOverlay.kt Box filling parent. Semi-transparent overlay.
                    Centered CircularProgressIndicator (PrimaryBlue).
                    Param: visible:Boolean

  ErrorState.kt     Centred column: error icon + message + Retry button.
                    Params: message:String, onRetry:()->Unit

  EmptyState.kt     Centred column: icon + message.
                    Params: message:String, icon:ImageVector

  OfflineBanner.kt  Full-width amber bar pinned top.
                    "📡 You're offline. Showing cached data."
                    Param: isOffline:Boolean

  PullToRefresh.kt  Wrapper using rememberPullToRefreshState (CMP).
                    Params: onRefresh:suspend()->Unit, content:@Composable()->Unit

  SubjectCard.kt    Card: subject name bold + status text (Present=green/
                    Absent=red) + time caption. Used in 2-col grid.

-----------------------------------------------------------------------
SECTION 4 — DASHBOARD TASKS
-----------------------------------------------------------------------

TASK 2.1 — AppStateManager + AppState
  File: commonMain/di/AppStateManager.kt
  Singleton (Koin). Exposes StateFlow<AppState>.
  AppState data class → doc/CONTEXT.md §8.
  fun switchStudent(studentId: String): updates state + triggers reload.
  Observes NetworkConnectivityObserver → updates isOffline.

TASK 2.2 — NetworkConnectivityObserver (expect/actual)
  commonMain: expect class NetworkConnectivityObserver { fun observe():Flow<Boolean> }
  androidMain: ConnectivityManager + NetworkCallback actual
  iosMain: NWPathMonitor actual

TASK 2.3 — DashboardScreen + DashboardViewModel
  DashboardScreen.kt: full layout per wireframe above.
  DashboardViewModel.kt:
    StateFlow<DashboardUiState>: Loading|Success(DashboardData)|Error(msg)
    fun loadDashboard(studentId:String)
    On success: cache to SQLDelight with timestamp
    When offline: load from cache + set cacheAgeMinutes for stale banner

TASK 2.4 — StudentSwitcherBottomSheet
  ModalBottomSheet. Active student = blue checkmark. Other = empty circle.
  Tap other student → AppStateManager.switchStudent(id) → dismiss.
  "Link Another Student" → Snackbar "Coming soon".

TASK 2.5 — SQLDelight Schema
  File: commonMain/sqldelight/parentapp/Dashboard.sq
  Tables:
    cached_dashboard(student_id TEXT PK, json_data TEXT, cached_at INTEGER)
    linked_students(id TEXT PK, name TEXT, batch TEXT, profile_photo_url TEXT)

-----------------------------------------------------------------------
SECTION 5 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Dashboard renders: header, batch selector, 4 stat cards, calendar,
  activity feed, bottom nav — all from live API data
□ CalendarWidget event dots correct colours per event type
□ Notification badge shows correct unread count; hidden when 0
□ Student switcher opens on header tap
□ Switching student → dashboard reloads with new studentId
□ OfflineBanner shows in airplane mode
□ All 16 components exist in commonMain/ui/components/ and compile
□ PullToRefresh works on Dashboard
□ BottomNavBar "More" drawer opens with: Leave, Messages, Calendar, Settings
□ Other tabs navigate (screens show "Coming soon")

-----------------------------------------------------------------------
SECTION 6 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. All file paths (from repo root ParentAppMultiplatform/)
2. Complete code — no truncation
3. Note which components are ready for import by Day 3
4. API response field assumptions
5. Flagged items for backend/design
6. Update checklist for doc/CONTEXT.md §13 "After Day 2"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 3 — Attendance: Summary, Detail, Calendar View, Trend View

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 3 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.

STARTING POINT: Days 1–2 complete. Available to import from
commonMain/ui/components/: DonutChart, GaugeChart, LineChart,
AlertBanner, CalendarWidget, StatusBadge, BottomNavBar, OfflineBanner,
PullToRefresh, LoadingOverlay, ErrorState, EmptyState, SubjectCard.
Check doc/CONTEXT.md §13 "After Day 2" for full interface list.

-----------------------------------------------------------------------
SECTION 1 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL] Attach Figma frames for Attendance screens and
write "USE ATTACHED IMAGES" here. Figma has designs for this module.
Link: https://www.figma.com/design/RzRLfAzFbVJD466Qzf99hh/Parent-App

SPEC WIREFRAME — ATTENDANCE SUMMARY (Class View tab):
┌─────────────────────────────────┐
│ [Class View]  [Subject View]    │
│      [GaugeChart — 87.5%]       │
│   Present:210 | Absent:30       │
│ Including Leave  ○────────── ● │  ← Toggle switch (green when ON)
│ ──────────────────────────────  │
│ Physics     92% ██████░ green   │
│ Chemistry   85% █████░░ green   │
│ Mathematics 78% ████░░░ amber   │  ← 75–84%
│ English     68% ███░░░░  red    │  ← <75%  (tap → Detail screen)
│ ──────────────────────────────  │
│ ⚠️ English: 68% — Need 12 more  │  ← AlertBanner, if any <75%
│ ──────────────────────────────  │
│ [Calendar View]  [Trend View]   │  ← toggle bottom section
└─────────────────────────────────┘

SPEC WIREFRAME — ATTENDANCE DETAIL:
┌─────────────────────────────────┐
│ ← Physics                  92% │
│       [DonutChart]              │
│ Date        Period    Status    │
│ Mar 19      Period 3  Present   │  ← green text
│ Mar 18      Period 3  Absent    │  ← red text
│       [LineChart — monthly]     │
└─────────────────────────────────┘

Calendar View: CalendarWidget with cell fill colours.
Trend View: LineChart, 13 weeks, y=0–100%.
Full layout spec → doc/Parent_App_Spec.docx Screen 5 & 6.
Design tokens → doc/CONTEXT.md §5.

-----------------------------------------------------------------------
SECTION 2 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT 1 — Attendance Summary
  Method  : GET
  Path    : /parent/attendance/summary?studentId={studentId}
  Base URL: https://dev.indivue.in:8080
  Response: {
    "overallPercentage": 87.5,
    "overallPercentageWithLeave": 89.0,
    "presentCount": 210, "absentCount": 30, "leaveCount": 5,
    "subjects": [
      { "subjectId","subjectName","percentage":92.0,
        "presentCount":46,"absentCount":4 }
    ]
  }

ENDPOINT 2 — Attendance Calendar
  Method  : GET
  Path    : /parent/attendance/calendar?studentId={id}&month={1-12}&year={yyyy}
  Response: {
    "month":3,"year":2026,
    "days":[ { "date":"2026-03-19",
               "status":"PRESENT|ABSENT|LEAVE|HOLIDAY|WEEKEND" } ]
  }

ENDPOINT 3 — Attendance Trends
  Method  : GET
  Path    : /parent/attendance/trends?studentId={studentId}
  Response: { "weeks":[ { "label":"Feb W1","percentage":85.0 } ] }

ENDPOINT 4 — Subject Detail
  Method  : GET
  Path    : /parent/attendance/subject/{subjectId}?studentId={id}
  Response: {
    "subjectId","subjectName","percentage":92.0,
    "presentCount":46,"absentCount":4,
    "records":[ { "date","period","status":"PRESENT|ABSENT|LEAVE" } ]
  }

-----------------------------------------------------------------------
SECTION 3 — TASKS
-----------------------------------------------------------------------

TASK 3.1 — AttendanceSummaryScreen + AttendanceViewModel
  Two tabs: Class View (default) | Subject View
  Class View: GaugeChart + counts + "Including Leave" Switch + subject
    LinearProgressIndicator list + AlertBanner + calendar/trend toggle
  "Including Leave" toggle: client-side recalculation using
    overallPercentageWithLeave field — no additional API call
  AlertBanner formula: N = ceil((0.75×total - present) / 0.25)
    Message: "{Subject}: {X}% — Attend {N} more classes to reach 75%"
  Subject View: today's schedule as 2-col LazyVerticalGrid of SubjectCard
    + accordion "Analysis for Subject" with per-subject DonutChart

  AttendanceViewModel:
    StateFlow<AttendanceUiState>
    fun loadSummary(studentId), loadCalendar(studentId,month,year),
        loadTrends(studentId), toggleIncludeLeave(Boolean)
    Cache all responses; serve from SQLDelight when offline.

TASK 3.2 — Calendar View & Trend View (toggle sections)
  Calendar: CalendarWidget with attendanceDay list mapped to event dots.
    Cell fill: PRESENT=green, ABSENT=red, LEAVE=blue outline, HOLIDAY=grey.
    Month prev/next arrows trigger loadCalendar(newMonth, year).
  Trend: LineChart with weeks data from /attendance/trends API.

TASK 3.3 — AttendanceDetailScreen + AttendanceDetailViewModel
  TopAppBar: back + subject name + % StatusBadge.
  DonutChart (this subject — present/absent/leave segments).
  LazyColumn table: Date | Period | Status (coloured text).
  LineChart: monthly trend for this subject.

TASK 3.4 — SQLDelight Schema
  File: commonMain/sqldelight/parentapp/Attendance.sq
  Tables:
    cached_attendance_summary(student_id PK, json_data, cached_at)
    cached_attendance_calendar(student_id,month,year,json_data,cached_at — PK composite)

-----------------------------------------------------------------------
SECTION 4 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Attendance Summary loads and shows correct overall % via GaugeChart
□ Gauge colour matches threshold: ≥85% green, 75–84% amber, <75% red
□ "Including Leave" toggle updates % display (no API call)
□ Subject list progress bars use correct threshold colours
□ AlertBanner visible when any subject <75%; message shows correct N
□ Calendar View: correct coloured cells per status
□ Month navigation loads correct API month data
□ Trend View: line chart renders with week labels on x-axis
□ Tapping subject card → Attendance Detail with correct subjectId
□ Detail screen: day-by-day table with colour-coded status text
□ Pull-to-refresh on Summary and Detail
□ Offline: cached data + OfflineBanner

-----------------------------------------------------------------------
SECTION 5 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. All file paths from ParentAppMultiplatform/
2. Complete code — no truncation
3. API field assumptions
4. Flagged items for backend/design
5. Update checklist for doc/CONTEXT.md §13 "After Day 3"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 4 — Fees: List, Detail, Razorpay Payment, Payment Result

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 4 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.

STARTING POINT: Days 1–3 complete. Components available to import:
StatusBadge, BottomNavBar, LoadingOverlay, ErrorState, EmptyState,
OfflineBanner, PullToRefresh, AlertBanner.
Check doc/CONTEXT.md §13 "After Day 3" for full interface list.
Full fee screen spec → doc/Parent_App_Spec.docx Screens 7, 8, 9.
Payment flow → doc/CONTEXT.md §9 (offline strategy) and §3 (stack).

-----------------------------------------------------------------------
SECTION 1 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL] Attach Figma frames for Fee screens and write
"USE ATTACHED IMAGES" here. No Figma exists — spec wireframes used.

SPEC WIREFRAME — FEE LIST:
┌─────────────────────────────────┐
│ ← Fees                          │
│  Total       Paid      Pending  │
│  ₹1,20,000  ₹90,000   ₹30,000 │  ← summary card
│ ─────────────────────────────── │
│ Term 2 Tuition          [PENDING│  ← amber StatusBadge
│ ₹15,000  |  Due: Mar 20        │
│ [Pay Now]                       │
│ ─────────────────────────────── │
│ Term 1 Tuition            [PAID]│  ← green StatusBadge
│ ₹15,000  |  Paid: Jan 10      │
│ [Download Receipt]              │
└─────────────────────────────────┘

SPEC WIREFRAME — FEE DETAIL:
┌─────────────────────────────────┐
│ ← Term 2 Tuition       [PENDING]│
│ Base Amount     :   ₹14,500    │
│ Late Fee        :   ₹500       │
│ Tax             :   ₹0         │
│ Total           :   ₹15,000    │
│ ─────────────────────────────── │
│ Payment History  (empty)        │
│ [      Pay Now — ₹15,000      ]│
└─────────────────────────────────┘

PAYMENT RESULT SUCCESS:
  Large green ✅ (animated) + "Payment Successful!" + Transaction ID
  + Amount + [Download Receipt] + [Back to Fees]

PAYMENT RESULT FAILURE:
  Red ❌ + "Payment Failed" + error message + [Try Again] + [Back to Fees]

-----------------------------------------------------------------------
SECTION 2 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT 1 — Fee List
  Method  : GET
  Path    : /parent/fees?studentId={studentId}
  Base URL: https://dev.indivue.in:8080
  Response: {
    "totalAmount":120000,"paidAmount":90000,"pendingAmount":30000,
    "fees":[ { "feeId","name","term","amount":15000,
               "dueDate":"2026-03-20",
               "status":"PAID|PENDING|OVERDUE","paidDate":"string|null" } ]
  }

ENDPOINT 2 — Fee Detail
  Method  : GET
  Path    : /parent/fees/{feeId}
  Response: {
    "feeId","name","status",
    "baseAmount":14500,"lateFee":500,"tax":0,"totalAmount":15000,
    "dueDate","paymentHistory":[],"restructureHistory":[]
  }

ENDPOINT 3 — Initiate Payment
  Method  : POST
  Path    : /parent/fees/pay
  Request : { "feeId":"string","studentId":"string" }
  Response: { "razorpayOrderId","amount":15000,"currency":"INR",
              "studentName","studentEmail" }

ENDPOINT 4 — Verify Payment
  Method  : POST
  Path    : /parent/fees/pay/verify
  Request : { "razorpayPaymentId","razorpayOrderId","razorpaySignature","feeId" }
  Response: { "success":true,"transactionId":"string","receiptUrl":"string" }

ENDPOINT 5 — Download Receipt
  Method  : GET
  Path    : /parent/fees/{feeId}/receipt
  Notes   : Open receiptUrl in system browser if response is a redirect URL

-----------------------------------------------------------------------
SECTION 3 — PAYMENT expect/actual STRUCTURE
-----------------------------------------------------------------------
Full rationale → doc/DECISIONS.md D-010.

commonMain/data/payment/PaymentLauncher.kt:
  data class PaymentRequest(orderId,amount,currency,description,studentName,studentEmail)
  sealed class PaymentOutcome {
    data class Success(paymentId,orderId,signature) : PaymentOutcome()
    data class Failure(errorCode,errorMessage)      : PaymentOutcome()
    object Cancelled                                : PaymentOutcome()
  }
  expect class PaymentLauncher { suspend fun launch(req:PaymentRequest):PaymentOutcome }

androidMain: Razorpay Android SDK (com.razorpay:checkout:1.6.33)
  Activity implements PaymentResultWithData. Wrap callback in suspendCoroutine.

iosMain: [USER INPUT — update if Q-003 in doc/DECISIONS.md is resolved]
  Until resolved, use stub:
  actual class PaymentLauncher {
    actual suspend fun launch(req:PaymentRequest) =
      PaymentOutcome.Failure("IOS_PENDING","iOS payment setup pending")
  }

-----------------------------------------------------------------------
SECTION 4 — TASKS
-----------------------------------------------------------------------

TASK 4.1 — PaymentLauncher expect/actual
  Build per Section 3. Wire in Koin AppModule as singleton.

TASK 4.2 — FeeListScreen + FeeListViewModel
  Summary card (3-column). LazyColumn of FeeItemCard with StatusBadge.
  "Pay Now" on PENDING/OVERDUE. "Download Receipt" on PAID.
  Pull-to-refresh. Cache in SQLDelight.

TASK 4.3 — FeeDetailScreen + FeeDetailViewModel
  Breakdown table. Payment history list. "Pay Now" button (disabled if PAID).
  Payment flow in ViewModel:
    1. POST /parent/fees/pay → orderId
    2. paymentLauncher.launch(request) → PaymentOutcome
    3. Success → POST /parent/fees/pay/verify → navigate PaymentResult(success=true)
    4. Failure/Cancelled → navigate PaymentResult(success=false, error=msg)

TASK 4.4 — PaymentResultScreen
  Success: AnimatedVisibility checkmark + transaction details
           [Download Receipt] opens receiptUrl in browser
           [Back to Fees] pops to FEE_LIST (clears detail from stack)
  Failure: X icon + error + [Try Again] (back to Detail) + [Back to Fees]

TASK 4.5 — Offline Payment Guard
  If AppState.isOffline → disable "Pay Now" button
  Show Snackbar: "Payment requires an internet connection"

TASK 4.6 — SQLDelight Schema
  File: commonMain/sqldelight/parentapp/Fees.sq
  Table: cached_fee_list(student_id PK, json_data, cached_at)

-----------------------------------------------------------------------
SECTION 5 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Fee List: correct summary totals; PAID/PENDING/OVERDUE badge colours
□ Tapping fee card → Fee Detail with correct feeId
□ Fee Detail: correct breakdown amounts
□ "Pay Now" → POST pay → Razorpay opens on Android (sandbox mode)
□ Razorpay success → verify called → Payment Result success screen
□ Razorpay cancelled → Payment Result failure screen
□ Success screen: checkmark animates in; transaction ID visible
□ "Back to Fees" from result → Fee List (not Detail — back stack cleared)
□ Offline: cached list shows + OfflineBanner + Pay Now disabled

-----------------------------------------------------------------------
SECTION 6 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. All file paths from ParentAppMultiplatform/
2. Complete code — no truncation
3. Exact Razorpay Gradle dependency string
4. API response field assumptions
5. Flagged items (especially payment verify flow)
6. Update checklist for doc/CONTEXT.md §13 "After Day 4"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 5 — Sprint 1 Polish, Edge States & Integration Testing

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 5 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.
No new screens today. Fix bugs, add missing edge states, validate.
Full testing checklist → doc/CONTEXT.md §12.

STARTING POINT: Days 1–4 complete — 10 screens built.
Check doc/CONTEXT.md §13 "After Day 4" for full interface list.

-----------------------------------------------------------------------
SECTION 1 — BUGS TO FIX (fill this before pasting)
-----------------------------------------------------------------------
[USER INPUT — REQUIRED]
List every bug found while testing Days 1–4. Format:
  BUG 1: [Screen] — [Observed] — [Expected]
  BUG 2: ...
Priority order: Login > Payment > Attendance > Fees > Dashboard

[PASTE BUG LIST HERE — write "NO KNOWN BUGS" if none yet]

-----------------------------------------------------------------------
SECTION 2 — MANDATORY AUDIT TASKS
-----------------------------------------------------------------------

AUDIT A — Edge State Coverage
For EVERY screen (Login, Dashboard, Attendance Summary, Attendance
Detail, Fee List, Fee Detail, Payment Result, Notification stub):
  □ Shows LoadingOverlay while API call in progress
  □ Shows ErrorState + Retry on API failure
  □ Shows EmptyState when list is empty
  □ PullToRefresh works and dismisses on complete
  □ OfflineBanner shows when AppState.isOffline == true
Add any missing states now.

AUDIT B — Navigation Back Stack
  □ Back press on Dashboard tab → stays on Dashboard (no exit)
  □ Back press on other main tabs → goes to Dashboard
  □ FeeDetail → PaymentResult → "Back to Fees" → Fee List (not Detail)
  □ AttendanceDetail → back → Attendance Summary
  □ StudentSwitcher backdrop tap → dismisses, stays on Dashboard

AUDIT C — commonMain Purity
  Run: grep -r "import android\." shared/src/commonMain/
  Any results = violation. Move to androidMain actual implementation.
  Also check: java.io.*, UIKit.*, Foundation.*

AUDIT D — API Error Handling
  Every ViewModel catch block must handle:
    Network timeout → ErrorState "Connection timed out. Retry?"
    HTTP 401       → handled silently by Ktor token refresh
    HTTP 500       → ErrorState "Server error. Please try later."
    HTTP 404       → EmptyState with context-appropriate message

AUDIT E — Notification Badge
  □ Bell badge shows AppState.unreadNotificationCount
  □ Hidden (not shown as "0") when count is 0; shows "99+" above 99
  □ Count updated when dashboard API responds

AUDIT F — Multi-Student Reload Chain
  □ AppStateManager.switchStudent(id) updates AppState.currentStudentId
  □ DashboardViewModel observes via collectLatest → re-fetches
  □ AttendanceViewModel and FeeListViewModel do the same
  Verify this chain end-to-end.

-----------------------------------------------------------------------
SECTION 3 — POLISH TASKS
-----------------------------------------------------------------------

TASK 5.1 — Cache Freshness Banners
  Add to DashboardViewModel + AttendanceViewModel + FeeListViewModel:
    val cacheAgeMessage: StateFlow<String?>
    Logic: if (now - cachedAt) > threshold → "Last updated {N} min ago"
    Thresholds: Dashboard=5min, Attendance=60min, Fees=60min
    Show OfflineBanner OR stale banner — never both; prefer OfflineBanner.

TASK 5.2 — Strings Centralisation
  File: commonMain/util/Strings.kt
  All user-visible text as constants. No hardcoded strings in Composables.

TASK 5.3 — Compose Stability Annotations
  Add @Stable or @Immutable to all data classes passed to composables:
  StudentSummary, DashboardData, FeeItem, SubjectAttendance,
  CalendarEvent, ActivityItem.

TASK 5.4 — 150% Font Scaling Fixes
  Test at 150% system font size. Fix:
    Stat card text overflow → use maxLines=1 + TextOverflow.Ellipsis
    Calendar day cell overflow → min font 10sp
    Bottom nav label cutoff → 10sp minimum or hide labels at 150%

TASK 5.5 — iOS Compile Verification
  Run: ./gradlew :shared:compileKotlinIosSimulatorArm64
  Fix ALL compile errors. Report: "iOS build PASSED" or list errors.

-----------------------------------------------------------------------
SECTION 4 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Full flow: Login→Dashboard→Attendance→Fees→Pay(sandbox) — no crash
□ All screens: loading/error/empty states present
□ All lists: pull-to-refresh works
□ Offline: cached data + OfflineBanner on all main screens
□ ZERO android.* imports in commonMain (grep audit passed)
□ All API errors handled (timeout/401/500/404)
□ Student switching reloads all screens correctly
□ Notification badge shows/hides correctly
□ Back navigation correct on all screens
□ 150% font scaling: no truncation on critical UI
□ iOS shared module compiles without errors

-----------------------------------------------------------------------
SECTION 5 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. Every file modified — 1-line summary of change
2. Every bug from Section 1: Fixed | Not Fixed | Needs Info
3. Every audit item: PASS | FIXED | FLAGGED
4. iOS compile status
5. Items requiring backend/design input
6. Update checklist for doc/CONTEXT.md §13 "After Day 5"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 6 — Leave Management: Apply, History, Detail

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 6 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.

STARTING POINT: Days 1–5 complete. All shared components available.
Full leave screen spec → doc/Parent_App_Spec.docx Screens 11, 12, 13.
Check doc/CONTEXT.md §13 "After Day 5" for full interface list.

-----------------------------------------------------------------------
SECTION 1 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL] Attach Figma/screenshots for Leave screens.
Write "USE ATTACHED IMAGES" here if provided. No Figma — spec used.

SPEC WIREFRAME — APPLY LEAVE:
┌─────────────────────────────────┐
│ ← Apply for Leave               │
│ Leave Type                  [▼] │  ← ExposedDropdownMenuBox
│ From [Mar 20]  |  To [Mar 21]   │  ← DatePickerDialog buttons
│ Reason *                        │
│ ┌─────────────────────────────┐ │
│ │ (min 10 chars)      45/500  │ │  ← multiline + char counter
│ └─────────────────────────────┘ │
│ [📎 Attach Document (optional)] │  ← FilePicker, max 5MB
│ [     Submit Application      ] │  ← disabled until form valid
└─────────────────────────────────┘

CONFIRMATION DIALOG:
  "Submit Leave Application?"
  [Cancel]  [Submit]

SPEC WIREFRAME — LEAVE HISTORY:
┌─────────────────────────────────┐
│ [All][Pending][Approved][Rejected]│  ← TabRow
│ Sick Leave            [PENDING] │
│ Mar 20–21, 2026                 │
│ Submitted: Mar 19, 2026         │
└─────────────────────────────────┘

SPEC WIREFRAME — LEAVE DETAIL:
┌─────────────────────────────────┐
│ Sick Leave             [PENDING]│
│ ● Submitted      Mar 19 10:30  │  ← timeline: filled = done
│ ○ Under Review                  │
│ ○ Approved / Rejected           │
│ Reason: "Child has fever..."    │
│ [📎 Doctor_cert.pdf]            │
│ [  Cancel Application  ]        │  ← only if status=SUBMITTED
└─────────────────────────────────┘

Status colours → doc/CONTEXT.md §5.

-----------------------------------------------------------------------
SECTION 2 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT 1 — Apply Leave
  Method  : POST
  Path    : /parent/leave/apply
  Base URL: https://dev.indivue.in:8080
  Request : multipart/form-data {
    "studentId","leaveType":"SICK|FAMILY_EMERGENCY|PERSONAL|OTHER",
    "startDate":"2026-03-20","endDate":"2026-03-21",
    "reason":"string","document":File(optional,PDF/JPG/PNG,max 5MB)
  }
  Response: { "leaveId","status":"SUBMITTED","message" }

ENDPOINT 2 — Leave History
  Method  : GET
  Path    : /parent/leave/history?studentId={studentId}
  Response: [ { "leaveId","leaveType","startDate","endDate",
                "status":"SUBMITTED|PENDING|APPROVED|REJECTED",
                "submittedAt" } ]

ENDPOINT 3 — Leave Detail
  Method  : GET
  Path    : /parent/leave/{leaveId}
  Response: { "leaveId","leaveType","startDate","endDate","reason",
              "status","submittedAt","reviewedAt"|null,
              "reviewerName"|null,"reviewerRemarks"|null,
              "attachmentUrl"|null }

ENDPOINT 4 — Cancel Leave
  Method  : DELETE
  Path    : /parent/leave/{leaveId}
  Response: { "message":"string" }

-----------------------------------------------------------------------
SECTION 3 — FILE PICKER (expect/actual — required today)
-----------------------------------------------------------------------
commonMain/data/platform/FilePicker.kt:
  data class PickedFile(name,sizeBytes,mimeType,bytes:ByteArray)
  expect class FilePicker { suspend fun pickFile(allowedMimeTypes:List<String>):PickedFile? }

androidMain: ActivityResultContracts.OpenDocument, filter PDF/JPG/PNG
iosMain: UIDocumentPickerViewController interop, or stub returning null

-----------------------------------------------------------------------
SECTION 4 — TASKS
-----------------------------------------------------------------------
Full task details → use wireframes above + doc/Parent_App_Spec.docx

TASK 6.1 — FilePicker expect/actual. Wire in Koin AppModule.

TASK 6.2 — ApplyLeaveScreen + ApplyLeaveViewModel
  Validation rules (all client-side):
    leaveType: required | startDate: not in past | endDate ≥ startDate
    reason: ≥10 chars | attachment if present: ≤5MB, PDF/JPG/PNG only
  Offline path: save to pending_leave_queue SQLDelight table + Snackbar.
  Online path: multipart POST → navigate to Leave History on success.

TASK 6.3 — LeaveHistoryScreen + LeaveHistoryViewModel
  TabRow filter (All/Pending/Approved/Rejected) — client-side filtering.
  FAB navigates to Apply Leave. Pull-to-refresh. Cache in SQLDelight.

TASK 6.4 — LeaveDetailScreen
  Vertical stepper timeline (3 steps). Cancel button: visible only if
  status=="SUBMITTED". Cancel → AlertDialog → DELETE → back to History.

TASK 6.5 — SQLDelight Schema
  File: commonMain/sqldelight/parentapp/Leave.sq
  Tables: pending_leave_queue(id,leave_type,start_date,end_date,reason,
                               attachment_bytes,queued_at,synced)
          cached_leave_history(student_id PK, json_data, cached_at)

-----------------------------------------------------------------------
SECTION 5 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Leave type dropdown: 4 options
□ Date pickers work on Android
□ Reason: char counter + min 10 chars enforced
□ File >5MB shows validation error
□ Submit disabled until all fields valid
□ Confirmation dialog shown before submit
□ Success → Leave History + new item appears
□ Filter tabs work (client-side, no API call)
□ Status timeline: correct filled/empty steps per status
□ Cancel button visible ONLY for SUBMITTED status
□ Offline submit: queued + Snackbar + syncs when online

-----------------------------------------------------------------------
SECTION 6 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. All file paths from ParentAppMultiplatform/
2. Complete code — no truncation
3. Multipart upload implementation with Ktor
4. API field assumptions
5. iOS FilePicker limitations noted
6. Update checklist for doc/CONTEXT.md §13 "After Day 6"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 7 — Communication: Conversations, Message Thread, Announcements

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 7 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.

STARTING POINT: Days 1–6 complete. All shared components available.
Full messaging spec → doc/Parent_App_Spec.docx Screens 14, 15, 16.
Check doc/CONTEXT.md §13 "After Day 6" for full interface list.

-----------------------------------------------------------------------
SECTION 1 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL] Attach Figma/screenshots for messaging screens.
Write "USE ATTACHED IMAGES" here if provided. No Figma — spec used.

SPEC WIREFRAME — CONVERSATIONS LIST:
┌─────────────────────────────────┐
│ 🔍 Search conversations...      │
│ [P] Mrs. Priya (Physics)    [3] │  ← [3]=unread badge
│     "The assignment is due..."  │
│                       10:30 AM  │
│ [A] Admin Office                │
│     "Fee payment reminder..."   │
│                        Mar 18   │
│                          [✉️+]  │  ← FAB
└─────────────────────────────────┘

SPEC WIREFRAME — MESSAGE THREAD:
┌─────────────────────────────────┐
│ ←  Mrs. Priya (Physics)         │
│          --- Mar 19 ---         │  ← date separator
│          "When is the test?" ← │  ← parent: right, #1976D2 bubble
│                             ✓✓ │  ← read receipt
│ "Test is on Friday." →          │  ← teacher: left, #F5F5F5 bubble
│          "Thank you!"        ← │
│ [📎] [Type a message...]  [Send]│  ← pinned input bar
└─────────────────────────────────┘

Parent bubble BG: #1976D2, text: #FFFFFF, bottom-right radius: 4dp
Teacher bubble BG: #F5F5F5, text: #212121, bottom-left radius: 4dp

SPEC WIREFRAME — ANNOUNCEMENTS:
┌─────────────────────────────────┐
│ 📢 Annual Day Celebration [NEW] │  ← NEW badge if not acknowledged
│ Mar 25, 2026                    │
│ "Students are invited..."       │  ← 2-line preview
└─────────────────────────────────┘

Detail: full body + attachments + [Acknowledge] → [✓ Acknowledged].

-----------------------------------------------------------------------
SECTION 2 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT 1 — Conversations List
  Method  : GET
  Path    : /parent/messages?studentId={studentId}
  Base URL: https://dev.indivue.in:8080
  Response: [ { "conversationId","participantName","participantRole",
                "participantAvatar"|null,"lastMessage","lastMessageAt",
                "unreadCount":3 } ]

ENDPOINT 2 — Message Thread
  Method  : GET
  Path    : /parent/messages/{conversationId}
  Response: { "conversationId","participantName",
              "messages":[ { "messageId","senderId",
                "senderType":"PARENT|TEACHER|ADMIN","text",
                "sentAt","isRead":true } ] }

ENDPOINT 3 — Send Message
  Method  : POST
  Path    : /parent/messages
  Request : { "conversationId","studentId","text" }
  Response: { "messageId","sentAt" }

ENDPOINT 4 — Announcements List
  Method  : GET
  Path    : /parent/announcements?studentId={studentId}
  Response: [ { "id","title","body","date","acknowledged":false,
                "attachments":[ { "name","url","type":"PDF|IMAGE" } ] } ]

ENDPOINT 5 — Acknowledge Announcement
  Method  : POST
  Path    : /parent/announcements/{id}/acknowledge
  Request : { "studentId":"string" }
  Response: { "success":true }

-----------------------------------------------------------------------
SECTION 3 — TASKS
-----------------------------------------------------------------------

TASK 7.1 — ConversationsScreen + ConversationsViewModel
  SearchBar (Material 3) filters client-side by participantName.
  LazyColumn: avatar circle + name/role + preview + timestamp + unread badge.
  Tapping → MESSAGE_THREAD route with conversationId.
  FAB: Snackbar "New conversation coming soon". Pull-to-refresh.

TASK 7.2 — MessageThreadScreen + MessageThreadViewModel
  LazyColumn (reverseLayout=true).
  MessageBubble: PARENT=right blue, TEACHER/ADMIN=left grey, per wireframe radii.
  Date separator between messages on different days (centred caption).
  Input bar pinned to bottom above keyboard. Send button disabled when empty.
  Optimistic sending: add message to local list immediately.
    On API success → update messageId + sentAt.
    On failure → mark as "failed" + retry option on that bubble.
  Offline: add to pending_message_queue. Snackbar.
  Auto-scroll: LaunchedEffect(messages.size) { listState.animateScrollToItem(0) }

TASK 7.3 — AnnouncementsScreen + AnnouncementDetailScreen
  List: "NEW" badge when acknowledged==false.
  Detail: full body + attachment links (open in browser).
  [Acknowledge] → POST → change to "✓ Acknowledged" (disabled, green).
  Update local state; no re-fetch needed.

TASK 7.4 — SQLDelight Schema
  File: commonMain/sqldelight/parentapp/Messages.sq
  Tables: cached_conversations(student_id PK, json_data, cached_at)
          cached_messages(conversation_id PK, json_data, cached_at)
          pending_message_queue(id PK, conversation_id, text, queued_at, synced)

TASK 7.5 — Extend OfflineSyncManager
  Add syncPendingMessages(): query WHERE synced=0 → POST → mark synced=1
  Emit sync result via SharedFlow<String> → UI collects → Snackbar.

-----------------------------------------------------------------------
SECTION 4 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Conversations list loads with previews + unread badges
□ Search filters by name (no API call)
□ Message Thread: parent=right blue, teacher=left grey bubbles
□ Date separators between different days
□ Send: message appears immediately (optimistic) + syncs to API
□ Failed message shows retry option
□ Auto-scroll to bottom on open
□ Announcements "NEW" badge visible for unacknowledged items
□ Acknowledge → button becomes "✓ Acknowledged"
□ Offline queued messages sync when reconnected

-----------------------------------------------------------------------
SECTION 5 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. All file paths from ParentAppMultiplatform/
2. Complete code — no truncation
3. Optimistic sending state implementation
4. API field assumptions
5. Backend questions (read receipts: polling or WebSocket?)
6. Update checklist for doc/CONTEXT.md §13 "After Day 7"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 8 — Notifications Centre + Preferences + FCM + Analytics Dashboard

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 8 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.

STARTING POINT: Days 1–7 complete. All shared components available.
Full screen specs → doc/Parent_App_Spec.docx Screens 10, 17, 20.
Push notification architecture → doc/CONTEXT.md §3 (expect/actual stack).
Check doc/CONTEXT.md §13 "After Day 7" for full interface list.

-----------------------------------------------------------------------
SECTION 1 — FCM CONFIGURATION (fill before pasting)
-----------------------------------------------------------------------
[USER INPUT — REQUIRED if files are available; see doc/DECISIONS.md Q-004]

Firebase config status:
  google-services.json (Android) : [AVAILABLE at path: ___  /  NOT YET]
  GoogleService-Info.plist (iOS)  : [AVAILABLE at path: ___  /  NOT YET]
  Firebase project name           : [e.g. "edmik-parent-app"  /  UNKNOWN]

If NOT YET: implement PushNotificationManager with stub getDeviceToken()
returning null + warning log. Device registration call is skipped.
Do not block other Day 8 tasks for this.

-----------------------------------------------------------------------
SECTION 2 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL] Attach screenshots for Notification or Analytics
screens. Write "USE ATTACHED IMAGES" here if provided. No Figma — spec used.

SPEC WIREFRAME — NOTIFICATION CENTRE:
┌─────────────────────────────────┐
│ ← Notifications  [Mark All ✓]  │
│ 💰 Fee Due Reminder             │  ← blue left border = unread
│    Term 2 fee of ₹15,000...    │
│                      10:30 AM   │
│ 📅 Attendance Alert             │  ← no border = read
│    English attendance is 68%   │
│                       Mar 18    │
└─────────────────────────────────┘

SPEC WIREFRAME — NOTIFICATION PREFERENCES:
  Per-category toggles: Fee Reminders, Attendance Alerts, Exam Schedule,
  Homework Due, Leave Updates, Announcements.
  Quiet Hours master switch + Start/End TimePicker.
  [Save Preferences] button.

SPEC WIREFRAME — ANALYTICS DASHBOARD:
┌─────────────────────────────────┐
│ "Rahul is performing well..."   │  ← plain language summary
│ ← Physics 85% ↑  Chem 78% →   │  ← horizontal scroll subject cards
│      Maths 62% ↓               │
│ ⚠️ Weak Areas                   │
│ • Mathematics: Calculus (42%)  │
│ [LineChart — 8 weeks trend]     │
└─────────────────────────────────┘

Trend arrows: UP=green ↑, DOWN=red ↓, STABLE=grey →
Category icons → doc/CONTEXT.md §6 (StatusBadge).

-----------------------------------------------------------------------
SECTION 3 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT 1 — Notifications List (paginated)
  Method  : GET
  Path    : /parent/notifications?studentId={id}&page={0}&size={20}
  Base URL: https://dev.indivue.in:8080
  Response: { "notifications":[ { "id","type":"FEE_DUE|ATTENDANCE_LOW|
              EXAM_SCHEDULE|HOMEWORK_DUE|LEAVE_STATUS|ANNOUNCEMENT",
              "title","body","createdAt","isRead":false,"entityId"|null } ],
              "totalCount":42,"hasMore":true }

ENDPOINT 2 — Mark All Read
  Method  : PUT
  Path    : /parent/notifications/read-all
  Request : { "studentId":"string" }
  Response: { "success":true }

ENDPOINT 3 — Mark Single Read
  Method  : PUT
  Path    : /parent/notifications/{id}/read
  Response: { "success":true }

ENDPOINT 4 — Get Notification Preferences
  Method  : GET
  Path    : /parent/notifications/preferences
  Response: { "feeReminders":true,"attendanceAlerts":true,
              "examSchedule":true,"homeworkDue":false,
              "leaveUpdates":true,"announcements":true,
              "quietHoursEnabled":true,
              "quietHoursStart":"22:00","quietHoursEnd":"07:00" }

ENDPOINT 5 — Save Preferences
  Method  : PUT
  Path    : /parent/notifications/preferences
  Request : (same shape as GET response)

ENDPOINT 6 — Register Device Token
  Method  : POST
  Path    : /parent/device/register
  Request : { "deviceToken":"string","platform":"ANDROID|IOS" }
  Response: { "deviceId":"string" }

ENDPOINT 7 — Analytics Dashboard
  Method  : GET
  Path    : /pulse/analytics/parent/dashboard?studentId={id}
  ⚠️ NOTE : Uses /pulse/ prefix — NOT /parent/. Same base URL.
             See doc/DECISIONS.md Q-006 if path is uncertain.
  Response: { "summaryText","subjects":[ { "subjectId","subjectName",
              "score":85.0,"trend":"UP|DOWN|STABLE","lastExamScore" } ],
              "weakAreas":[ { "subject","topic","score":42.0 } ],
              "progressTrend":[ { "label":"Week 1","score":72.0 } ] }

-----------------------------------------------------------------------
SECTION 4 — PUSH NOTIFICATION (expect/actual)
-----------------------------------------------------------------------
Rationale → doc/DECISIONS.md D-011.

commonMain:
  expect class PushNotificationManager {
    suspend fun getDeviceToken(): String?
    fun requestPermission(): Flow<Boolean>
    fun observeIncomingNotifications(): Flow<PushPayload>
  }
  data class PushPayload(type,entityId,title,body)

androidMain: FirebaseMessagingService subclass + getToken()
iosMain: UNUserNotificationCenter + Firebase iOS SDK (or stub)

Deep-link routing in AppNavHost.kt (LaunchedEffect):
  FEE_DUE → Routes.FEE_DETAIL (entityId=feeId)
  ATTENDANCE_LOW → Routes.ATTENDANCE
  LEAVE_STATUS → Routes.LEAVE_DETAIL (entityId=leaveId)
  NEW_MESSAGE → Routes.MESSAGE_THREAD (entityId=conversationId)
  ANNOUNCEMENT → Routes.ANNOUNCEMENTS
  EXAM_SCHEDULE → Routes.CALENDAR

Registration: on app init in AppStateManager, get token → if changed →
POST /parent/device/register → store new token in multiplatform-settings.

-----------------------------------------------------------------------
SECTION 5 — TASKS
-----------------------------------------------------------------------

TASK 8.1 — PushNotificationManager expect/actual. Wire in AppStateManager.

TASK 8.2 — NotificationCentreScreen + NotificationViewModel
  Paginated LazyColumn. Unread=blue 4dp left border. Read=no border.
  Mark All Read: PUT read-all → refresh list → update AppState unread count.
  Tap item: PUT /{id}/read → remove border → navigate via deep-link map.
  Load next page when approaching list end. Pull-to-refresh.

TASK 8.3 — NotificationPreferencesScreen
  Load GET on open. TimePicker enabled only when quietHoursEnabled=true.
  Save PUT → Snackbar "Preferences saved" → navigate back.

TASK 8.4 — AnalyticsDashboardScreen + AnalyticsViewModel
  API path is /pulse/analytics/... — verify Ktor routes to correct path.
  LazyRow horizontal scroll of SubjectPerformanceCard (score + trend arrow).
  Weak areas: bulleted list. LineChart: 8-week progress trend.
  Tap subject card → Routes.ANALYTICS_SUBJECT with subjectId.
  Pull-to-refresh. Cache in SQLDelight (60min threshold).

-----------------------------------------------------------------------
SECTION 6 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Notification Centre loads paginated list
□ Unread items show blue left border; read items do not
□ "Mark All Read" removes all borders; AppState count → 0
□ Tapping notification navigates to correct screen
□ Preferences loads current toggles correctly
□ Quiet hours pickers show/hide based on master switch
□ "Save" shows success Snackbar
□ Analytics Dashboard loads summary + subject cards + weak areas + chart
□ Trend arrows correct: UP=green, DOWN=red, STABLE=grey
□ FCM token registration: runs on start or logs stub warning
□ Push deep links: all 6 types navigate correctly

-----------------------------------------------------------------------
SECTION 7 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. All file paths from ParentAppMultiplatform/
2. Complete code — no truncation
3. FCM setup instructions (what to do once google-services.json arrives)
4. Analytics API path assumption (note if /pulse/ needs backend confirm)
5. Flagged items (Q-004, Q-006 from doc/DECISIONS.md)
6. Update checklist for doc/CONTEXT.md §13 "After Day 8"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 9 — Academic Calendar + Analytics Subject Detail + Full Offline Sync

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 9 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.

STARTING POINT: Days 1–8 complete. CalendarWidget, LineChart,
OfflineSyncManager (partial) all exist in commonMain.
Full screen specs → doc/Parent_App_Spec.docx Screens 18, 19.
Offline strategy → doc/CONTEXT.md §9.
Check doc/CONTEXT.md §13 "After Day 8" for full interface list.

-----------------------------------------------------------------------
SECTION 1 — UI REFERENCE
-----------------------------------------------------------------------
[USER INPUT — OPTIONAL] Attach Figma/screenshots for Calendar or
Analytics Subject Detail. Write "USE ATTACHED IMAGES" here if provided.

SPEC WIREFRAME — ACADEMIC CALENDAR:
┌─────────────────────────────────┐
│ ← Academic Calendar             │
│ ← March 2026 →                 │  ← CalendarWidget full screen
│ SUN MON TUE WED THU FRI SAT   │
│  1   2   3   4   5   6   7    │
│  🔵       🔴       🟠  🟢     │
├─────────────────────────────────┤
│ 🔵Lecture 🔴Exam 🟠HW 🟢Fees ⚫│  ← legend row
└─────────────────────────────────┘

DAY TAP → ModalBottomSheet:
  "March 11, 2026" header
  🔵 Physics Lecture — 10:00 AM
  🔴 Chemistry Test — 12:00 PM
  (or "No events on this day")

SPEC WIREFRAME — ANALYTICS SUBJECT DETAIL:
┌─────────────────────────────────┐
│ ← Physics Analytics             │
│ Current Score: 85%   Grade: A   │
│ Teacher: Mrs. Priya Sharma      │
│ ─────────────────────────────── │
│ Test Name       Date    Score   │
│ Unit Test 1     Mar 10  88/100  │
│ Mid-term Exam   Feb 20  75/100  │
│ ─────────────────────────────── │
│ [LineChart — score over time]   │
│ vs Class Average                │
│ You   ████████ 85%              │  ← horizontal bars
│ Class ██████░░ 74%              │
│ ─────────────────────────────── │
│ Teacher Remarks                 │
│ "Good improvement..." —Mrs.Priya│  ← only if remarks present
└─────────────────────────────────┘

-----------------------------------------------------------------------
SECTION 2 — API CONFIGURATION (update here if endpoints change)
-----------------------------------------------------------------------

ENDPOINT 1 — Calendar Events
  Method  : GET
  Path    : /parent/calendar?studentId={id}&month={1-12}&year={yyyy}
  Base URL: https://dev.indivue.in:8080
  Response: { "month":3,"year":2026,
              "events":[ { "date":"2026-03-11",
                "type":"LECTURE|EXAM|HOMEWORK|FEES|HOLIDAY",
                "title":"string","time":"10:00 AM|null" } ] }

ENDPOINT 2 — Analytics Subject Detail
  Method  : GET
  Path    : /parent/analytics/subject/{subjectId}?studentId={id}
  NOTE    : See doc/DECISIONS.md Q-006 — confirm /parent/ vs /pulse/ prefix.
            Use /parent/ for now; add TODO comment if unconfirmed.
  Response: { "subjectId","subjectName","teacherName"|null,
              "currentScore":85.0,"grade":"A|B|C|D|F",
              "classAverage":74.0,
              "scoreHistory":[ { "testName","date","score":88,"maxScore":100 } ],
              "teacherRemarks":"string|null" }

-----------------------------------------------------------------------
SECTION 3 — OFFLINE SYNC — COMPLETE IMPLEMENTATION
-----------------------------------------------------------------------
Full strategy → doc/CONTEXT.md §9.

Finalise commonMain/data/sync/OfflineSyncManager.kt:
  Observe NetworkConnectivityObserver.observe().distinctUntilChanged()
  On reconnect (false→true):
    syncLeaveQueue()   → query pending_leave_queue WHERE synced=0
                       → POST /parent/leave/apply for each
                       → mark synced=1 on success
    syncMessageQueue() → same pattern for pending_message_queue
  Emit success strings via SharedFlow<String> → UI collects → Snackbar.
  Start in AppStateManager.init via Koin-injected CoroutineScope.

CACHE FRESHNESS RULES — apply to all caching ViewModels:
  Stale thresholds (from doc/CONTEXT.md §9):
    Dashboard=5min, Attendance=60min, Fees=60min,
    Notifications=10min, Calendar=24hr, Analytics=60min
  If stale AND online  → show stale banner + auto-refresh
  If stale AND offline → show stale banner only
  If fresh             → no banner

-----------------------------------------------------------------------
SECTION 4 — TASKS
-----------------------------------------------------------------------

TASK 9.1 — AcademicCalendarScreen + AcademicCalendarViewModel
  Full-screen CalendarWidget (expanded cells, larger than Dashboard widget).
  Event dot legend row (5 colour dots with labels).
  onDayClick → ModalBottomSheet: date title + EventItem list.
  EventItem: colour icon + event title + time (or "No events" empty state).
  Month navigation: loadMonth(studentId, month, year) → cache 24hr.

TASK 9.2 — AnalyticsSubjectDetailScreen + AnalyticsSubjectDetailViewModel
  Score history: LazyColumn table (testName | date | score/maxScore | %).
  LineChart: x=test dates (format "Mar 10"), y=score %.
  Class avg comparison: two horizontal Canvas progress bars.
  Teacher remarks Card: visible only when teacherRemarks != null.
  Pull-to-refresh. Cache in SQLDelight.

TASK 9.3 — SQLDelight Schema
  File: commonMain/sqldelight/parentapp/Calendar.sq
    cached_calendar(student_id,month,year,json_data,cached_at — PK composite)
  File: commonMain/sqldelight/parentapp/Analytics.sq
    cached_analytics_dashboard(student_id PK, json_data, cached_at)
    cached_analytics_subject(student_id,subject_id,json_data,cached_at — PK composite)

TASK 9.4 — Complete OfflineSyncManager per Section 3.

TASK 9.5 — Cache Freshness Audit
  Apply stale thresholds to every caching ViewModel.
  Stale + online → auto-refresh + banner.
  Stale + offline → stale banner only.

-----------------------------------------------------------------------
SECTION 5 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ Academic Calendar: monthly view + coloured event dots
□ Month navigation fetches new month data + caches
□ Day tap: bottom sheet shows events or "No events" empty state
□ Analytics Subject Detail: score history table + line chart + class avg bars
□ Teacher remarks card visible only when data present
□ Offline leave queue: auto-syncs on reconnect + Snackbar shown
□ Offline message queue: auto-syncs on reconnect
□ Cache stale banner appears after threshold exceeded
□ Stale + online: auto-refresh triggered

-----------------------------------------------------------------------
SECTION 6 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. All file paths from ParentAppMultiplatform/
2. Complete code — no truncation
3. SharedFlow sync notification wiring explanation
4. Final SQLDelight schema summary (all .sq files + tables)
5. Q-006 status (analytics endpoint path confirmed or still assumed)
6. Update checklist for doc/CONTEXT.md §13 "After Day 9"
═══════════════════════════════════════════════════════════════════════
```

---
---

## DAY 10 — Full Testing, Release Build & Play Store Submission

```
═══════════════════════════════════════════════════════════════════════
PARENT APP — AI AGENT TASK PROMPT
Day 10 of 10  |  KMP + Compose Multiplatform
Reference docs attached: doc/CONTEXT.md · doc/DECISIONS.md
═══════════════════════════════════════════════════════════════════════

READ doc/CONTEXT.md AND doc/DECISIONS.md IN FULL before writing code.
Full testing checklist → doc/CONTEXT.md §12.
All 20 screen specs → doc/Parent_App_Spec.docx.
Sprint completion criteria → doc/Parent_App_Sprint_Plan.docx.
Check doc/CONTEXT.md §13 "After Day 9" for full interface list.

-----------------------------------------------------------------------
SECTION 1 — BUGS TO FIX (fill this before pasting)
-----------------------------------------------------------------------
[USER INPUT — REQUIRED]
List all bugs found during Days 6–9. Format:
  BUG 1: [Screen] — [Observed] — [Expected]
  BUG 2: ...
Priority: Login > Payment > Attendance > Fees > Dashboard
          > Leave > Messages > Analytics > Calendar > Notifications

[PASTE BUG LIST HERE — write "NO KNOWN BUGS" if none]

-----------------------------------------------------------------------
SECTION 2 — RELEASE BUILD CONFIGURATION (fill before pasting)
-----------------------------------------------------------------------
[USER INPUT — REQUIRED]

Keystore info:
  Keystore file : [full path e.g. ../parentapp-release.keystore]
                  Write "GENERATE NEW" if it does not exist yet.
  Key alias     : [e.g. parentapp]
  Store password: [stored in env var — write the env var name, e.g. KEYSTORE_PASSWORD]
  Key password  : [stored in env var — write the env var name, e.g. KEY_PASSWORD]

If GENERATE NEW:
  keytool -genkey -v -keystore parentapp-release.keystore \
    -alias parentapp -keyalg RSA -keysize 2048 -validity 10000
  Store the .keystore file OUTSIDE the git repository.
  Add to .gitignore: *.keystore

-----------------------------------------------------------------------
SECTION 3 — FULL REGRESSION CHECKLIST
-----------------------------------------------------------------------
Run every item. Report each as: PASS | FAIL (with 1-line description).
Fix all FAILs before proceeding to release build.

AUTH:
  □ Valid login → tokens saved → Dashboard loads
  □ Invalid login → error shown, no crash
  □ 5 failures → 30s countdown lockout
  □ Token auto-refresh: 15min idle → next API call succeeds silently

DASHBOARD:
  □ All 4 stat cards show correct API values
  □ Calendar event dots: correct colours per type
  □ Student switcher → switch → Dashboard reloads

ATTENDANCE:
  □ GaugeChart % correct + threshold colour
  □ "Including Leave" toggle updates % (no API call)
  □ AlertBanner shows/hides correctly at <75% threshold
  □ Calendar view cell colours correct
  □ Subject detail table with coloured status text

FEES:
  □ Summary totals match API
  □ Status badges: PAID=green, PENDING=amber, OVERDUE=red
  □ Razorpay sandbox end-to-end (Android)
  □ Success screen: transaction ID visible
  □ Failure screen: error message + Try Again

LEAVE:
  □ Form validation (all rules)
  □ File attachment validates size + type
  □ Submission → appears in History
  □ Filter tabs: client-side filtering
  □ Cancel button: SUBMITTED status only

COMMUNICATION:
  □ Conversations: unread badges correct
  □ Message Thread: parent=right blue, teacher=left grey
  □ Send message: optimistic + API sync
  □ Announcements: acknowledge button works

NOTIFICATIONS:
  □ Notification Centre pagination
  □ Deep-link tap → correct screen
  □ Mark All Read → all borders removed

ANALYTICS:
  □ Dashboard: summary + subject cards + chart
  □ Trend arrows: UP=green, DOWN=red, STABLE=grey
  □ Subject Detail: score history + chart

CALENDAR:
  □ Monthly view + event dots
  □ Day tap → bottom sheet events

OFFLINE:
  □ Cached data + OfflineBanner on all main screens
  □ Leave offline queue: syncs on reconnect
  □ Message offline queue: syncs on reconnect

ACCESSIBILITY:
  □ 150% font scaling: no overflow on any screen
  □ Min touch targets 44×44dp on all buttons
  □ contentDescription on all icon-only buttons

CROSS-PLATFORM:
  □ Android: runs on physical device or API 26 emulator
  □ iOS: ./gradlew :shared:compileKotlinIosSimulatorArm64 passes
  □ ZERO android.* imports in commonMain (grep audit)

-----------------------------------------------------------------------
SECTION 4 — RELEASE BUILD TASKS
-----------------------------------------------------------------------

TASK 10.1 — Signing Config in androidApp/build.gradle.kts
  signingConfigs { create("release") {
    storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../parentapp-release.keystore")
    storePassword = System.getenv("KEYSTORE_PASSWORD")
    keyAlias = System.getenv("KEY_ALIAS") ?: "parentapp"
    keyPassword = System.getenv("KEY_PASSWORD")
  }}
  buildTypes { release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    signingConfig = signingConfigs.getByName("release")
  }}
  Never hardcode passwords — always System.getenv().

TASK 10.2 — ProGuard Rules in androidApp/proguard-rules.pro
  Rules for: Ktor, Kotlinx Serialization, Razorpay, Koin, SQLDelight,
  Coil, and all com.edmik.parentapp.data.model.** classes.

TASK 10.3 — Build Commands
  Release AAB: ./gradlew :androidApp:bundleRelease
  Output: androidApp/build/outputs/bundle/release/androidApp-release.aab
  Release APK: ./gradlew :androidApp:assembleRelease
  Verify: aab file exists, size <50MB.

TASK 10.4 — ProGuard Smoke Test
  Install release APK: adb install androidApp-release.apk
  Run Login → Dashboard → Attendance → Fees flow.
  Fix any ProGuard-related crashes (usually missing @Keep or keep rules).

-----------------------------------------------------------------------
SECTION 5 — PLAY STORE LISTING ASSETS
-----------------------------------------------------------------------

TASK 10.5 — Generate listing text

App title (30 chars):
  Parent App — School Monitor

Short description (80 chars):
  Track attendance, fees, exams & messages for your child in one app.

Full description:
  [Generate a 400–600 word description covering: attendance tracking,
  fee management with Razorpay, academic calendar, leave management,
  direct teacher messaging, performance analytics, smart notifications,
  multi-child support, biometric login, offline support.
  Tone: trustworthy, simple, parent-focused.]

Screenshots (5 screens — list which screens to capture):
  1. Dashboard (all 4 stat cards visible)
  2. Attendance Summary (GaugeChart visible)
  3. Fee List (PAID + PENDING + OVERDUE items visible)
  4. Analytics Dashboard (subject cards visible)
  5. Notification Centre (mixed read/unread items)

Content rating : Everyone
Category       : Education

TASK 10.6 — Update documentation
  Mark ALL checkboxes in doc/CONTEXT.md §13 as complete.
  Resolve any remaining Open Questions in doc/DECISIONS.md.
  Add final known issues / deferred items as new rows in doc/DECISIONS.md
  under a new "Deferred to v1.0.1" section.

-----------------------------------------------------------------------
SECTION 6 — FINAL COMMIT
-----------------------------------------------------------------------

TASK 10.7
  □ Remove all debug logs from release build (BuildConfig.DEBUG guard)
  □ Remove resolved TODO comments
  □ Verify no API keys / passwords in any source file
  □ Confirm .gitignore includes: *.keystore, google-services.json,
    GoogleService-Info.plist, local.properties

  git add .
  git commit -m "v1.0.0 — Sprint complete: 20 screens, KMP + CMP, Android + iOS"
  git tag v1.0.0
  git push origin main --tags

-----------------------------------------------------------------------
SECTION 7 — ACCEPTANCE CRITERIA
-----------------------------------------------------------------------
□ All Section 3 regression items: PASS
□ Signed release AAB generated successfully (<50MB)
□ Release APK installs + Login→Fees flow works in release mode
□ Play Store listing text generated and ready
□ No hardcoded credentials in any file
□ iOS shared module compiles clean
□ doc/CONTEXT.md §13 all checkboxes ticked
□ v1.0.0 git tag pushed

-----------------------------------------------------------------------
SECTION 8 — OUTPUT FORMAT
-----------------------------------------------------------------------
1. Regression checklist: each item PASS | FAIL (with description)
2. Bugs fixed today: each with 1-line summary
3. Release build output: file path + file size
4. Play Store listing text: copy-paste ready
5. Known issues / deferred to v1.0.1
6. iOS remaining work list (what's needed for App Store release)
7. Final doc/CONTEXT.md §13 update (all checkboxes)
═══════════════════════════════════════════════════════════════════════
```
