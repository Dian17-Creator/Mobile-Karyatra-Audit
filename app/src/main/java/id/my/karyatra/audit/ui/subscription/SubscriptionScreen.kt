package id.my.karyatra.audit.ui.subscription

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.karyatra.audit.component.Header
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.data.model.subscription.SubscriptionPlan
import id.my.karyatra.audit.data.viewmodel.SubscriptionViewModel
import id.my.karyatra.audit.component.UiUtils
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val user = remember { sessionManager.getUser() }
    val isOwner = user?.is_owner == true
    
    val primaryColor = Color(0xFFB63352)
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var showUpgradeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchSubscriptionState()
        viewModel.fetchPlans()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
            showUpgradeDialog = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Current Plan Status
            CurrentPlanCard(
                state = uiState.subscriptionState,
                isLoading = uiState.isLoading
            )

            Text(
                text = "Pilihan Paket",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.isPlansLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.plans) { plan ->
                        PlanItemCard(
                            plan = plan,
                            isCurrent = uiState.subscriptionState?.plan?.lowercase() == plan.name.lowercase(),
                            onSelect = {
                                if (isOwner) {
                                    selectedPlan = plan
                                    showUpgradeDialog = true
                                } else {
                                    Toast.makeText(context, "Hanya Owner yang dapat melakukan upgrade.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showUpgradeDialog && selectedPlan != null) {
        UpgradeDialog(
            plan = selectedPlan!!,
            isUploading = uiState.isUploading,
            onDismiss = { showUpgradeDialog = false },
            onConfirm = { file, ref ->
                viewModel.requestSubscription(selectedPlan!!, file, ref)
            }
        )
    }
}

@Composable
fun CurrentPlanCard(
    state: id.my.karyatra.audit.data.model.subscription.SubscriptionStateResponse?,
    isLoading: Boolean
) {
    val primaryColor = Color(0xFFB63352)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Status Berlangganan",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = primaryColor
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = state?.plan?.uppercase() ?: "FREE",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.Black
                    )
                    
                    Surface(
                        color = when(state?.plan?.lowercase()) {
                            "pro" -> Color(0xFFFFD700)
                            "trial" -> Color(0xFF64B5F6)
                            else -> Color(0xFFE0E0E0)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (state?.isPro() == true) "AKTIF" else "BASIC",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (state?.plan?.lowercase() == "pro") Color.Black else Color.White
                        )
                    }
                }

                if (state?.isPro() == true && state.proUntil != null) {
                    Text(
                        text = "Berlaku hingga: ${UiUtils.formatDateIndo(state.proUntil)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor
                    )
                }

                if (state?.isUpgradePending == true) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Pending, null, tint = Color(0xFF0D47A1), modifier = Modifier.size(20.dp))
                            Text(
                                text = "Upgrade sedang diverifikasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF0D47A1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlanItemCard(
    plan: SubscriptionPlan,
    isCurrent: Boolean,
    onSelect: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) primaryColor.copy(alpha = 0.05f) else Color.White
        ),
        border = BorderStroke(
            width = if (isCurrent) 2.dp else 1.dp,
            color = if (isCurrent) primaryColor else Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                if (isCurrent) {
                    Icon(Icons.Default.CheckCircle, null, tint = primaryColor)
                }
            }
            
            Text(
                text = "Rp ${String.format("%,.0f", plan.price)} / ${plan.durationMonths} Bulan",
                style = MaterialTheme.typography.titleMedium,
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = plan.description ?: "Nikmati fitur premium untuk meningkatkan efisiensi audit Anda.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSelect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isCurrent,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (plan.name.lowercase() == "pro") Color(0xFFFFD700) else primaryColor,
                    contentColor = if (plan.name.lowercase() == "pro") Color.Black else Color.White
                )
            ) {
                Text(
                    text = if (isCurrent) "Paket Aktif" else "Pilih Paket",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun UpgradeDialog(
    plan: SubscriptionPlan,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (File, String?) -> Unit
) {
    var paymentRef by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    AlertDialog(
        onDismissRequest = if (isUploading) ({}) else onDismiss,
        title = { Text("Pembayaran ${plan.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Chosen Plan Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PAKET DIPILIH", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(plan.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Rp ${String.format("%,.0f", plan.price)}", fontWeight = FontWeight.Bold, color = Color(0xFFB63352))
                        }
                        Text("Nominal di atas adalah harga final yang harus dibayar.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                // Payment Method Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD).copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, Color(0xFF2196F3).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("METODE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0D47A1))
                        Text("Bank BCA", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("TUJUAN PEMBAYARAN", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0D47A1))
                        Text("123456789 a.n. Admin Auditra", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Paket akan aktif maksimal 2 hari kerja setelah pembayaran terkonfirmasi", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0D47A1))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Referensi pembayaran (opsional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = paymentRef,
                        onValueChange = { paymentRef = it },
                        placeholder = { 
                            Text(
                                "Contoh: nomor referensi transfer",
                                maxLines = 1,
                                softWrap = false
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bukti pembayaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f)),
                        enabled = !isUploading,
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.CloudUpload, null, tint = Color.DarkGray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedImageUri != null) "Bukti Terpilih" else "Unggah Bukti Bayar",
                            color = Color.DarkGray
                        )
                    }
                    
                    if (selectedImageUri != null) {
                        Text(
                            text = "File siap diunggah",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons inside the same column for perfect alignment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isUploading,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFB63352))
                    ) {
                        Text("Batal", color = Color(0xFFB63352), fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                val file = getFileFromUri(context, uri)
                                if (file != null) {
                                    onConfirm(file, paymentRef.ifBlank { null })
                                }
                            }
                        },
                        enabled = !isUploading && selectedImageUri != null,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Kirim", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = null,
        shape = RoundedCornerShape(24.dp)
    )
}

private fun getFileFromUri(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_payment_proof_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file
    } catch (e: Exception) {
        null
    }
}
