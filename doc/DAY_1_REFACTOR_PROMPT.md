# DAY_1_REFACTOR_PROMPT.md — Parent App
# Migrate Existing Day 1 Implementation to New Folder Structure
# Location: ParentAppMultiplatform/doc/DAY_1_REFACTOR_PROMPT.md
#
# ══════════════════════════════════════════════════════════════════════
# HOW TO USE THIS FILE
# ══════════════════════════════════════════════════════════════════════
# 1. Open a NEW chat session
# 2. Attach ALL THREE files:
#       doc/CONTEXT.md
#       doc/DECISIONS.md
#       doc/DAY_1_REFACTOR_PROMPT.md  ← this file
# 3. Paste Prompt R-A first → review → send proceed signal → paste R-B…
# 4. Proceed signal: "R-A looks good. Proceed to Prompt R-B."
# 5. After R-E passes: copy "After Day 1" block into CONTEXT.md §16
#
# ══════════════════════════════════════════════════════════════════════
# WHAT THIS REFACTOR DOES
# ══════════════════════════════════════════════════════════════════════
# The project was built with the OLD folder structure from the spec doc.
# This prompt migrates it — file by file — to the TARGET structure
# defined in doc/CONTEXT.md §4, without breaking any existing logic or UI.
#
# OLD structure (what exists today):
#   shared/src/commonMain/kotlin/com/edmik/parentapp/
#     di/                         ← Hilt-style flat DI
#     data/
#       api/                      ← flat API interfaces
#       model/                    ← flat DTOs + domain mixed
#       repository/               ← Impl + toDomain() inline
#       local/                    ← flat local folder
#     domain/
#       model/                    ← domain models
#       repository/               ← interfaces
#       usecase/                  ← flat (no feature subfolders)
#     ui/                         ← old presentation layer name
#       theme/
#       components/
#       navigation/
#       login/                    ← LoginScreen, LoginViewModel (old UiState)
#       forgot_password/
#
# TARGET structure (after this refactor):
#   shared/src/commonMain/kotlin/com/edmik/parentapp/
#     data/
#       remote/api/               ← Ktor service interfaces
#       remote/dto/               ← @Serializable DTOs only
#       mapper/                   ← standalone DTO→domain mappers
#       local/database/           ← AppDatabase, TokenManager
#       local/entity/             ← .sq SQLDelight files
#       repository/               ← Impls (import from mapper/)
#       platform/                 ← expect declarations
#       payment/                  ← expect PaymentLauncher
#       sync/                     ← OfflineSyncManager
#     domain/
#       model/                    ← pure Kotlin, NO @Serializable
#       repository/               ← interfaces (return domain models)
#       usecase/auth/             ← feature-grouped usecases
#     di/                         ← Koin modules ONLY
#     presentation/
#       app/                      ← AppState, AppStateManager (NOT di/)
#       theme/
#       components/
#       navigation/
#       screens/
#         login/                  ← LoginScreen + LoginViewModel + LoginState + LoginEvent
#         forgot_password/        ← ForgotPasswordScreen + ViewModel + State + Event
#
# ══════════════════════════════════════════════════════════════════════
# GOLDEN RULES FOR THIS REFACTOR
# ══════════════════════════════════════════════════════════════════════
# 1. PRESERVE ALL LOGIC — do not rewrite business logic, only move/restructure
# 2. PRESERVE ALL UI — do not change any Composable's visual output
# 3. MOVE, DON'T REWRITE — keep existing code intact; only change packages and file locations
# 4. ADD STATE+EVENT — LoginState.kt, LoginEvent.kt are NEW files that wrap existing logic
# 5. COMPILE AFTER EACH STEP — every prompt ends with a compile check
# 6. NO android.* in commonMain — if any existed, flag and fix via expect/actual
# ══════════════════════════════════════════════════════════════════════


# SPLIT OVERVIEW — 5 prompts
# R-A │ Audit existing structure + plan file-by-file migration map
# R-B │ Migrate data layer (api→remote/api, model→remote/dto, mapper extraction, local split)
# R-C │ Migrate domain layer (usecase feature subfolders) + fix Koin modules
# R-D │ Migrate presentation layer (ui→presentation, add State+Event, fix ViewModels)
# R-E │ Final verification + compile checks + CONTEXT.md update


╔══════════════════════════════════════════════════════════════════════╗
║  DAY 1 REFACTOR — Migrate to New Folder Structure                   ║
║  5 prompts · New chat · Attach CONTEXT.md + DECISIONS.md            ║
╚══════════════════════════════════════════════════════════════════════╝

──────────────────────────────────────────────────────────────────────
PROMPT R-A │ Audit Existing Structure + Build Migration Map
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 Refactor · Prompt R-A of 5 · Audit
Docs attached: doc/CONTEXT.md + doc/DECISIONS.md
Package: com.edmik.parentapp | Repo: ParentAppMultiplatform/
══════════════════════════════════════════════════════════════════════

CONTEXT: The project has a working Day 1 implementation built with the OLD
folder structure from the original spec. The goal of this entire session is
to migrate it to the TARGET structure defined in doc/CONTEXT.md §4 WITHOUT
breaking any existing functionality or UI.

══ STEP 1 — SCAN THE EXISTING PROJECT ══════════════════════════════

Run these commands and report the EXACT output of each:

  find shared/src/commonMain/kotlin -name "*.kt" | sort

  find shared/src/androidMain/kotlin -name "*.kt" | sort

  find shared/src/iosMain/kotlin -name "*.kt" 2>/dev/null | sort

  cat gradle/libs.versions.toml

══ STEP 2 — IDENTIFY EVERY FILE THAT NEEDS TO MOVE ══════════════════

Using the scan output, build a complete migration table:

| Current File Path | Target File Path | Action |
|---|---|---|
| (fill from scan) | (from CONTEXT.md §4) | MOVE / RENAME / SPLIT / CREATE / DELETE |

Use these action types:
- MOVE — same content, different location
- RENAME — same content, file or directory name changes
- SPLIT — one file becomes two (e.g. model.kt → dto/ + domain/model/)
- REFACTOR — location changes + code changes needed (list what changes)
- CREATE — new file needed (State.kt, Event.kt, Mapper.kt)
- DELETE — file to remove after content moved elsewhere

══ STEP 3 — FLAG VIOLATIONS ══════════════════════════════════════════

Search for these known violations in the existing codebase:

  grep -r "import android\." shared/src/commonMain/
  grep -r "@Serializable" shared/src/commonMain/domain/
  grep -r "import com.edmik.parentapp.data" shared/src/commonMain/ | grep "ui\|presentation"
  grep -rn "toDomain\(\)" shared/src/commonMain/data/repository/

Report each result: FOUND [file:line] or NONE FOUND.
These violations must be fixed during the refactor.

══ STEP 4 — DECLARE MIGRATION PLAN ══════════════════════════════════

State the plan clearly before touching any file:
1. What changes in Prompt R-B (data layer only)
2. What changes in Prompt R-C (domain layer + DI)
3. What changes in Prompt R-D (presentation layer)
4. What compile checks will be run in R-E

ACCEPTANCE CRITERIA:
□ Complete scan output produced
□ Every existing .kt file appears in the migration table
□ All violations listed with file:line references
□ Migration plan approved before any file is touched

OUTPUT: scan results + complete migration table + violations list + plan
👉 REVIEW GATE: Review the migration table carefully. Confirm every file
   is accounted for and the plan is correct.
   Send: "R-A looks good. Proceed to Prompt R-B."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT R-B │ Migrate Data Layer
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 Refactor · Prompt R-B of 5 · Data Layer Migration
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ R-A: full audit complete, migration table approved

SCOPE: Migrate data/ layer only. Do NOT touch domain/, di/, ui/, or presentation/.

══ MIGRATION 1 — data/api/ → data/remote/api/ ════════════════════════

Move every file from data/api/ to data/remote/api/.
Update the package declaration in each moved file:
  FROM: package com.edmik.parentapp.data.api
  TO:   package com.edmik.parentapp.data.remote.api

Files expected (adjust if scan showed different names):
  AuthApiService.kt → data/remote/api/AuthApiService.kt

Keep file content IDENTICAL except the package line.

Also move HttpClientFactory.kt (or ApiClient.kt / NetworkModule network setup)
to data/remote/api/ if it lives in data/api/ or data/network/.

══ MIGRATION 2 — data/model/ → data/remote/dto/ ════════════════════════

The old data/model/ folder contains @Serializable classes (DTOs).
Move them to data/remote/dto/.

  All files → data/remote/dto/
  Rename the file: AuthModels.kt → AuthDto.kt (or keep original name — either is fine)
  Update package: FROM data.model  TO  data.remote.dto
  Keep ALL @Serializable annotations intact.
  Do NOT add or remove any fields.

If any domain model accidentally ended up in data/model/ (e.g. a plain data class
without @Serializable), move it to domain/model/ instead.

══ MIGRATION 3 — Extract Mapper from RepositoryImpl ════════════════════

Locate any toDomain() or map() extension functions currently inside
RepositoryImpl files. Extract them into a new dedicated file:
  CREATE: data/mapper/AuthMapper.kt

Pattern for AuthMapper.kt:
  package com.edmik.parentapp.data.mapper

  import com.edmik.parentapp.data.remote.dto.LoginResponse
  import com.edmik.parentapp.domain.model.Student

  fun LoginResponse.toDomain() = Student(
    id = studentId,
    name = studentName,
    batch = batch,
    photoUrl = profilePhotoUrl
  )

After extraction:
  - Delete the toDomain() function from RepositoryImpl
  - Add import com.edmik.parentapp.data.mapper.toDomain to RepositoryImpl
  - Verify RepositoryImpl still compiles and behaviour is identical

If NO toDomain() exists yet in RepositoryImpl, create AuthMapper.kt with the
mapping from LoginResponse → Student based on the existing field names.

══ MIGRATION 4 — data/local/ → data/local/database/ ════════════════════

Move TokenManager (and AppDatabase if it exists) into data/local/database/:
  TokenManager.kt → data/local/database/TokenManager.kt
  AppDatabase.kt  → data/local/database/AppDatabase.kt  (if exists)

Update package: FROM data.local  TO  data.local.database

If TokenManager currently uses EncryptedSharedPreferences (android-only),
it must be replaced with multiplatform-settings. See replacement below:

  BEFORE (EncryptedSharedPreferences — ANDROID ONLY — MUST REPLACE):
    class TokenManager(context: Context) {
      private val prefs = EncryptedSharedPreferences.create(...)
      fun getAccessToken() = prefs.getString("access_token", null)
      ...
    }

  AFTER (multiplatform-settings — works in commonMain):
    import com.russhwolf.settings.Settings

    class TokenManager(private val settings: Settings) {
      fun getAccessToken(): String? = settings.getStringOrNull("access_token")
      fun getRefreshToken(): String? = settings.getStringOrNull("refresh_token")
      fun setTokens(access: String, refresh: String) {
        settings.putString("access_token", access)
        settings.putString("refresh_token", refresh)
      }
      fun clearTokens() {
        settings.remove("access_token")
        settings.remove("refresh_token")
      }
    }

  Update di/AppModule.kt (or wherever TokenManager is provided in Koin):
    single { TokenManager(get()) }
    — ensure Settings is also provided:
    single<Settings> { ... }  ← platform-specific Settings factory

If TokenManager ALREADY uses multiplatform-settings: just MOVE it, no code change needed.

══ MIGRATION 5 — Update RepositoryImpl import paths ════════════════════

RepositoryImpl files import from old locations. Update ALL import statements:
  FROM: import com.edmik.parentapp.data.api.*
  TO:   import com.edmik.parentapp.data.remote.api.*

  FROM: import com.edmik.parentapp.data.model.*
  TO:   import com.edmik.parentapp.data.remote.dto.*
        import com.edmik.parentapp.data.mapper.*

  FROM: import com.edmik.parentapp.data.local.*
  TO:   import com.edmik.parentapp.data.local.database.*

Do NOT change any logic — only import paths.

══ COMPILE CHECK ══════════════════════════════════════════════════════

After all migrations in this prompt:
  ./gradlew :shared:compileKotlinAndroid

If it fails: fix the errors. Do NOT proceed to R-C until this is green.
Report: BUILD SUCCESSFUL ✅ or list each error with the fix applied.

ACCEPTANCE CRITERIA:
□ data/api/ is gone — all files are now in data/remote/api/
□ data/model/ is gone — @Serializable classes are now in data/remote/dto/
□ data/mapper/AuthMapper.kt exists with toDomain() extension function
□ RepositoryImpl imports from data/mapper/ via import statement
□ TokenManager is in data/local/database/ and uses multiplatform-settings (no android.* import)
□ ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL

OUTPUT: list of every file moved/created/modified + compile result
👉 Send: "R-B looks good. Proceed to Prompt R-C."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT R-C │ Migrate Domain Layer + Fix Koin DI Modules
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 Refactor · Prompt R-C of 5 · Domain + DI Migration
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ R-A: audit  ✅ R-B: data layer migrated + compiles

SCOPE: Migrate domain/usecase/ grouping + fix all Koin DI modules.
Do NOT touch presentation/ or ui/ yet — that is R-D.

══ MIGRATION 1 — domain/usecase/ flat → feature subfolders ═══════════

The old structure has usecases in a flat folder:
  domain/usecase/LoginUseCase.kt
  domain/usecase/ForgotPasswordUseCase.kt
  (potentially others)

Target structure groups them by feature:
  domain/usecase/auth/LoginUseCase.kt
  domain/usecase/auth/ForgotPasswordUseCase.kt

ACTION:
  Create subdirectory: domain/usecase/auth/
  Move LoginUseCase.kt → domain/usecase/auth/LoginUseCase.kt
  Move ForgotPasswordUseCase.kt → domain/usecase/auth/ForgotPasswordUseCase.kt

Update package declarations in each moved file:
  FROM: package com.edmik.parentapp.domain.usecase
  TO:   package com.edmik.parentapp.domain.usecase.auth

Keep ALL UseCase logic IDENTICAL — only move and update package.

══ MIGRATION 2 — Verify domain/model/ is clean ═══════════════════════

domain/model/ should contain ONLY pure Kotlin data classes — NO @Serializable.
Run: grep -r "@Serializable" shared/src/commonMain/domain/model/

If any class has @Serializable:
  Remove the @Serializable annotation
  Remove the Kotlinx Serialization import from that file
  Ensure the corresponding DTO in data/remote/dto/ has @Serializable

If Student.kt does NOT yet exist in domain/model/:
  CREATE domain/model/Student.kt:
    package com.edmik.parentapp.domain.model

    data class Student(
        val id: String,
        val name: String,
        val batch: String,
        val photoUrl: String? = null
    )
    // NO @Serializable — domain models are pure Kotlin

══ MIGRATION 3 — Update Koin DI Modules ═════════════════════════════

The di/ folder must contain ONLY Koin module definitions.
Check the current structure:
  find shared/src/commonMain/kotlin -path "*/di/*.kt" | sort

Target state (from CONTEXT.md §4):
  di/NetworkModule.kt
  di/RepositoryModule.kt
  di/UseCaseModule.kt
  di/DatabaseModule.kt
  di/AppModule.kt

For EACH existing DI file:

  NetworkModule.kt — should provide:
    single { HttpClientFactory.create(get()) }
    single { AuthApiService (Ktor implementation) }
    (any other API service instances)

  RepositoryModule.kt — should bind interfaces to implementations:
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    NOTE: AuthRepositoryImpl now imports from data.remote.api, data.remote.dto, data.mapper, data.local.database

  UseCaseModule.kt — should provide all UseCases:
    single { LoginUseCase(get()) }
    single { ForgotPasswordUseCase(get()) }
    NOTE: Koin must find the new package com.edmik.parentapp.domain.usecase.auth.LoginUseCase

  DatabaseModule.kt — should provide:
    expect fun createDriver(): SqlDriver  (actual in androidMain/iosMain)
    single { createDriver() }
    (create actual files if they don't exist)

  AppModule.kt — should be the root module that includes all others:
    val appModule = module {
      includes(networkModule, repositoryModule, useCaseModule, databaseModule)
      single { TokenManager(get()) }
    }
    NOTE: AppStateManager will be added here in R-D

Update all import paths in DI files:
  FROM: import com.edmik.parentapp.domain.usecase.LoginUseCase
  TO:   import com.edmik.parentapp.domain.usecase.auth.LoginUseCase

  FROM: import com.edmik.parentapp.data.repository.AuthRepositoryImpl
  TO:   same (data/repository/ path unchanged)

══ MIGRATION 4 — Check androidMain / iosMain DI ═════════════════════

If TokenManager used android.security.crypto.EncryptedSharedPreferences in
androidMain before — and we already replaced it with multiplatform-settings
in R-B — verify the androidMain DI module no longer references it.

AndroidDatabaseModule.kt (androidMain):
  Should provide: actual fun createDriver(): SqlDriver = AndroidSqliteDriver(...)
  If this file doesn't exist yet, create it.

IosDatabaseModule.kt (iosMain):
  Should provide: actual fun createDriver(): SqlDriver = NativeSqliteDriver(...)
  If this file doesn't exist yet, create it.

══ COMPILE CHECK ══════════════════════════════════════════════════════

  ./gradlew :shared:compileKotlinAndroid

Fix any errors. Report: BUILD SUCCESSFUL ✅ or errors + fixes.

ACCEPTANCE CRITERIA:
□ domain/usecase/ flat → domain/usecase/auth/ with correct packages
□ domain/model/Student.kt: NO @Serializable annotation
□ All 5 Koin modules in di/ — correct import paths after all moves
□ UseCaseModule.kt imports from domain.usecase.auth.*
□ androidMain/di/AndroidDatabaseModule.kt exists with actual createDriver()
□ ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL

OUTPUT: list of every file moved/created/modified + compile result
👉 Send: "R-C looks good. Proceed to Prompt R-D."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT R-D │ Migrate Presentation Layer + Add MVI State+Event
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 Refactor · Prompt R-D of 5 · Presentation Migration
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ R-A: audit  ✅ R-B: data layer  ✅ R-C: domain + DI

SCOPE: Rename ui/ → presentation/ with new subfolder layout.
       Add State.kt + Event.kt files (MVI).
       Refactor ViewModels to use new State+Event pattern.
       DO NOT change any Composable's visual output.

══ MIGRATION 1 — ui/ → presentation/ top-level rename ═══════════════

Every file currently under:
  shared/src/commonMain/kotlin/com/edmik/parentapp/ui/

Must move to:
  shared/src/commonMain/kotlin/com/edmik/parentapp/presentation/

Subdirectory mapping:
  ui/theme/          → presentation/theme/
  ui/components/     → presentation/components/
  ui/navigation/     → presentation/navigation/
  ui/login/          → presentation/screens/login/
  ui/forgot_password/→ presentation/screens/forgot_password/
  (any other ui/xxx/)→ presentation/screens/xxx/

Update package declarations in EVERY moved file:
  FROM: package com.edmik.parentapp.ui.theme
  TO:   package com.edmik.parentapp.presentation.theme

  FROM: package com.edmik.parentapp.ui.components
  TO:   package com.edmik.parentapp.presentation.components

  FROM: package com.edmik.parentapp.ui.navigation
  TO:   package com.edmik.parentapp.presentation.navigation

  FROM: package com.edmik.parentapp.ui.login
  TO:   package com.edmik.parentapp.presentation.screens.login

  FROM: package com.edmik.parentapp.ui.forgot_password
  TO:   package com.edmik.parentapp.presentation.screens.forgot_password

Update ALL import statements in every file to use the new packages.
App.kt (or the top-level entry Composable) must import from presentation.*

══ MIGRATION 2 — Create presentation/app/ ════════════════════════════

Create the app/ subfolder for app-level state (NOT in di/):

  CREATE: presentation/app/AppState.kt
    package com.edmik.parentapp.presentation.app

    import com.edmik.parentapp.domain.model.Student

    data class AppState(
        val currentStudentId: String = "",
        val currentStudentName: String = "",
        val currentStudentBatch: String = "",
        val linkedStudents: List<Student> = emptyList(),
        val unreadNotificationCount: Int = 0,
        val isOffline: Boolean = false
    )

  CREATE: presentation/app/AppStateManager.kt
    package com.edmik.parentapp.presentation.app

    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow

    class AppStateManager {
        private val _state = MutableStateFlow(AppState())
        val state: StateFlow<AppState> = _state.asStateFlow()

        fun switchStudent(studentId: String) {
            val student = _state.value.linkedStudents.find { it.id == studentId } ?: return
            _state.value = _state.value.copy(
                currentStudentId = student.id,
                currentStudentName = student.name,
                currentStudentBatch = student.batch
            )
        }

        fun updateLinkedStudents(students: List<Student>) {
            _state.value = _state.value.copy(linkedStudents = students)
        }

        fun updateUnreadCount(count: Int) {
            _state.value = _state.value.copy(unreadNotificationCount = count)
        }

        fun setOffline(isOffline: Boolean) {
            _state.value = _state.value.copy(isOffline = isOffline)
        }
    }

  Wire AppStateManager into AppModule.kt:
    single { AppStateManager() }

If App.kt or the top-level entry Composable is in ui/, move it to presentation/app/App.kt.

══ MIGRATION 3 — Add MVI State + Event to Login ══════════════════════

The existing LoginViewModel likely uses a custom UiState sealed class or direct
StateFlow fields. We need to standardise it to the new data-class State pattern.

──── Step 3a — CREATE LoginState.kt (new file) ────

  File: presentation/screens/login/LoginState.kt
  package com.edmik.parentapp.presentation.screens.login

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

──── Step 3b — CREATE LoginEvent.kt (new file) ────

  File: presentation/screens/login/LoginEvent.kt
  package com.edmik.parentapp.presentation.screens.login

  sealed class LoginEvent {
      data class StudentIdChanged(val value: String) : LoginEvent()
      data class PasswordChanged(val value: String) : LoginEvent()
      data class RememberMeToggled(val checked: Boolean) : LoginEvent()
      object TogglePasswordVisibility : LoginEvent()
      object LoginClicked : LoginEvent()
      object BiometricClicked : LoginEvent()
      object ForgotPasswordClicked : LoginEvent()
  }

──── Step 3c — REFACTOR LoginViewModel.kt ────

  Preserve ALL existing login logic. Only change the state management shape.

  Target pattern for LoginViewModel:
    class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {
        private val _state = MutableStateFlow(LoginState())
        val state: StateFlow<LoginState> = _state.asStateFlow()

        fun onEvent(event: LoginEvent) {
            when (event) {
                is LoginEvent.StudentIdChanged -> _state.update { it.copy(studentId = event.value) }
                is LoginEvent.PasswordChanged -> _state.update { it.copy(password = event.value) }
                is LoginEvent.RememberMeToggled -> _state.update { it.copy(rememberMe = event.checked) }
                LoginEvent.TogglePasswordVisibility -> _state.update { it.copy(showPassword = !_state.value.showPassword) }
                LoginEvent.LoginClicked -> performLogin()
                LoginEvent.BiometricClicked -> { /* implement biometric flow */ }
                LoginEvent.ForgotPasswordClicked -> { /* navigation handled in Screen */ }
            }
        }

        private fun performLogin() {
            // Move existing login logic here — DO NOT rewrite, just relocate
        }
    }

  If the existing ViewModel already exposes individual fields as StateFlows,
  consolidate them into a single StateFlow<LoginState>.

  If the existing ViewModel has a sealed UiState (Loading/Success/Error),
  replace it:
    BEFORE: sealed class LoginUiState { object Loading; data class Success(...); data class Error(...) }
    AFTER:  data class LoginState(isLoading, data:..., errorMessage)
    The visual result must be IDENTICAL.

──── Step 3d — REFACTOR LoginScreen.kt ────

  Update to observe the new single state:
    BEFORE: val uiState by vm.uiState.collectAsStateWithLifecycle()
    AFTER:  val state by vm.state.collectAsStateWithLifecycle()

  Replace individual event calls with onEvent():
    BEFORE: vm.onStudentIdChanged(it)
    AFTER:  vm.onEvent(LoginEvent.StudentIdChanged(it))

  Navigation should be called via onNavigate lambda passed to LoginScreen,
  triggered by a state change (e.g. when state.isLoading==false and no error).

  The Composable's VISUAL OUTPUT must remain IDENTICAL to before this change.
  Do not change any colour, layout, or text.

══ MIGRATION 4 — Add MVI State + Event to ForgotPassword ════════════

Repeat the same pattern for ForgotPasswordScreen:

  CREATE: presentation/screens/forgot_password/ForgotPasswordState.kt
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

  CREATE: presentation/screens/forgot_password/ForgotPasswordEvent.kt
    sealed class ForgotPasswordEvent {
        data class IdentifierChanged(val value: String) : ForgotPasswordEvent()
        data class OtpChanged(val value: String) : ForgotPasswordEvent()
        data class NewPasswordChanged(val value: String) : ForgotPasswordEvent()
        data class ConfirmPasswordChanged(val value: String) : ForgotPasswordEvent()
        object SendOtpClicked : ForgotPasswordEvent()
        object VerifyOtpClicked : ForgotPasswordEvent()
        object ResetPasswordClicked : ForgotPasswordEvent()
        object BackClicked : ForgotPasswordEvent()
    }

  REFACTOR ForgotPasswordViewModel.kt and ForgotPasswordScreen.kt
  using the same approach as Login — preserve all logic, only change the
  state management shape to use the new State/Event pattern.

══ MIGRATION 5 — Update Navigation ══════════════════════════════════

If AppNavHost.kt imports from ui.*:
  Update to import from presentation.screens.*
  Update to import from presentation.navigation.*

Example import updates:
  FROM: import com.edmik.parentapp.ui.login.LoginScreen
  TO:   import com.edmik.parentapp.presentation.screens.login.LoginScreen

Also update androidMain/MainActivity.kt if it imports from ui.*:
  import com.edmik.parentapp.presentation.app.App

══ COMPILE CHECK ══════════════════════════════════════════════════════

  ./gradlew :shared:compileKotlinAndroid

Fix any errors. Report: BUILD SUCCESSFUL ✅ or errors + fixes.

ACCEPTANCE CRITERIA:
□ ui/ folder is gone — all files are now under presentation/
□ presentation/screens/login/ has: LoginScreen.kt, LoginViewModel.kt, LoginState.kt, LoginEvent.kt
□ presentation/screens/forgot_password/ has: ForgotPasswordScreen.kt, ViewModel, State, Event
□ presentation/app/ has: AppState.kt, AppStateManager.kt
□ LoginViewModel: single StateFlow<LoginState>, onEvent() dispatcher
□ LoginState: data class (not sealed)
□ LoginEvent: sealed class
□ NO ui.* imports anywhere in the project
□ All Composable visuals UNCHANGED (same layout, colours, text)
□ ./gradlew :shared:compileKotlinAndroid → BUILD SUCCESSFUL

OUTPUT: complete list of every file moved/created/modified + compile result
👉 Send: "R-D looks good. Proceed to Prompt R-E."
══════════════════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────────────────
PROMPT R-E │ Final Verification + Sign-Off
──────────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════════
PARENT APP · Day 1 Refactor · Prompt R-E of 5 (Final) · Verification
══════════════════════════════════════════════════════════════════════
CHAT CONTEXT ✅ R-A: audit  ✅ R-B: data layer  ✅ R-C: domain+DI  ✅ R-D: presentation

══ STEP 1 — FULL PROJECT SCAN (post-refactor) ═══════════════════════

Run again and confirm the new structure:

  find shared/src/commonMain/kotlin -name "*.kt" | sort

Verify the output matches the target structure from doc/CONTEXT.md §4:
  ✅ No files under ui/
  ✅ No files under data/api/ or data/model/ or data/local/ (flat)
  ✅ All screens under presentation/screens/{group}/
  ✅ All DTOs under data/remote/dto/
  ✅ All mappers under data/mapper/
  ✅ TokenManager under data/local/database/
  ✅ UseCases under domain/usecase/auth/

══ STEP 2 — COMPILE BOTH PLATFORMS ═══════════════════════════════════

  CHECK 1: ./gradlew :shared:compileKotlinAndroid
           Expected: BUILD SUCCESSFUL

  CHECK 2: ./gradlew :shared:compileKotlinIosSimulatorArm64
           Expected: BUILD SUCCESSFUL

If either fails: fix the errors and re-run before proceeding.

══ STEP 3 — ARCHITECTURAL PURITY CHECKS ════════════════════════════════

Run each grep and report EXACT output:

  CHECK 3: grep -r "import android\." shared/src/commonMain/
           Expected: empty (no output)

  CHECK 4: grep -r "import com.edmik.parentapp.data" shared/src/commonMain/presentation/
           Expected: empty (no output)

  CHECK 5: grep -r "import com.edmik.parentapp.ui" shared/src/
           Expected: empty (no output — old ui package gone)

  CHECK 6: grep -r "@Serializable" shared/src/commonMain/domain/
           Expected: empty (no output)

  CHECK 7: grep -rn "toDomain()" shared/src/commonMain/data/repository/
           Expected: empty (toDomain() must only be in data/mapper/, not RepositoryImpl)

  CHECK 8: grep -r "class.*ViewModel.*Repository" shared/src/commonMain/presentation/
           Expected: empty (ViewModels must inject UseCases, not Repositories)

For any check that returns output → fix the violation and re-run.

══ STEP 4 — FUNCTIONAL VERIFICATION ════════════════════════════════════

Confirm the following without running the app (code review only):

  □ LoginScreen.kt: collects StateFlow<LoginState> via collectAsStateWithLifecycle()
  □ LoginViewModel.kt: has onEvent(LoginEvent) function and MutableStateFlow<LoginState>
  □ LoginViewModel.kt: injects LoginUseCase — NOT AuthRepository or AuthRepositoryImpl
  □ AuthRepositoryImpl.kt: imports toDomain from data.mapper.AuthMapper
  □ AuthRepositoryImpl.kt: does NOT contain any toDomain() function definition
  □ TokenManager.kt: uses com.russhwolf.settings.Settings — no android.security.* import
  □ All 5 Koin modules exist in di/: Network, Repository, UseCase, Database, App
  □ AppStateManager.kt is in presentation/app/ — NOT in di/

══ STEP 5 — GENERATE SIGN-OFF TEXT ════════════════════════════════════

Write the exact text block to paste into doc/CONTEXT.md → Section 16 "After Day 1":

### After Day 1 ✅ (Refactored)
  ✅ Project migrated from old spec structure to CONTEXT.md §4 target structure
  ✅ data/remote/api/ — AuthApiService.kt
  ✅ data/remote/dto/ — AuthDto.kt (all @Serializable)
  ✅ data/mapper/ — AuthMapper.kt (LoginResponse.toDomain() → Student)
  ✅ data/local/database/ — TokenManager.kt (multiplatform-settings)
  ✅ data/repository/ — AuthRepositoryImpl.kt (imports from mapper)
  ✅ domain/model/ — Student.kt (pure Kotlin, no @Serializable)
  ✅ domain/repository/ — AuthRepository.kt interface
  ✅ domain/usecase/auth/ — LoginUseCase.kt, ForgotPasswordUseCase.kt
  ✅ di/ — NetworkModule, RepositoryModule, UseCaseModule, DatabaseModule, AppModule
  ✅ presentation/theme/ — Color.kt, Typography.kt, Shapes.kt, Theme.kt
  ✅ presentation/navigation/ — Routes.kt, AppNavHost.kt
  ✅ presentation/app/ — AppState.kt, AppStateManager.kt, App.kt
  ✅ presentation/screens/login/ — LoginScreen, LoginViewModel, LoginState, LoginEvent
  ✅ presentation/screens/forgot_password/ — ForgotPasswordScreen, ViewModel, State, Event
  ✅ All architecture checks: 8 grep audits = empty
  ✅ Both platforms compile: Android ✅ iOS ✅

ACCEPTANCE CRITERIA:
□ Full scan matches CONTEXT.md §4 structure
□ CHECK 1 + CHECK 2: both BUILD SUCCESSFUL
□ CHECK 3 through CHECK 8: all return empty output
□ Functional verification: all 8 items confirmed
□ Sign-off text generated

OUTPUT: scan output + all 8 check results + functional verification + sign-off text block
══════════════════════════════════════════════════════════════════════

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
REFACTOR SUMMARY — What changes, what stays the same

  CHANGED (structure + code):
    • All file locations updated to match CONTEXT.md §4
    • All package declarations updated
    • All import paths updated
    • toDomain() extracted to data/mapper/AuthMapper.kt
    • TokenManager → multiplatform-settings (if it was EncryptedSharedPreferences)
    • LoginViewModel → StateFlow<LoginState> + onEvent(LoginEvent)
    • ForgotPasswordViewModel → StateFlow<ForgotPasswordState> + onEvent(ForgotPasswordEvent)
    • UseCases moved to domain/usecase/auth/ feature subfolder

  UNCHANGED (logic + UI preserved):
    • All business logic in ViewModels
    • All API contracts (endpoints, request/response shapes)
    • All Composable visual layouts, colours, text
    • All navigation behaviour
    • Token storage + auto-refresh logic
    • Login rate-limiting (5 attempts → 30s lockout)
    • OTP flow steps in ForgotPassword

  ADDED (new files):
    • data/mapper/AuthMapper.kt
    • domain/model/Student.kt (if missing)
    • presentation/app/AppState.kt
    • presentation/app/AppStateManager.kt
    • presentation/screens/login/LoginState.kt
    • presentation/screens/login/LoginEvent.kt
    • presentation/screens/forgot_password/ForgotPasswordState.kt
    • presentation/screens/forgot_password/ForgotPasswordEvent.kt
    • androidMain/di/AndroidDatabaseModule.kt (if missing)
    • iosMain/di/IosDatabaseModule.kt (if missing)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
