package id.my.karyatra.audit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import id.my.karyatra.audit.ui.theme.Karyatra_AuditTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import id.my.karyatra.audit.AuditPertanyaan
import id.my.karyatra.audit.AuditProses
import id.my.karyatra.audit.ui.theme.Karyatra_AuditTheme
import kotlin.jvm.java

class AuditHome : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Karyatra_AuditTheme() {
                HomeScreen()
            }
        }
    }
}

data class HomeMenu(
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String = "Master Data, Audit dan Laporan.",
    onMenuClick: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current

    val menus = listOf(
        HomeMenu("Kategori &\nPertanyaan", Icons.Default.QuestionAnswer),
        HomeMenu("Pemetaan\nDepartemen", Icons.Default.Business),
        HomeMenu("Audit", Icons.Default.Assignment),
        HomeMenu("Hasil Audit", Icons.Default.AssignmentTurnedIn),)


    Scaffold(
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Text(
                text = "Audit Matahati",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HorizontalDivider()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(menus) { menu ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable {

                                when (menu.title) {

                                    "Kategori &\nPertanyaan" -> {
                                        context.startActivity(
                                            Intent(context, AuditPertanyaan::class.java)
                                        )
                                    }

                                    "Pemetaan\nDepartemen" -> {
                                        context.startActivity(
                                            Intent(context, AuditDepartemen::class.java)
                                        )
                                    }

                                    "Audit" -> {
                                        context.startActivity(
                                            Intent(context, AuditProses::class.java)
                                        )
                                    }

                                    "Hasil Audit" -> {
                                        context.startActivity(
                                            Intent(context, AuditHasil::class.java)
                                        )
                                    }
                                }

                                onMenuClick(menu.title)
                            },
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        )
                    ) {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                Icon(
                                    imageVector = menu.icon,
                                    contentDescription = menu.title,
                                    modifier = Modifier.size(48.dp)
                                )

                                Text(
                                    text = menu.title,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLogout()
                    }
            ) {

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout"
                    )

                    Text(
                        text = "Logout",
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}