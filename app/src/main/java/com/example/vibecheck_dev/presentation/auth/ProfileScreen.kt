package com.example.vibecheck_dev.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vibecheck_dev.presentation.components.y2kBlinkEffect
import com.example.vibecheck_dev.presentation.components.y2kGlitchEffect
import com.example.vibecheck_dev.ui.theme.Y2KTypography
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var newUsername by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") } // <-- INI DIA
    var newPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    var showSuccessMsg by remember { mutableStateOf(false) }

    val currentUsername by viewModel.currentUsername.collectAsState()
    val currentEmail = viewModel.currentEmail

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            showSuccessMsg = true
            viewModel.resetState()
            newUsername = ""
            oldPassword = ""
            newPassword = ""
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.border(2.dp, Color.White, RectangleShape).background(Color.DarkGray.copy(alpha = 0.2f)).padding(24.dp)
        ) {
            Text("USER_CONFIG.sys", style = Y2KTypography.titleLarge, color = Color.Cyan, modifier = Modifier.y2kGlitchEffect())

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth().background(Color.Black).border(1.dp, Color.DarkGray, RectangleShape).padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CURRENT NODE ID:", color = Color.Gray, style = Y2KTypography.bodySmall)
                Text(currentUsername.ifEmpty { "UNKNOWN" }, color = Color.White, style = Y2KTypography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Text("LINKED EMAIL:", color = Color.Gray, style = Y2KTypography.bodySmall)
                Text(currentEmail, color = Color.White, style = Y2KTypography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // UBAH USERNAME
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it; showSuccessMsg = false; viewModel.resetState() },
                textStyle = Y2KTypography.bodyMedium.copy(color = Color.Yellow),
                placeholder = { Text("NEW_USERNAME", style = Y2KTypography.bodyMedium, color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Cyan, unfocusedBorderColor = Color.White, cursorColor = Color.Yellow),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { if (newUsername.isNotBlank()) viewModel.updateUsername(newUsername) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(1.dp, Color.Cyan, RectangleShape)
            ) { Text("UPDATE USERNAME >>", color = Color.Cyan, style = Y2KTypography.bodySmall) }

            Spacer(modifier = Modifier.height(24.dp))

            // UBAH PASSWORD
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it; showSuccessMsg = false; viewModel.resetState() },
                textStyle = Y2KTypography.bodyMedium.copy(color = Color.White),
                placeholder = { Text("OLD_PASSWORD", style = Y2KTypography.bodyMedium, color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Magenta, unfocusedBorderColor = Color.White, cursorColor = Color.Magenta),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it; showSuccessMsg = false; viewModel.resetState() },
                textStyle = Y2KTypography.bodyMedium.copy(color = Color.Magenta),
                placeholder = { Text("NEW_PASSWORD", style = Y2KTypography.bodyMedium, color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Magenta, unfocusedBorderColor = Color.White, cursorColor = Color.Magenta),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { if (oldPassword.isNotBlank() && newPassword.isNotBlank()) viewModel.updatePassword(oldPassword, newPassword) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).border(1.dp, Color.Magenta, RectangleShape)
            ) { Text("UPDATE PASSWORD >>", color = Color.Magenta, style = Y2KTypography.bodySmall) }

            Spacer(modifier = Modifier.height(16.dp))

            val isLoading = authState is AuthState.Loading
            if (isLoading) Text("CONNECTING TO SERVER...", color = Color.Yellow, style = Y2KTypography.bodySmall, modifier = Modifier.y2kBlinkEffect(300))
            else if (showSuccessMsg) Text(">> UPDATE SUCCESSFUL <<", color = Color.Green, style = Y2KTypography.bodySmall)
            else if (authState is AuthState.Error) Text((authState as AuthState.Error).message, color = Color.Red, style = Y2KTypography.bodySmall, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { viewModel.logout(onLogoutComplete = { onLogoutSuccess() }) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().border(2.dp, Color.White, RectangleShape)
            ) { Text("TERMINATE_SESSION (LOGOUT)", color = Color.White, style = Y2KTypography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp)) }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color.White, RectangleShape)
            ) { Text("< BACK_TO_SYSTEM", color = Color.White, style = Y2KTypography.bodySmall) }
        }
    }
}