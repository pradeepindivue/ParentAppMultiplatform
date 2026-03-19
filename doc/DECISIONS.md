# DECISIONS.md — Parent App (KMP + Compose Multiplatform)
> **LOCATION:** `ParentAppMultiplatform/doc/DECISIONS.md`
> **AGENT INSTRUCTION:** If a decision here conflicts with `doc/CONTEXT.md`,
> this file wins — it is more recent. Never re-open a closed decision.
> Log any new question in the Open Questions table instead.

---

## Architecture Decisions

| ID | Date | Decision | Rationale | Impact |
|---|---|---|---|---|
| D-001 | Mar 19 | **Compose Multiplatform** over KMP shared-logic-only | Single UI codebase, faster 2-week delivery for 1 developer | All UI in `commonMain` |
| D-002 | Mar 19 | **iOS 16+** minimum target | CMP 1.7.x stable; covers ~95% active iOS devices; all required UIKit interop patterns supported | `iosDeploymentTarget = "16.0"` in Xcode |
| D-003 | Mar 19 | **Koin 4.x** for DI — NOT Hilt | Hilt is Android-only; Koin supports `commonMain` natively | All modules in `commonMain/di/` |
| D-004 | Mar 19 | **Ktor Client 3.x** for networking — NOT Retrofit | Retrofit is Android-JVM only; Ktor is the KMP standard | OkHttp engine (Android), Darwin engine (iOS) |
| D-005 | Mar 19 | **SQLDelight 2.x** for local DB — NOT Room | Room is Android-only; SQLDelight generates multiplatform type-safe code | Schema in `commonMain/sqldelight/` |
| D-006 | Mar 19 | **Kotlinx Serialization** for JSON — NOT Moshi | Moshi is Android-JVM; Kotlinx Serialization works in `commonMain` | All models use `@Serializable` |
| D-007 | Mar 19 | **multiplatform-settings** (encrypted) for tokens | Works in `commonMain`; uses EncryptedSharedPrefs on Android, Keychain on iOS | `TokenManager` lives in `commonMain` |
| D-008 | Mar 19 | **Navigation Compose Multiplatform** (JetBrains) | Official CMP navigation; type-safe routes | `NavHost` in `commonMain` |
| D-009 | Mar 19 | **Coil 3.x** for images | CMP support built-in; `AsyncImage` works in `commonMain` | No platform split needed |
| D-010 | Mar 19 | **Payment via expect/actual** | Razorpay has separate Android and iOS SDKs | `PaymentLauncher` in `commonMain/data/payment/` |
| D-011 | Mar 19 | **Push via expect/actual** | FCM (Android) and APNs/Firebase iOS are platform-specific | `PushNotificationManager` in `commonMain` |
| D-012 | Mar 19 | **Biometric via expect/actual** | `BiometricPrompt` (Android) vs `LocalAuthentication` (iOS) | `BiometricAuthManager` in `commonMain` |
| D-013 | Mar 19 | **Offline queue in SQLDelight** | Consistent storage layer; survives app restarts | `pending_leave_queue`, `pending_message_queue` tables |
| D-014 | Mar 19 | **KMP-first sprint** — both platforms built simultaneously | Agreed on Mar 19 sprint rewrite | Sprint plan rewritten in `doc/DAILY_PROMPTS.md` |
| D-015 | Mar 19 | **`StateFlow<ScreenUiState>`** — sealed class Loading/Success/Error | Predictable, testable, idiomatic KMP | All ViewModels follow this pattern |
| D-016 | Mar 19 | **No mock APIs** — backend pushes same-day | Backend team committed; if delayed >1 day, agent uses hardcoded stub data temporarily | Agent notes stub usage in commit message |
| D-017 | Mar 19 | **Canvas-based Compose charts** for Donut/Gauge | No stable KMP chart library confirmed; Canvas is reliable and zero-dependency | `DonutChart.kt`, `GaugeChart.kt` custom composables |
| D-018 | Mar 19 | **Canvas-based LineChart** for trends | Same rationale as D-017 | `LineChart.kt` custom composable |
| D-019 | Mar 19 | **Removed Flutter/React state management table** from spec | Copy-paste error from another project's spec | `doc/Parent_App_Spec.docx` has this error — ignore that section |
| D-020 | Mar 19 | **Project created via Android Studio** KMP wizard | Developer already created the project with package `com.edmik.parentapp` | Day 1 skips scaffold creation; starts from dependency configuration |
| D-021 | Mar 19 | **All docs in `doc/` folder** | Centralised documentation; all agent prompts reference `doc/` paths | Every agent session attaches `doc/CONTEXT.md` + `doc/DECISIONS.md` |

---

## Open Questions

| ID | Question | Owner | Status |
|---|---|---|---|
| Q-001 | Is the dev SSL cert valid (not self-signed)? iOS ATS rejects self-signed. | Backend team | ⏳ Open |
| Q-002 | Does backend send `Access-Control-Allow-Origin` headers? (iOS simulator) | Backend team | ⏳ Open |
| Q-003 | Razorpay iOS SDK integration method — CocoaPods or SPM? | Dev lead | ⏳ Open |
| Q-004 | Firebase project exists for Edmik? Share `google-services.json` + `GoogleService-Info.plist` | DevOps | ⏳ Open |
| Q-005 | Is `/parent/refresh-token` the correct token refresh endpoint path? | Backend team | ⏳ Open |
| Q-006 | Analytics Subject Detail — `/parent/analytics/` or `/pulse/analytics/` prefix? | Backend team | ⏳ Open |

---

## How to Use This File

- **Developer:** Add a row for every new decision made during the sprint.
  Resolve Open Questions by updating their Status column.
- **Agent:** Before choosing any library or pattern, check this table.
  If not listed here, make the conservative choice, document it as a new row,
  and flag it in your output.
- **Blocked items:** Any unresolved Q-item that blocks a daily task must be
  noted in the agent's output under "Flagged Items".
