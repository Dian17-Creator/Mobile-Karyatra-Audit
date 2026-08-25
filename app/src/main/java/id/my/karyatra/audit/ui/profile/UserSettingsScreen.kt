package id.my.karyatra.audit.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.data.UserData
import id.my.karyatra.audit.data.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(
    onBack: () -> Unit,
    viewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUser = sessionManager.getUser()
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFFB63352)

    LaunchedEffect(Unit) {
        currentUser?.id?.let { viewModel.fetchUsers(it) }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pengguna")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading && uiState.userList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.userList) { user ->
                        UserListItem(
                            user = user,
                            onUpdateLevel = { level ->
                                currentUser?.id?.let { viewModel.updateUserLevel(it, user.id, level) }
                            },
                            onDelete = {
                                currentUser?.id?.let { viewModel.deleteUser(it, user.id) }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddUserDialog(
            isTrial = currentUser?.is_trial ?: false,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, pass, level ->
                currentUser?.id?.let { viewModel.addUser(it, name, email, pass, level) }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun UserListItem(
    user: UserData,
    onUpdateLevel: (String) -> Unit,
    onDelete: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)
    var selectedLevel by remember { mutableStateOf(user.role.admin.let { if (it) "admin" else "audit" }) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = user.email, color = Color.Gray, fontSize = 14.sp)
                    Text(text = "Dibuat: ${user.created_at ?: "-"}", color = Color.Gray, fontSize = 12.sp)
                }
                
                if (user.is_owner == true) {
                    Surface(
                        color = primaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Pemilik",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                    }
                }
            }

            if (user.is_owner != true) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Level Selector (Simple Row of Radio buttons or similar)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        RadioButton(selected = selectedLevel == "admin", onClick = { selectedLevel = "admin" })
                        Text("Admin", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        RadioButton(selected = selectedLevel == "audit", onClick = { selectedLevel = "audit" })
                        Text("Audit", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onUpdateLevel(selectedLevel) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Simpan", fontSize = 12.sp)
                    }

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun AddUserDialog(
    isTrial: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf("audit") }

    val primaryColor = Color(0xFFB63352)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tambah Pengguna",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
            }
        },
        text = {
            if (isTrial) {
                Text(
                    "Manajemen pengguna dinonaktifkan selama mode trial. Silakan verifikasi email owner.",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        HorizontalDivider(
//                            modifier = Modifier.weight(1f),
//                            thickness = 1.dp,
//                            color = Color.LightGray.copy(alpha = 0.5f)
//                        )
//                        Text(
//                            text = "Rule",
//                            modifier = Modifier.padding(horizontal = 12.dp),
//                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
//                            color = Color.Gray
//                        )
//                        HorizontalDivider(
//                            modifier = Modifier.weight(1f),
//                            thickness = 1.dp,
//                            color = Color.LightGray.copy(alpha = 0.5f)
//                        )
//                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { level = "admin" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (level == "admin") primaryColor else Color.Transparent,
                                contentColor = if (level == "admin") Color.White else primaryColor
                            ),
                            border = if (level == "admin") null else BorderStroke(1.dp, primaryColor)
                        ) {
                            Text("Admin", fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { level = "audit" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (level == "audit") primaryColor else Color.Transparent,
                                contentColor = if (level == "audit") Color.White else primaryColor
                            ),
                            border = if (level == "audit") null else BorderStroke(1.dp, primaryColor)
                        ) {
                            Text("Auditor", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal", color = primaryColor, fontWeight = FontWeight.Bold)
                }

                if (!isTrial) {
                    Button(
                        onClick = { 
                            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Harap isi semua bidang", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onConfirm(name, email, password, level) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tambah", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )

}

@Composable
fun EditProfileDialog(
    user: UserData?,
    onDismiss: () -> Unit,
    viewModel: UserViewModel,
    sessionManager: SessionManager
) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var activeTab by remember { mutableStateOf(0) } // 0: Profile, 1: Password
    val primaryColor = Color(0xFFB63352)

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    // Bug Fix: Clear messages on initial load to prevent auto-close if previous action was successful
    LaunchedEffect(Unit) {
        viewModel.clearMessages()
    }

    LaunchedEffect(uiState.isActionSuccess) {
        if (uiState.isActionSuccess) {
            onDismiss()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Edit Akun", 
                    fontWeight = FontWeight.Bold, 
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                TabRow(
                    selectedTabIndex = activeTab, 
                    contentColor = primaryColor,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[activeTab]), color = primaryColor)
                    }
                ) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                        Text("Profil", modifier = Modifier.padding(8.dp))
                    }
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                        Text("Password", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                if (activeTab == 0) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(user?.is_owner == true && user.is_email_verified == true),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    if (user?.is_owner == true && user.is_email_verified == true) {
                        Text("Email owner terverifikasi tidak dapat diubah.", color = Color.Gray, fontSize = 10.sp)
                    }
                } else {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Password Saat Ini") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Password Baru") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Konfirmasi Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = null)
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (activeTab == 0) {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        user?.id?.let { viewModel.updateProfile(it, name, email, sessionManager) }
                    } else {
                        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                            Toast.makeText(context, "Semua field password wajib diisi", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            Toast.makeText(context, "Konfirmasi password baru tidak cocok", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        user?.id?.let { viewModel.changePassword(it, currentPassword, newPassword, confirmPassword) }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
