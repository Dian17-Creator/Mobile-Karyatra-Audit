# Walkthrough - Unified Department Dropdown Style

I have updated the department selection dropdowns in the **Audit Proses** and **Pemetaan Departemen** screens to match the modern style used in the **Hasil Audit** screen.

## Key Changes

### 1. Modernized `AuditProses.kt`
- Replaced the previous manual card layout with a standard `ExposedDropdownMenuBox`.
- **Improved Visuals**: Added the "Departemen" label and the `auditdept` icon as a leading icon, ensuring a consistent look and feel.
- **Consistent Styling**: Used the same `RoundedCornerShape(16.dp)` and color scheme as the results screen.

### 2. Modernized `AuditDepartemen.kt`
- Updated the `DepartmentSelector` to use the new unified dropdown design.
- **Enhanced UX**: The dropdown popup is now scrollable and follows the same typography and padding rules as other screens.

### 3. Unified Styling
- All department selectors now feature:
    - **Leading Icon**: `R.drawable.auditdept`
    - **Label**: "Departemen"
    - **Border Radius**: 16.dp
    - **Theme Colors**: Consistent use of primary and brand colors for focused and unfocused states.

## Verification
- Navigating to **Audit Proses** shows the new, cleaner dropdown.
- Navigating to **Pemetaan Departemen** shows the same updated UI.
- Selection logic and scrollable popups are verified to work correctly on both screens.
