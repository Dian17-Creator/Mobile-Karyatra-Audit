package id.my.karyatra.audit

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.my.karyatra.audit.data.*
import id.my.karyatra.audit.data.viewmodel.AuditExecutionUiState
import id.my.karyatra.audit.data.viewmodel.AuditExecutionViewModel
import id.my.karyatra.audit.ui.theme.Karyatra_AuditTheme
import id.my.karyatra.audit.component.Header
import kotlinx.coroutines.launch
import java.io.File

class AuditProses : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Karyatra_AuditTheme {
                AuditExecutionScreen(
                    auditId = intent.getIntExtra("audit_id", -1),
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditExecutionScreen(
    auditId: Int = -1,
    viewModel: AuditExecutionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = remember { sessionManager.getUser()?.id ?: 1 }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.initialize(auditId)
    }

    val primaryColor = Color(0xFFB63352)
    val backColor = MaterialTheme.colorScheme.background

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Auto-scroll logic
    LaunchedEffect(uiState.highlightedQuestionId) {
        uiState.highlightedQuestionId?.let { id ->
            val categories = uiState.auditDetail?.categories ?: emptyList()
            var totalIndex = 1 // Offset for Info Card
            var found = false
            
            for (category in categories) {
                totalIndex++ // Category Header
                val qIndex = category.questions.indexOfFirst { it.id == id }
                if (qIndex != -1) {
                    totalIndex += qIndex
                    found = true
                    break
                }
                if (uiState.expandedCategoryIds.contains(category.id)) {
                    totalIndex += category.questions.size
                }
            }
            
            if (found) {
                coroutineScope.launch {
                    listState.animateScrollToItem(totalIndex)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.auditDetail == null) {
                StartAuditSection(
                    departments = uiState.departments,
                    selectedDepartment = uiState.selectedDepartment,
                    existingDraftId = uiState.existingDraftId,
                    isLoading = uiState.isLoading,
                    onSelect = { viewModel.selectDepartment(it) },
                    onStart = { viewModel.startAudit(userId) }
                )
            } else {
                AuditExecutionContent(
                    uiState = uiState,
                    listState = listState,
                    viewModel = viewModel
                )
            }
        }

        if (uiState.isLoading && uiState.auditDetail == null) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp), color = primaryColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartAuditSection(
    departments: List<DepartmentData>,
    selectedDepartment: DepartmentData?,
    existingDraftId: Int?,
    isLoading: Boolean,
    onSelect: (DepartmentData) -> Unit,
    onStart: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.LightGray
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Mulai Audit Baru",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        
        Text(
            text = "Pilih departemen untuk memulai proses audit.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Department Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedCard(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (selectedDepartment != null) primaryColor.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = if (selectedDepartment != null) primaryColor else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = selectedDepartment?.name ?: "Pilih Departemen",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedDepartment != null) Color.Black else Color.Gray
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }
            
            Text(
                text = "Departemen",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 4.dp)
                    .offset(y = (-8).dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color.White)
            ) {
                departments.forEach { dept ->
                    DropdownMenuItem(
                        text = { Text(dept.name ?: "") },
                        onClick = {
                            onSelect(dept)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Business, contentDescription = null, tint = primaryColor)
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            enabled = selectedDepartment != null && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = if (existingDraftId != null) "Lanjutkan Draft" else "Mulai Audit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun AuditExecutionContent(
    uiState: AuditExecutionUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    viewModel: AuditExecutionViewModel
) {
    val detail = uiState.auditDetail ?: return
    val audit = detail.audit
    val primaryColor = Color(0xFFB63352)
    var showSubmitDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = audit.departmentName ?: "", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(status = audit.status ?: "", isDraft = audit.status == "Draft")
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Progres: ${audit.percentage}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (audit.percentage / 100f).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = primaryColor,
                            trackColor = primaryColor.copy(alpha = 0.1f)
                        )
                    }
                }
            }

            detail.categories.forEach { category ->
                val isExpanded = uiState.expandedCategoryIds.contains(category.id)
                item(key = "cat_${category.id}") {
                    CategoryHeader(
                        category = category,
                        isExpanded = isExpanded,
                        onToggle = { viewModel.toggleCategory(category.id) }
                    )
                }

                if (isExpanded) {
                    itemsIndexed(category.questions, key = { _, q -> "q_${q.id}" }) { index, question ->
                        QuestionExecutionCard(
                            question = question,
                            index = index + 1,
                            isSaving = uiState.isSaving,
                            isHighlighted = uiState.highlightedQuestionId == question.id,
                            onSave = { score, note -> viewModel.onAnswerChanged(question.id, score, note) }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        // Bottom Action Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { /* Handle Save Draft */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryColor)
                ) {
                    Text("Simpan Draft", color = primaryColor, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showSubmitDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = audit.percentage >= 100.0
                ) {
                    Text("Submit Audit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showSubmitDialog) {
        SubmitAuditDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showSubmitDialog = false },
            onConfirm = { name, signFile ->
                viewModel.submitAudit(name, signFile)
                showSubmitDialog = false
            }
        )
    }
}

@Composable
fun StatusChip(
    status: String,
    isDraft: Boolean = false,
    isSolid: Boolean = false
) {
    val color = when {
        status.equals("Draft", ignoreCase = true) || isDraft -> Color(0xFF2196F3)
        status.equals("Submitted", ignoreCase = true) || status.equals("Selesai", ignoreCase = true) -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
    Surface(
        color = if (isSolid) color else color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isSolid) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isSolid) Color.White else color
            )
        }
    }
}

@Composable
fun CategoryHeader(
    category: AuditCategoryDetail,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        color = if (isExpanded) primaryColor.copy(alpha = 0.05f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name ?: "",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isExpanded) primaryColor else Color.Black
                )
                Text(
                    text = "${category.questions.size} Pertanyaan",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = if (isExpanded) primaryColor else Color.Gray
            )
        }
    }
}

@Composable
fun QuestionExecutionCard(
    question: AuditQuestionDetail,
    index: Int,
    isSaving: Boolean,
    isHighlighted: Boolean,
    onSave: (String?, String?) -> Unit
) {
    val primaryColor = Color(0xFFB63352)
    var note by remember(question.id) { mutableStateOf(question.response?.remark ?: "") }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isHighlighted) 2.dp else 0.dp,
                color = if (isHighlighted) primaryColor else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = primaryColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = index.toString(), style = MaterialTheme.typography.labelSmall, color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = question.question ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Score Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("YES", "NO", "N/A").forEach { score ->
                    val isSelected = when(score) {
                        "YES" -> question.response?.score == 1.0 && question.response?.isNa == false
                        "NO" -> question.response?.score == 0.0 && question.response?.isNa == false
                        "N/A" -> question.response?.isNa == true
                        else -> false
                    }
                    
                    ScoreChip(
                        score = score,
                        isSelected = isSelected,
                        isSaving = isSaving,
                        onClick = { onSave(if(score == "YES") "1" else if(score == "NO") "0" else "N/A", note.ifBlank { null }) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan Temuan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = {
                    if (note != (question.response?.remark ?: "")) {
                        IconButton(onClick = { onSave(if(question.response?.isNa == true) "N/A" else question.response?.score?.toInt()?.toString(), note.ifBlank { null }) }) {
                            Icon(Icons.Default.Save, null, tint = primaryColor)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ScoreChip(
    score: String,
    isSelected: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit
) {
    val color = when (score) {
        "YES" -> Color(0xFF4CAF50)
        "NO" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Surface(
        modifier = Modifier
            .width(90.dp)
            .height(40.dp)
            .clickable(enabled = !isSaving) { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) color else Color.LightGray.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isSaving && isSelected) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(
                    text = score,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.White else Color.Gray
                )
            }
        }
    }
}

@Composable
fun SubmitAuditDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, File) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Hasil Audit") },
        text = { Text("Pastikan semua data sudah benar sebelum mengirim.") },
        confirmButton = {
            Button(onClick = { /* onConfirm implementation */ }) {
                Text("Kirim Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
