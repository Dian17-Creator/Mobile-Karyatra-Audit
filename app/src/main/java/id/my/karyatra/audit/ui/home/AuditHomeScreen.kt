package id.my.karyatra.audit.ui.home

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.karyatra.audit.*
import id.my.karyatra.audit.R
import android.widget.Toast
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.data.RecentActivityData
import id.my.karyatra.audit.data.viewmodel.HomeViewModel
import id.my.karyatra.audit.data.viewmodel.SubscriptionViewModel
import id.my.karyatra.audit.component.UiUtils

data class HomeMenu(
    val title: String,
    @DrawableRes val iconRes: Int = 0,
    val iconVector: ImageVector? = null
)

@Composable
fun AuditHomeScreen(
    username: String,
    viewModel: HomeViewModel = viewModel(),
    subViewModel: SubscriptionViewModel = viewModel(),
    onManageUsers: () -> Unit = {},
    onManageDepartments: () -> Unit = {},
    onManageCompany: () -> Unit = {},
    onKategoriPertanyaan: () -> Unit = {},
    onPemetaanDepartemen: () -> Unit = {},
    onAudit: (Int) -> Unit = {},
    onHasilAudit: () -> Unit = {},
    onSubscription: () -> Unit = {}
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val sessionManager = remember { SessionManager(context) }
    val user = sessionManager.getUser()
    val userId = remember { user?.id ?: -1 }
    val isOwner = remember { user?.is_owner ?: false }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subState by subViewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    val primaryColor = Color(0xFFB63352)
    var showGatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.resendMessage) {
        uiState.resendMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchDashboardSummary()
                subViewModel.fetchSubscriptionState()
                if (userId != -1) {
                    viewModel.startVerificationCheck(userId)
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopVerificationCheck()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val menus = listOf(
        HomeMenu("Kategori &\nPertanyaan", R.drawable.auditquest),
        HomeMenu("Pemetaan\nDepartemen", R.drawable.auditdept),
        HomeMenu("Audit", R.drawable.audits),
        HomeMenu("Hasil Audit", R.drawable.auditdone)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        uiState.currentUser?.let { u ->
            // Only show verification banner for owners
            if (u.is_owner == true && u.is_email_verified == false) {
                VerificationBanner(
                    isResending = uiState.isResending,
                    onResend = { viewModel.resendVerification() }
                )
            }
        }

        subState.subscriptionState?.let { state ->
            if (state.isUpgradePending) {
                InfoBanner(
                    message = "Pengajuan Pro Anda sedang dalam verifikasi tim Finance",
                    containerColor = Color(0xFFE3F2FD),
                    contentColor = Color(0xFF0D47A1),
                    icon = Icons.Default.Pending
                )
            } else if (state.isRejectionVisible) {
                InfoBanner(
                    message = "Pengajuan sebelumnya ditolak. Silakan ajukan ulang.",
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFB71C1C),
                    icon = Icons.Default.Error
                )
            } else if (!state.isPro()) {
                TryProBanner(onClick = onSubscription)
            }
        }

        WelcomeCard(
            username = username,
            plan = subState.subscriptionState?.plan ?: "Free",
            validUntil = subState.subscriptionState?.proUntil,
            onPlanClick = onSubscription
        )

        if (isOwner) {
            HomeSectionTitle(title = "Kelola User, Departemen & Perusahaan")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuCard(
                        menu = HomeMenu("Kelola\nUser", R.drawable.ic_profile3),
                        modifier = Modifier.weight(1f),
                        onClick = { onManageUsers() }
                    )
                    MenuCard(
                        menu = HomeMenu("Kelola\nDepartemen", R.drawable.auditdept),
                        modifier = Modifier.weight(1f),
                        onClick = { onManageDepartments() }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MenuCard(
                        menu = HomeMenu("Kelola\nPerusahaan", iconRes = R.drawable.ic_company),
                        modifier = Modifier.weight(1f),
                        onClick = { onManageCompany() }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        HomeSectionTitle(title = "Audit")

        SummaryStatsSection(
            totalKategori = uiState.totalKategori,
            totalPertanyaan = uiState.totalPertanyaan,
            totalAudit = uiState.totalAudit
        )

        MainMenuSection(menus = menus) { menuTitle ->
            when (menuTitle) {
                "Kategori &\nPertanyaan" -> onKategoriPertanyaan()
                "Pemetaan\nDepartemen" -> onPemetaanDepartemen()
                "Audit" -> {
                    val sub = subState.subscriptionState
                    if (sub?.isTrial() == true && uiState.totalAudit.toIntOrNull() ?: 0 >= 1) {
                        showGatingDialog = true
                    } else {
                        onAudit(-1)
                    }
                }
                "Hasil Audit" -> onHasilAudit()
            }
        }

        RecentActivitySection(activities = uiState.recentActivities) { activityId ->
            onAudit(activityId)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (showGatingDialog) {
        AlertDialog(
            onDismissRequest = { showGatingDialog = false },
            title = { Text("Limit Dokumen Trial Tercapai", fontWeight = FontWeight.Bold) },
            text = { Text("Masa Trial hanya dapat membuat maksimal 1 dokumen audit. Silakan verifikasi email owner atau upgrade ke paket PRO untuk akses tak terbatas.") },
            confirmButton = {
                Button(
                    onClick = {
                        showGatingDialog = false
                        onSubscription()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Upgrade PRO")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGatingDialog = false }) {
                    Text("Nanti Saja")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun HomeSectionTitle(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun WelcomeCard(username: String, plan: String, validUntil: String? = null, onPlanClick: () -> Unit = {}) {
    val primaryColor = Color(0xFFB63352)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Selamat Datang,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = when(plan.lowercase()) {
                            "pro" -> Color(0xFFFFD700)
                            "trial" -> Color(0xFF64B5F6)
                            else -> Color(0xFFE0E0E0)
                        },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onPlanClick() }
                    ) {
                        Text(
                            text = plan.uppercase(),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (plan.lowercase() == "pro") Color.Black else Color.White
                        )
                    }
                }
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
                if (plan.lowercase() == "pro" && validUntil != null) {
                    Text(
                        text = "Pro aktif hingga: ${UiUtils.formatDateIndo(validUntil)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Semoga aktivitas audit hari ini berjalan lancar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB63352)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile2),
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryStatsSection(totalKategori: String, totalPertanyaan: String, totalAudit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "TOTAL KATEGORI",
            value = totalKategori,
            icon = Icons.Default.Category,
            modifier = Modifier.weight(1f),
            containerColor = Color(0xFFE3F2FD) // Soft Blue
        )
        StatCard(
            title = "TOTAL PERTANYAAN",
            value = totalPertanyaan,
            icon = Icons.Default.QuestionAnswer,
            modifier = Modifier.weight(1.2f),
            containerColor = Color(0xFFF1F8E9) // Soft Green
        )
        StatCard(
            title = "AUDIT",
            value = totalAudit,
            icon = Icons.Default.Assignment,
            modifier = Modifier.weight(0.8f),
            containerColor = Color(0xFFFFF3E0) // Soft Orange
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun MainMenuSection(menus: List<HomeMenu>, onMenuClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuCard(menu = menus[0], modifier = Modifier.weight(1f), onClick = onMenuClick)
            MenuCard(menu = menus[1], modifier = Modifier.weight(1f), onClick = onMenuClick)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuCard(menu = menus[2], modifier = Modifier.weight(1f), onClick = onMenuClick)
            MenuCard(menu = menus[3], modifier = Modifier.weight(1f), onClick = onMenuClick)
        }
    }
}

@Composable
fun MenuCard(menu: HomeMenu, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    Card(
        modifier = modifier
            .clickable { onClick(menu.title) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB63352).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (menu.iconVector != null) {
                    Icon(
                        imageVector = menu.iconVector,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFB63352)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = menu.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFB63352)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = menu.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color.Black,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<RecentActivityData>, onActivityClick: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas Terbaru",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB63352),
                modifier = Modifier.clickable { }
            )
        }
        
        if (activities.isEmpty()) {
            EmptyRecentActivity()
        } else {
            activities.forEach { activity ->
                ActivityItem(
                    title = activity.title, 
                    subtitle = activity.subtitle, 
                    status = activity.status, 
                    statusColor = if (activity.status == "Selesai" || activity.status == "Submitted") Color(0xFF4CAF50) else Color(0xFF2196F3),
                    onClick = { onActivityClick(activity.id) }
                )
            }
        }
    }
}

@Composable
fun EmptyRecentActivity() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada proses audit",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = "Proses audit yang dibuat akan muncul di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, status: String, statusColor: Color, onClick: () -> Unit) {
    val isFinished = status == "Selesai" || status == "Submitted"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFinished) Icons.Default.CheckCircle else Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = statusColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun VerificationBanner(
    isResending: Boolean,
    onResend: () -> Unit
) {
    InfoBanner(
        message = "Email belum diverifikasi. Silakan cek inbox Anda.",
        containerColor = Color(0xFFFFF3E0),
        contentColor = Color(0xFFE65100),
        icon = Icons.Default.Info,
        action = {
            TextButton(
                onClick = onResend,
                enabled = !isResending
            ) {
                if (isResending) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFE65100))
                } else {
                    Text("Kirim Ulang", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                }
            }
        }
    )
}

@Composable
fun InfoBanner(
    message: String,
    containerColor: Color,
    contentColor: Color,
    icon: ImageVector,
    action: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = contentColor)
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
            action?.invoke()
        }
    }
}

@Composable
fun TryProBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, Color(0xFFFFD700))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, null, tint = Color.Black)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Coba Fitur Pro!",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "Export PDF, Kirim Email, dan Foto tak terbatas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Black)
        }
    }
}
