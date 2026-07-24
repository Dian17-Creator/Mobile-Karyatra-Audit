package id.my.karyatra.audit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import id.my.karyatra.audit.ui.theme.Karyatra_AuditTheme

class AuditProses : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Karyatra_AuditTheme() {

                val primaryColor = Color(0xFFB63352)
                val backColor = Color(0xFFFFF5F5)

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Audit Proses",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        startActivity(
                                            Intent(
                                                this@AuditProses,
                                                AuditHome::class.java
                                            )
                                        )
                                        finish()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = primaryColor,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White
                            )
                        )
                    }
                ) { innerPadding ->

                    AuditProsesScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backColor)
                            .padding(innerPadding)
                    )

                }
            }
        }
    }
}

@Composable
fun AuditProsesScreen(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "Halaman Audit Proses",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = Color(0xFFB63352)
        )
    }
}