package com.edmik.parentapp.ui.login

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import parentappmultiplatform.composeapp.generated.resources.Res
import parentappmultiplatform.composeapp.generated.resources.login_bg_image

private val FPPrimaryBlue = Color(0xFF3D7EFF)
private val FPButtonGray = Color(0xFFAEB5C5)
private val FPTextGray = Color(0xFF9E9E9E)

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val currentStep by viewModel.step.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Observe per-step inputs to reactively enable/color the action button
    val stagedIdentifier by viewModel.stagedIdentifier.collectAsStateWithLifecycle()
    val stagedOtp by viewModel.stagedOtp.collectAsStateWithLifecycle()
    val stagedPassword by viewModel.stagedPassword.collectAsStateWithLifecycle()
    val stagedConfirmPassword by viewModel.stagedConfirmPassword.collectAsStateWithLifecycle()

    val isInputFilled = when (currentStep) {
        is ForgotPasswordStep.EnterContact -> stagedIdentifier.isNotBlank()
        is ForgotPasswordStep.EnterOtp -> stagedOtp.length == 6
        is ForgotPasswordStep.ResetPassword ->
            stagedPassword.isNotBlank() && stagedPassword == stagedConfirmPassword
        is ForgotPasswordStep.Success -> true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(Res.drawable.login_bg_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Transparent top — shows background
            Spacer(modifier = Modifier.fillMaxHeight(0.28f))

            // White card — same style as login screen
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

                    // Title
                    Text(
                        text = "Forgot Password?",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = FPPrimaryBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle
                    Text(
                        text = when (currentStep) {
                            is ForgotPasswordStep.EnterContact ->
                                "Get your Username and Password on your registered email!"
                            is ForgotPasswordStep.EnterOtp ->
                                "Enter the 6-digit OTP sent to your registered email."
                            is ForgotPasswordStep.ResetPassword ->
                                "Enter your new password below."
                            is ForgotPasswordStep.Success ->
                                "Your password has been reset successfully!"
                        },
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Step content
                    when (currentStep) {
                        is ForgotPasswordStep.EnterContact ->
                            FPStep1EnterContact(viewModel, isLoading)
                        is ForgotPasswordStep.EnterOtp ->
                            FPStep2EnterOtp(viewModel, isLoading)
                        is ForgotPasswordStep.ResetPassword ->
                            FPStep3ResetPassword(viewModel, isLoading)
                        is ForgotPasswordStep.Success ->
                            FPSuccess()
                    }

                    // Push button to bottom
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    // Action button
                    val buttonLabel = when (currentStep) {
                        is ForgotPasswordStep.EnterContact -> "Get Password"
                        is ForgotPasswordStep.EnterOtp -> "Verify OTP"
                        is ForgotPasswordStep.ResetPassword -> "Reset Password"
                        is ForgotPasswordStep.Success -> "Return to Login"
                    }

                    val buttonActive = isInputFilled && !isLoading
                    val buttonColor = if (buttonActive) FPPrimaryBlue else FPButtonGray

                    Button(
                        onClick = {
                            if (currentStep is ForgotPasswordStep.Success) onBack()
                            else viewModel.onActionButtonClicked()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = buttonActive,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            disabledContainerColor = FPButtonGray
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(buttonLabel, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Return to Login text link
                    if (currentStep !is ForgotPasswordStep.Success) {
                        TextButton(onClick = onBack) {
                            Text(
                                text = "Return to Login",
                                color = FPPrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// ─── Step sub-composables ────────────────────────────────────────────────────

@Composable
private fun FPStep1EnterContact(viewModel: ForgotPasswordViewModel, isLoading: Boolean) {
    val identifier by viewModel.stagedIdentifier.collectAsStateWithLifecycle()

    OutlinedTextField(
        value = identifier,
        onValueChange = { viewModel.setContactIdentifier(it) },
        placeholder = { Text("Student Id", color = FPTextGray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FPPrimaryBlue,
            unfocusedBorderColor = Color(0xFFDDDDDD)
        )
    )
}

@Composable
private fun FPStep2EnterOtp(viewModel: ForgotPasswordViewModel, isLoading: Boolean) {
    val otp by viewModel.stagedOtp.collectAsStateWithLifecycle()

    OutlinedTextField(
        value = otp,
        onValueChange = { if (it.length <= 6) viewModel.setOtpInput(it) },
        placeholder = { Text("6-Digit OTP", color = FPTextGray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FPPrimaryBlue,
            unfocusedBorderColor = Color(0xFFDDDDDD)
        )
    )
}

@Composable
private fun FPStep3ResetPassword(viewModel: ForgotPasswordViewModel, isLoading: Boolean) {
    val password by viewModel.stagedPassword.collectAsStateWithLifecycle()
    val confirmPassword by viewModel.stagedConfirmPassword.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = { viewModel.setNewPassword(it, confirmPassword) },
        placeholder = { Text("New Password", color = FPTextGray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = FPTextGray
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FPPrimaryBlue,
            unfocusedBorderColor = Color(0xFFDDDDDD)
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { viewModel.setNewPassword(password, it) },
        placeholder = { Text("Confirm Password", color = FPTextGray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isLoading,
        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                Icon(
                    if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = FPTextGray
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FPPrimaryBlue,
            unfocusedBorderColor = Color(0xFFDDDDDD)
        )
    )

    if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
        Spacer(modifier = Modifier.height(8.dp))
        Text("Passwords do not match", color = Color.Red, fontSize = 12.sp)
    }
}

@Composable
private fun FPSuccess() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "✅ Password reset successfully!",
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}
