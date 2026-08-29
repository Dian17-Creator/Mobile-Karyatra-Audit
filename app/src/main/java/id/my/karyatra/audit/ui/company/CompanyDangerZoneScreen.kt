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
import id.my.karyatra.audit.component.UiUtils
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.data.viewmodel.CompanyDangerZoneViewModel
import id.my.karyatra.audit.data.viewmodel.CompanyUiState
import id.my.karyatra.audit.data.viewmodel.SubscriptionViewModel

@Composable
fun CompanyDangerZoneScreen(
    onBack: () -> Unit,
    onCompanyStatusLoaded: (Boolean, Boolean) -> Unit = { _, _ -> },
    viewModel: CompanyDangerZoneViewModel = viewModel(),
    subViewModel: SubscriptionViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val user = sessionManager.getUser()
    val userId = remember { user?.id ?: -1 }
    val actualCompanyName = remember { user?.company ?: "" }

    val subState by subViewModel.uiState.collectAsStateWithLifecycle()
    val isPro = remember(subState) { subState.subscriptionState?.isPro() == true }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var typedCompanyName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var confirmDeletion by remember { mutableStateOf(false) }
    var confirmFinanceRetention by remember { mutableStateOf(false) }
    var confirmProNoRefund by remember { mutableStateOf(false) }

    var pendingFinanceAlertDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId != -1) {
            viewModel.loadStatus(userId)
            subViewModel.fetchSubscriptionState()
        }
    }

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is CompanyUiState.Success) {
            val isPending = currentState.lifecycleData.isDeletionPending
            val isInactive = currentState.lifecycleData.isCompanyInactive
            onCompanyStatusLoaded(isInactive || isPending, isPending)
            if (currentState.message != null) {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                typedCompanyName = ""
                password = ""
                confirmDeletion = false
                confirmFinanceRetention = false
                confirmProNoRefund = false

                // If deletion was cancelled successfully (isPending is now false), navigate to Home
                if (!isPending && currentState.message.contains("batal", ignoreCase = true)) {
                    onBack()
                }
                viewModel.clearMessages()
            }
            if (currentState.actionError != null) {
                val errorMsg = currentState.actionError
                if (errorMsg.contains("Finance", ignoreCase = true) || errorMsg.contains("langganan", ignoreCase = true)) {
                    pendingFinanceAlertDialog = errorMsg
                } else {
                    snackbarHostState.showSnackbar(errorMsg)
                }
                viewModel.clearMessages()
            }
        } else if (currentState is CompanyUiState.Error) {
            val errorMsg = currentState.errorMessage
            if (errorMsg.contains("Finance", ignoreCase = true) || errorMsg.contains("langganan", ignoreCase = true)) {
                pendingFinanceAlertDialog = errorMsg
            } else {
                snackbarHostState.showSnackbar(errorMsg)
            }
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
                        color = Color(0xFFDC2626)
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
                        DangerZoneHeaderBanner()

                        if (data.isDeletionPending) {
                            // STATE B: Perusahaan Dalam Masa Tenggang Hapus
                            StateBDeletionPendingContent(
                                lifecycleData = data,
                                password = password,
                                onPasswordChange = { password = it },
                                isPasswordVisible = isPasswordVisible,
                                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                onCancelDeletionClick = {
                                    if (userId != -1 && password.isNotBlank()) {
                                        viewModel.cancelDeletion(userId, password)
                                    }
                                }
                            )
                        } else {
                            // STATE A: Perusahaan Normal / Belum Dijadwalkan Hapus
                            StateANormalDeletionContent(
                                actualCompanyName = actualCompanyName,
                                typedCompanyName = typedCompanyName,
                                onTypedCompanyNameChange = { typedCompanyName = it },
                                password = password,
                                onPasswordChange = { password = it },
                                isPasswordVisible = isPasswordVisible,
                                onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                                confirmDeletion = confirmDeletion,
                                onConfirmDeletionChange = { confirmDeletion = it },
                                confirmFinanceRetention = confirmFinanceRetention,
                                onConfirmFinanceRetentionChange = { confirmFinanceRetention = it },
                                confirmProNoRefund = confirmProNoRefund,
                                onConfirmProNoRefundChange = { confirmProNoRefund = it },
                                isPro = isPro,
                                onRequestDeletionClick = {
                                    if (userId != -1) {
                                        viewModel.requestDeletion(
                                            userId = userId,
                                            companyName = typedCompanyName.trim(),
                                            password = password,
                                            isPro = isPro
                                        )
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
        }
    }

    if (pendingFinanceAlertDialog != null) {
        AlertDialog(
            onDismissRequest = { pendingFinanceAlertDialog = null },
            title = {
                Text(
                    text = "Proses Langganan Menunggu Decision",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            },
            text = {
                Text(
                    text = pendingFinanceAlertDialog ?: "Masih ada pengajuan langganan yang menunggu keputusan Finance. Selesaikan proses tersebut sebelum menghapus perusahaan."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingFinanceAlertDialog = null
                        viewModel.clearMessages()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Mengerti")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun DangerZoneHeaderBanner() {
    val dangerRed = Color(0xFFDC2626)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, dangerRed.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Zona Berbahaya ⚠️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = dangerRed
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Penghapusan akun & seluruh data perusahaan secara permanen.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StateANormalDeletionContent(
    actualCompanyName: String,
    typedCompanyName: String,
    onTypedCompanyNameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    confirmDeletion: Boolean,
    onConfirmDeletionChange: (Boolean) -> Unit,
    confirmFinanceRetention: Boolean,
    onConfirmFinanceRetentionChange: (Boolean) -> Unit,
    confirmProNoRefund: Boolean,
    onConfirmProNoRefundChange: (Boolean) -> Unit,
    isPro: Boolean,
    onRequestDeletionClick: () -> Unit
) {
    val dangerRed = Color(0xFFDC2626)
    val roseBg = Color(0xFFFFF1F2)

    val isCompanyNameMatched = typedCompanyName.trim().equals(actualCompanyName.trim(), ignoreCase = true)
    val isFormValid = isCompanyNameMatched &&
            password.isNotBlank() &&
            confirmDeletion &&
            confirmFinanceRetention &&
            (!isPro || confirmProNoRefund)

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
                text = "Jadwalkan Penghapusan Perusahaan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = dangerRed
            )

            // Danger Warning Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = roseBg),
                border = BorderStroke(1.dp, dangerRed.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = dangerRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Tindakan ini akan menghapus SELURUH data audit, stok opname, pengguna, foto, dan master data perusahaan secara permanen. Setelah masa tenggang (24 jam untuk akun Trial / 7 hari untuk terverifikasi) berakhir, data TIDAK DAPAT DIPULIHKAN.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = dangerRed
                    )
                }
            }

            // Input Nama Perusahaan untuk Konfirmasi
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ketik nama perusahaan Anda \"$actualCompanyName\" untuk konfirmasi:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                OutlinedTextField(
                    value = typedCompanyName,
                    onValueChange = onTypedCompanyNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Masukkan nama perusahaan") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
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

            // Checkbox 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = confirmDeletion,
                    onCheckedChange = onConfirmDeletionChange,
                    colors = CheckboxDefaults.colors(checkedColor = dangerRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Saya memahami seluruh data perusahaan akan dihapus secara permanen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            // Checkbox 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = confirmFinanceRetention,
                    onCheckedChange = onConfirmFinanceRetentionChange,
                    colors = CheckboxDefaults.colors(checkedColor = dangerRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Saya memahami data transaksi keuangan tetap disimpan untuk kepatuhan hukum.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            // Checkbox 3 (If Pro active)
            if (isPro) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = confirmProNoRefund,
                        onCheckedChange = onConfirmProNoRefundChange,
                        colors = CheckboxDefaults.colors(checkedColor = dangerRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Saya menyetujui sisa masa berlangganan Pro hangus tanpa refund.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            // Action Button (Red)
            Button(
                onClick = onRequestDeletionClick,
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = dangerRed,
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jadwalkan Penghapusan Perusahaan",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                )
            }
        }
    }
}

@Composable
fun StateBDeletionPendingContent(
    lifecycleData: id.my.karyatra.audit.data.CompanyLifecycleData,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onCancelDeletionClick: () -> Unit
) {
    val darkRed = Color(0xFF991B1B)
    val roseBg = Color(0xFFFFF1F2)
    val successGreen = Color(0xFF16A34A)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alert Card (Dark Red)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = roseBg),
            border = BorderStroke(1.dp, darkRed.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = darkRed,
                    modifier = Modifier.size(32.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Penghapusan Perusahaan Dijadwalkan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = darkRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Perusahaan Anda dalam proses penghapusan permanen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = darkRed.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Details Box Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Rincian Permintaan",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )

                lifecycleData.ddeletionrequested?.let { dateReq ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Waktu Permintaan:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(UiUtils.formatDateIndo(dateReq), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                    }
                }

                lifecycleData.ddeleteafter?.let { dateExec ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jadwal Eksekusi:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(UiUtils.formatDateIndo(dateExec), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = darkRed)
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

                Text(
                    text = "Catatan: Akses seluruh anggota terblokir. Selama masa tenggang belum berakhir, Anda dapat membatalkan penghapusan di bawah ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }

        // Form Pembatalan
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
                    text = "Pembatalan Penghapusan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )

                Text(
                    text = "Masukkan password owner untuk membatalkan penghapusan dan memulihkan akses perusahaan.",
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
                    onClick = onCancelDeletionClick,
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
                        imageVector = Icons.Default.Undo,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Batalkan Penghapusan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
