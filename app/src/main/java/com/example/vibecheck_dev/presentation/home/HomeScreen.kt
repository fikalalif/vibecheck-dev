package com.example.vibecheck_dev.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.vibecheck_dev.presentation.components.y2kBlinkEffect
import com.example.vibecheck_dev.ui.theme.Y2KTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToRemote: () -> Unit,
    onNavigateToProfile: () -> Unit, // Tambahan
    onLogout: () -> Unit             // Tambahan
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.8f),
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF050505),
                drawerShape = RectangleShape,
                modifier = Modifier.width(300.dp).border(2.dp, Color.Cyan, RectangleShape)
            ) {
                Column(modifier = Modifier.padding(24.dp).fillMaxHeight()) {
                    Text("SYS_MENU.exe", style = Y2KTypography.titleLarge, color = Color.Magenta)
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.Cyan, thickness = 2.dp)
                    Spacer(modifier = Modifier.height(32.dp))

                    DrawerMenuItem(title = "PROFILE.cfg") {
                        coroutineScope.launch { drawerState.close() }
                        onNavigateToProfile() // Langsung ke layar Profile beneran!
                    }
                    DrawerMenuItem(title = "THEME.ini") {
                        coroutineScope.launch { drawerState.close() }
                        showThemeDialog = true
                    }
                    DrawerMenuItem(title = "ABOUT.txt") {
                        coroutineScope.launch { drawerState.close() }
                        showAboutDialog = true
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier.fillMaxWidth().clickable {
                            coroutineScope.launch { drawerState.close() }
                            showLogoutDialog = true
                        }.background(Color.Red.copy(alpha = 0.2f)).border(2.dp, Color.Red, RectangleShape).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("[ SYSTEM_LOGOUT ]", color = Color.White, style = Y2KTypography.bodyMedium)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("VIBECHECK_OS", style = Y2KTypography.titleMedium, color = Color.Green) },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.Green)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                    modifier = Modifier.drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height - strokeWidth / 2
                        drawLine(Color.Green, Offset(0f, y), Offset(size.width, y), strokeWidth)
                    }
                )
            },
            containerColor = Color.Black
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SELECT ROLE", style = Y2KTypography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pilih perangkat ini mau jadi lensa kamera atau remote kontrol.", style = Y2KTypography.bodyMedium, color = Color.LightGray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(48.dp))

                RoleButton("HOST (CAMERA)", "Aktifin AI & Pancarin P2P", Icons.Default.Camera, Color.Magenta, onNavigateToCamera)
                Spacer(modifier = Modifier.height(24.dp))
                RoleButton("REMOTE (CTRL)", "Konek ke Host & Jepret", Icons.Default.PhoneAndroid, Color.Cyan, onNavigateToRemote)
            }
        }
    }

    if (showThemeDialog) {
        Y2KDialogWrapper(title = "SELECT_THEME", borderColor = Color.Magenta, onDismiss = { showThemeDialog = false }) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ThemeOptionItem("Y2K BRIGHT NEON", true)
                ThemeOptionItem("MATRIX TERMINAL", false)
                ThemeOptionItem("UBUNTU DARK", false)
                Spacer(modifier = Modifier.height(12.dp))
                Y2KDialogButton("APPLY", Color.Magenta) { showThemeDialog = false }
            }
        }
    }

    if (showAboutDialog) {
        Y2KDialogWrapper(title = "ABOUT_SYS", borderColor = Color.Green, onDismiss = { showAboutDialog = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("VIBECHECK_OS v1.0.0", color = Color.Green, style = Y2KTypography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Engine: Jetpack Compose\nProtocol: Wi-Fi P2P Direct\nVision: ML Kit Pose Detecion", color = Color.LightGray, style = Y2KTypography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Coded with blood, sweat, and pure logic.", color = Color.White, style = Y2KTypography.bodyMedium, modifier = Modifier.y2kBlinkEffect(1000))
                Spacer(modifier = Modifier.height(24.dp))
                Y2KDialogButton("CLOSE", Color.Green) { showAboutDialog = false }
            }
        }
    }

    if (showLogoutDialog) {
        Y2KDialogWrapper(title = "WARNING!", borderColor = Color.Red, onDismiss = { showLogoutDialog = false }) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TERMINATE SESSION?", color = Color.Red, style = Y2KTypography.titleLarge, modifier = Modifier.y2kBlinkEffect(500))
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.weight(1f)) { Y2KDialogButton("NO / ABORT", Color.DarkGray) { showLogoutDialog = false } }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        Y2KDialogButton("YES / KILL", Color.Red) {
                            showLogoutDialog = false
                            onLogout() // Eksekusi fungsi logout!
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoleButton(title: String, description: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clickable { onClick() }.background(Color.DarkGray.copy(alpha = 0.3f)).border(2.dp, color, RectangleShape).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = Y2KTypography.bodyLarge, color = color)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = Y2KTypography.bodySmall, color = Color.White)
            }
        }
    }
}

@Composable
fun DrawerMenuItem(title: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() }.background(Color(0xFF001A1A)).border(2.dp, Color.Cyan, RectangleShape).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("[", color = Color.White, style = Y2KTypography.bodyMedium)
            Text(" RUN ", color = Color.Magenta, style = Y2KTypography.bodyMedium, modifier = Modifier.y2kBlinkEffect(800))
            Text("] ", color = Color.White, style = Y2KTypography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = Y2KTypography.bodyLarge, color = Color.Cyan)
        }
    }
}

@Composable
fun Y2KDialogWrapper(title: String, borderColor: Color, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().background(Color.Black).border(3.dp, borderColor, RectangleShape).padding(2.dp).border(1.dp, Color.DarkGray, RectangleShape).padding(20.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("[$title]", color = borderColor, style = Y2KTypography.titleMedium)
                    Icon(Icons.Default.Close, "Close", tint = Color.Gray, modifier = Modifier.clickable { onDismiss() })
                }
                HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
fun Y2KDialogButton(text: String, color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().clickable { onClick() }.background(color.copy(alpha = 0.2f)).border(2.dp, color, RectangleShape).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(text, color = if(color == Color.DarkGray) Color.LightGray else color, style = Y2KTypography.bodyMedium)
    }
}

@Composable
fun ThemeOptionItem(name: String, isActive: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().background(if(isActive) Color.DarkGray else Color.Transparent).border(1.dp, if(isActive) Color.White else Color.DarkGray, RectangleShape).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(16.dp).background(if(isActive) Color.Magenta else Color.Black).border(1.dp, Color.White, RectangleShape))
        Spacer(modifier = Modifier.width(16.dp))
        Text(name, color = if(isActive) Color.White else Color.Gray, style = Y2KTypography.bodyMedium)
    }
}