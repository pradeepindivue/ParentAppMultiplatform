package com.edmik.parentapp.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import parentappmultiplatform.composeapp.generated.resources.Res
import parentappmultiplatform.composeapp.generated.resources.login_bg_image

private val PrimaryBlue = Color(0xFF3D7EFF)
private val ButtonGray = Color(0xFFAEB5C5)
private val TextGray = Color(0xFF9E9E9E)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var studentId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image — covers the full screen
        Image(
            painter = painterResource(Res.drawable.login_bg_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // White card that slides up from the bottom ~60% of screen
        Column(modifier = Modifier.fillMaxSize()) {
            // Transparent spacer to show background at top
            Spacer(modifier = Modifier.fillMaxHeight(0.28f))

            // White card with rounded top corners only
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "Welcome",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // Student ID field
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        placeholder = { Text("Student Id", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = uiState !is LoginUiState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = uiState !is LoginUiState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextGray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Remember Me checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            enabled = uiState !is LoginUiState.Loading,
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryBlue,
                                uncheckedColor = TextGray
                            )
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Remember Me",
                            fontSize = 14.sp,
                            color = Color(0xFF555555)
                        )

                        // Forgot password — right-aligned
                        TextButton(onClick = onForgotPasswordClick) {
                            Text(
                                text = "Forgot password?",
                                color = PrimaryBlue,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Error / RateLimit message
                    if (uiState is LoginUiState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as LoginUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    // Biometric login (if previously logged in)
                    if (viewModel.hasLoggedInBefore()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { /* TODO: Launch Biometric Auth */ },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text("🔐 Login with Biometric", color = PrimaryBlue)
                        }
                    }

                    // Push Login button to bottom
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    val isLoginEnabled = studentId.isNotBlank() && password.isNotBlank() && 
                        uiState !is LoginUiState.Loading && uiState !is LoginUiState.RateLimited

                    // Login button
                    Button(
                        onClick = { viewModel.login(studentId, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = isLoginEnabled,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLoginEnabled) PrimaryBlue else ButtonGray,
                            disabledContainerColor = ButtonGray
                        )
                    ) {
                        if (uiState is LoginUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else if (uiState is LoginUiState.RateLimited) {
                            Text(
                                "Try again in ${(uiState as LoginUiState.RateLimited).secondsLeft}s",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text("Log in", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
