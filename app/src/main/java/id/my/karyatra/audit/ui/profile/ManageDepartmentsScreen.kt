package id.my.karyatra.audit.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.karyatra.audit.R
import id.my.karyatra.audit.data.DepartmentData
import id.my.karyatra.audit.data.SessionManager
import id.my.karyatra.audit.data.viewmodel.DepartmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDepartmentsScreen(
    onBack: () -> Unit,
    viewModel: DepartmentViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUser = sessionManager.getUser()
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var departmentToEdit by remember { mutableStateOf<DepartmentData?>(null) }
    
    val primaryColor = Color(0xFFB63352)

    LaunchedEffect(Unit) {
        viewModel.fetchDepartments()
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
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Departemen")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (uiState.isLoading && uiState.departmentList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = primaryColor)
            } else if (uiState.departmentList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada departemen", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.departmentList) { department ->
                        DepartmentListItem(
                            department = department,
                            onEdit = { departmentToEdit = department },
                            onDelete = {
                                currentUser?.id?.let { viewModel.deleteDepartment(it, department.id) }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        DepartmentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                currentUser?.id?.let { viewModel.addDepartment(it, name) }
                showAddDialog = false
            }
        )
    }

    if (departmentToEdit != null) {
        DepartmentDialog(
            department = departmentToEdit,
            onDismiss = { departmentToEdit = null },
            onConfirm = { name ->
                currentUser?.id?.let { viewModel.updateDepartment(it, departmentToEdit!!.id, name) }
                departmentToEdit = null
            }
        )
    }
}

@Composable
fun DepartmentListItem(
    department: DepartmentData,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = department.name ?: "Tanpa Nama", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primaryColor)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun DepartmentDialog(
    department: DepartmentData? = null,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(department?.name ?: "") }
    val primaryColor = Color(0xFFB63352)
    val context = LocalContext.current

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header with Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = if (department == null) "Tambah Departemen" else "Edit Departemen",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            fontSize = 18.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    Surface(
                        onClick = onDismiss,
                        shape = CircleShape,
                        color = Color.LightGray.copy(alpha = 0.2f),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Departemen") },
//                        placeholder = { Text("Masukkan nama departemen") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        maxLines = 1,
                        leadingIcon = { 
                            Icon(
                                painter = painterResource(id = R.drawable.auditdept),
                                contentDescription = null, 
                                tint = primaryColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            ) 
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { 
                            if (name.isBlank()) {
                                Toast.makeText(context, "Nama departemen tidak boleh kosong", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onConfirm(name) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            if (department == null) "Tambah Departemen" else "Simpan Perubahan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
