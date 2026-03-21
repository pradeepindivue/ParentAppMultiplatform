package com.edmik.parentapp.presentation.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.*
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import parentappmultiplatform.composeapp.generated.resources.Res
import parentappmultiplatform.composeapp.generated.resources.login_bg_image

private val PrimaryBlue = Color(0xFF3D7EFF)
private val ButtonGray = Color(0xFFAEB5C5)
private val TextGray = Color(0xFF9E9E9E)

private const val DELAY_CARD    = 0
private const val DELAY_TITLE   = 80
private const val DELAY_FIELD1  = DELAY_TITLE  + 120  // 200
private const val DELAY_FIELD2  = DELAY_FIELD1 + 120  // 320
private const val DELAY_OPTIONS = DELAY_FIELD2 + 120  // 440
private const val DELAY_BUTTON  = DELAY_OPTIONS + 120 // 560

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

    var masterVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { masterVisible = true }

    LaunchedEffect(uiState) {
        if (uiState is LoginState.Success) onLoginSuccess()
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Image(
            painter = painterResource(Res.drawable.login_bg_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.fillMaxHeight(0.28f))

            var cardTriggered by remember { mutableStateOf(false) }
            LaunchedEffect(masterVisible) {
                if (masterVisible) {
                    delay(DELAY_CARD.toLong())
                    cardTriggered = true
                }
            }

            var screenHeight by remember { mutableStateOf(0f) }

            val cardTranslationY by animateFloatAsState(
                targetValue = if (cardTriggered) 0f else screenHeight,
                animationSpec = CardEntrySpring,
                label = "cardTranslationY"
            )
            val cardAlpha by animateFloatAsState(
                targetValue = if (cardTriggered) 1f else 0f,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "cardAlpha"
            )

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coords ->
                        if (screenHeight == 0f) {
                            screenHeight = coords.size.height.toFloat()
                        }
                    }
                    .graphicsLayer {
                        this.translationY = cardTranslationY
                        this.alpha = cardAlpha
                    },
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(40.dp))

                        StaggeredFadeSlide(masterVisible = masterVisible, delayMillis = DELAY_TITLE) {
                            Text(
                                text = "Welcome",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        var isStudentIdFocused by remember { mutableStateOf(false) }
                        StaggeredFadeSlide(masterVisible = masterVisible, delayMillis = DELAY_FIELD1) {
                            OutlinedTextField(
                                value = studentId,
                                onValueChange = { studentId = it },
                                label = { Text("Student Id", color = TextGray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isStudentIdFocused = it.isFocused }
                                    .focusScaleEffect(isStudentIdFocused),
                                singleLine = true,
                                enabled = uiState !is LoginState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = Color(0xFFDDDDDD)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        var isPasswordFocused by remember { mutableStateOf(false) }
                        StaggeredFadeSlide(masterVisible = masterVisible, delayMillis = DELAY_FIELD2) {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password", color = TextGray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { isPasswordFocused = it.isFocused }
                                    .focusScaleEffect(isPasswordFocused),
                                singleLine = true,
                                enabled = uiState !is LoginState.Loading,
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (passwordVisible)
                                    VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible)
                                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (passwordVisible)
                                                "Hide password" else "Show password",
                                            tint = TextGray
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = Color(0xFFDDDDDD)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        StaggeredFadeSlide(masterVisible = masterVisible, delayMillis = DELAY_OPTIONS) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    enabled = uiState !is LoginState.Loading,
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
                                TextButton(onClick = onForgotPasswordClick) {
                                    Text(
                                        text = "Forgot password?",
                                        color = PrimaryBlue,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        if (uiState is LoginState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = (uiState as LoginState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.errorShake(uiState)
                            )
                        }

                        if (viewModel.hasLoggedInBefore()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { /* TODO */ },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50.dp),
                                border = ButtonDefaults.outlinedButtonBorder
                            ) {
                                Text("\ud83d\udd10 Login with Biometric", color = PrimaryBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    val isLoginEnabled = studentId.isNotBlank() &&
                            password.isNotBlank() &&
                            uiState !is LoginState.Loading &&
                            uiState !is LoginState.RateLimited

                    StaggeredFadeSlide(masterVisible = masterVisible, delayMillis = DELAY_BUTTON) {
                        val loginInteractionSource = remember { MutableInteractionSource() }
                        Button(
                            onClick = { viewModel.onEvent(LoginEvent.OnLoginClicked(studentId, password)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .pressClickEffect(loginInteractionSource),
                            enabled = isLoginEnabled,
                            interactionSource = loginInteractionSource,
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLoginEnabled) PrimaryBlue else ButtonGray,
                                disabledContainerColor = ButtonGray
                            )
                        ) {
                            when {
                                uiState is LoginState.Loading -> CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                uiState is LoginState.RateLimited -> Text(
                                    "Try again in ${(uiState as LoginState.RateLimited).secondsLeft}s",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                else -> Text(
                                    "Log in",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
