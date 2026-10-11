package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bobstore.data.model.UserRole
import com.example.bobstore.ui.components.LuxuryBottomBar
import com.example.bobstore.ui.components.TopLuxuryBar
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.screens.*
import com.example.bobstore.ui.viewmodel.AppNavTab
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.bobstore.util.WhatsAppHelper
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BoBViewModel = viewModel()
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()

            // Dynamic RTL / LTR layout direction based on language
            CompositionLocalProvider(LocalLayoutDirection provides currentLanguage.layoutDirection) {
                BoBStoreTheme {
                    if (currentUser == null) {
                        AuthScreen(viewModel = viewModel)
                    } else {
                        BoBMainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BoBMainScreen(viewModel: BoBViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val showCheckout by viewModel.showCheckoutDialog.collectAsState()
    val lastPlacedOrder by viewModel.lastPlacedOrder.collectAsState()
    val notification by viewModel.notification.collectAsState()
    val adminNotifications by viewModel.adminNotifications.collectAsState()
    val storeCustomization by viewModel.storeCustomization.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }

    val isAdmin = currentUser?.role == UserRole.ADMIN
    val totalCartCount = remember(cartItems) {
        cartItems.sumOf { it.quantity }
    }

    // Handle back button
    BackHandler(enabled = selectedProduct != null || showCheckout) {
        if (selectedProduct != null) {
            viewModel.selectProduct(null)
        } else if (showCheckout) {
            viewModel.setShowCheckout(false)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        topBar = {
            TopLuxuryBar(
                currentUser = currentUser,
                currentLanguage = currentLanguage,
                currentCurrency = currentCurrency,
                cartItemCount = totalCartCount,
                unreadNotificationsCount = adminNotifications.size,
                storeName = storeCustomization.storeName,
                lbpExchangeRate = storeCustomization.lbpExchangeRate,
                onToggleLanguage = { viewModel.toggleLanguage() },
                onSelectCurrency = { viewModel.setCurrency(it) },
                onOpenCart = { viewModel.setTab(AppNavTab.CART) },
                onOpenProfile = { showProfileDialog = true },
                onLogout = { viewModel.logout() },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            LuxuryBottomBar(
                currentUser = currentUser,
                currentTab = currentTab,
                currentLanguage = currentLanguage,
                cartItemCount = totalCartCount,
                onTabSelected = { viewModel.setTab(it) }
            )
        },
        containerColor = ObsidianBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Navigation Body
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "NavTransition"
            ) { targetTab ->
                if (isAdmin) {
                    // Admin Dedicated Workspaces
                    when (targetTab) {
                        AppNavTab.ADMIN_DASHBOARD -> AdminOverviewScreen(viewModel = viewModel)
                        AppNavTab.ADMIN_USERS -> AdminUsersScreen(viewModel = viewModel)
                        AppNavTab.ADMIN_ORDERS -> AdminOrdersScreen(viewModel = viewModel)
                        AppNavTab.ADMIN_CUSTOMIZATION -> AdminCustomizationScreen(viewModel = viewModel)
                        else -> AdminOverviewScreen(viewModel = viewModel)
                    }
                } else {
                    // Customer Views Only
                    when (targetTab) {
                        AppNavTab.DISCOVER -> CustomerHomeScreen(viewModel = viewModel)
                        AppNavTab.WISHLIST -> WishlistScreen(viewModel = viewModel)
                        AppNavTab.ORDERS -> OrdersTrackingScreen(viewModel = viewModel)
                        AppNavTab.CART -> CartScreen(viewModel = viewModel)
                        else -> CustomerHomeScreen(viewModel = viewModel)
                    }
                }
            }

            // Notification Banner / Snackbar
            AnimatedVisibility(
                visible = notification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                notification?.let { notif ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (notif.isError) DangerRed else CharcoalSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (notif.isError) Color.Transparent else ChampagneGold
                        ),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (notif.isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (notif.isError) Color.White else ChampagneGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = notif.message,
                                color = if (notif.isError) Color.White else TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Product Detail Modal
    selectedProduct?.let { prod ->
        ProductDetailModal(
            product = prod,
            viewModel = viewModel,
            currentLang = currentLanguage,
            formattedPrice = viewModel.formatPrice(prod.price),
            similarProducts = viewModel.getSimilarProducts(prod.id, prod.categoryId),
            onDismiss = { viewModel.selectProduct(null) },
            onSelectSimilarProduct = { similarProd ->
                viewModel.selectProduct(similarProd)
            },
            onAddToCart = { color, size ->
                viewModel.addToCart(prod, color, size)
                viewModel.selectProduct(null)
            },
            onBuyNow = { color, size ->
                viewModel.addToCart(prod, color, size)
                viewModel.selectProduct(null)
                viewModel.setShowCheckout(true)
            }
        )
    }

    // Checkout Dialog
    if (showCheckout) {
        CheckoutDialog(
            currentLang = currentLanguage,
            onDismiss = { viewModel.setShowCheckout(false) },
            onConfirm = { name, phone, address, city, payment ->
                viewModel.placeOrder(name, phone, address, city, payment) { placedOrder ->
                    // Automatically trigger WhatsApp with prefilled order details
                    WhatsAppHelper.sendOrderToWhatsApp(context, placedOrder, "$${placedOrder.totalAmount}")
                }
            }
        )
    }

    // Order Success Modal
    lastPlacedOrder?.let { order ->
        OrderSuccessDialog(
            order = order,
            currentLang = currentLanguage,
            onDismiss = {
                viewModel.setLastPlacedOrder(null)
                viewModel.setTab(AppNavTab.ORDERS)
            }
        )
    }

    // Profile Dialog
    if (showProfileDialog) {
        UserProfileDialog(
            user = currentUser,
            onDismiss = { showProfileDialog = false },
            onLogout = {
                showProfileDialog = false
                viewModel.logout()
            }
        )
    }
}

@Composable
fun UserProfileDialog(
    user: com.example.bobstore.data.model.User?,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, ChampagneGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الملف الشخصي والحساب",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(ChampagneGoldDark, ChampagneGold))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user?.name?.take(1) ?: "B").uppercase(),
                        color = ObsidianBlack,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.name ?: "مستخدم",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = user?.email ?: "",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // User Info Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SlateElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "نوع الحساب:", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = if (user?.role == UserRole.ADMIN) "مدير المتجر (Admin)" else "زبون مسجل (Customer)",
                                color = if (user?.role == UserRole.ADMIN) ChampagneGold else EnergyCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!user?.phone.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "رقم الهاتف:", color = TextSecondary, fontSize = 12.sp)
                                Text(text = user?.phone ?: "", color = TextPrimary, fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "حالة الحساب:", color = TextSecondary, fontSize = 12.sp)
                            Text(text = "نشط وفعال ✔", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تسجيل الخروج من الحساب", color = DangerRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
