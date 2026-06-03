package com.example.vibecheck_dev.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.vibecheck_dev.R
import com.example.vibecheck_dev.presentation.components.y2kBlinkEffect
import com.example.vibecheck_dev.presentation.components.y2kGlitchEffect
import com.example.vibecheck_dev.ui.theme.Y2KTypography
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var isLoginMode by remember { mutableStateOf(true) }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var localError by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            viewModel.resetState()
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .border(2.dp, Color.White, RectangleShape)
                .background(Color.DarkGray.copy(alpha = 0.2f))
                .padding(24.dp)
        ) {
            Text(
                text = if (isLoginMode) "ACCESS_DB.exe" else "NEW_NODE_REG.exe",
                style = Y2KTypography.titleLarge,
                color = Color.Magenta,
                modifier = Modifier.y2kGlitchEffect()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLoginMode) "Konek ke cloud buat buka fitur Photobooth & Filter Premium."
                else "Daftarin node lu ke jaringan cloud VibeCheck.",
                style = Y2KTypography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isLoginMode) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; localError = ""; viewModel.resetState() },
                    textStyle = Y2KTypography.bodyMedium.copy(color = Color.Cyan),
                    placeholder = { Text("USERNAME", style = Y2KTypography.bodyMedium, color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Magenta, unfocusedBorderColor = Color.White, cursorColor = Color.Cyan
                    ),
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; localError = ""; viewModel.resetState() },
                textStyle = Y2KTypography.bodyMedium.copy(color = Color.Cyan),
                placeholder = { Text("EMAIL", style = Y2KTypography.bodyMedium, color = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Magenta, unfocusedBorderColor = Color.White, cursorColor = Color.Cyan
                ),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; localError = ""; viewModel.resetState() },
                textStyle = Y2KTypography.bodyMedium.copy(color = Color.Cyan),
                placeholder = { Text("PASSWORD", style = Y2KTypography.bodyMedium, color = Color.Gray) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Magenta, unfocusedBorderColor = Color.White, cursorColor = Color.Cyan
                ),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; localError = ""; viewModel.resetState() },
                    textStyle = Y2KTypography.bodyMedium.copy(color = Color.Cyan),
                    placeholder = { Text("CONFIRM_PASSWORD", style = Y2KTypography.bodyMedium, color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Magenta, unfocusedBorderColor = Color.White, cursorColor = Color.Cyan
                    ),
                    shape = RectangleShape,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val displayError = localError.ifEmpty { (authState as? AuthState.Error)?.message ?: "" }
            if (displayError.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = displayError, color = Color.Red, style = Y2KTypography.bodySmall, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isLoading = authState is AuthState.Loading

            Button(
                onClick = {
                    if (isLoading) return@Button
                    if (!isLoginMode && password != confirmPassword) {
                        localError = "ERR: PASSWORD_MISMATCH"
                        return@Button
                    }
                    if (!isLoginMode && username.isBlank()) {
                        localError = "ERR: USERNAME_EMPTY"
                        return@Button
                    }
                    if (email.isNotBlank() && password.isNotBlank()) {
                        if (isLoginMode) viewModel.login(email, password) else viewModel.register(username, email, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isLoading) Color.DarkGray else Color.Magenta),
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().border(2.dp, Color.White, RectangleShape)
            ) {
                Text(
                    text = if (isLoading) "PROCESSING..." else if (isLoginMode) "INITIALIZE >>" else "CREATE_NODE >>",
                    color = Color.White,
                    style = Y2KTypography.bodyMedium,
                    modifier = if (isLoading) Modifier.padding(vertical = 8.dp).y2kBlinkEffect(300) else Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PEMBATAS Y2K ---
            Text(text = "- - - - OR - - - -", style = Y2KTypography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // --- TOMBOL GOOGLE CUMA LOGO KOTAK AJA ---
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        localError = "" // Reset error tiap kali klik
                        val credentialManager = CredentialManager.create(context)

                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("372157512376-o3ob70o1jffopl8hvqndnhj6mj999jl7.apps.googleusercontent.com")
                            .setAutoSelectEnabled(true)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        try {
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential

                            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                            }
                        } catch (e: Exception) {
                            localError = "G-AUTH ERR: ${e.message?.take(40)}..."
                            e.printStackTrace()
                        }
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RectangleShape,
                contentPadding = PaddingValues(0.dp), // Biar logonya beneran di tengah tanpa jarak default
                modifier = Modifier
                    .size(48.dp) // Ukuran tombol jadi kotak presisi (lebar & tinggi sama)
                    .border(1.dp, Color.White, RectangleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp), // Ukuran logo di dalam kotak
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isLoginMode) "Belum punya akses? [ REGISTER ]" else "Udah terdaftar? [ LOGIN ]",
                style = Y2KTypography.bodySmall,
                color = Color.Yellow,
                modifier = Modifier.clickable {
                    isLoginMode = !isLoginMode
                    localError = ""
                    viewModel.resetState()
                }
            )
        }
    }
}