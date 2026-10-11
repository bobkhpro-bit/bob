package com.example.bobstore.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bobstore.ui.components.ProductImageThumbnail
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val currentLang by viewModel.currentLanguage.collectAsState()
    var isLoginTab by remember { mutableStateOf(true) }

    // Login Form State - completely clean, no pre-saved hints
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }

    // Error Alert Dialog & Effect State
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorDialogTitle by remember { mutableStateOf("") }
    var errorDialogMessage by remember { mutableStateOf("") }
    val shakeOffset = remember { Animatable(0f) }
    val errorGlow = remember { Animatable(0f) }

    fun triggerErrorEffect(title: String, message: String) {
        errorDialogTitle = title
        errorDialogMessage = message
        showErrorDialog = true

        // Haptic Feedback for physical error indication
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {}

        // Visual Shake & Red Glow Animation for error effect
        coroutineScope.launch {
            launch {
                errorGlow.snapTo(1f)
                errorGlow.animateTo(0f, animationSpec = tween(1200))
            }
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -10f at 250
                    10f at 300
                    -5f at 350
                    0f at 400
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Switcher in top corner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SlateElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.clickable { viewModel.toggleLanguage() }
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "English" else "العربية",
                        color = ChampagneGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Emblem
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(SlateElevated, CharcoalSurface)))
                    .border(1.5.dp, ChampagneGold, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                ProductImageThumbnail(
                    imageResName = "ic_bob_store_logo",
                    modifier = Modifier.size(62.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = Strings.appTitle(currentLang),
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = Strings.luxuryTagline(currentLang),
                color = ChampagneGoldLight,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card with Shake and Red Glow Effect on Error
            val cardBorderColor = if (errorGlow.value > 0.05f) DangerRed else SlateBorder
            val cardBorderWidth = if (errorGlow.value > 0.05f) 2.dp else 1.dp
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(cardBorderWidth, cardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = shakeOffset.value.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Switch Tabs: Login vs Register
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateElevated)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isLoginTab) ChampagneGold else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isLoginTab = true }
                        ) {
                            Text(
                                text = Strings.loginTab(currentLang),
                                color = if (isLoginTab) ObsidianBlack else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isLoginTab) ChampagneGold else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isLoginTab = false }
                        ) {
                            Text(
                                text = Strings.registerTab(currentLang),
                                color = if (!isLoginTab) ObsidianBlack else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoginTab) {
                        // LOGIN FORM
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text(Strings.emailOrUsername(currentLang)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ChampagneGold)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateElevated,
                                unfocusedContainerColor = SlateElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text(Strings.password(currentLang)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ChampagneGold)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateElevated,
                                unfocusedContainerColor = SlateElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (loginEmail.isBlank() || loginPassword.isBlank()) {
                                    val title = if (currentLang == AppLanguage.ARABIC) "حقول الدخول مطلوبة" else "Required Fields"
                                    val msg = if (currentLang == AppLanguage.ARABIC)
                                        "يرجى كتابة اسم المستخدم أو البريد الإلكتروني وكلمة المرور للمتابعة."
                                    else
                                        "Please enter your username or email and password to proceed."
                                    triggerErrorEffect(title, msg)
                                } else {
                                    val success = viewModel.login(loginEmail.trim(), loginPassword.trim())
                                    if (!success) {
                                        val title = if (currentLang == AppLanguage.ARABIC) "⚠️ خطأ في تسجيل الدخول" else "⚠️ Login Failed"
                                        val msg = if (currentLang == AppLanguage.ARABIC)
                                            "بيانات الدخول غير صحيحة أو الحساب غير موجود!\n\nيرجى التأكد من اسم المستخدم أو البريد الإلكتروني وكلمة المرور والمحاولة مجدداً.\n\n🔒 ملاحظة أمان: جميع عمليات الدخول مشفرة بالكامل ولا يتم إظهار أو اقتراح أي حسابات سابقة لضمان خصوصيتك وأمانك التام."
                                        else
                                            "Invalid login credentials or account does not exist!\n\nPlease check your username/email and password and try again.\n\n🔒 Security Notice: All login attempts are end-to-end encrypted. No past accounts are retained or displayed for your privacy."
                                        triggerErrorEffect(title, msg)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_btn")
                        ) {
                            Text(
                                text = Strings.loginBtn(currentLang),
                                color = ObsidianBlack,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        // REGISTER FORM
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text(Strings.fullName(currentLang)) },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = ChampagneGold)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateElevated,
                                unfocusedContainerColor = SlateElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text(Strings.emailOrUsername(currentLang)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = ChampagneGold)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateElevated,
                                unfocusedContainerColor = SlateElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text(Strings.phoneNumber(currentLang)) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = ChampagneGold)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateElevated,
                                unfocusedContainerColor = SlateElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_phone_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text(Strings.password(currentLang)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ChampagneGold)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateElevated,
                                unfocusedContainerColor = SlateElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = SlateBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (regName.isBlank() || regEmail.isBlank() || regPassword.isBlank()) {
                                    val title = if (currentLang == AppLanguage.ARABIC) "بيانات غير مكتملة" else "Incomplete Data"
                                    val msg = if (currentLang == AppLanguage.ARABIC)
                                        "يرجى ملء جميع الحقول المطلوبة (الاسم الكامل، البريد الإلكتروني، وكلمة المرور)."
                                    else
                                        "Please fill in all required fields (Full name, Email, and Password)."
                                    triggerErrorEffect(title, msg)
                                } else {
                                    val success = viewModel.registerCustomer(
                                        regName.trim(),
                                        regEmail.trim(),
                                        regPassword.trim(),
                                        regPhone.trim().takeIf { it.isNotBlank() }
                                    )
                                    if (!success) {
                                        val title = if (currentLang == AppLanguage.ARABIC) "فشل إنشاء الحساب" else "Registration Failed"
                                        val msg = if (currentLang == AppLanguage.ARABIC)
                                            "تعذر تسجيل الحساب! قد يكون هذا البريد مسجلاً مسبقاً، يرجى المحاولة ببريد آخر."
                                        else
                                            "Could not register account! This email might already exist, please try another."
                                        triggerErrorEffect(title, msg)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_submit_btn")
                        ) {
                            Text(
                                text = Strings.registerBtn(currentLang),
                                color = ObsidianBlack,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // CENTERED LUXURY ERROR POPUP / MODAL DIALOG
    // ==========================================
    if (showErrorDialog) {
        Dialog(
            onDismissRequest = { showErrorDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, DangerRed),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Double Ring Red Error Emblem in Center of Screen
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DangerRed.copy(alpha = 0.12f))
                            .border(2.dp, DangerRed.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = 0.22f))
                                .border(1.5.dp, DangerRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dangerous,
                                contentDescription = "Error",
                                tint = DangerRed,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Title
                    Text(
                        text = errorDialogTitle,
                        color = DangerRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Error Message
                    Text(
                        text = errorDialogMessage,
                        color = TextPrimary,
                        fontSize = 13.5.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Retry / Dismiss Button
                    Button(
                        onClick = { showErrorDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dismiss_error_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "إعادة المحاولة / حسناً" else "Try Again / Dismiss",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
