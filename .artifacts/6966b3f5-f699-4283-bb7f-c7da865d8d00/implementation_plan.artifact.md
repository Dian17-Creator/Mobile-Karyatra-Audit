# Unify Department Dropdown Style Plan

This plan aims to synchronize the department dropdown UI across "Audit Proses" and "Pemetaan Departemen" screens to match the modern style used in the "Hasil Audit" screen.

## Proposed Changes

### UI Components

#### [MODIFY] [AuditProses.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/AuditProses.kt)
- Update `StartAuditSection`:
    - Replace the manual `OutlinedCard` + `DropdownMenu` implementation with `ExposedDropdownMenuBox`.
    - Use `OutlinedTextField` as the menu anchor.
    - Match styling: `RoundedCornerShape(16.dp)`, label "Departemen", and `R.drawable.auditdept` as leading icon.

#### [MODIFY] [AuditDepartemen.kt](file:///D:/Project/Karyatra_Audit/app/src/main/java/id/my/karyatra/audit/AuditDepartemen.kt)
- Update `DepartmentSelector`:
    - Replace the manual `OutlinedCard` + `DropdownMenu` implementation with `ExposedDropdownMenuBox`.
    - Use `OutlinedTextField` as the menu anchor.
    - Match styling: `RoundedCornerShape(16.dp)`, label "Departemen", and `R.drawable.auditdept` as leading icon.

## Verification Plan

### Manual Verification
1.  Navigate to **Audit** -> **Audit Proses**. Verify the department dropdown looks exactly like the one in "Hasil Audit".
2.  Navigate to **Pemetaan Departemen**. Verify the department dropdown follows the same style.
3.  Confirm that selecting an item from the dropdown still works correctly in both screens.
4.  Verify that the dropdown popup is scrollable and styled correctly.
