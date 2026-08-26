package id.my.karyatra.audit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.karyatra.audit.data.DepartmentData
import id.my.karyatra.audit.data.MappingCategory
import id.my.karyatra.audit.data.viewmodel.AuditDepartmentViewModel
import id.my.karyatra.audit.ui.theme.Karyatra_AuditTheme
import id.my.karyatra.audit.component.Header

class AuditDepartemen : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Karyatra_AuditTheme {
                AuditDepartemenScreen(
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
fun AuditDepartemenScreen(
    viewModel: AuditDepartmentViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
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

    // Calculate stats
    val totalPertanyaan = remember(uiState.categories) {
        uiState.categories.sumOf { it.questions.size }
    }
    val totalDipilih = uiState.selectedQuestionIds.size

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.selectedDepartment != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                ) {
                    Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Button(
                            onClick = { viewModel.saveMapping() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simpan Pemetaan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = backColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Department Selector
            DepartmentSelector(
                departments = uiState.departments,
                selectedDepartment = uiState.selectedDepartment,
                onSelect = { viewModel.selectDepartment(it) }
            )

            if (uiState.selectedDepartment == null) {
                EmptyMappingState()
            } else {
                // Bulk Actions
                BulkActionsBar(
                    onSelectAll = { viewModel.toggleAll(true) },
                    onClearAll = { viewModel.toggleAll(false) }
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "$totalDipilih dari $totalPertanyaan pertanyaan dipilih",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(uiState.categories) { category ->
                            CategoryMappingCard(
                                category = category,
                                selectedQuestionIds = uiState.selectedQuestionIds,
                                onToggleQuestion = { viewModel.toggleQuestion(it) },
                                onToggleCategory = { select -> viewModel.toggleCategory(category.id, select) }
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }

                    if (uiState.isLoading && !uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = primaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DepartmentSelector(
    departments: List<DepartmentData>,
    selectedDepartment: DepartmentData?,
    onSelect: (DepartmentData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFFB63352)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (selectedDepartment != null) primaryColor.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pilih Departemen",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Box {
                OutlinedCard(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = if (selectedDepartment != null) primaryColor else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedDepartment?.name ?: "Pilih Departemen...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedDepartment != null) Color.Black else Color.Gray
                            )
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }

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
        }
    }
}

@Composable
fun BulkActionsBar(
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSelectAll,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(18.dp), tint = primaryColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pilih Semua", fontSize = 12.sp, color = primaryColor)
        }

        OutlinedButton(
            onClick = onClearAll,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bersihkan", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CategoryMappingCard(
    category: MappingCategory,
    selectedQuestionIds: Set<Int>,
    onToggleQuestion: (Int) -> Unit,
    onToggleCategory: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFFB63352)
    
    val allSelected = category.questions.all { it.id in selectedQuestionIds }
    val someSelected = category.questions.any { it.id in selectedQuestionIds }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, if (someSelected) primaryColor.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { onToggleCategory(it) },
                    colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (someSelected) primaryColor else Color.Black
                    )
                    Text(
                        text = "${category.questions.size} Pertanyaan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    category.questions.forEach { question ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleQuestion(question.id) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = question.id in selectedQuestionIds,
                                onCheckedChange = { onToggleQuestion(question.id) },
                                colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = question.question ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (question.id in selectedQuestionIds) Color.Black else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMappingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AssignmentInd,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Belum Ada Departemen Dipilih",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pilih departemen di atas untuk mulai memetakan pertanyaan audit yang sesuai.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
