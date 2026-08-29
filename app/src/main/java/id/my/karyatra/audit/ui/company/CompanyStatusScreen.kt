package id.my.karyatra.audit.ui.company

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.karyatra.audit.data.CompanyLifecycleData
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.data.viewmodel.CompanyLifecycleViewModel
import id.my.karyatra.audit.data.viewmodel.CompanyUiState

@Composable
fun CompanyStatusScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onCompanyStatusLoaded: (Boolean) -> Unit = {},
    viewModel: CompanyLifecycleViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val user = sessionManager.getUser()
    val userId = remember { user?.id ?: -1 }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var password by remember { mutableStateOf("") }
    var isConfirmed by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != -1) {
            viewModel.loadStatus(userId)
        }
    }

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is CompanyUiState.Success) {
            val isInactive = currentState.lifecycleData.isCompanyInactive
            onCompanyStatusLoaded(isInactive)
            if (currentState.message != null) {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                password = ""
                isConfirmed = false
                
                // If company was successfully reactivated, navigate back to Home
                if (!isInactive) {
                    onBack()
                }
            }
        } else if (currentState is CompanyUiState.Error) {
            snackbarHostState.showSnackbar(currentState.errorMessage)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            when (val state = uiState) {
                is CompanyUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFB63352)
                    )
                }

                is CompanyUiState.Success -> {
                    val data = state.lifecycleData
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Banner
                        HeaderBannerCard()

                        if (data.isCompanyInactive) {
                            // STATE B: Perusahaan Sedang Dinonaktifkan
                            StateBInactiveContent(
                                password = password,
                                onPasswordChange = { password = it },
                                isPasswordVisible = isPasswordVisible,
                                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                onReactivateClick = {
                                    if (userId != -1 && password.isNotBlank()) {
                                        viewModel.reactivateCompany(userId, password)
                                    }
                                },
                                onLogout = onLogout
                            )
                        } else {
                            // STATE A: Perusahaan Sedang Aktif
                            StateAActiveContent(
                                password = password,
                                onPasswordChange = { password = it },
                                isConfirmed = isConfirmed,
                                onConfirmedChange = { isConfirmed = it },
                                isPasswordVisible = isPasswordVisible,
                                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                onDeactivateClick = {
                                    if (userId != -1 && password.isNotBlank() && isConfirmed) {
                                        viewModel.deactivateCompany(userId, password)
                                    }
                                }
                            )
                        }
                    }
                }

                is CompanyUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { if (userId != -1) viewModel.loadStatus(userId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderBannerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status Perusahaan",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pengaturan akses sementara. Tidak ada data yang dihapus.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StateAActiveContent(
    password: String,
    onPasswordChange: (String) -> Unit,
    isConfirmed: Boolean,
    onConfirmedChange: (Boolean) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onDeactivateClick: () -> Unit
) {
    val warningAmber = Color(0xFFD97706)
    val warningBg = Color(0xFFFFFBEB)
    val dangerButtonColor = Color(0xFFD97706)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nonaktifkan Sementara",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )

            Text(
                text = "Semua pengguna perusahaan akan langsung kehilangan akses ke Auditra sampai owner mengaktifkan kembali perusahaan. Data audit, stok opname, master data, foto, pengguna, dan riwayat tetap tersimpan.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )

            // Warning Box (Amber)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = warningBg),
                border = BorderStroke(1.dp, warningAmber.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = warningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Masa Pro tetap berjalan selama perusahaan dinonaktifkan dan tidak dijeda atau diperpanjang.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = warningAmber
                    )
                }
            }

            // Input Password
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password Saat Ini") },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Checkbox Confirmation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isConfirmed,
                    onCheckedChange = onConfirmedChange,
                    colors = CheckboxDefaults.colors(checkedColor = warningAmber)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Saya memahami bahwa seluruh pengguna kehilangan akses sementara sampai perusahaan diaktifkan kembali.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            // Action Button (Amber/Danger)
            Button(
                onClick = onDeactivateClick,
                enabled = password.isNotBlank() && isConfirmed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = dangerButtonColor,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nonaktifkan Sementara",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun StateBInactiveContent(
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onReactivateClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val alertRed = Color(0xFFDC2626)
    val alertBg = Color(0xFFFEF2F2)
    val successGreen = Color(0xFF16A34A)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alert Card (Red)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = alertBg),
            border = BorderStroke(1.dp, alertRed.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = alertRed,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Perusahaan Sedang Dinonaktifkan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = alertRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Seluruh pengguna dalam perusahaan Anda saat ini terblokir dari aplikasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = alertRed.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Form Reaktivasi
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Reaktivasi Perusahaan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )

                Text(
                    text = "Masukkan password owner untuk mengaktifkan kembali seluruh akun pengguna dalam perusahaan Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password Saat Ini (Owner)") },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = onReactivateClick,
                    enabled = password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = successGreen,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aktifkan Kembali Perusahaan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Logout Button
        val primaryColor = Color(0xFFB63352)
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Keluar Aplikasi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
    }
}
