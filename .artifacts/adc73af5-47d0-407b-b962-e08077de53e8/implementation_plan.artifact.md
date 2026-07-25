# Implementation Plan - Add "Remember Me" Option

Implement a "Remember Me" (Ingatkan Saya) feature on the login screen to allow users to choose whether they want to stay logged in across app restarts.

## User Review Required

> [!NOTE]
> **Behavior**: If "Remember Me" is checked, the app will skip the login screen on next launch. If not checked, the user will be redirected to the login screen every time the app is opened, even if they were previously logged in during the last session.

## Proposed Changes

### [Data Layer]

#### [MODIFY] [SessionManager.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/data/SessionManager.kt)
- Add `KEY_REMEMBER_ME` to `SharedPreferences`.
- Update `saveSession` signature to `saveSession(user: UserData, rememberMe: Boolean)`.
- Add `isRememberMe(): Boolean` helper method.

### [UI Layer]

#### [MODIFY] [AuditLogin.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/AuditLogin.kt)
- Add a `rememberMe` state variable.
- Implement a `Checkbox` with the label "Ingatkan Saya" in the login form.
- Pass the `rememberMe` value when calling `sessionManager.saveSession`.

#### [MODIFY] [MainActivity.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/MainActivity.kt)
- Update the routing logic: Navigate to `AuditHome` only if `isLoggedIn()` AND `isRememberMe()` are both true.
- If either is false, navigate to `AuditLogin`.

## Verification Plan

### Manual Verification
- **Test Case 1**: Login with "Remember Me" checked. Close app, reopen. App should go directly to `AuditHome`.
- **Test Case 2**: Login with "Remember Me" UNCHECKED. Close app, reopen. App should go to `AuditLogin`.
- **Test Case 3**: Logout. Verify session is cleared.
