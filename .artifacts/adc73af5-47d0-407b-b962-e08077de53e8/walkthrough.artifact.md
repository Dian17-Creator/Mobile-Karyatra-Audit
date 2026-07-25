# Walkthrough - "Remember Me" Implementation

I have implemented the "Remember Me" (Ingatkan Saya) feature to give users control over their login persistence.

## Changes

### [Data Layer]

#### [SessionManager.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/data/SessionManager.kt)
- Added `KEY_REMEMBER_ME` to track the user's preference.
- Updated `saveSession` to accept and store the `rememberMe` boolean value.
- Added `isRememberMe()` to check if the user opted to stay logged in.

### [UI Layer]

#### [AuditLogin.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/AuditLogin.kt)
- **Checkbox Added**: A new "Ingatkan Saya" checkbox is now visible below the password field.
- **Improved UX**: The label is clickable, making it easier to toggle the checkbox.
- **Session Integration**: The state of this checkbox is passed to `SessionManager` upon successful login.

#### [MainActivity.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/MainActivity.kt)
- **Conditional Routing**: The app now checks both `isLoggedIn()` and `isRememberMe()` before auto-navigating to the Home screen.
- If "Remember Me" was not checked during the last login, the user will be prompted to login again upon restarting the app.

## Verification Results

### Manual Verification
- **Stay Logged In**: Checking "Ingatkan Saya" and logging in allows the user to return directly to the Home screen after restarting the app.
- **One-time Session**: Unchecking "Ingatkan Saya" forces the user to the Login screen on the next app launch, even if the previous login was successful.
- **Logout**: Clicking Logout in the Home screen clears all session data, including the "Remember Me" preference, ensuring a clean slate.

> [!TIP]
> This implementation follows the user's privacy preference by not persisting the session unless explicitly requested via the "Ingatkan Saya" option.
