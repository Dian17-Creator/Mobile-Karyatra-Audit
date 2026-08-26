package id.my.karyatra.audit.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
    var userToDelete by remember { mutableStateOf<UserData?>(null) }
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
                                userToDelete = user
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

    userToDelete?.let { user ->
        DeleteConfirmDialog(
            userName = user.name,
            onDismiss = { userToDelete = null },
            onConfirm = {
                currentUser?.id?.let { viewModel.deleteUser(it, user.id) }
                userToDelete = null
            }
        )
    }
}

@Composable
fun DeleteConfirmDialog(
    userName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Hapus Pengguna",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        },
        text = {
            Text(
                text = "Apakah Anda yakin ingin menghapus pengguna \"$userName\"? Tindakan ini tidak dapat dibatalkan.",
                fontSize = 14.sp,
                color = Color.Gray
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryColor)
                ) {
                    Text("Batal", color = primaryColor, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Initial Circle
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (user.is_owner == true) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = primaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Pemilik",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                )
                            }
                        }
                    }
                    Text(text = user.email, color = Color.Gray, fontSize = 13.sp)
                    Text(
                        text = "Dibuat: ${user.created_at?.substringBefore(" ") ?: "-"}",
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            if (user.is_owner != true) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Role Selector Buttons
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { selectedLevel = "admin" },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLevel == "admin") primaryColor else Color.Transparent,
                                contentColor = if (selectedLevel == "admin") Color.White else primaryColor
                            ),
                            border = if (selectedLevel == "admin") null else BorderStroke(1.dp, primaryColor)
                        ) {
                            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { selectedLevel = "audit" },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedLevel == "audit") primaryColor else Color.Transparent,
                                contentColor = if (selectedLevel == "audit") Color.White else primaryColor
                            ),
                            border = if (selectedLevel == "audit") null else BorderStroke(1.dp, primaryColor)
                        ) {
                            Text("Audit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Save Button
                    Button(
                        onClick = { onUpdateLevel(selectedLevel) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Simpan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Delete Button (Matching exact button height)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDelete),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
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
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryColor)
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
