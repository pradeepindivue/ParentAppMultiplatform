# DECISIONS.md — Parent App (KMP + Compose Multiplatform)
> **LOCATION:** `ParentAppMultiplatform/doc/DECISIONS.md`
> **LAST UPDATED:** 2026-03-20
>
> **AGENT INSTRUCTION:** Before choosing any library or pattern, check this file.
> If a decision is listed here, follow it — do not re-evaluate. If something is not
> listed, make the conservative choice, add a new row, and flag it in your output.
> **If a decision here conflicts with `doc/CONTEXT.md`, this file wins — it is newer.**

---

## Architecture Decisions

| ID | Decision | Chosen | Rejected | Rationale |
|---|---|---|---|---|
| D-001 | **UI Framework** | Compose Multiplatform (CMP) 1.7.x | Jetpack Compose (Android-only) | Single UI codebase for 1 developer · Spec originally said Android-only but KMP/CMP chosen to deliver Android + iOS in the same sprint without a rewrite |
| D-002 | **Target Platforms** | Android + iOS simultaneously from Day 1 | Android-only Phase 1 then iOS Phase 2 | KMP removes the cost of a Phase 2 rewrite · `commonMain` is the single source of truth for both |
| D-003 | **Dependency Injection** | Koin 4.x | Hilt (Dagger) | Hilt is Android-only — cannot be used in `commonMain` · Koin works natively in KMP |
| D-004 | **HTTP Client** | Ktor Client 3.x | Retrofit 2 + OkHttp | Retrofit is JVM-only — cannot be used in `commonMain` · Ktor is the KMP standard |
| D-005 | **Local Database** | SQLDelight 2.x | Room | Room is Android-only · SQLDelight generates type-safe Kotlin from `.sq` files and works in `commonMain` |
| D-006 | **JSON Serialization** | Kotlinx Serialization 1.7+ | Moshi | Moshi is JVM-only · Kotlinx Serialization works in `commonMain` |
| D-007 | **Secure Token Storage** | multiplatform-settings 1.2+ (encrypted) | EncryptedSharedPreferences | EncryptedSharedPreferences is Android-only · multiplatform-settings wraps Keychain (iOS) and EncryptedSharedPreferences (Android) behind a common API |
| D-008 | **Navigation** | Navigation Compose CMP 2.8.x (JetBrains) | Voyager, Decompose | Official JetBrains KMP navigation library · type-safe routes · `NavHost` compiles in `commonMain` |
| D-009 | **Image Loading** | Coil 3.x | Glide, Picasso | Coil 3.x has native CMP support · `AsyncImage` compiles in `commonMain` |
| D-010 | **Charts** | Canvas-based custom Compose | MPAndroidChart, Vico | MPAndroidChart is Android-only · Vico has CMP support but adds a dependency · Custom Canvas charts are pure Kotlin and give full design control |
| D-011 | **Date/Time** | Kotlinx DateTime 0.6+ | java.time, ThreeTenBP | `java.time` is JVM-only · Kotlinx DateTime works in `commonMain` |
| D-012 | **Payment** | Razorpay via `expect/actual` | Direct SDK in commonMain | Razorpay has separate Android SDK (`com.razorpay:checkout:1.6.33`) and iOS SDK · `expect/actual` bridges them cleanly |
| D-013 | **Push Notifications** | FCM + APNs via `expect/actual` | — | Firebase handles Android · APNs handles iOS · `expect class PushNotificationManager` in `data/platform/` |
| D-014 | **Biometric Auth** | `expect/actual` (BiometricPrompt + LocalAuthentication) | — | Android: `BiometricPrompt` API · iOS: `LocalAuthentication` framework |
| D-015 | **State management pattern** | MVVM + MVI (`State` + `Event` + `StateFlow`) | LiveData, sealed UiState only | `State` = data class snapshot · `Event` = user intents · `StateFlow` = reactive stream · ViewModels expose no DTOs — domain models only |
| D-016 | **ViewModel UiState shape** | `data class ScreenState(isLoading, data: DomainModel?, errorMessage)` | `sealed class: Loading \| Success \| Error` | Data class with nullable fields avoids pattern-match boilerplate · Easy to update individual fields with `_state.update { it.copy(...) }` |
| D-017 | **Mapper location** | Dedicated `data/mapper/*Mapper.kt` files | Inline `toDomain()` inside `RepositoryImpl` | Inline mappers bloat `RepositoryImpl` · Dedicated files are individually testable · Clear separation between transport and domain shape |
| D-018 | **AppStateManager location** | `presentation/app/AppStateManager.kt` | Inside `di/` | `di/` contains only Koin wiring · `AppStateManager` is a presentation-layer state holder, not infrastructure |
| D-019 | **Presentation structure** | `presentation/screens/{screen_group}/` — flat per-screen folders | `ui/{screen}/` or feature modules | One folder per screen group, each containing Screen + ViewModel + State + Event + optional components/ · No per-screen `di/` or `utils/` |
| D-020 | **Shared vs screen-scoped components** | Shared in `presentation/components/` · Screen-scoped in `screens/{screen}/components/` | All in one components folder | Components used across ≥2 features go in `presentation/components/` · Components only used in one feature stay in that feature's `components/` |
| D-021 | **SQLDelight file location** | `.sq` files in `data/local/entity/` | Directly in `commonMain/sqldelight/` | Keeps all local data concerns together under `data/local/` · `entity/` mirrors the Android architecture convention for Room entities |
| D-022 | **API service location** | `data/remote/api/` | `data/api/` (flat) | `remote/` sub-layer cleanly separates remote vs local data sources · Matches the `data/remote/dto/` pattern |
| D-023 | **DTO location** | `data/remote/dto/` | `data/model/` | `model/` is ambiguous — could be domain model or DTO · `remote/dto/` is unambiguous: `@Serializable` network transfer objects only |
| D-024 | **UseCase grouping** | Grouped by feature in `domain/usecase/{feature}/` | Flat `domain/usecase/` | 28+ use cases in a flat folder is hard to navigate · Feature subfolders make it obvious which use cases belong together |
| D-025 | **expect/actual file paths** | `actual` mirrors `commonMain` package path exactly | Flat `actual/` folder | KMP resolves `expect/actual` by fully-qualified class name · `expect class PaymentLauncher` in `com.edmik.parentapp.data.payment` → actual at `androidMain/kotlin/com/edmik/parentapp/data/payment/PaymentLauncher.android.kt` |
| D-026 | **Offline sync mechanism** | `OfflineSyncManager` in `data/sync/` calls UseCases on reconnect | Direct repository calls | Sync code must use the same business logic path as online code · Calling UseCases ensures consistent validation and domain model flow |
| D-027 | **Project already scaffolded** | Developer pre-creates project in Android Studio KMP wizard | Agent creates from scratch | Avoid scaffolding errors · Agent configures existing files instead |
| D-028 | **Backend delivers same-day** | Frontend integrates live APIs immediately | Mock API server | Sprint plan commits backend to same-day delivery · No stub layer needed |
| D-029 | **Analytics API prefix** | Screen 17: `/pulse/analytics/parent/dashboard` (confirmed from spec) · Screen 18: `/parent/analytics/subject/{id}` | — | Spec document explicitly states Screen 17 uses `/pulse/` prefix · Subject detail uses `/parent/` prefix |

---

## Open Questions

| ID | Question | Who resolves | Status |
|---|---|---|---|
| Q-001 | Is the dev SSL certificate at `https://dev.indivue.in:8080` a valid CA-signed cert? iOS ATS rejects self-signed certificates. | Backend team / DevOps | ⏳ Open |
| Q-002 | Does the backend send `Access-Control-Allow-Origin` headers? Required for any future web PWA build. | Backend team | ⏳ Open |
| Q-003 | Razorpay iOS SDK — CocoaPods or Swift Package Manager? Affects `iosApp` dependency setup. | Dev lead / Razorpay | ⏳ Open |
| Q-004 | Firebase project: does the Edmik Firebase project already exist? Share `google-services.json` and `GoogleService-Info.plist` when ready. Until then, use stub `PushNotificationManager`. | DevOps | ⏳ Open |
| Q-005 | Exact path for token refresh: is it `POST /parent/refresh-token`? Needed for Ktor `bearerTokens {}` config. | Backend team | ⏳ Open |
| Q-006 | Screen 18 Analytics Subject Detail — endpoint confirmed as `/parent/analytics/subject/{subjectId}` (not `/pulse/`)? Spec says `/parent/` for this one. | Backend team | ⏳ Open |
| Q-007 | Leave document upload: does `/parent/leave/apply` accept `multipart/form-data` when no file is attached, or does it switch to `application/json`? | Backend team | ⏳ Open |
| Q-008 | Are there any other endpoints using `/pulse/` prefix besides Analytics Dashboard (Screen 17)? | Backend team | ⏳ Open |

---

## Resolved Decisions Log

Decisions that were initially uncertain and have since been settled:

| ID | Question | Resolution | Date |
|---|---|---|---|
| R-001 | KMP or Android-only? | KMP from Day 1 — single codebase for Android + iOS | Mar 18 |
| R-002 | Which state management pattern? | MVVM + MVI with `State` (data class) + `Event` (sealed class) + `StateFlow` | Mar 19 |
| R-003 | Where do mappers live? | Dedicated `data/mapper/` files — not inline in `RepositoryImpl` | Mar 20 |
| R-004 | Screen folder naming convention? | `presentation/screens/{screen_group}/` — no `feature_` prefix, no per-screen `di/` or `utils/` | Mar 20 |
| R-005 | Spec says Hilt+Retrofit+Room — override? | Yes, overridden by KMP decision. Koin+Ktor+SQLDelight are the correct choices for `commonMain`. | Mar 20 |

---

## How to Use This File

**Developer:** Add a row whenever a new architectural decision is made. Update Open Questions when answers arrive from the team.

**Agent:** Before choosing a library, pattern, or file path — check this table. If the question is answered here, follow it. If not, make the conservative choice, document it as a new row, and include it in your output's "Assumptions" section.

**Priority:** `DECISIONS.md` > `CONTEXT.md` for any conflict. This file is more recent.
