package com.edmik.parentapp.presentation.screens.forgot_password

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var identifier by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.login_bg_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.fillMaxHeight(0.28f))

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
                        text = "Forgot Password?",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = FPPrimaryBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Get your Username and Password on your registered email!",
                        fontSize = 14.sp,
                        color = Color(0xFF555555),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { identifier = it },
                        placeholder = { Text("Student Id", color = FPTextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = uiState !is ForgotPasswordState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FPPrimaryBlue,
                            unfocusedBorderColor = Color(0xFFDDDDDD)
                        )
                    )

                    if (uiState is ForgotPasswordState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as ForgotPasswordState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    if (uiState is ForgotPasswordState.Success) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (uiState as ForgotPasswordState.Success).message,
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(24.dp))

                    val buttonActive = identifier.isNotBlank() && uiState !is ForgotPasswordState.Loading
                    
                    Button(
                        onClick = {
                            if (uiState is ForgotPasswordState.Success) onBack()
                            else viewModel.onEvent(ForgotPasswordEvent.OnSubmitClicked(identifier))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = buttonActive,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (buttonActive) FPPrimaryBlue else FPButtonGray,
                            disabledContainerColor = FPButtonGray
                        )
                    ) {
                        if (uiState is ForgotPasswordState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.`White`,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                if (uiState is ForgotPasswordState.Success) "Return to Login" else "Get Password",
                                color = Color.`White`,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState !is ForgotPasswordState.Success) {
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
