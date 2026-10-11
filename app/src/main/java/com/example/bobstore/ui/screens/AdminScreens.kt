package com.example.bobstore.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bobstore.data.model.*
import com.example.bobstore.ui.components.LuxuryBadge
import com.example.bobstore.ui.components.LuxuryCard
import com.example.bobstore.ui.components.ProductImageThumbnail
import com.example.bobstore.ui.components.WhatsAppGreen
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.bobstore.util.WhatsAppHelper
import com.example.ui.theme.*

// ==========================================
// 1. ADMIN OVERVIEW & PRODUCTS SCREEN (الرئيسية)
// ==========================================
@Composable
fun AdminOverviewScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val users by viewModel.users.collectAsState()
    val notifications by viewModel.adminNotifications.collectAsState()

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }

    val analytics = remember(products, orders, users) {
        viewModel.repository.getAnalytics()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Admin Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "الرئيسية: البضاعة والمبيعات" else "Overview: Stock & Sales",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LuxuryBadge(text = "DASHBOARD", isGold = false)
                    }
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "إحصائيات فورية وإدارة مخزون المتجر" else "Realtime stock & sales performance",
                        color = RoyalVioletLight,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = Strings.logout(currentLang),
                        color = DangerRed,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Order Notifications Alert
        if (notifications.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoyalViolet.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalViolet),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = RoyalVioletLight)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تنبيه طلب جديد: ${notifications.first().title}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = notifications.first().message,
                                color = ChampagneGoldLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Stat Cards: Sales & Stock (المبيعات وعدد البضاعة)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard(
                    title = if (currentLang == AppLanguage.ARABIC) "إجمالي المبيعات" else "Total Revenue",
                    value = viewModel.formatPrice(analytics.totalRevenue),
                    subText = "${analytics.totalOrders} طلبية مسجلة",
                    icon = Icons.Default.AttachMoney,
                    color = ChampagneGold,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = if (currentLang == AppLanguage.ARABIC) "عدد البضاعة بالمخزون" else "Total Stock Units",
                    value = "${analytics.totalStockCount} قطعة",
                    subText = "${analytics.totalProducts} صنف معروض",
                    icon = Icons.Default.Inventory2,
                    color = EnergyCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard(
                    title = if (currentLang == AppLanguage.ARABIC) "الطلبات المعلقة" else "Pending Orders",
                    value = analytics.pendingOrders.toString(),
                    subText = "بانتظار التأكيد والشحن",
                    icon = Icons.Default.PendingActions,
                    color = RoyalVioletLight,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = if (currentLang == AppLanguage.ARABIC) "تنبيه مخزون منخفض" else "Low Stock Alerts",
                    value = "${analytics.lowStockCount} أصناف",
                    subText = "أقل من 10 قطع متوفرة",
                    icon = Icons.Default.WarningAmber,
                    color = if (analytics.lowStockCount > 0) DangerRed else SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Products Section Header with "+ إضافة منتج جديد"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "قائمة البضاعة والمنتجات (${products.size})" else "Product Catalog (${products.size})",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "تحكم بالصور، الألوان، المقاسات، والأسعار" else "Manage images, colors, sizes & prices",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = {
                        productToEdit = null
                        showProductDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("admin_add_product_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "+ إضافة منتج" else "+ Add Item",
                        color = ObsidianBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Product Items List
        items(products, key = { it.id }) { product ->
            AdminProductItemRow(
                product = product,
                currentLang = currentLang,
                formattedPrice = viewModel.formatPrice(product.price),
                onEdit = {
                    productToEdit = product
                    showProductDialog = true
                },
                onDelete = { viewModel.deleteProduct(product.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }

    // Add / Edit Product Dialog with Main Image, Extra Images, Colors, Sizes
    if (showProductDialog) {
        AdminProductDialog(
            product = productToEdit,
            currentLang = currentLang,
            onDismiss = { showProductDialog = false },
            onSave = { updatedProduct ->
                viewModel.addOrUpdateProduct(updatedProduct)
                showProductDialog = false
            }
        )
    }
}

// ==========================================
// 2. ADMIN ACCOUNTS SCREEN (الحسابات)
// ==========================================
@Composable
fun AdminUsersScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val users by viewModel.users.collectAsState()
    var showAddAdminDialog by remember { mutableStateOf(false) }

    val adminCount = users.count { it.role == UserRole.ADMIN }
    val customerCount = users.count { it.role == UserRole.CUSTOMER }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Accounts Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "إدارة الحسابات والصلاحيات" else "Accounts & Permissions",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LuxuryBadge(text = "${users.size} USERS", isGold = false)
                    }
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "عرض جميع الحسابات، إضافة أدمن جديد، وتعيين الرتب" else "Manage accounts, promote admins & control access",
                        color = RoyalVioletLight,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showAddAdminDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalViolet),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "+ إضافة أدمن" else "+ Add Admin",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Accounts Stats Summary
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard(
                    title = if (currentLang == AppLanguage.ARABIC) "المدراء (الأدمن)" else "Admins",
                    value = adminCount.toString(),
                    subText = "تحكم كامل بالمتجر",
                    icon = Icons.Default.AdminPanelSettings,
                    color = RoyalVioletLight,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = if (currentLang == AppLanguage.ARABIC) "الزبائن المسجلون" else "Customers",
                    value = customerCount.toString(),
                    subText = "تصفح وطلب الإكسسوارات",
                    icon = Icons.Default.People,
                    color = EnergyCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section Title
        item {
            Text(
                text = if (currentLang == AppLanguage.ARABIC) "قائمة الحسابات المسجلة" else "Registered Accounts",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        // Users List
        items(users, key = { it.id }) { user ->
            AdminUserItemCard(
                user = user,
                currentLang = currentLang,
                onToggleRole = { viewModel.toggleUserAdminRole(user) },
                onDelete = { viewModel.deleteUser(user.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }

    // Add New Admin Dialog
    if (showAddAdminDialog) {
        AddNewAdminDialog(
            currentLang = currentLang,
            onDismiss = { showAddAdminDialog = false },
            onSave = { name, email, pass, phone ->
                viewModel.addNewAdmin(name, email, pass, phone)
                showAddAdminDialog = false
            }
        )
    }
}

// ==========================================
// 3. ADMIN ORDERS SCREEN (الطلبات)
// ==========================================
@Composable
fun AdminOrdersScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val orders by viewModel.orders.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Orders Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "إدارة وتتبع الطلبيات" else "Orders Management",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LuxuryBadge(text = "${orders.size} ORDERS", isGold = true)
                    }
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "متابعة دفعات Whish Money والتواصل الفوري مع الزبائن" else "Track Whish Money payments & WhatsApp clients",
                        color = ChampagneGoldLight,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AssignmentLate, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "لا توجد طلبات جديدة حالياً" else "No orders yet",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
                AdminOrderItemCard(
                    order = order,
                    currentLang = currentLang,
                    formattedTotal = viewModel.formatPrice(order.totalAmount),
                    onStatusChange = { newStatus -> viewModel.updateOrderStatus(order.id, newStatus) },
                    onChatWhatsApp = {
                        val msg = "مرحباً ${order.customerName}، نتواصل معك من متجر BoB Store بخصوص طلبك رقم (${order.id})."
                        WhatsAppHelper.openGeneralInquiry(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                )
            }
        }
    }
}

// ==========================================
// 4. ADMIN CUSTOMIZATION & BRANDING SCREEN (تخصيص وألوان)
// ==========================================
@Composable
fun AdminCustomizationScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val customization by viewModel.storeCustomization.collectAsState()

    var storeName by remember(customization) { mutableStateOf(customization.storeName) }
    var lbpRateText by remember(customization) { mutableStateOf(customization.lbpExchangeRate.toInt().toString()) }
    var selectedColorHex by remember(customization) { mutableStateOf(customization.accentColorHex) }
    var bannerTitleAr by remember(customization) { mutableStateOf(customization.bannerTitleAr) }
    var bannerSubtitleAr by remember(customization) { mutableStateOf(customization.bannerSubtitleAr) }
    var bannerImage by remember(customization) { mutableStateOf(customization.bannerImageRes) }
    var announcement by remember(customization) { mutableStateOf(customization.announcementAr) }
    var whatsappNum by remember(customization) { mutableStateOf(customization.whatsappNumber) }

    val presetColors = listOf(
        Pair("الذهبي الفاخر (Champagne Gold)", "#D4AF37"),
        Pair("البنفسجي الملكي (Royal Violet)", "#8A2BE2"),
        Pair("السيان المتوهج (Energy Cyan)", "#00F5D4"),
        Pair("الزمردي النقي (Emerald Green)", "#10B981"),
        Pair("الوردي الملكي (Rose Gold)", "#E0A96D")
    )

    val presetImages = listOf("hero_luxury_case", "hero_wireless_dock", "hero_earbuds_case", "ic_bob_store_logo")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(bottom = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "تخصيص الألوان والصور والتصميم" else "App Customization & Branding",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LuxuryBadge(text = "STYLING", isGold = true)
                }
                Text(
                    text = if (currentLang == AppLanguage.ARABIC) "تغيير اللون الأساسي، صور الإعلانات، ورقم الواتساب الخاص بالمتجر" else "Customize theme accent colors, banners and WhatsApp number",
                    color = RoyalVioletLight,
                    fontSize = 11.sp
                )
            }
        }

        // Section 0: Store Name & Currency Exchange Rate
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.2.dp, ChampagneGold.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "اسم المتجر وسعر الصرف بالليرة اللبنانية" else "Store Name & LBP Exchange Rate",
                            color = TextPrimary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("اسم المتجر (المعروض في الشريط العلوي (BoB store))") },
                        placeholder = { Text("BoB store") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = lbpRateText,
                        onValueChange = { lbpRateText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("سعر صرف الدولار بالليرة اللبنانية (1$ = ? ل.ل)") },
                        placeholder = { Text("89500") },
                        supportingText = {
                            Text(
                                text = "يتم اعتماد هذا السعر فوراً لجميع حسابات وأسعار الزبائن في التطبيق",
                                color = ChampagneGoldLight,
                                fontSize = 10.5.sp
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 1: Accent Color Picker
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "اللون الرئيسي للتطبيق (Accent Theme Color)" else "App Accent Color",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    presetColors.forEach { (name, hex) ->
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) SlateElevated else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) ChampagneGold else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedColorHex = hex }
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = name,
                                    color = if (isSelected) ChampagneGold else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Banner Image Selector
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "صورة الإعلان الترويجي الرئيسية (Hero Banner Image)" else "Hero Banner Visual",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetImages) { imgName ->
                            val isSelected = bannerImage == imgName
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SlateElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isSelected) ChampagneGold else SlateBorder
                                ),
                                modifier = Modifier
                                    .size(width = 100.dp, height = 75.dp)
                                    .clickable { bannerImage = imgName }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    ProductImageThumbnail(
                                        imageResName = imgName,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(ChampagneGold),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Banner Texts & WhatsApp Number
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "نصوص الإعلان ورقم الواتساب" else "Banner Copy & WhatsApp",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bannerTitleAr,
                        onValueChange = { bannerTitleAr = it },
                        label = { Text("عنوان البانر الرئيسي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bannerSubtitleAr,
                        onValueChange = { bannerSubtitleAr = it },
                        label = { Text("النص الفرعي للبانر") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = announcement,
                        onValueChange = { announcement = it },
                        label = { Text("شريط التنبيهات والإعلانات") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = whatsappNum,
                        onValueChange = { whatsappNum = it },
                        label = { Text("رقم الواتساب لاستقبال الاستفسارات والطلبات") },
                        placeholder = { Text("0096170198429") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Save Customization Button
        item {
            Button(
                onClick = {
                    val updated = StoreCustomization(
                        storeName = if (storeName.isNotBlank()) storeName.trim() else "BoB store",
                        lbpExchangeRate = lbpRateText.toDoubleOrNull() ?: 89500.0,
                        accentColorHex = selectedColorHex,
                        bannerTitleAr = bannerTitleAr,
                        bannerSubtitleAr = bannerSubtitleAr,
                        bannerImageRes = bannerImage,
                        announcementAr = announcement,
                        whatsappNumber = whatsappNum
                    )
                    viewModel.updateCustomization(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = ObsidianBlack)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (currentLang == AppLanguage.ARABIC) "حفظ وتطبيق جميع التغييرات 💾" else "Save & Apply Customization 💾",
                    color = ObsidianBlack,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// ==========================================
// SHARED ADMIN COMPONENT ITEMS
// ==========================================

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    subText: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!subText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subText,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AdminProductItemRow(
    product: Product,
    currentLang: AppLanguage,
    formattedPrice: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = if (currentLang == AppLanguage.ARABIC) product.nameAr else product.nameEn

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateElevated),
                contentAlignment = Alignment.Center
            ) {
                ProductImageThumbnail(
                    imageResName = product.imageRes,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedPrice,
                        color = ChampagneGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "المخزون: ${product.stock} ق",
                        color = if (product.stock < 10) DangerRed else SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${product.sizes.size} مقاسات • ${product.colors.size} ألوان",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            // Edit & Delete Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = ChampagneGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = DangerRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminUserItemCard(
    user: User,
    currentLang: AppLanguage,
    onToggleRole: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = user.role == UserRole.ADMIN

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdmin) RoyalViolet else SlateBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isAdmin) RoyalViolet.copy(alpha = 0.25f) else SlateElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isAdmin) RoyalVioletLight else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    LuxuryBadge(
                        text = if (isAdmin) "ADMIN" else "CUSTOMER",
                        isGold = !isAdmin
                    )
                }

                Text(
                    text = user.email,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!user.phone.isNullOrBlank()) {
                    Text(
                        text = "الهاتف: ${user.phone}",
                        color = EnergyCyan,
                        fontSize = 10.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Role Toggle Button
                if (user.id != "admin-1") {
                    TextButton(
                        onClick = onToggleRole,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isAdmin) "تحويل لزبون" else "ترقية لأدمن",
                            color = if (isAdmin) ChampagneGold else RoyalVioletLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderItemCard(
    order: Order,
    currentLang: AppLanguage,
    formattedTotal: String,
    onStatusChange: (String) -> Unit,
    onChatWhatsApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Order ID & Status Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.id}",
                        color = ChampagneGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${order.trackingNumber})",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Box {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = getStatusColor(order.status).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, getStatusColor(order.status)),
                        modifier = Modifier.clickable { showStatusMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getLocalizedStatus(order.status, currentLang),
                                color = getStatusColor(order.status),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = getStatusColor(order.status),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                        modifier = Modifier.background(SlateElevated)
                    ) {
                        listOf("PENDING", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED").forEach { s ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = getLocalizedStatus(s, currentLang),
                                        color = getStatusColor(s),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    onStatusChange(s)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Customer Name & Phone & City
            Text(
                text = "👤 ${order.customerName} • 📞 ${order.customerPhone}",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "📍 ${order.customerAddress} (${order.city})",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Row 3: Payment method and Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (order.paymentMethod.contains("Whish", ignoreCase = true)) EnergyCyan.copy(alpha = 0.15f) else SlateElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (order.paymentMethod.contains("Whish", ignoreCase = true)) EnergyCyan else SlateBorder
                    )
                ) {
                    Text(
                        text = "💳 ${order.paymentMethod}",
                        color = if (order.paymentMethod.contains("Whish", ignoreCase = true)) EnergyCyan else TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = formattedTotal,
                    color = ChampagneGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action: Direct WhatsApp Chat with Customer
            Button(
                onClick = onChatWhatsApp,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            ) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مراسلة الزبون عبر واتساب بخصوص الطلبية",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// ADD / EDIT PRODUCT DIALOG WITH FULL CONTROLS
// ==========================================
@Composable
fun AdminProductDialog(
    product: Product?,
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var nameAr by remember { mutableStateOf(product?.nameAr ?: "") }
    var nameEn by remember { mutableStateOf(product?.nameEn ?: "") }
    var descAr by remember { mutableStateOf(product?.descAr ?: "") }
    var descEn by remember { mutableStateOf(product?.descEn ?: "") }
    var priceStr by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var originalPriceStr by remember { mutableStateOf(product?.originalPrice?.toString() ?: "") }
    var discountPercentStr by remember {
        mutableStateOf(
            if (product?.originalPrice != null && product.originalPrice > product.price) {
                val pct = ((product.originalPrice - product.price) / product.originalPrice * 100).toInt()
                pct.toString()
            } else ""
        )
    }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "BoB Elite") }
    var categoryId by remember { mutableStateOf(product?.categoryId ?: "cases") }
    var mainImage by remember { mutableStateOf(product?.imageRes ?: "") }

    // Additional Images - start completely empty for new products
    var additionalImagesList by remember { mutableStateOf(product?.additionalImages?.toMutableList() ?: mutableListOf<String>()) }
    var newImageInput by remember { mutableStateOf("") }

    // Colors: Admin adds whatever color they want, starts empty for new products!
    var colorsList by remember { mutableStateOf(product?.colors?.toMutableList() ?: mutableListOf<String>()) }
    var customColorInput by remember { mutableStateOf("") }

    // Sizes: Admin writes custom sizes, starts empty for new products!
    var sizesList by remember { mutableStateOf(product?.sizes?.toMutableList() ?: mutableListOf<String>()) }
    var customSizeInput by remember { mutableStateOf("") }

    // Photo Pickers from Phone
    val mainPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            mainImage = it.toString()
        }
    }

    val extraPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            additionalImagesList.add(it.toString())
        }
    }

    val presetImages = listOf("hero_luxury_case", "hero_wireless_dock", "hero_earbuds_case", "ic_bob_store_logo")
    val presetPalette = listOf(
        Pair("ذهبي", "#D4AF37"),
        Pair("أسود", "#1E1E24"),
        Pair("رمادي", "#708090"),
        Pair("بنفسجي", "#8A2BE2"),
        Pair("سيان", "#00F5D4"),
        Pair("أحمر", "#FF4444"),
        Pair("أزرق", "#1E90FF"),
        Pair("أخضر", "#10B981")
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, ChampagneGold, RoundedCornerShape(20.dp)),
            color = CharcoalSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (product == null) Strings.addNewProduct(currentLang) else Strings.editProduct(currentLang),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. MAIN IMAGE (Phone Picker + Presets)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الصورة الأساسية للمنتج:",
                                    color = ChampagneGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Phone Image Picker Button
                                Button(
                                    onClick = {
                                        mainPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("اختيار صورة من الهاتف 📱", color = ObsidianBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Current Main Image Preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = CharcoalSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, ChampagneGold),
                                    modifier = Modifier.size(70.dp)
                                ) {
                                    ProductImageThumbnail(
                                        imageResName = mainImage,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("الصورة المختارة حالياً:", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = when {
                                            mainImage.startsWith("content://") -> "صورة مأخوذة من الهاتف 📱"
                                            mainImage.isNotBlank() -> mainImage
                                            else -> "لم يتم تحديد صورة بعد (اختر من الهاتف أو النماذج)"
                                        },
                                        color = if (mainImage.isNotBlank()) TextPrimary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("أو اختر من النماذج الجاهزة أدناه:", color = TextSecondary, fontSize = 10.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Preset Images Row
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(presetImages) { img ->
                                    val isSel = mainImage == img
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CharcoalSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.5.dp,
                                            if (isSel) ChampagneGold else SlateBorder
                                        ),
                                        modifier = Modifier
                                            .size(width = 64.dp, height = 48.dp)
                                            .clickable { mainImage = img }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            ProductImageThumbnail(imageResName = img, modifier = Modifier.fillMaxSize())
                                            if (isSel) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(2.dp)
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(ChampagneGold),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(9.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. ADDITIONAL IMAGES (Phone Picker + Input)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "صور إضافية للمنتج (${additionalImagesList.size}):",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Add Extra Image from Phone
                                OutlinedButton(
                                    onClick = {
                                        extraPhotoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ صورة من الهاتف", color = ChampagneGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Manual URL / Name Input
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newImageInput,
                                    onValueChange = { newImageInput = it },
                                    placeholder = { Text("أو اكتب رابط/اسم صورة إضافية", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (newImageInput.isNotBlank()) {
                                            additionalImagesList.add(newImageInput.trim())
                                            newImageInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurface),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ إضافة", color = ChampagneGold, fontSize = 11.sp)
                                }
                            }

                            if (additionalImagesList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(additionalImagesList) { extraImg ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = CharcoalSurface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp))) {
                                                    ProductImageThumbnail(imageResName = extraImg, modifier = Modifier.fillMaxSize())
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (extraImg.startsWith("content://")) "صورة هاتف" else extraImg.take(12),
                                                    color = TextPrimary,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = DangerRed,
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clickable { additionalImagesList.remove(extraImg) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. COLORS MANAGER (Custom Color Typing + Swatches)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "ألوان المنتج (أضف أي لون تريده):",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom Color Typing Input Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customColorInput,
                                    onValueChange = { customColorInput = it },
                                    placeholder = { Text("اكتب اللون (مثال: ذهبي، كحلي، أحمر ملكي، أو #FF5733)", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (customColorInput.isNotBlank()) {
                                            val trimmed = customColorInput.trim()
                                            if (!colorsList.contains(trimmed)) {
                                                colorsList.add(trimmed)
                                            }
                                            customColorInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ إضافة لون", color = ObsidianBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Preset Swatches Palette
                            Text("أو اختر لوناً سريعاً:", color = TextSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(presetPalette) { (cName, hex) ->
                                    val alreadyHas = colorsList.contains(hex) || colorsList.contains(cName)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (alreadyHas) ChampagneGold.copy(alpha = 0.15f) else CharcoalSurface,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (alreadyHas) ChampagneGold else SlateBorder
                                        ),
                                        modifier = Modifier.clickable {
                                            if (alreadyHas) {
                                                colorsList.remove(hex)
                                                colorsList.remove(cName)
                                            } else {
                                                colorsList.add(hex)
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(cName, color = TextPrimary, fontSize = 10.sp)
                                            if (alreadyHas) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Check, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            // Active Colors List (with delete button)
                            if (colorsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("الألوان المضافة للمنتج (${colorsList.size}):", color = ChampagneGoldLight, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(colorsList) { cItem ->
                                        val isHex = cItem.startsWith("#") && (cItem.length == 7 || cItem.length == 9)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = CharcoalSurface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val parsedColor = remember(cItem, isHex) {
                                                    if (isHex) {
                                                        try {
                                                            Color(android.graphics.Color.parseColor(cItem))
                                                        } catch (_: Exception) {
                                                            null
                                                        }
                                                    } else null
                                                }
                                                if (parsedColor != null) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clip(CircleShape)
                                                            .background(parsedColor)
                                                            .border(0.8.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Text(cItem, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = DangerRed,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable { colorsList.remove(cItem) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. SIZES MANAGER (Custom Size Typing)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "مقاسات وموديلات المنتج (اكتب المقاس الذي تريده):",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom Size Typing Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customSizeInput,
                                    onValueChange = { customSizeInput = it },
                                    placeholder = { Text("اكتب المقاس (مثال: M, L, XL, iPhone 16 Pro Max, 128GB...)", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        if (customSizeInput.isNotBlank()) {
                                            val trimmed = customSizeInput.trim()
                                            if (!sizesList.contains(trimmed)) {
                                                sizesList.add(trimmed)
                                            }
                                            customSizeInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EnergyCyan),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ إضافة مقاس", color = ObsidianBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Suggested Quick Shortcuts
                            Text("اقتراحات سريعة:", color = TextSecondary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            val suggestions = listOf("S", "M", "L", "XL", "iPhone 16 Pro Max", "Galaxy S25 Ultra", "Universal", "128GB", "256GB")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(suggestions) { sug ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CharcoalSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                        modifier = Modifier.clickable {
                                            if (!sizesList.contains(sug)) {
                                                sizesList.add(sug)
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "+ $sug",
                                            color = EnergyCyan,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Active Sizes List (with delete button)
                            if (sizesList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("المقاسات المعتمدة للمنتج (${sizesList.size}):", color = ChampagneGoldLight, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(sizesList) { sizeItem ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = CharcoalSurface,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, EnergyCyan.copy(alpha = 0.5f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(sizeItem, color = EnergyCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = DangerRed,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable { sizesList.remove(sizeItem) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. NAME & PRICE & STOCK
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text("اسم المنتج بالعربية") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("English Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("الماركة / العلامة التجارية (Brand)") },
                        placeholder = { Text("مثال: BoB Elite, Apple, Pitaka") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Price, Discount Percentage, Original Price, and Stock
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("السعر النهائي ($)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = discountPercentStr,
                            onValueChange = { discountPercentStr = it },
                            label = { Text("نسبة الخصم (%)") },
                            placeholder = { Text("مثال: 20") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = originalPriceStr,
                            onValueChange = { originalPriceStr = it },
                            label = { Text("السعر قبل الخصم إن وجد ($)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = stockStr,
                            onValueChange = { stockStr = it },
                            label = { Text("الكمية بالمخزون") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = descAr,
                        onValueChange = { descAr = it },
                        label = { Text("الوصف بالعربية") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = descEn,
                        onValueChange = { descEn = it },
                        label = { Text("English Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Strings.cancel(currentLang))
                    }

                    Button(
                        onClick = {
                            val basePrice = priceStr.toDoubleOrNull() ?: 0.0
                            val discPct = discountPercentStr.toDoubleOrNull()
                            val origP = originalPriceStr.toDoubleOrNull() ?: if (discPct != null && discPct > 0 && basePrice > 0) {
                                (basePrice / (1.0 - (discPct / 100.0)))
                            } else null

                            val stock = stockStr.toIntOrNull() ?: 10
                            val finalProduct = Product(
                                id = product?.id ?: ("bob-" + System.currentTimeMillis().toString(36)),
                                nameAr = nameAr.ifBlank { "منتج BoB جديد" },
                                nameEn = nameEn.ifBlank { "BoB Gear" },
                                descAr = descAr.ifBlank { "منتج بجودة عالية" },
                                descEn = descEn.ifBlank { "High quality mobile gear" },
                                price = basePrice,
                                originalPrice = origP,
                                badgeAr = if (discPct != null && discPct > 0) "خصم ${discPct.toInt()}%" else null,
                                badgeEn = if (discPct != null && discPct > 0) "${discPct.toInt()}% OFF" else null,
                                categoryId = categoryId,
                                brand = brand.ifBlank { "BoB Elite" },
                                stock = stock,
                                imageRes = mainImage.ifBlank { "hero_luxury_case" },
                                additionalImages = additionalImagesList.toList(),
                                colors = if (colorsList.isEmpty()) listOf("#D4AF37") else colorsList.toList(),
                                sizes = if (sizesList.isEmpty()) listOf("Universal") else sizesList.toList()
                            )
                            onSave(finalProduct)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(Strings.saveChanges(currentLang), color = ObsidianBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Add New Admin Dialog
@Composable
fun AddNewAdminDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (name: String, email: String, pass: String, phone: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, RoyalViolet),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = Strings.addNewAdmin(currentLang),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الأدمن الكامل") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(Strings.cancel(currentLang))
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                onSave(name, email, password, phone)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalViolet),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إضافة الأدمن", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
