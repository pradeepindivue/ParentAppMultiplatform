# DAILY_PROMPTS_V3.md — Parent App · 10 Days · All Split Prompts
# Location: ParentAppMultiplatform/doc/DAILY_PROMPTS_V3.md
#
# ══════════════════════════════════════════════════════════════════════
# HOW TO USE
# ══════════════════════════════════════════════════════════════════════
# • Each day = ONE new chat session
# • Attach doc/CONTEXT.md + doc/DECISIONS.md to every new chat before pasting
# • Paste Prompt A → review output → send proceed signal → paste Prompt B …
# • Proceed signal: "A looks good. Proceed to Prompt B."  (same pattern each day)
# • After the day's final prompt: copy the "After Day X" text into CONTEXT.md §16
#
# COMPACT REFERENCE (every prompt repeats this in its header):
#   Package  : com.edmik.parentapp  |  Repo: ParentAppMultiplatform/
#   API base : https://dev.indivue.in:8080  |  Auth: JWT Bearer
#   Stack    : CMP 1.7.x · Ktor 3.x · Koin 4.x · SQLDelight 2.x · Kotlinx Serialization
#   Rule     : ZERO android.* in commonMain
#   Flow     : Screen → onEvent() → ViewModel → UseCase → Repository → Impl+Mapper
#   State    : data class ScreenState(isLoading, data: DomainModel?, errorMessage)
#   Event    : sealed class ScreenEvent { ... }
#   Mapper   : data/mapper/*Mapper.kt  ← standalone, NOT inside RepositoryImpl
#
# KEY PATH RULES (agents must follow these exactly):
#   API services  → data/remote/api/
#   DTOs          → data/remote/dto/        (@Serializable, never leave data layer)
#   Mappers       → data/mapper/            (fun Dto.toDomain(): DomainModel)
#   DB driver     → data/local/database/
#   .sq files     → data/local/entity/
#   RepositoryImpl→ data/repository/        (imports from data/mapper/)
#   Domain models → domain/model/           (NO @Serializable)
#   Interfaces    → domain/repository/
#   UseCases      → domain/usecase/{feature}/
#   Koin modules  → di/                     (wiring only, no logic)
#   Screens       → presentation/screens/{group}/Screen.kt + ViewModel.kt + State.kt + Event.kt
#   Shared UI     → presentation/components/
#   App state     → presentation/app/       (NOT di/)
# ══════════════════════════════════════════════════════════════════════

# SPLIT OVERVIEW — 38 prompts total
# Day 1  │ 4 prompts │ Setup · Auth DTOs+Repo+UseCase · Login · Forgot Password
# Day 2  │ 5 prompts │ Domain+AppState+SQLDelight · Components I · Components II · Dashboard · Switcher
# Day 3  │ 4 prompts │ Attendance layer · Summary screen · Calendar+Trend · Detail screen
# Day 4  │ 4 prompts │ Fee layer+PaymentLauncher · Fee List · Fee Detail+Payment · Result+Notification
# Day 5  │ 3 prompts │ Bugs+Edge states · Audits · Polish+iOS
# Day 6  │ 3 prompts │ Leave layer+FilePicker · Apply Leave · History+Detail
# Day 7  │ 4 prompts │ Message layer · Conversations · Message Thread · Announcements
# Day 8  │ 4 prompts │ Push+Notification layer · Notification Centre · Prefs+Analytics · Sign-off
# Day 9  │ 4 prompts │ Calendar+Analytics layer+Sync · Calendar screen · Analytics Detail · Cache audit
# Day 10 │ 3 prompts │ Regression Sprint1 · Regression Sprint2+a11y · Release+Store
# ══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 1 — Project Setup + Auth + Login + Forgot Password             ║
║  4 prompts · New chat · Attach CONTEXT.md + DECISIONS.md first      ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 1-A │ Project Setup — Gradle + Theme + Ktor + Koin + Navigation
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 · Prompt A of 4 · Project Setup
Docs attached: doc/CONTEXT.md + doc/DECISIONS.md
Package: com.edmik.parentapp | ZERO android.* in commonMain
══════════════════════════════════════════════════════════════════════
PROJECT is already created via Android Studio KMP wizard (D-027).
Configure the existing files — do not scaffold from scratch.

──── TASK A1 — libs.versions.toml ────
File: gradle/libs.versions.toml
Declare ALL versions and dependencies:
  kotlin = "2.0.0"
  compose-multiplatform = "1.7.0"
  koin = "4.0.0"
  ktor = "3.0.0"
  sqldelight = "2.0.2"
  kotlinx-serialization = "1.7.3"
  kotlinx-coroutines = "1.9.0"
  lifecycle-viewmodel = "2.8.7"
  coil = "3.0.4"
  kotlinx-datetime = "0.6.1"
  multiplatform-settings = "1.2.0"
  navigation-compose = "2.8.4"
  razorpay = "1.6.33"  (Android only, declared but used in androidApp)

──── TASK A2 — Material 3 Theme ────
Files: presentation/theme/Color.kt, Typography.kt, Shapes.kt, Theme.kt
Use exact hex values from doc/CONTEXT.md §8 Design System.
PrimaryBlue=#2196F3, PrimaryBlueDark=#1976D2, ColorPresent=#4CAF50,
ColorAbsent=#F44336, ColorLeave=#FFC107, ColorPending=#FF9800,
TextPrimary=#212121, TextSecondary=#757575

──── TASK A3 — Ktor HttpClientFactory ────
File: data/remote/api/HttpClientFactory.kt
  - Ktor client with OkHttp engine (Android), Darwin (iOS)
  - ContentNegotiation plugin: kotlinx-serialization JSON
  - bearerTokens { } plugin using TokenManager for auto-refresh on 401
  - Logging plugin (DEBUG builds only)
  - Timeout: 30s connect, 60s request

──── TASK A4 — TokenManager ────
File: data/local/database/TokenManager.kt
  Uses multiplatform-settings (encrypted) — NOT EncryptedSharedPreferences.
  fun getAccessToken(): String?
  fun getRefreshToken(): String?
  fun setTokens(access: String, refresh: String)
  fun clearTokens()

──── TASK A5 — Koin Modules ────
Files: di/NetworkModule.kt, di/RepositoryModule.kt, di/UseCaseModule.kt,
       di/DatabaseModule.kt, di/AppModule.kt
  NetworkModule: HttpClientFactory.create(), all API service instances
  RepositoryModule: bind interfaces → implementations (placeholder for now)
  UseCaseModule: placeholder (filled per day)
  DatabaseModule: expect fun createSqlDriver() — actual in androidMain/iosMain
  AppModule: includes all modules, single { AppStateManager(get()) }

──── TASK A6 — Navigation + App Entry ────
File: presentation/navigation/Routes.kt — all 19 route constants from CONTEXT.md §9
File: presentation/navigation/AppNavHost.kt — NavHost with all routes,
  ComingSoonScreen composable for unbuilt screens
File: presentation/app/App.kt — ParentAppTheme { AppNavHost() }
File: iosMain/.../presentation/app/MainViewController.kt
  — fun MainViewController() = ComposeUIViewController { App() }

ACCEPTANCE CRITERIA:
□ libs.versions.toml has all required versions
□ Theme compiles with correct hex values
□ HttpClientFactory uses bearerTokens {} not manual interceptor
□ TokenManager uses multiplatform-settings — no android.* import
□ All 5 Koin modules created and included in AppModule
□ All 19 Routes.kt constants present
□ ./gradlew :shared:compileKotlinAndroid → zero errors

OUTPUT: all file paths + complete code + acceptance results
👉 REVIEW GATE: Verify theme hex values match Figma before proceeding.
   Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 1-B │ Auth Data Layer — DTO + API + Mapper + Repository + UseCases
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 · Prompt B of 4 · Auth Data Layer
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 1-A: Gradle, theme, HttpClientFactory, TokenManager, Koin modules, Navigation

API ENDPOINTS:
  POST /parent/login          body: { studentId, password, rememberMe }
  POST /parent/forgot-password body: { mobile/email } → { message, otpToken }
  POST /parent/refresh-token   body: { refreshToken } → { accessToken, refreshToken }

──── TASK B1 — Auth DTOs ────
File: data/remote/dto/AuthDto.kt
  @Serializable data class LoginRequest(val studentId:String, val password:String, val rememberMe:Boolean)
  @Serializable data class LoginResponse(val accessToken:String, val refreshToken:String,
    val studentId:String, val studentName:String, val batch:String, val profilePhotoUrl:String?=null)
  @Serializable data class ForgotPasswordRequest(val identifier:String)
  @Serializable data class VerifyOtpRequest(val otpToken:String, val otp:String)
  @Serializable data class ResetPasswordRequest(val otpToken:String, val newPassword:String)
  @Serializable data class RefreshTokenRequest(val refreshToken:String)
  @Serializable data class RefreshTokenResponse(val accessToken:String, val refreshToken:String)

──── TASK B2 — Auth API Service ────
File: data/remote/api/AuthApiService.kt
  suspend fun login(request: LoginRequest): LoginResponse
  suspend fun forgotPassword(request: ForgotPasswordRequest): Map<String,String>
  suspend fun verifyOtp(request: VerifyOtpRequest): Map<String,String>
  suspend fun resetPassword(request: ResetPasswordRequest): Map<String,String>
  suspend fun refreshToken(request: RefreshTokenRequest): RefreshTokenResponse

──── TASK B3 — Domain Model ────
File: domain/model/Student.kt
  data class Student(val id:String, val name:String, val batch:String, val photoUrl:String?)
  // NO @Serializable

──── TASK B4 — Auth Repository Interface + Implementation ────
Interface: domain/repository/AuthRepository.kt
  suspend fun login(studentId:String, password:String, rememberMe:Boolean): Result<Student>
  suspend fun forgotPassword(identifier:String): Result<String>
  suspend fun verifyOtp(token:String, otp:String): Result<String>
  suspend fun resetPassword(token:String, newPassword:String): Result<Unit>

Implementation: data/repository/AuthRepositoryImpl.kt
  Note: NO mapper file needed for auth (LoginResponse maps to Student directly inline)
  On login success: call tokenManager.setTokens(access, refresh)

──── TASK B5 — UseCases ────
File: domain/usecase/auth/LoginUseCase.kt
  class LoginUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(studentId:String, password:String, rememberMe:Boolean): Result<Student>
      = repo.login(studentId, password, rememberMe)
  }
File: domain/usecase/auth/ForgotPasswordUseCase.kt — same pattern

Wire both in di/UseCaseModule.kt.

ACCEPTANCE CRITERIA:
□ AuthDto.kt: all @Serializable, zero android.* imports
□ AuthRepository interface returns Student (domain model) — not LoginResponse (DTO)
□ LoginUseCase injects AuthRepository — not AuthRepositoryImpl
□ Compiles clean

OUTPUT: file paths + complete code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 1-C │ Login Screen — State + Event + ViewModel + Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 · Prompt C of 4 · Login Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 1-A: project setup  ✅ 1-B: Auth DTOs, AuthRepository, LoginUseCase

API: POST /parent/login (see 1-B for request/response shape)

WIREFRAME:
  App logo centred (top third)
  OutlinedTextField — Student ID
  OutlinedTextField — Password (visualTransformation, show/hide toggle)
  Row: Checkbox "Remember Me"
  Button "Login" — full width (disabled during loading)
  TextButton "Forgot Password?"
  Biometric login button (fingerprint icon) — visible only after first successful login

BEHAVIOR:
  Success → store tokens via TokenManager → navigate to Routes.DASHBOARD
  Failure → show inline error (NOT a Toast — update State.errorMessage)
  Rate limit: 5 failed attempts → disable Login button 30 seconds (countdown in State)
  Biometric: shown when multiplatform-settings has "biometric_enabled" == true

──── TASK C1 — LoginState + LoginEvent ────
File: presentation/screens/login/LoginState.kt
  data class LoginState(
    val studentId: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val showPassword: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lockoutSecondsRemaining: Int = 0,
    val showBiometric: Boolean = false
  )

File: presentation/screens/login/LoginEvent.kt
  sealed class LoginEvent {
    data class StudentIdChanged(val value: String) : LoginEvent()
    data class PasswordChanged(val value: String) : LoginEvent()
    data class RememberMeToggled(val checked: Boolean) : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    object LoginClicked : LoginEvent()
    object BiometricClicked : LoginEvent()
    object ForgotPasswordClicked : LoginEvent()
  }

──── TASK C2 — LoginViewModel ────
File: presentation/screens/login/LoginViewModel.kt
  Inject: LoginUseCase, TokenManager
  fun onEvent(event: LoginEvent) — dispatcher
  Login flow: validate inputs → LoginUseCase → update State
  Track failed attempts in-memory; start countdown coroutine on 5th failure

──── TASK C3 — LoginScreen ────
File: presentation/screens/login/LoginScreen.kt
  @Composable fun LoginScreen(vm: LoginViewModel = koinViewModel(), onNavigate: (String) -> Unit)
  Collect state via collectAsStateWithLifecycle()
  Navigate to Routes.DASHBOARD or Routes.FORGOT_PASSWORD inside LaunchedEffect on events

ACCEPTANCE CRITERIA:
□ LoginState is a data class (not sealed)
□ LoginEvent is a sealed class
□ ViewModel injects LoginUseCase — NOT AuthRepository directly
□ Error shown inline (State.errorMessage) — no Toast
□ Rate limit: button disabled + countdown correctly shown at 5 failures
□ Navigation handled in Screen via onNavigate lambda — not in ViewModel

OUTPUT: file paths + complete code
👉 Send: "C looks good. Proceed to Prompt D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 1-D │ Forgot Password Screen + Day 1 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 · Prompt D of 4 (Final) · Forgot Password + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 1-A: setup  ✅ 1-B: auth layer  ✅ 1-C: Login screen

API: POST /parent/forgot-password → POST verify OTP → POST reset password (3-step flow)

──── TASK D1 — ForgotPasswordState + ForgotPasswordEvent ────
File: presentation/screens/forgot_password/ForgotPasswordState.kt
  data class ForgotPasswordState(
    val step: ForgotPasswordStep = ForgotPasswordStep.ENTER_IDENTIFIER,
    val identifier: String = "",
    val otp: String = "",
    val otpToken: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
  )
  enum class ForgotPasswordStep { ENTER_IDENTIFIER, ENTER_OTP, ENTER_NEW_PASSWORD }

File: presentation/screens/forgot_password/ForgotPasswordEvent.kt
  sealed class ForgotPasswordEvent {
    data class IdentifierChanged(val value:String) : ForgotPasswordEvent()
    data class OtpChanged(val value:String) : ForgotPasswordEvent()
    data class NewPasswordChanged(val value:String) : ForgotPasswordEvent()
    data class ConfirmPasswordChanged(val value:String) : ForgotPasswordEvent()
    object SendOtpClicked : ForgotPasswordEvent()
    object VerifyOtpClicked : ForgotPasswordEvent()
    object ResetPasswordClicked : ForgotPasswordEvent()
    object BackClicked : ForgotPasswordEvent()
  }

──── TASK D2 — ForgotPasswordViewModel + Screen ────
File: presentation/screens/forgot_password/ForgotPasswordViewModel.kt
  Inject: ForgotPasswordUseCase
  Step progression driven by state machine in onEvent()
  On final success: emit navigation to Routes.LOGIN via callback

File: presentation/screens/forgot_password/ForgotPasswordScreen.kt
  screens/forgot_password/components/OtpInputRow.kt — 6-digit OTP field

──── TASK D3 — Day 1 Final Checks ────
  CHECK 1: ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL
  CHECK 2: ./gradlew :shared:compileKotlinIosSimulatorArm64 → BUILD SUCCESSFUL
  CHECK 3: grep -r "import android\." shared/src/commonMain/ → empty
  CHECK 4: grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/ → empty

ACCEPTANCE CRITERIA:
□ 3-step OTP flow: step state machine correct in ViewModel
□ OTP input: 6-digit, numeric only
□ Password validation: newPassword == confirmPassword before submit
□ All 4 checks pass

OUTPUT: file paths + code + check results + text block for CONTEXT.md §16 "After Day 1"
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 2 — Dashboard + Student Switcher + All 16 Components           ║
║  5 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 2-A │ Foundation — Domain Models + AppState + NetworkObserver + SQLDelight
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 2 · Prompt A of 5 · Foundation
══════════════════════════════════════════════════════════════════════
EXISTS (Day 1): Gradle, theme, HttpClientFactory, TokenManager, Koin,
  Navigation, Auth layer, LoginScreen, ForgotPasswordScreen
Builds foundation only — NO Composables yet.

──── TASK A1 — Dashboard Domain Models ────
File: domain/model/Dashboard.kt
  data class DashboardData(
    val student: Student, val attendancePercentage: Double,
    val pendingFeesAmount: Long, val upcomingExamsCount: Int,
    val pendingHomeworkCount: Int, val recentActivities: List<ActivityItem>,
    val calendarEvents: List<CalendarEvent>, val unreadNotificationCount: Int
  )
  data class ActivityItem(val id:String, val title:String, val date:String, val type:String)
  data class CalendarEvent(val date:String, val type:String, val title:String)
  // Student.kt already created in 1-B  |  NO @Serializable on any of these

──── TASK A2 — AppState + AppStateManager ────
File: presentation/app/AppState.kt  (NOT in di/)
  data class AppState(
    val currentStudentId: String = "",
    val currentStudentName: String = "",
    val currentStudentBatch: String = "",
    val linkedStudents: List<Student> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isOffline: Boolean = false
  )
File: presentation/app/AppStateManager.kt
  Koin singleton. Exposes StateFlow<AppState>.
  fun switchStudent(studentId: String)
  fun updateLinkedStudents(students: List<Student>)
  fun updateUnreadCount(count: Int)
  fun setOffline(isOffline: Boolean)
  init: collect NetworkConnectivityObserver.observe() → setOffline(!connected)
Wire: AppModule — single { AppStateManager(get()) }

──── TASK A3 — NetworkConnectivityObserver (expect/actual) ────
expect: data/platform/NetworkConnectivityObserver.kt
  expect class NetworkConnectivityObserver { fun observe(): Flow<Boolean> }
actual: androidMain/kotlin/com/edmik/parentapp/data/platform/
  NetworkConnectivityObserver.android.kt — ConnectivityManager + callbackFlow
actual: iosMain/kotlin/com/edmik/parentapp/data/platform/
  NetworkConnectivityObserver.ios.kt — NWPathMonitor + callbackFlow
Note: actual package path mirrors commonMain exactly (D-025)

──── TASK A4 — SQLDelight Schema (Dashboard) ────
File: data/local/entity/Dashboard.sq
  CREATE TABLE cached_dashboard(student_id TEXT NOT NULL PRIMARY KEY,
    json_data TEXT NOT NULL, cached_at INTEGER NOT NULL);
  CREATE TABLE linked_students(id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL, batch TEXT NOT NULL, photo_url TEXT);
  Standard selectX/insertX/deleteX named queries.

──── TASK A5 — Update AppModule ────
  Add: single { NetworkConnectivityObserver() }
  Ensure AppStateManager is wired with NetworkConnectivityObserver injected

ACCEPTANCE CRITERIA:
□ Domain models: zero @Serializable, Student reused from Day 1
□ AppStateManager in presentation/app/ — NOT in di/
□ NetworkConnectivityObserver actual paths match commonMain package (D-025)
□ Dashboard.sq has correct table + named queries
□ Compiles clean on Android and iOS

OUTPUT: file paths + complete code + acceptance results
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 2-B │ UI Components I — Layout + Utility (12 components)
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 2 · Prompt B of 5 · UI Components I
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 2-A: domain models, AppStateManager, NetworkObserver, Dashboard.sq
Build 12 layout+utility Composables in presentation/components/.
Do NOT build DonutChart, GaugeChart, LineChart, CalendarWidget yet (→ 2-C).

DESIGN TOKENS (exact values — see CONTEXT.md §8):
  PrimaryBlue=#2196F3, PrimaryBlueDark=#1976D2, ColorPresent=#4CAF50,
  ColorAbsent=#F44336, ColorPending=#FF9800, TextPrimary=#212121, TextSecondary=#757575

BottomNavBar.kt
  5 tabs: Home/Attendance/Fees/Analytics/More(drawer with Leave,Messages,Calendar,Settings)
  Active: PrimaryBlueDark bg + white. Height: 56dp. Param: currentRoute, onTabSelected

StudentHeader.kt
  40dp avatar circle (Coil AsyncImage or initials fallback) + name bold 16sp + batch 12sp
  + bell IconButton + blue badge (hidden@0, count shown, "99+" cap at >99)
  Params: studentName, batch, photoUrl, unreadCount, onBellClick

BatchSelector.kt
  Rounded pill #E3F2FD, graduation cap icon, batch name PrimaryBlueDark bold,
  "Tap to change Batch" caption, chevron. Param: batchName, onTap

ActivityCard.kt
  Row: 40dp circle (EXAM=red, HOMEWORK=blue, FEES=teal, ATTENDANCE=green, ANNOUNCEMENT=purple)
  + title bold + date + type tag. Uses ActivityItem domain model. Min height 56dp.

StatusBadge.kt
  Rounded pill. PAID/APPROVED=green, PENDING/SUBMITTED=amber, OVERDUE/REJECTED=red.
  12sp bold uppercase. Param: status:String

AlertBanner.kt
  Full-width card #FFEBEE bg, warning icon #FFC107, bold title + body.
  AnimatedVisibility. Params: visible, title, message

LoadingOverlay.kt — fillMaxSize Box, 45% black scrim, centred CPI. AnimatedVisibility.

ErrorState.kt — centred column: error icon + message + OutlinedButton "Retry"

EmptyState.kt — centred column: icon + message

OfflineBanner.kt
  #FFF3E0 bg, WifiOff icon orange, "You're offline. Showing cached data."
  AnimatedVisibility(slideInVertically from top). Param: isOffline

SubjectCard.kt — Card, subject name bold, Present=green/Absent=red status, time caption

PullToRefresh.kt — rememberPullToRefreshState (CMP Material 3).
  Params: isRefreshing, onRefresh, content:@Composable()->Unit

ACCEPTANCE CRITERIA:
□ All 12 files in presentation/components/
□ ActivityCard uses ActivityItem domain model — not a DTO
□ ZERO android.* imports
□ Compiles clean

OUTPUT: file paths + complete code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 2-C │ UI Components II — Charts + Calendar (4 components)
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 2 · Prompt C of 5 · UI Components II (Charts)
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 2-A: domain models  ✅ 2-B: 12 components built
All charts: Canvas only — NO third-party chart library (D-010).
CalendarEvent domain model from domain/model/Dashboard.kt.

DonutChart.kt
  data class DonutSegment(value:Float, color:Color, label:String)
  Canvas drawArc proportional segments, 2° gap, strokeWidth=20dp.
  Center text: total count + "Total". Legend right.
  Uses CalendarEvent indirectly — standalone component.
  Signature: DonutChart(segments:List<DonutSegment>, centerLabel:String, modifier)

GaugeChart.kt
  Canvas half-circle gauge. Background=grey#E0E0E0.
  Threshold: ≥85%=green#4CAF50, ≥75%=amber#FF9800, else red#F44336.
  Overlay text: "{pct.toInt()}%" bold 22sp + "Attendance" 12sp.
  Signature: GaugeChart(percentage:Float, modifier)

LineChart.kt
  data class ChartPoint(label:String, value:Float)
  Canvas: Y=0-100, connected path + 0.15α fill, dots, x-axis labels.
  Optional dashed secondary line (class average).
  Signature: LineChart(points, modifier, secondaryPoints?=null)

CalendarWidget.kt
  Uses CalendarEvent domain model from domain/model/Dashboard.kt — NOT a DTO.
  Uses kotlinx.datetime.LocalDate — NOT java.time.
  7-col SUN-SAT grid. Today=blue outline circle. Selected=filled blue.
  Up to 3 event dots per day. Header: ← "Month Year" → with arrows.
  Signature: CalendarWidget(events:List<CalendarEvent>, selectedDate:LocalDate?,
    onDayClick:(LocalDate)->Unit, onMonthChange:(month,year)->Unit, modifier)

ACCEPTANCE CRITERIA:
□ CalendarWidget accepts List<CalendarEvent> (domain model) — never a DTO
□ Uses kotlinx.datetime.LocalDate — zero java.time imports
□ GaugeChart threshold colours: ≥85 green, ≥75 amber, <75 red
□ All 4 charts Canvas-only — no MPAndroidChart or Vico imports
□ Compiles clean

OUTPUT: file paths + complete code
👉 Send: "C looks good. Proceed to Prompt D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 2-D │ Dashboard — DTO + Mapper + Repository + UseCase + Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 2 · Prompt D of 5 · Dashboard Feature
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 2-A: domain models  ✅ 2-B+2-C: all 16 components

API: GET /parent/dashboard?studentId={id}  |  GET /parent/students

[USER INPUT] Attach Figma for Dashboard if available. Figma:
https://www.figma.com/design/RzRLfAzFbVJD466Qzf99hh/Parent-App

WIREFRAME:
  OfflineBanner → StudentHeader → BatchSelector → 4 StatCards
  → CalendarWidget → "Recent Activity" header → ActivityCard list
  → BottomNavBar

──── TASK D1 — Dashboard DTOs ────
File: data/remote/dto/DashboardDto.kt
  @Serializable data class DashboardDto(val student:StudentInfoDto,
    val attendancePercentage:Double, val pendingFeesAmount:Long,
    val upcomingExamsCount:Int, val pendingHomeworkCount:Int,
    val recentActivities:List<ActivityItemDto>,
    val calendarEvents:List<CalendarEventDto>, val unreadNotificationCount:Int)
  @Serializable data class StudentInfoDto(val id:String, val name:String,
    val batch:String, val profilePhotoUrl:String?=null)
  @Serializable data class ActivityItemDto(val id:String, val title:String,
    val date:String, val type:String)
  @Serializable data class CalendarEventDto(val date:String, val type:String, val title:String)
  @Serializable data class LinkedStudentDto(val id:String, val name:String,
    val batch:String, val profilePhotoUrl:String?=null)

──── TASK D2 — Dashboard Mapper ────
File: data/mapper/DashboardMapper.kt  ← standalone file, NOT inside RepositoryImpl
  fun DashboardDto.toDomain() = DashboardData(
    student = student.toDomain(), attendancePercentage, pendingFeesAmount,
    upcomingExamsCount, pendingHomeworkCount,
    recentActivities = recentActivities.map { it.toDomain() },
    calendarEvents = calendarEvents.map { it.toDomain() }, unreadNotificationCount
  )
  fun StudentInfoDto.toDomain() = Student(id, name, batch, profilePhotoUrl)
  fun ActivityItemDto.toDomain() = ActivityItem(id, title, date, type)
  fun CalendarEventDto.toDomain() = CalendarEvent(date, type, title)
  fun LinkedStudentDto.toDomain() = Student(id, name, batch, profilePhotoUrl)

──── TASK D3 — Dashboard API Service + Repository ────
File: data/remote/api/DashboardApiService.kt
  suspend fun getDashboard(studentId:String): DashboardDto
  suspend fun getLinkedStudents(): List<LinkedStudentDto>

Interface: domain/repository/DashboardRepository.kt
  suspend fun getDashboard(studentId:String): Result<DashboardData>  // domain ✅
  suspend fun getCachedDashboard(studentId:String): DashboardData?
  suspend fun getLinkedStudents(): Result<List<Student>>             // domain ✅
  fun getCachedAt(studentId:String): Long?

Implementation: data/repository/DashboardRepositoryImpl.kt
  import com.edmik.parentapp.data.mapper.toDomain  // ← mapper import
  On getDashboard success: cache to SQLDelight + update AppStateManager.updateUnreadCount()
  On getLinkedStudents success: AppStateManager.updateLinkedStudents(students)

──── TASK D4 — UseCases ────
File: domain/usecase/dashboard/GetDashboardUseCase.kt
File: domain/usecase/dashboard/GetLinkedStudentsUseCase.kt
Both: single operator fun invoke() wrapping repository. Wire in UseCaseModule.kt.

──── TASK D5 — DashboardState + Event + ViewModel + Screen ────
File: presentation/screens/home/DashboardState.kt
  data class DashboardState(val isLoading:Boolean=false, val data:DashboardData?=null,
    val errorMessage:String?=null, val isOffline:Boolean=false, val cacheAgeMessage:String?=null)

File: presentation/screens/home/DashboardEvent.kt
  sealed class DashboardEvent { object Load; object Refresh;
    data class SwitchStudent(val id:String); object BellTapped }

File: presentation/screens/home/DashboardViewModel.kt
  Inject: GetDashboardUseCase, AppStateManager (NOT DashboardRepository)
  init: observe AppState.currentStudentId via collectLatest → reload
  Stale threshold: 5 min → show cacheAgeMessage

File: presentation/screens/home/DashboardScreen.kt
  Scaffold + BottomNavBar. All 6 sections in LazyColumn.
  StatCard component (in screens/home/components/StatCard.kt):
    white card, elevation 2dp, value bold + label caption.
    Attendance: "87.5%", Fees: "₹15K" (Indian format), Exams: "3", HW: "2"

ACCEPTANCE CRITERIA:
□ DashboardMapper.kt is standalone in data/mapper/ — no toDomain() in RepositoryImpl
□ DashboardRepositoryImpl imports from data/mapper/ via import statement
□ DashboardRepository interface returns DashboardData (domain) — NOT DashboardDto
□ DashboardViewModel injects GetDashboardUseCase — NOT DashboardRepository
□ DashboardState.data is DashboardData? — NOT DashboardDto?
□ CalendarWidget receives List<CalendarEvent> (domain) — not CalendarEventDto list
□ Stale cache threshold: 5 minutes

OUTPUT: file paths + complete code + architecture checklist
👉 Send: "D looks good. Proceed to Prompt E."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 2-E │ Student Switcher + Linked Students + Day 2 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 2 · Prompt E of 5 (Final) · Student Switcher
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 2-A→2-D: all domain models, 16 components, Dashboard feature complete

API: GET /parent/students → LinkedStudentDto list → mapped to Student domain list

WIREFRAME (ModalBottomSheet):
  "Switch Student" header + [✕] close
  Active row: blue checkmark + avatar + name bold + batch + #E3F2FD bg
  Inactive rows: circle outline + avatar + name + batch
  [+ Link Another Student] OutlinedButton → Snackbar "Coming soon"

──── TASK E1 — StudentSwitcherBottomSheet ────
File: presentation/screens/home/StudentSwitcherBottomSheet.kt
  Params: isVisible, currentStudentId, students:List<Student>,
          onStudentSelected:(Student)->Unit, onDismiss:()->Unit
  Uses Student domain model — NOT LinkedStudentDto.
  GetLinkedStudentsUseCase called from DashboardViewModel (already wired in 2-D).

──── TASK E2 — Wire into DashboardScreen ────
  var showSwitcher by remember { mutableStateOf(false) }
  BatchSelector.onTap + StudentHeader.onBellClick(avatar area) → showSwitcher=true
  StudentSwitcherBottomSheet with students = appState.linkedStudents
  onStudentSelected → appStateManager.switchStudent(student.id)

──── TASK E3 — Day 2 Final Checks ────
  CHECK 1: ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL
  CHECK 2: ./gradlew :shared:compileKotlinIosSimulatorArm64 → BUILD SUCCESSFUL
  CHECK 3: grep -r "import android\." shared/src/commonMain/ → empty
  CHECK 4: grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/ → empty
  CHECK 5: ls shared/src/commonMain/.../presentation/components/ | wc -l → 16

ACCEPTANCE CRITERIA:
□ StudentSwitcherBottomSheet uses List<Student> (domain) — not LinkedStudentDto
□ All 5 checks pass

OUTPUT: file paths + code + check results + CONTEXT.md §16 "After Day 2" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 3 — Attendance: Summary, Detail, Calendar, Trends              ║
║  4 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 3-A │ Attendance Layer — Domain + DTOs + Mapper + Repository + UseCases + SQLDelight
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 3 · Prompt A of 4 · Attendance Data Layer
══════════════════════════════════════════════════════════════════════
EXISTS (Days 1–2): All 16 components, Dashboard feature complete.
See CONTEXT.md §16 After Day 2.

──── TASK A1 — Attendance Domain Models ────
File: domain/model/Attendance.kt
  enum class AttendanceStatus { PRESENT, ABSENT, LEAVE, HOLIDAY, WEEKEND }
  data class AttendanceSummary(val overallPercentage:Double, val overallPercentageWithLeave:Double,
    val presentCount:Int, val absentCount:Int, val leaveCount:Int, val subjects:List<SubjectAttendance>)
  data class SubjectAttendance(val subjectId:String, val subjectName:String, val percentage:Double,
    val presentCount:Int, val absentCount:Int)
  data class AttendanceCalendar(val month:Int, val year:Int, val days:List<AttendanceDay>)
  data class AttendanceDay(val date:String, val status:AttendanceStatus)
  data class AttendanceTrends(val weeks:List<WeekTrend>)
  data class WeekTrend(val label:String, val percentage:Double)
  data class SubjectDetail(val subjectId:String, val subjectName:String, val percentage:Double,
    val presentCount:Int, val absentCount:Int, val records:List<AttendanceRecord>)
  data class AttendanceRecord(val date:String, val period:String, val status:AttendanceStatus)
  // NO @Serializable

──── TASK A2 — Attendance DTOs ────
File: data/remote/dto/AttendanceDto.kt
  All @Serializable — mirrors API response. Status stored as String in DTOs.
  (Same fields as domain models but status:String not enum)

──── TASK A3 — Attendance Mapper ────
File: data/mapper/AttendanceMapper.kt  ← standalone mapper file
  fun AttendanceSummaryDto.toDomain(): AttendanceSummary
  fun AttendanceDayDto.toDomain(): AttendanceDay — converts status String → AttendanceStatus.valueOf()
  fun SubjectDetailDto.toDomain(): SubjectDetail
  fun AttendanceRecordDto.toDomain(): AttendanceRecord — converts status String → enum

──── TASK A4 — Repository Interface + Implementation ────
Interface: domain/repository/AttendanceRepository.kt
  All methods return domain types (AttendanceSummary, AttendanceCalendar etc.)
  Includes getCachedSummary, getCachedCalendar, getCachedAt

Implementation: data/repository/AttendanceRepositoryImpl.kt
  import com.edmik.parentapp.data.mapper.toDomain
  Endpoints:
    GET /parent/attendance/summary?studentId={id}
    GET /parent/attendance/calendar?studentId={id}&month={m}&year={y}
    GET /parent/attendance/trends?studentId={id}
    GET /parent/attendance/subject/{subjectId}?studentId={id}

──── TASK A5 — UseCases ────
Files: domain/usecase/attendance/GetAttendanceSummaryUseCase.kt,
       GetAttendanceCalendarUseCase.kt, GetAttendanceTrendsUseCase.kt,
       GetSubjectAttendanceUseCase.kt
Wire all 4 in UseCaseModule.kt.

──── TASK A6 — SQLDelight Schema ────
File: data/local/entity/Attendance.sq
  cached_attendance_summary(student_id PK, json_data, cached_at)
  cached_attendance_calendar(student_id, month, year, json_data, cached_at — composite PK)
  cached_attendance_subject(student_id, subject_id, json_data, cached_at — composite PK)

ACCEPTANCE CRITERIA:
□ AttendanceMapper.kt is standalone in data/mapper/ (not inside RepositoryImpl)
□ AttendanceStatus converted from String via valueOf() in mapper
□ All 4 repository methods return domain types — not DTOs
□ 4 UseCases in domain/usecase/attendance/ folder
□ Compiles clean

OUTPUT: file paths + complete code
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 3-B │ Attendance Summary Screen + ViewModel
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 3 · Prompt B of 4 · Attendance Summary Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 3-A: attendance domain models, mapper, repository, 4 usecases, SQLDelight

[USER INPUT — OPTIONAL] Attach Figma for Attendance screens.

WIREFRAME (Class View tab):
  GaugeChart(overall%) → "Including Leave" Switch → Present/Absent/Leave counts
  Subject list: name + LinearProgressIndicator + % badge
    (≥85%=green, 75–84%=amber, <75%=red)
  AlertBanner if any subject <75%: "SubjectName: X% — Attend N more classes to reach 75%"
    N = ceil((0.75 × totalClasses - presentCount) / 0.25)  totalClasses = present + absent
  Toggle: [Calendar View] [Trend View]

──── TASK B1 — AttendanceSummaryState + Event ────
File: presentation/screens/attendance/AttendanceSummaryState.kt
  data class AttendanceSummaryState(
    val isLoading:Boolean=false, val summary:AttendanceSummary?=null,
    val calendar:AttendanceCalendar?=null, val trends:AttendanceTrends?=null,
    val includeLeave:Boolean=false, val activeView:AttendanceView=AttendanceView.SUMMARY,
    val errorMessage:String?=null, val isOffline:Boolean=false, val cacheAgeMessage:String?=null
  )
  enum class AttendanceView { SUMMARY, CALENDAR, TREND }

File: presentation/screens/attendance/AttendanceSummaryEvent.kt
  sealed class: Load(studentId), Refresh, ToggleLeave, ShowCalendar, ShowTrend,
    CalendarMonthChanged(month,year), SubjectTapped(subjectId)

──── TASK B2 — AttendanceSummaryViewModel ────
  Inject: GetAttendanceSummaryUseCase, GetAttendanceCalendarUseCase,
          GetAttendanceTrendsUseCase, AppStateManager
  init: observe AppState.currentStudentId → reload
  60min stale threshold.

──── TASK B3 — AttendanceSummaryScreen ────
File: presentation/screens/attendance/AttendanceSummaryScreen.kt
  GaugeChart uses state.summary — percentage chosen based on includeLeave flag.
  AlertBanner N formula correctly applied from spec.
  Calendar/Trend toggle controlled by AttendanceView enum in state.
  SubjectProgressRow in screens/attendance/components/SubjectProgressRow.kt.
  AttendanceDayCell in screens/attendance/components/AttendanceDayCell.kt.

ACCEPTANCE CRITERIA:
□ AttendanceSummaryViewModel injects UseCases — NOT AttendanceRepository
□ GaugeChart receives Double from AttendanceSummary domain model
□ AlertBanner N formula: ceil((0.75 × (present+absent) - present) / 0.25)
□ AttendanceView enum drives calendar/trend toggle

OUTPUT: file paths + complete code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 3-C │ Calendar View + Trend View Toggle Sections
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 3 · Prompt C of 4 · Calendar + Trend Views
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 3-A: data layer  ✅ 3-B: AttendanceSummaryScreen with AttendanceView toggle

CalendarWidget in presentation/components/ accepts List<CalendarEvent> (domain model).
Need to map AttendanceCalendar (domain) → List<CalendarEvent> for the widget.

──── TASK C1 — Calendar View Section ────
When AttendanceView.CALENDAR active:
  Map AttendanceCalendar.days → List<CalendarEvent>:
    PRESENT → CalendarEvent(date, "PRESENT", "")
    ABSENT → CalendarEvent(date, "ABSENT", "")
    LEAVE → CalendarEvent(date, "LEAVE", "")
    HOLIDAY → CalendarEvent(date, "HOLIDAY", "")
  Custom AttendanceDayCell override cell bg:
    PRESENT=green#4CAF50, ABSENT=red#F44336, LEAVE=blue#2196F3 outline, HOLIDAY=grey#EEEEEE
  Month navigation → dispatch AttendanceSummaryEvent.CalendarMonthChanged(newMonth, year)
    → ViewModel calls GetAttendanceCalendarUseCase

──── TASK C2 — Trend View Section ────
When AttendanceView.TREND active:
  Map AttendanceTrends.weeks → List<ChartPoint>:
    weeks.map { ChartPoint(label=it.label, value=it.percentage.toFloat()) }
  LineChart(points = chartPoints) — x-axis labels = week labels

──── TASK C3 — Verify No DTO Leakage ────
  AttendanceSummaryScreen must NOT import from data/remote/dto/
  All state fields use domain types: AttendanceSummary, AttendanceCalendar, AttendanceTrends

ACCEPTANCE CRITERIA:
□ CalendarWidget receives List<CalendarEvent> derived from AttendanceCalendar domain model
□ Mapping is done in the Screen (not ViewModel) — ViewModel keeps domain types
□ AttendanceStatus enum drives cell colours — not raw String comparison
□ Month navigation triggers GetAttendanceCalendarUseCase via ViewModel event

OUTPUT: modified files + complete updated sections
👉 Send: "C looks good. Proceed to Prompt D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 3-D │ Attendance Detail Screen + Day 3 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 3 · Prompt D of 4 (Final) · Detail + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 3-A: data layer  ✅ 3-B: summary screen  ✅ 3-C: calendar+trend

API: GET /parent/attendance/subject/{subjectId}?studentId={id}
→ mapped to SubjectDetail domain model via AttendanceMapper.kt

WIREFRAME: TopAppBar ← SubjectName [92%] | DonutChart | day-by-day table | LineChart

──── TASK D1 — AttendanceDetailState + Event + ViewModel + Screen ────
File: presentation/screens/attendance/AttendanceDetailState.kt
  data class AttendanceDetailState(val isLoading:Boolean=false,
    val detail:SubjectDetail?=null, val errorMessage:String?=null, val isOffline:Boolean=false)

File: presentation/screens/attendance/AttendanceDetailEvent.kt
  sealed: Load(subjectId,studentId), Refresh

File: presentation/screens/attendance/AttendanceDetailViewModel.kt
  Inject: GetSubjectAttendanceUseCase — NOT AttendanceRepository

File: presentation/screens/attendance/AttendanceDetailScreen.kt
  DonutChart: segments from SubjectDetail (presentCount/absentCount/leaveCount)
    Colors: PRESENT=#4CAF50, ABSENT=#F44336, LEAVE=#2196F3
  Table: SubjectDetail.records as AttendanceRecord list
    Status text: AttendanceStatus.PRESENT=green, ABSENT=red, LEAVE=blue (from enum)
  LineChart: derive weekly % from records grouped by week

──── TASK D2 — Day 3 Final Checks ────
  CHECK 1: ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL
  CHECK 2: ./gradlew :shared:compileKotlinIosSimulatorArm64 → BUILD SUCCESSFUL
  CHECK 3: grep -r "import android\." shared/src/commonMain/ → empty
  CHECK 4: grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/ → empty

ACCEPTANCE CRITERIA:
□ AttendanceDetailViewModel injects GetSubjectAttendanceUseCase
□ AttendanceDetailState.detail is SubjectDetail (domain) — not a DTO
□ Status colour from AttendanceStatus enum — not String.equals()
□ All 4 checks pass

OUTPUT: file paths + code + check results + CONTEXT.md §16 "After Day 3" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 4 — Fees, Payment, Notification Centre (basic)                 ║
║  4 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 4-A │ Fee Layer — Domain + DTOs + Mapper + PaymentLauncher + SQLDelight
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 4 · Prompt A of 4 · Fee Data Layer
══════════════════════════════════════════════════════════════════════

──── TASK A1 — Fee Domain Models ────
File: domain/model/Fee.kt
  enum class FeeStatus { PAID, PENDING, OVERDUE }
  data class FeeListData(val totalAmount:Long, val paidAmount:Long, val pendingAmount:Long, val fees:List<Fee>)
  data class Fee(val feeId:String, val name:String, val term:String, val amount:Long,
    val dueDate:String, val status:FeeStatus, val paidDate:String?=null)
  data class FeeDetail(val feeId:String, val name:String, val status:FeeStatus,
    val baseAmount:Long, val lateFee:Long, val tax:Long, val totalAmount:Long,
    val dueDate:String, val paymentHistory:List<PaymentRecord>)
  data class PaymentRecord(val date:String, val amount:Long, val mode:String, val transactionId:String)
  // NO @Serializable

──── TASK A2 — Fee DTOs ────
File: data/remote/dto/FeeDto.kt  (all @Serializable)
  FeeListDto, FeeItemDto (status:String), FeeDetailDto, PaymentRecordDto,
  InitiatePaymentRequest, InitiatePaymentResponse (razorpayOrderId, amount, currency, studentName, studentEmail),
  VerifyPaymentRequest (razorpayPaymentId, orderId, signature, feeId),
  VerifyPaymentResponse (success:Boolean, transactionId:String, receiptUrl:String)

──── TASK A3 — Fee Mapper ────
File: data/mapper/FeeMapper.kt
  fun FeeListDto.toDomain(): FeeListData
  fun FeeItemDto.toDomain(): Fee  — FeeStatus.valueOf(status)
  fun FeeDetailDto.toDomain(): FeeDetail

──── TASK A4 — Repository Interface + Implementation ────
Interface: domain/repository/FeeRepository.kt
  suspend fun getFeeList(studentId:String): Result<FeeListData>   // domain ✅
  suspend fun getFeeDetail(feeId:String): Result<FeeDetail>       // domain ✅
  suspend fun initiatePayment(feeId:String, studentId:String): Result<InitiatePaymentResponse>
  suspend fun verifyPayment(req:VerifyPaymentRequest): Result<VerifyPaymentResponse>
  suspend fun getCachedFeeList(studentId:String): FeeListData?

Note: InitiatePaymentResponse stays in data layer — it contains gateway-specific fields.
Wire: RepositoryModule + 4 UseCases in domain/usecase/fees/ + UseCaseModule.

──── TASK A5 — PaymentLauncher (expect/actual) ────
expect: data/payment/PaymentLauncher.kt
  data class PaymentRequest(orderId, amount, currency, description, studentName, studentEmail)
  sealed class PaymentOutcome { Success(paymentId,orderId,signature); Failure(code,msg); Cancelled }
  expect class PaymentLauncher { suspend fun launch(req:PaymentRequest): PaymentOutcome }

actual: androidMain/kotlin/com/edmik/parentapp/data/payment/PaymentLauncher.android.kt
  Razorpay SDK: com.razorpay:checkout:1.6.33. Bridge via suspendCoroutine.

actual: iosMain/kotlin/com/edmik/parentapp/data/payment/PaymentLauncher.ios.kt
  Stub: return PaymentOutcome.Failure("IOS_PENDING", "iOS payment setup pending") (Q-003 open)

Actual paths must mirror commonMain package exactly (D-025).

──── TASK A6 — SQLDelight Schema ────
File: data/local/entity/Fees.sq
  cached_fee_list(student_id PK, json_data, cached_at)

ACCEPTANCE CRITERIA:
□ FeeMapper.kt standalone in data/mapper/ — FeeStatus.valueOf() in mapper
□ FeeRepository interface returns FeeListData/FeeDetail domain types
□ PaymentLauncher actual paths: .../data/payment/ in both androidMain and iosMain
□ 4 Fee UseCases in domain/usecase/fees/
□ Compiles clean

OUTPUT: file paths + code + Razorpay Gradle dep string
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 4-B │ Fee List Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 4 · Prompt B of 4 · Fee List Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 4-A: Fee domain models, mapper, repository, 4 UseCases, PaymentLauncher

WIREFRAME: Summary card (Total/Paid/Pending) + Fee cards with StatusBadge + Pay Now/Receipt buttons

──── TASKS ────
presentation/screens/fees/FeeListState.kt
  data class FeeListState(val isLoading:Boolean=false, val data:FeeListData?=null,
    val errorMessage:String?=null, val isOffline:Boolean=false, val cacheAgeMessage:String?=null)

presentation/screens/fees/FeeListEvent.kt — Load, Refresh, FeeTapped(feeId), PayNowTapped(feeId)

presentation/screens/fees/FeeListViewModel.kt
  Inject: GetFeeListUseCase (NOT FeeRepository)
  60min stale. Observe currentStudentId → reload. Offline: disable Pay Now.

presentation/screens/fees/FeeListScreen.kt
  Summary card: Indian format ₹1,20,000.
  FeeItemCard (screens/fees/components/FeeItemCard.kt):
    StatusBadge(fee.status.name). "Pay Now" for PENDING/OVERDUE. "Download Receipt" for PAID.
  OfflineBanner + PullToRefresh + LoadingOverlay + ErrorState.

ACCEPTANCE CRITERIA:
□ FeeListViewModel injects GetFeeListUseCase
□ FeeListState.data is FeeListData (domain) — not FeeListDto
□ StatusBadge receives fee.status.name (from FeeStatus enum — not hardcoded String)
□ "Pay Now" disabled when isOffline=true

OUTPUT: file paths + complete code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 4-C │ Fee Detail + Payment Flow
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 4 · Prompt C of 4 · Fee Detail + Payment
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 4-A: FeeDetail domain model, PaymentLauncher  ✅ 4-B: FeeListScreen

API:
  GET  /parent/fees/{feeId}        → FeeDetailDto → FeeDetail (domain)
  POST /parent/fees/pay            → InitiatePaymentResponse (gateway fields)
  POST /parent/fees/pay/verify     → VerifyPaymentResponse

WIREFRAME: TopAppBar + breakdown card + payment history list + "Pay Now" button

──── TASKS ────
presentation/screens/fees/FeeDetailState.kt
  data class FeeDetailState(val isLoading:Boolean=false, val detail:FeeDetail?=null,
    val paymentState:PaymentState=PaymentState.Idle,
    val errorMessage:String?=null, val isOffline:Boolean=false)
  sealed class PaymentState { object Idle; object Processing; data class Done(val outcome:PaymentOutcome) }

presentation/screens/fees/FeeDetailEvent.kt — Load(feeId), PayNowTapped

presentation/screens/fees/FeeDetailViewModel.kt
  Inject: GetFeeDetailUseCase, InitiatePaymentUseCase, VerifyPaymentUseCase,
          PaymentLauncher, AppStateManager
  Payment chain:
    1. InitiatePaymentUseCase(feeId, studentId) → InitiatePaymentResponse
    2. PaymentLauncher.launch(PaymentRequest)
    3. Success → VerifyPaymentUseCase → emit SharedFlow<PaymentNavEvent>
    4. Failure/Cancelled → emit PaymentNavEvent(success=false)
  data class PaymentNavEvent(success:Boolean, transactionId:String?, receiptUrl:String?, error:String?)

presentation/screens/fees/FeeDetailScreen.kt
  FeeBreakdownRow in screens/fees/components/FeeBreakdownRow.kt.
  "Pay Now" disabled when detail.status == FeeStatus.PAID || isOffline.
  LoadingOverlay shown when paymentState == Processing.
  Collect PaymentNavEvent → navigate to Routes.PAYMENT_RESULT.

ACCEPTANCE CRITERIA:
□ FeeDetailViewModel injects UseCases — NOT FeeRepository
□ FeeDetailState.detail is FeeDetail (domain)
□ Payment chain goes: UseCase → PaymentLauncher → VerifyUseCase
□ "Pay Now" disabled check uses FeeStatus.PAID enum comparison

OUTPUT: file paths + complete code + payment flow diagram in comments
👉 Send: "C looks good. Proceed to Prompt D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 4-D │ Payment Result + Notification Centre (basic) + Day 4 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 4 · Prompt D of 4 (Final) · Result + Notifications + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 4-A→4-C: full fee feature

──── TASK D1 — PaymentResultScreen ────
File: presentation/screens/fees/PaymentResultScreen.kt
  Nav args: success:Boolean, transactionId:String?, receiptUrl:String?, errorMessage:String?
  Success: spring-scale animated green checkmark + txId + "Download Receipt" (opens browser)
    + "Back to Fees" (popBackStack inclusive to Routes.FEES, clears FeeDetail from stack)
  Failure: red ✕ + error + "Try Again" (back to FeeDetail) + "Back to Fees"

──── TASK D2 — Notification Centre (basic, no push yet) ────
Files: presentation/screens/notifications/NotificationCentreState.kt
       presentation/screens/notifications/NotificationCentreEvent.kt
       presentation/screens/notifications/NotificationCentreViewModel.kt
       presentation/screens/notifications/NotificationCentreScreen.kt
  Basic implementation: list of placeholders with "Notifications coming in Day 8"
  EmptyState composable if no data. State, Event, ViewModel structure ready for Day 8 integration.

──── TASK D3 — Day 4 Final Checks ────
  All 4 checks (compile Android, iOS, grep android.*, grep data.* in presentation)

ACCEPTANCE CRITERIA:
□ PaymentResult: "Back to Fees" pops inclusive to FEES (FeeDetail NOT in back stack)
□ "Try Again": popBackStack to FeeDetail (detail stays in stack)
□ NotificationCentreScreen: compiles, shows EmptyState with placeholder text
□ All 4 checks pass

OUTPUT: file paths + code + check results + CONTEXT.md §16 "After Day 4" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 5 — Sprint 1 Polish, Edge States, Integration Testing          ║
║  3 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 5-A │ Bug Fixes + Edge State Coverage
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 5 · Prompt A of 3 · Bugs + Edge States
══════════════════════════════════════════════════════════════════════
EXISTS (Days 1–4): 10 screens. See CONTEXT.md §16 After Day 4.

SECTION 1 — BUG LIST [USER INPUT — fill before pasting]:
  BUG 1: [Screen] — [Observed] — [Expected]
  [PASTE BUGS HERE or write "NO KNOWN BUGS"]

SECTION 2 — EDGE STATE AUDIT
For every screen (Login, Dashboard, AttendanceSummary, AttendanceDetail,
FeeList, FeeDetail, PaymentResult, NotificationCentre):
  □ LoadingOverlay during initial API call (isLoading=true in State)
  □ ErrorState + "Retry" on failure (errorMessage in State, onRetry event)
  □ EmptyState for empty lists
  □ PullToRefresh wrapper
  □ OfflineBanner when AppState.isOffline=true

SECTION 3 — BACK STACK AUDIT
  □ Back on Dashboard tab → stays Dashboard (no app exit)
  □ FeeDetail → PaymentResult → "Back to Fees" → FeeList (not FeeDetail)
  □ AttendanceDetail → back → AttendanceSummary
  □ Login → Dashboard → back → does NOT return to Login (clear back stack on login)

SECTION 4 — ARCHITECTURE QUICK AUDIT
  grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/ → must be empty
  grep -r "Repository" shared/src/commonMain/presentation/ → must be empty (ViewModels use UseCases only)
  Report violations and fix them.

ACCEPTANCE CRITERIA:
□ All listed bugs: Fixed | Needs Info
□ Every screen: loading/error/empty/offline states present
□ Back stack correct for all 4 paths
□ Both grep audits return empty

OUTPUT: bug status + edge state audit per screen + back stack + architecture audit results
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 5-B │ Code Quality Audits — Purity + Error Handling + Reload Chain
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 5 · Prompt B of 3 · Code Audits
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 5-A: bugs fixed, edge states added

AUDIT A — commonMain Purity:
  grep -r "import android\." shared/src/commonMain/ → empty
  grep -r "import java.io\." shared/src/commonMain/ → empty
  Fix any violations via expect/actual.

AUDIT B — API Error Handling in every ViewModel:
  Network timeout → errorMessage = "Connection timed out. Retry?"
  HTTP 401 → handled by Ktor bearerTokens plugin — ViewModel does NOT catch this
  HTTP 500 → errorMessage = "Server error. Please try later."
  HTTP 404 → EmptyState message (context-appropriate)
  Verify: LoginViewModel, DashboardViewModel, AttendanceSummaryViewModel,
          AttendanceDetailViewModel, FeeListViewModel, FeeDetailViewModel

AUDIT C — UseCase Layer Completeness:
  Every ViewModel must inject and call UseCases only.
  grep -r "RepositoryImpl\|Repository(" shared/src/commonMain/presentation/ → empty
  Each UseCase wired in UseCaseModule.kt — verify.

AUDIT D — Notification Badge + Student Reload:
  □ AppState.unreadNotificationCount updated in DashboardViewModel on success
  □ StudentHeader bell badge: hidden@0, count shown, "99+" cap
  □ currentStudentId change → DashboardViewModel, AttendanceSummaryViewModel,
    FeeListViewModel all reload via collectLatest

ACCEPTANCE CRITERIA:
□ Both grep audits empty
□ All 6 ViewModels: timeout/500/404 handled correctly
□ All ViewModels: inject UseCases only (no Repository injection)
□ Student reload chain: 3 ViewModels reload on currentStudentId change

OUTPUT: each audit → PASS | FIXED (what changed)
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 5-C │ Polish + iOS Compile + Day 5 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 5 · Prompt C of 3 (Final) · Polish + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 5-A: bugs+edge states  ✅ 5-B: all audits passed

TASK C1 — Strings.kt: util/Strings.kt — all user-visible text constants.
  Replace every hardcoded string in Composables with Strings.XXX reference.

TASK C2 — @Immutable: Add to all domain model data classes in domain/model/:
  Student, DashboardData, ActivityItem, CalendarEvent, AttendanceSummary,
  SubjectAttendance, SubjectDetail, AttendanceRecord, FeeListData, Fee, FeeDetail, PaymentRecord
  (@Immutable prevents unnecessary recomposition)

TASK C3 — 150% Font Scaling Fixes:
  Dashboard stat cards → maxLines=1 + TextOverflow.Ellipsis
  CalendarWidget day cells → minimum 10sp
  BottomNavBar labels → minimum 10sp
  Fee list summary card columns → don't collapse

TASK C4 — iOS Compile:
  ./gradlew :shared:compileKotlinIosSimulatorArm64
  Fix ALL errors. Report: BUILD SUCCESSFUL ✅ or list errors + fixes.

ACCEPTANCE CRITERIA:
□ Strings.kt exists; zero hardcoded UI strings in Composables
□ @Immutable on all 12 domain model data classes
□ 150% scaling: no overflow on any Sprint 1 screen
□ iOS compile: BUILD SUCCESSFUL

OUTPUT: files modified + 150% fixes + iOS compile result + CONTEXT.md §16 "After Day 5" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 6 — Leave Management: Apply, History, Detail                   ║
║  3 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 6-A │ Leave Layer — Domain + DTOs + Mapper + FilePicker + SQLDelight
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 6 · Prompt A of 3 · Leave Data Layer
══════════════════════════════════════════════════════════════════════

──── TASKS ────
domain/model/Leave.kt
  enum class LeaveType { SICK, FAMILY_EMERGENCY, PERSONAL, OTHER }
  enum class LeaveStatus { SUBMITTED, PENDING, APPROVED, REJECTED }
  data class LeaveHistoryItem(leaveId,leaveType:LeaveType,startDate,endDate,status:LeaveStatus,submittedAt)
  data class LeaveDetail(leaveId,leaveType:LeaveType,startDate,endDate,reason,status:LeaveStatus,
    submittedAt,reviewedAt?,reviewerName?,reviewerRemarks?,attachmentUrl?)
  // NO @Serializable

data/remote/dto/LeaveDto.kt — all @Serializable, status/type as String

data/mapper/LeaveMapper.kt
  fun LeaveHistoryDto.toDomain(): LeaveHistoryItem — LeaveType.valueOf() + LeaveStatus.valueOf()
  fun LeaveDetailDto.toDomain(): LeaveDetail

domain/repository/LeaveRepository.kt — all methods return domain types
data/repository/LeaveRepositoryImpl.kt — import from data/mapper/

UseCases: domain/usecase/leave/ — SubmitLeaveUseCase, GetLeaveHistoryUseCase,
  GetLeaveDetailUseCase, CancelLeaveUseCase. Wire in UseCaseModule.kt.

FilePicker (expect/actual):
  expect: data/platform/FilePicker.kt
    data class PickedFile(name:String, sizeBytes:Long, mimeType:String, bytes:ByteArray)
    expect class FilePicker { suspend fun pickFile(allowedMimeTypes:List<String>): PickedFile? }
  actual paths: .../data/platform/ in both androidMain and iosMain (D-025)
  Android actual: ActivityResultContracts.OpenDocument, filter PDF/JPG/PNG
  iOS actual: UIDocumentPickerViewController stub

SQLDelight: data/local/entity/Leave.sq
  pending_leave_queue(id PK,leave_type,start_date,end_date,reason,attachment_bytes BLOB,queued_at,synced DEFAULT 0)
  cached_leave_history(student_id PK, json_data, cached_at)

ACCEPTANCE CRITERIA:
□ LeaveMapper.kt standalone in data/mapper/ — enum valueOf() conversions
□ FilePicker actual paths: .../data/platform/ mirroring commonMain (D-025)
□ 4 UseCases in domain/usecase/leave/
□ Leave.sq: pending_leave_queue table has synced column with DEFAULT 0
□ Compiles clean

OUTPUT: file paths + complete code
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 6-B │ Apply Leave Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 6 · Prompt B of 3 · Apply Leave Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 6-A: Leave domain models, mapper, UseCases, FilePicker, SQLDelight

[USER INPUT — OPTIONAL] Attach screenshot for Apply Leave screen.

WIREFRAME: Dropdown (4 types), date pickers, reason textarea (min 10 chars + counter), file picker, Submit (disabled until valid), confirmation dialog

TASKS:
presentation/screens/leave/ApplyLeaveState.kt
  data class ApplyLeaveState(val leaveType:LeaveType?=null, startDate, endDate, reason,
    file:PickedFile?=null, val isLoading:Boolean=false, val errorMessage:String?=null,
    fieldErrors:Map<String,String> — per-field validation errors)

presentation/screens/leave/ApplyLeaveEvent.kt
  LeaveTypeSelected(type:LeaveType), StartDateSelected, EndDateSelected, ReasonChanged,
  AttachDocumentClicked, RemoveDocument, SubmitClicked, ConfirmSubmit, DismissConfirmation

presentation/screens/leave/ApplyLeaveViewModel.kt
  Inject: SubmitLeaveUseCase, FilePicker, AppStateManager
  Validation: leaveType required, startDate not in past, endDate ≥ startDate,
    reason ≥ 10 chars, file ≤ 5 MB + PDF/JPG/PNG
  Online: SubmitLeaveUseCase → navigate LEAVE_HISTORY
  Offline: insert to pending_leave_queue → Snackbar "Will submit when reconnected"

presentation/screens/leave/ApplyLeaveScreen.kt
  LeaveTypeDropdown in screens/leave/components/LeaveTypeDropdown.kt
    Uses LeaveType.values() — not hardcoded strings
  DatePickerDialog (Material 3 CMP)
  Confirmation AlertDialog before submit

ACCEPTANCE CRITERIA:
□ ApplyLeaveViewModel injects SubmitLeaveUseCase — not LeaveRepository
□ LeaveType.values() drives dropdown options
□ File validation: sizeBytes ≤ 5*1024*1024 + mimeType check
□ Submit disabled until all validations pass (driven by State.fieldErrors)

OUTPUT: file paths + complete code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 6-C │ Leave History + Leave Detail + Day 6 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 6 · Prompt C of 3 (Final) · History + Detail + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 6-A: data layer  ✅ 6-B: ApplyLeaveScreen

WIREFRAMES:
  History: TabRow All/Pending/Approved/Rejected + LeaveHistoryItem cards + FAB → ApplyLeave
  Detail: full info + 3-step status timeline + Cancel button (SUBMITTED only)

TASKS:
LeaveHistoryState.kt — data class with data:List<LeaveHistoryItem>?, filter:LeaveStatus?
LeaveHistoryEvent.kt — Load, Refresh, FilterChanged(status:LeaveStatus?), LeaveDetailTapped(leaveId)
LeaveHistoryViewModel.kt — inject GetLeaveHistoryUseCase. Client-side filter via LeaveStatus enum.
LeaveHistoryScreen.kt — StatusBadge(item.status.name). Filter using enum comparison.

LeaveDetailState.kt — data class with detail:LeaveDetail?, showCancelDialog:Boolean
LeaveDetailEvent.kt — Load(leaveId), CancelClicked, ConfirmCancel, DismissCancel
LeaveDetailViewModel.kt — inject GetLeaveDetailUseCase, CancelLeaveUseCase
LeaveDetailScreen.kt:
  LeaveStatusTimeline (screens/leave/components/LeaveStatusTimeline.kt):
    3 steps: Submitted → Under Review → Approved/Rejected
    SUBMITTED: step 1 filled | PENDING: 1+2 | APPROVED/REJECTED: all 3
    Step 3: green=APPROVED, red=REJECTED
  Cancel button: visible ONLY when detail.status == LeaveStatus.SUBMITTED

FINAL CHECKS:
  All 4 standard checks (compile Android, iOS, grep android.*, grep data.* in presentation)

ACCEPTANCE CRITERIA:
□ Both ViewModels inject UseCases — not LeaveRepository
□ Filter uses LeaveStatus enum comparison — not String.equals()
□ Cancel button conditional on LeaveStatus.SUBMITTED enum value
□ All 4 checks pass

OUTPUT: file paths + code + check results + CONTEXT.md §16 "After Day 6" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 7 — Communication: Conversations, Messages, Announcements      ║
║  4 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 7-A │ Message Layer — Domain + DTOs + Mapper + SQLDelight + OfflineSyncManager
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 7 · Prompt A of 4 · Communication Data Layer
══════════════════════════════════════════════════════════════════════

TASKS:
domain/model/Message.kt
  enum class SenderType { PARENT, TEACHER, ADMIN }
  enum class MessageLocalState { SENT, SENDING, FAILED }
  data class Conversation(conversationId,participantName,participantRole,participantPhotoUrl?,
    lastMessage,lastMessageAt,unreadCount:Int)
  data class Message(messageId:String?,senderId,senderType:SenderType,text,sentAt,
    isRead:Boolean,localState:MessageLocalState=MessageLocalState.SENT)
  data class MessageThread(conversationId,participantName,messages:List<Message>)
  // NO @Serializable

domain/model/Announcement.kt
  data class Announcement(id,title,body,date,acknowledged:Boolean,attachments:List<AnnouncementAttachment>)
  data class AnnouncementAttachment(name,url,type)

data/remote/dto/MessageDto.kt — all @Serializable, senderType as String

data/mapper/MessageMapper.kt
  Conversions: SenderType.valueOf() for all message types
  fun ConversationDto.toDomain(), fun MessageDto.toDomain(), fun AnnouncementDto.toDomain()

domain/repository/MessagingRepository.kt — returns domain types only
data/repository/MessagingRepositoryImpl.kt — imports from data/mapper/

UseCases: domain/usecase/messages/ — GetConversationsUseCase, GetMessageThreadUseCase,
  SendMessageUseCase, GetAnnouncementsUseCase, AcknowledgeAnnouncementUseCase
Wire in UseCaseModule.kt.

data/local/entity/Messages.sq
  cached_conversations(student_id PK, json_data, cached_at)
  cached_messages(conversation_id PK, json_data, cached_at)
  pending_message_queue(id PK, conversation_id, text, queued_at, synced DEFAULT 0)

OfflineSyncManager (data/sync/OfflineSyncManager.kt):
  Observe NetworkConnectivityObserver.observe().filter { isConnected }
  On reconnect: syncPendingLeave() + syncPendingMessages() (both call UseCases)
  Emit SharedFlow<String> for Snackbar notifications
  Start in AppStateManager.init
  NOTE: syncPendingMessages() calls SendMessageUseCase — NOT MessagingRepositoryImpl directly

ACCEPTANCE CRITERIA:
□ MessageMapper.kt standalone in data/mapper/ — SenderType.valueOf()
□ OfflineSyncManager calls UseCases (SendMessageUseCase, SubmitLeaveUseCase) — not Repositories
□ Messages.sq: pending_message_queue has synced DEFAULT 0
□ 5 UseCases in domain/usecase/messages/
□ Compiles clean

OUTPUT: file paths + complete code
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 7-B │ Conversations Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 7 · Prompt B of 4 · Conversations Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 7-A: Conversation/Message/Announcement domain models, 5 UseCases, Messages.sq

WIREFRAME: SearchBar + ConversationCard list (participant name, preview, timestamp, unread badge) + FAB

TASKS:
ConversationsState.kt — data:List<Conversation>?, searchQuery:String, filteredList derived
ConversationsEvent.kt — Load, Refresh, SearchChanged(query), ConversationTapped(conversationId)
ConversationsViewModel.kt — inject GetConversationsUseCase.
  filteredList = full list filtered by searchQuery on participantName (client-side).
ConversationsScreen.kt — ConversationCard (screens/messages/components/ConversationCard.kt).
  Unread badge: conversation.unreadCount > 0. FAB: Snackbar "Coming soon".

ACCEPTANCE CRITERIA:
□ ViewModel injects GetConversationsUseCase — not MessagingRepository
□ ConversationsState.data is List<Conversation> (domain) — not DTO
□ Search filters on Conversation domain model fields

OUTPUT: file paths + code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 7-C │ Message Thread Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 7 · Prompt C of 4 · Message Thread Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 7-A: Message domain model (SenderType enum, MessageLocalState enum)  ✅ 7-B: Conversations

WIREFRAME: LazyColumn reversed, date separators, parent=right blue#1976D2, teacher=left grey#F5F5F5, input bar pinned

TASKS:
MessageThreadState.kt
  data class MessageThreadState(val isLoading:Boolean=false,
    val messages:List<Message>=emptyList(), val participantName:String="",
    val inputText:String="", val errorMessage:String?=null, val isOffline:Boolean=false)

MessageThreadEvent.kt — Load(conversationId), InputChanged(text), SendClicked, RetryFailed(messageId?)

MessageThreadViewModel.kt — inject GetMessageThreadUseCase, SendMessageUseCase
  Optimistic send: add Message(localState=SENDING) immediately → SendMessageUseCase
    → on success: update localState=SENT | on failure: update localState=FAILED
  Offline: insert to pending_message_queue

MessageThreadScreen.kt:
  LazyColumn(reverseLayout=true). MessageBubble (screens/messages/components/MessageBubble.kt):
    senderType == SenderType.PARENT → right, bg=#1976D2, text=white
    SenderType.TEACHER or ADMIN → left, bg=#F5F5F5, text=#212121
    MessageLocalState.FAILED → red exclamation + "Tap to retry" below bubble
  Date separators: compare adjacent Message.sentAt dates via kotlinx.datetime
  MessageInputBar (screens/messages/components/MessageInputBar.kt):
    OutlinedTextField + Send button (disabled when inputText.isBlank()) + attach icon
  Auto-scroll: LaunchedEffect(messages.size) { listState.animateScrollToItem(0) }

ACCEPTANCE CRITERIA:
□ ViewModel injects UseCases — not MessagingRepository
□ MessageThread.messages is List<Message> (domain) — SenderType enum drives bubble direction
□ Optimistic sending with MessageLocalState enum states
□ Date separator uses kotlinx.datetime comparison

OUTPUT: file paths + complete code
👉 Send: "C looks good. Proceed to Prompt D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 7-D │ Announcements Screen + Day 7 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 7 · Prompt D of 4 (Final) · Announcements + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 7-A: Announcement domain model  ✅ 7-B conversations  ✅ 7-C message thread

WIREFRAMES: list with NEW badge (unacknowledged) + detail with Acknowledge button

TASKS:
AnnouncementsState.kt — data:List<Announcement>?, selectedAnnouncement:Announcement?
AnnouncementsEvent.kt — Load, Refresh, AnnouncementTapped(id), AcknowledgeTapped(id)
AnnouncementsViewModel.kt
  inject GetAnnouncementsUseCase, AcknowledgeAnnouncementUseCase
  acknowledge: call UseCase → update local list.acknowledged=true (no re-fetch)
AnnouncementsScreen.kt:
  "NEW" badge when !announcement.acknowledged
  Detail view: full body + attachment list (tap opens URL in browser)
  Acknowledge button → "✓ Acknowledged" (disabled, green) after UseCase success

FINAL CHECKS: all 4 standard checks

ACCEPTANCE CRITERIA:
□ Both ViewModels inject UseCases — not MessagingRepository
□ Announcement.acknowledged is Boolean from domain model
□ AcknowledgeAnnouncementUseCase called — state updated locally
□ All 4 checks pass

OUTPUT: file paths + code + check results + CONTEXT.md §16 "After Day 7" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 8 — Notifications (full) + Push + Analytics Dashboard          ║
║  4 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 8-A │ Push Infra + Notification + Analytics Domain + DTOs + Mappers + UseCases
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 8 · Prompt A of 4 · Notification + Analytics Data Layer
══════════════════════════════════════════════════════════════════════

FCM STATUS [USER INPUT — fill before pasting]:
  google-services.json : [AVAILABLE at path: ___ / NOT YET AVAILABLE]
  GoogleService-Info.plist: [AVAILABLE at path: ___ / NOT YET AVAILABLE]
  If NOT YET: stub PushNotificationManager returning null token + warning log

TASKS:
domain/model/Notification.kt
  enum class NotificationType { FEE_DUE, ATTENDANCE_LOW, EXAM_SCHEDULE, HOMEWORK_DUE, LEAVE_STATUS, ANNOUNCEMENT }
  data class AppNotification(id,type:NotificationType,title,body,createdAt,isRead:Boolean,entityId:String?)
  data class NotificationPage(items:List<AppNotification>,hasMore:Boolean,totalCount:Int)
  data class NotificationPreferences(feeReminders,attendanceAlerts,examSchedule,homeworkDue,
    leaveUpdates,announcements,quietHoursEnabled,quietHoursStart,quietHoursEnd: all Boolean/String)

domain/model/Analytics.kt
  enum class PerformanceTrend { UP, DOWN, STABLE }
  data class AnalyticsDashboard(summaryText,subjects:List<SubjectPerformance>,
    weakAreas:List<WeakArea>, progressTrend:List<ProgressPoint>)
  data class SubjectPerformance(subjectId,subjectName,score:Double,trend:PerformanceTrend,lastExamScore:Double)
  data class WeakArea(subject,topic,score:Double)
  data class ProgressPoint(label,score:Double)
  // NO @Serializable

data/remote/dto/NotificationDto.kt + AnalyticsDto.kt — all @Serializable, enums as String
data/mapper/NotificationMapper.kt — NotificationType.valueOf()
data/mapper/AnalyticsMapper.kt — PerformanceTrend.valueOf()

Repositories: domain/repository/NotificationRepository.kt, AnalyticsRepository.kt (return domain)
Impls: data/repository/ — import from data/mapper/

UseCases: domain/usecase/notifications/ (5 usecases) + domain/usecase/analytics/ (2 usecases)
Wire in UseCaseModule.kt.

PushNotificationManager (expect/actual):
  expect: data/platform/PushNotificationManager.kt
    data class PushPayload(type:String, entityId:String?, title:String, body:String)
    expect class PushNotificationManager {
      suspend fun getDeviceToken(): String?
      fun requestPermission(): Flow<Boolean>
      fun observeIncomingNotifications(): Flow<PushPayload>
    }
  actual paths: .../data/platform/ in both androidMain and iosMain (D-025)
  Android actual: FCM or stub | iOS actual: APNs or stub

Device registration in AppStateManager.init:
  token = pushManager.getDeviceToken()
  if (token != null && token != multiplatformSettings.getStringOrNull("fcm_token"))
    → POST /parent/device/register { deviceToken: token, platform: "ANDROID/IOS" }

Deep-link routing in AppNavHost.kt (LaunchedEffect):
  observeIncomingNotifications() → map NotificationType to Routes → navController.navigate()
  FEE_DUE → FEES, ATTENDANCE_LOW → ATTENDANCE, LEAVE_STATUS → LEAVE_HISTORY,
  NEW_MESSAGE → MESSAGES, ANNOUNCEMENT → ANNOUNCEMENTS, EXAM_SCHEDULE → CALENDAR

ACCEPTANCE CRITERIA:
□ All domain models: NO @Serializable, enums for type fields
□ NotificationMapper + AnalyticsMapper standalone in data/mapper/
□ PushNotificationManager actual paths: .../data/platform/ mirroring commonMain
□ Device registration in AppStateManager (not a ViewModel)
□ Deep-link routing covers all 6 NotificationType values
□ Compiles clean

OUTPUT: file paths + complete code
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 8-B │ Notification Centre Screen (full implementation)
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 8 · Prompt B of 4 · Notification Centre Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 8-A: AppNotification domain model, 5 NotificationUseCases, deep-link routing

WIREFRAME: TopAppBar + [Mark All Read] + NotificationItemRow list
  Unread = 4dp PrimaryBlue left border. Read = no border.
  Icon colour by NotificationType enum.

TASKS:
NotificationCentreState.kt
  data class NotificationCentreState(val isLoading:Boolean=false,
    val items:List<AppNotification>=emptyList(), val hasMore:Boolean=false,
    val errorMessage:String?=null, val isOffline:Boolean=false)

NotificationCentreEvent.kt — Load(studentId), LoadNextPage, MarkRead(id), MarkAllRead, Refresh

NotificationCentreViewModel.kt — inject GetNotificationsUseCase, MarkNotificationReadUseCase,
  MarkAllReadUseCase, AppStateManager
  On MarkRead: call UseCase + update item.isRead=true locally + decrement AppState.unreadNotificationCount
  On MarkAllRead: call UseCase + all items isRead=true + AppState count=0
  Pagination: accumulate items across pages

NotificationCentreScreen.kt
  NotificationItemRow (screens/notifications/components/NotificationItemRow.kt):
    Left border: if !item.isRead → Modifier.border(4.dp, PrimaryBlue, …)
    Icon colour mapped from item.type (NotificationType enum — not String)
  Pagination: load next page when approaching end of list
  Tap → MarkRead usecase + navigate via deep-link map

ACCEPTANCE CRITERIA:
□ ViewModel injects UseCases — not NotificationRepository
□ items is List<AppNotification> (domain) — isRead is Boolean (not String)
□ Left border conditional on notification.isRead Boolean
□ Icon colour from NotificationType enum — not raw String comparison
□ Pagination: next page loads on approaching end

OUTPUT: file paths + complete code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 8-C │ Notification Preferences + Analytics Dashboard Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 8 · Prompt C of 4 · Prefs + Analytics
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 8-A: NotificationPreferences + AnalyticsDashboard domain models + UseCases

APIs:
  GET/PUT /parent/notifications/preferences
  GET /pulse/analytics/parent/dashboard?studentId={id}  ⚠️ /pulse/ prefix — confirm Q-006

WIREFRAMES:
  Prefs: 6 category toggles + quiet hours (master switch + start/end time pickers when enabled) + Save
  Analytics: summaryText card + SubjectPerformanceCard LazyRow + weak areas + LineChart

TASKS:
NotificationPreferencesState.kt — data:NotificationPreferences?, isLoading, isSaved:Boolean
NotificationPreferencesEvent.kt — Load, ToggleChanged(field,value), QuietHoursToggled,
  StartTimeChanged, EndTimeChanged, SaveClicked
NotificationPreferencesViewModel.kt — inject GetNotificationPreferencesUseCase, SaveNotificationPreferencesUseCase
NotificationPreferencesScreen.kt — quiet hours TimePicker (enabled only when master switch ON)

AnalyticsDashboardState.kt — data:AnalyticsDashboard?, isLoading, errorMessage
AnalyticsDashboardEvent.kt — Load, Refresh, SubjectTapped(subjectId)
AnalyticsDashboardViewModel.kt — inject GetAnalyticsDashboardUseCase
AnalyticsDashboardScreen.kt
  SubjectPerformanceCard (screens/analytics/components/SubjectPerformanceCard.kt):
    subject name + score % + trend arrow: PerformanceTrend.UP=green↑, DOWN=red↓, STABLE=grey→
  WeakAreas list. LineChart(progressTrend.map { ChartPoint(it.label, it.score.toFloat()) })
  Tap → navigate to Routes.ANALYTICS_SUBJECT with subjectId

ACCEPTANCE CRITERIA:
□ Both ViewModels inject UseCases — not Repositories
□ NotificationPreferences is domain model in State (not a DTO)
□ AnalyticsDashboard is domain model in State (not a DTO)
□ PerformanceTrend enum drives trend arrow colour — not String comparison
□ Analytics API path: /pulse/analytics/parent/dashboard (verify with Q-006)

OUTPUT: file paths + complete code
👉 Send: "C looks good. Proceed to Prompt D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 8-D │ Day 8 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 8 · Prompt D of 4 (Final) · Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 8-A push+data  ✅ 8-B notification centre  ✅ 8-C prefs+analytics

RUN ALL CHECKS:
  CHECK 1: ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL
  CHECK 2: ./gradlew :shared:compileKotlinIosSimulatorArm64 → BUILD SUCCESSFUL
  CHECK 3: grep -r "import android\." shared/src/commonMain/ → empty
  CHECK 4: grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/ → empty

Also verify: all 6 deep-link notification types navigate to correct screens.
FCM status: note whether wired or stubbed.

ACCEPTANCE CRITERIA:
□ All 4 checks pass
□ 6 deep-link types verified
□ FCM status noted

OUTPUT: check results + FCM status + CONTEXT.md §16 "After Day 8" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 9 — Academic Calendar + Analytics Detail + Full Offline Sync   ║
║  4 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 9-A │ Calendar + Analytics Domain + DTOs + Mappers + SQLDelight + OfflineSyncManager
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 9 · Prompt A of 4 · Calendar + Analytics Data Layer
══════════════════════════════════════════════════════════════════════

TASKS:
domain/model/CalendarEvent.kt
  enum class EventType { LECTURE, EXAM, HOMEWORK, FEES, HOLIDAY }
  data class CalendarMonth(month:Int, year:Int, events:List<AcademicEvent>)
  data class AcademicEvent(date:String, type:EventType, title:String, time:String?)

domain/model/AnalyticsSubject.kt
  data class AnalyticsSubjectData(subjectId,subjectName,teacherName?,currentScore:Double,
    grade:String,classAverage:Double,scoreHistory:List<ScoreRecord>,teacherRemarks:String?)
  data class ScoreRecord(testName,date:String,score:Int,maxScore:Int)

data/remote/dto/ — CalendarDto.kt + AnalyticsSubjectDto.kt (all @Serializable)
data/mapper/ — CalendarMapper.kt (EventType.valueOf()), AnalyticsSubjectMapper.kt
domain/repository/ — CalendarRepository.kt, AnalyticsSubjectRepository.kt (domain return types)
data/repository/ — CalendarRepositoryImpl.kt, AnalyticsSubjectRepositoryImpl.kt (import mapper)

UseCases: domain/usecase/calendar/GetCalendarUseCase.kt
          domain/usecase/analytics/GetAnalyticsSubjectUseCase.kt
Wire in UseCaseModule.kt.

SQLDelight:
  data/local/entity/Calendar.sq — cached_calendar(student_id,month,year,json_data,cached_at — composite PK)
  data/local/entity/Analytics.sq — cached_analytics_dashboard(student_id PK), cached_analytics_subject(student_id,subject_id — composite PK)

Complete OfflineSyncManager (data/sync/OfflineSyncManager.kt):
  Ensure start() uses:
    connectivity.observe().distinctUntilChanged().filter { it }
      .collect { syncPendingLeave(); syncPendingMessages() }
  syncPendingLeave() → query pending_leave_queue WHERE synced=0 → SubmitLeaveUseCase → mark synced=1
  syncPendingMessages() → query pending_message_queue WHERE synced=0 → SendMessageUseCase → mark synced=1
  Both sync functions call UseCases — NOT repository implementations directly (D-026)
  SharedFlow<String> for Snackbar; collect in AppNavHost.

ACCEPTANCE CRITERIA:
□ CalendarMapper + AnalyticsSubjectMapper standalone in data/mapper/ — EventType.valueOf()
□ OfflineSyncManager calls UseCases (SubmitLeaveUseCase, SendMessageUseCase) — not Repositories
□ Calendar.sq + Analytics.sq created with correct composite PKs
□ GetCalendarUseCase in domain/usecase/calendar/
□ Compiles clean

OUTPUT: file paths + code
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 9-B │ Academic Calendar Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 9 · Prompt B of 4 · Academic Calendar Screen
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 9-A: CalendarMonth/AcademicEvent domain models, GetCalendarUseCase, Calendar.sq

WIREFRAME: Full-screen CalendarWidget + event type legend row + day-tap → ModalBottomSheet

TASKS:
AcademicCalendarState.kt
  data class AcademicCalendarState(val isLoading:Boolean=false,
    val calendar:CalendarMonth?=null, val selectedDate:LocalDate?=null,
    val errorMessage:String?=null, val isOffline:Boolean=false)

AcademicCalendarEvent.kt — Load(studentId), MonthChanged(month,year), DayTapped(date:LocalDate), DismissSheet

AcademicCalendarViewModel.kt
  inject: GetCalendarUseCase — NOT CalendarRepository
  24hr stale threshold. Observe currentStudentId → reload.
  Map CalendarMonth.events (AcademicEvent) → List<CalendarEvent> for CalendarWidget:
    AcademicEvent(date,EventType.LECTURE,…) → CalendarEvent(date,"LECTURE",title)
  Note: mapping in Screen Composable (ViewModel keeps CalendarMonth domain type clean)

AcademicCalendarScreen.kt
  Full-screen CalendarWidget (map in Screen, not ViewModel)
  Legend row: 5 EventType values with dot colours (LECTURE=blue, EXAM=red, etc.)
  Day tap → ModalBottomSheet:
    EventListBottomSheet (screens/calendar/components/EventListBottomSheet.kt)
    AcademicEvent list for selectedDate. EventType enum drives dot colour.
    EmptyState "No events on this day" when empty.

ACCEPTANCE CRITERIA:
□ ViewModel injects GetCalendarUseCase — not CalendarRepository
□ AcademicCalendarState.calendar is CalendarMonth (domain) — not a DTO
□ CalendarWidget receives List<CalendarEvent> mapped in Screen from CalendarMonth
□ EventType enum drives dot colours — not String comparison

OUTPUT: file paths + complete code
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 9-C │ Analytics Subject Detail Screen
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 9 · Prompt C of 4 · Analytics Subject Detail
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 9-A: AnalyticsSubjectData domain model, GetAnalyticsSubjectUseCase

API: GET /parent/analytics/subject/{subjectId}?studentId={id}
  [USER INPUT — confirm /parent/ vs /pulse/ with backend team — see Q-006]
  Add TODO comment if unconfirmed.

WIREFRAME: score card + teacher name? + score history table + LineChart + class avg bars + teacher remarks?

TASKS:
AnalyticsSubjectDetailState.kt — data:AnalyticsSubjectData?, isLoading, errorMessage
AnalyticsSubjectDetailEvent.kt — Load(subjectId, studentId), Refresh
AnalyticsSubjectDetailViewModel.kt — inject GetAnalyticsSubjectUseCase. 60min stale.

AnalyticsSubjectDetailScreen.kt
  Score history table: ScoreRecord list → testName | date | score/maxScore | pct%
    pct = (record.score.toFloat() / record.maxScore * 100).toInt()
  LineChart: scoreHistory.map { ChartPoint(formatDate(it.date), it.score.toFloat()/it.maxScore*100) }
  ClassAvgBars (screens/analytics/components/ClassAvgBars.kt):
    Two Canvas horizontal bars — student (PrimaryBlue) vs classAverage (grey)
  Teacher remarks Card: visible ONLY when data.teacherRemarks != null

ACCEPTANCE CRITERIA:
□ ViewModel injects GetAnalyticsSubjectUseCase — not AnalyticsSubjectRepository
□ AnalyticsSubjectDetailState.data is AnalyticsSubjectData (domain) — not a DTO
□ Teacher remarks card conditional on nullable field from domain model
□ Score % calculation: (score.toFloat() / maxScore * 100).toInt()

OUTPUT: file paths + code
👉 Send: "C looks good. Proceed to Prompt D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 9-D │ Cache Freshness Audit + Day 9 Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 9 · Prompt D of 4 (Final) · Cache Audit + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 9-A: schemas+sync  ✅ 9-B: calendar  ✅ 9-C: analytics detail

TASK D1 — Cache Freshness Audit (all caching ViewModels):
  Thresholds from CONTEXT.md §11:
    Dashboard=5min, Attendance=60min, Fees=60min,
    Notifications=10min, Calendar=24hr, Analytics=60min
  Logic per ViewModel:
    stale + online → auto-refresh + cacheAgeMessage in State
    stale + offline → cacheAgeMessage only
    fresh → no banner
  Audit: DashboardViewModel, AttendanceSummaryViewModel, FeeListViewModel,
    NotificationCentreViewModel, AcademicCalendarViewModel,
    AnalyticsDashboardViewModel, AnalyticsSubjectDetailViewModel
  Report: [ViewModel] → PASS | FIXED (what was added)

TASK D2 — Final SQLDelight Schema Summary:
  List all .sq files in data/local/entity/ and their tables.

TASK D3 — Day 9 Final Checks:
  CHECK 1: ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL
  CHECK 2: ./gradlew :shared:compileKotlinIosSimulatorArm64 → BUILD SUCCESSFUL
  CHECK 3: grep -r "import android\." shared/src/commonMain/ → empty
  CHECK 4: grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/ → empty
  CHECK 5: grep -r "Repository" shared/src/commonMain/presentation/ → empty

ACCEPTANCE CRITERIA:
□ All 7 ViewModels have consistent cache freshness logic
□ All 5 checks pass
□ SQLDelight schema summary complete

OUTPUT: cache audit results + schema summary + all 5 check results
  + CONTEXT.md §16 "After Day 9" text block
══════════════════════════════════════════════════════════════════════


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 10 — Full Testing, Release Build, App Store                    ║
║  3 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT 10-A │ Bug Fixes + Architecture Final Audit + Sprint 1 Regression
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 10 · Prompt A of 3 · Bugs + Sprint 1 Tests
══════════════════════════════════════════════════════════════════════

SECTION 1 — BUG LIST [USER INPUT — fill before pasting]:
  BUG 1: [Screen] — [Observed] — [Expected]
  [PASTE BUGS HERE]

SECTION 2 — ARCHITECTURE FINAL AUDIT (most critical checks):
  grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/ → must be empty
  grep -r "Repository" shared/src/commonMain/presentation/ → must be empty
  grep -r "import android\." shared/src/commonMain/ → must be empty
  Fix any violations before proceeding to regression.

SECTION 3 — SPRINT 1 REGRESSION (Screens 1–10):
  AUTH: valid login→Dashboard, invalid→inline error, 5-fail lockout, token auto-refresh silent
  DASHBOARD: 4 stat cards correct, calendar event dots, student switch reloads
  ATTENDANCE: GaugeChart %, "Including Leave" toggle (no API call), AlertBanner N formula,
    Calendar view (PRESENT=green, ABSENT=red), Subject detail table coloured status
  FEES: summary totals Indian format, PAID/PENDING/OVERDUE StatusBadge colours,
    Razorpay sandbox end-to-end, PaymentResult back stack correct
  ALL: loading/error/empty states, OfflineBanner, PullToRefresh
  Report each: PASS | FAIL | FIXED

ACCEPTANCE CRITERIA:
□ All 3 architecture grep audits: empty
□ All bugs: Fixed | Documented
□ All Sprint 1 regression: PASS

OUTPUT: architecture audit + bug status + regression results per screen
👉 Send: "A looks good. Proceed to Prompt B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 10-B │ Sprint 2 Regression + Accessibility Audit
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 10 · Prompt B of 3 · Sprint 2 Tests + Accessibility
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 10-A: architecture clean, Sprint 1 PASS

SPRINT 2 REGRESSION (Screens 11–20):
  LEAVE: form validation (LeaveType enum drives dropdown), file attachment limits,
    submit → appears in history, filter tabs (LeaveStatus enum comparison),
    timeline steps correct, cancel ONLY for SUBMITTED, DELETE → removed from history
  MESSAGES: unread badges, parent=right/teacher=left (SenderType enum),
    date separators, optimistic send (MessageLocalState states),
    failed message retry affordance, announcements acknowledge
  NOTIFICATIONS: pagination, unread border (isRead Boolean), Mark All Read → count=0,
    tap → deep-link all 6 NotificationType values
  ANALYTICS: summary+cards+chart, trend arrows (PerformanceTrend enum),
    subject detail score % correct, teacher remarks only when present
  CALENDAR: event dots (EventType enum), month nav, day tap bottom sheet
  OFFLINE: leave queue syncs on reconnect (via SubmitLeaveUseCase), message queue syncs

ACCESSIBILITY AUDIT (WCAG 2.1 AA):
  □ 150% font scaling: no overflow on any of 20 screens
  □ All interactive elements: Modifier.minimumInteractiveComponentSize() ≥ 44dp
  □ contentDescription on all icon-only buttons (bell, close, send, back, etc.)
  □ Contrast ratio ≥ 4.5:1 for all body text

Report each: PASS | FIXED | FAIL

ACCEPTANCE CRITERIA:
□ All Sprint 2 regression: PASS
□ 150% scaling: PASS on all 20 screens
□ Touch targets: all ≥ 44dp
□ Icon buttons: all have contentDescription

OUTPUT: Sprint 2 regression + accessibility results
👉 Send: "B looks good. Proceed to Prompt C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT 10-C │ Release Build + ProGuard + Play Store + Final Commit
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 10 · Prompt C of 3 (Final) · Release + Store + Sign-Off
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ 10-A: architecture+Sprint1 PASS  ✅ 10-B: Sprint2+a11y PASS

KEYSTORE [USER INPUT — fill before pasting]:
  Keystore path: [full path or "GENERATE NEW"]
  Key alias: [e.g. parentapp]
  Env vars: KEYSTORE_PASSWORD, KEY_PASSWORD (never hardcoded)

──── TASK C1 — Signing Config ────
androidApp/build.gradle.kts:
  signingConfigs { create("release") {
    storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../parentapp-release.keystore")
    storePassword = System.getenv("KEYSTORE_PASSWORD")
    keyAlias = System.getenv("KEY_ALIAS") ?: "parentapp"
    keyPassword = System.getenv("KEY_PASSWORD")
  }}
  buildTypes { release { isMinifyEnabled=true; isShrinkResources=true;
    proguardFiles(getDefaultProguardFile(...), "proguard-rules.pro")
    signingConfig = signingConfigs.getByName("release") }}

──── TASK C2 — ProGuard Rules ────
androidApp/proguard-rules.pro:
  -keep class com.edmik.parentapp.domain.model.** { *; }
  -keep class com.edmik.parentapp.data.remote.dto.** { *; }
  Keep rules for: Ktor, Kotlinx Serialization, Razorpay, Koin, SQLDelight, Coil

──── TASK C3 — Build + Smoke Test ────
  ./gradlew :androidApp:bundleRelease → AAB < 50MB
  ./gradlew :androidApp:assembleRelease → install + Login→Dashboard→Fees flow
  Fix any ProGuard crashes.

──── TASK C4 — Play Store Listing ────
  Title (30 chars): Parent App — School Monitor
  Short desc (80 chars): Track attendance, fees, exams & messages for your child in one app.
  Full description (400–600 words): attendance, Razorpay payment, leave, messaging, analytics,
    notifications, multi-child, biometric, offline. Tone: trustworthy, parent-focused.
  5 screenshots: Dashboard, Attendance (GaugeChart visible), Fee List, Analytics, Notification Centre

──── TASK C5 — Pre-Commit Cleanup ────
  Remove debug logs not behind BuildConfig.DEBUG
  .gitignore: *.keystore, google-services.json, GoogleService-Info.plist
  grep -r "KEYSTORE_PASSWORD\|api_key\|secret" shared/src/ → empty

──── TASK C6 — Final Git Tag ────
  git commit -m "v1.0.0 — 20 screens, MVVM+MVI+CleanArch, KMP+CMP, Android+iOS"
  git tag v1.0.0 && git push origin main --tags

──── TASK C7 — Update DECISIONS.md ────
  Resolve open questions Q-001 through Q-008 with actual answers
  Add "Deferred to v1.0.1" section for any outstanding items

ACCEPTANCE CRITERIA:
□ Signing config uses System.getenv() — no hardcoded credentials
□ ProGuard includes domain.model.** + data.remote.dto.** + all libraries
□ AAB < 50MB, release APK smoke test passes
□ Play Store listing text generated
□ grep audit: no secrets in source
□ v1.0.0 tag pushed
□ DECISIONS.md all open questions resolved or deferred

OUTPUT:
1. AAB file size + smoke test result
2. Play Store listing (copy-paste ready)
3. Cleanup results
4. iOS remaining work (Xcode provisioning, Razorpay iOS Q-003, APNs Q-004, App Store steps)
5. CONTEXT.md §16 "After Day 10" text block (all days complete ✅)
══════════════════════════════════════════════════════════════════════

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
QUICK REFERENCE — All Proceed Signals

  Day 1 : A→B→C→D  |  "X looks good. Proceed to Prompt Y."
  Day 2 : A→B→C→D→E
  Day 3 : A→B→C→D
  Day 4 : A→B→C→D
  Day 5 : A→B→C
  Day 6 : A→B→C
  Day 7 : A→B→C→D
  Day 8 : A→B→C→D
  Day 9 : A→B→C→D
  Day 10: A→B→C

  Need changes before proceeding:
  → Describe the change in the same chat
  → Wait for the agent to update
  → Then send the proceed signal
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
