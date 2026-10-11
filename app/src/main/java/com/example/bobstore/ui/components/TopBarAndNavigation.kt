package com.example.bobstore.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobstore.data.model.User
import com.example.bobstore.data.model.UserRole
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Currency
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.AppNavTab
import com.example.ui.theme.*

@Composable
fun TopLuxuryBar(
    currentUser: User?,
    currentLanguage: AppLanguage,
    currentCurrency: Currency,
    cartItemCount: Int,
    unreadNotificationsCount: Int,
    onToggleLanguage: () -> Unit,
    onSelectCurrency: (Currency) -> Unit,
    onOpenCart: () -> Unit,
    onOpenProfile: () -> Unit,
    onLogout: () -> Unit,
    storeName: String = "BoB store",
    lbpExchangeRate: Double = 89500.0,
    modifier: Modifier = Modifier
) {
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    val isAdmin = currentUser?.role == UserRole.ADMIN

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CharcoalSurface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // START CORNER: BoB store (3D Colorful, No Border, No Icon, No Parentheses) + User Icon + Currency Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // 1. 3D Colorful Store Title (No border/outline, no store icon, no parentheses)
                    Store3DLogo(storeName = storeName)

                    // 2. User Account Icon (Tapping shows full account info)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .testTag("user_profile_btn")
                            .clickable(onClick = onOpenProfile),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "معلومات الحساب",
                                tint = ChampagneGold,
                                modifier = Modifier.size(19.dp)
                            )
                            if (currentUser != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUser.name.split(" ").firstOrNull() ?: "",
                                    color = TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 3. Currency Box (USD / LBP with admin-defined rate)
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SlateElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier
                                .testTag("currency_picker_btn")
                                .clickable { showCurrencyDropdown = true },
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = if (currentCurrency == Currency.USD) "$ USD" else "ل.ل LBP",
                                    color = ChampagneGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Currency",
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCurrencyDropdown,
                            onDismissRequest = { showCurrencyDropdown = false },
                            modifier = Modifier.background(SlateElevated)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "USD ($) - دولار",
                                            color = if (currentCurrency == Currency.USD) ChampagneGold else TextPrimary,
                                            fontWeight = if (currentCurrency == Currency.USD) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.5.sp
                                        )
                                        if (currentCurrency == Currency.USD) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.Check, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectCurrency(Currency.USD)
                                    showCurrencyDropdown = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "LBP (ل.ل) - لبناني",
                                                color = if (currentCurrency == Currency.LBP) ChampagneGold else TextPrimary,
                                                fontWeight = if (currentCurrency == Currency.LBP) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.5.sp
                                            )
                                            if (currentCurrency == Currency.LBP) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        val formattedRate = java.text.NumberFormat.getIntegerInstance().format(lbpExchangeRate.toLong())
                                        Text(
                                            text = "سعر الصرف: 1$ = $formattedRate ل.ل (من الإدارة)",
                                            color = ChampagneGoldLight,
                                            fontSize = 10.sp
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectCurrency(Currency.LBP)
                                    showCurrencyDropdown = false
                                }
                            )
                        }
                    }
                }

                // END CONTROLS: Cart, Language Toggle, Admin Badge / Logout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isAdmin) {
                        LuxuryBadge(text = "ADMIN", isGold = false)
                    }

                    // Customer Cart Button
                    if (!isAdmin) {
                        Box(
                            modifier = Modifier
                                .testTag("cart_icon_btn")
                                .clickable(onClick = onOpenCart)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SlateElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            ) {
                                Box(modifier = Modifier.padding(7.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = "Cart",
                                        tint = if (cartItemCount > 0) ChampagneGold else TextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            if (cartItemCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(DangerRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cartItemCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        // Admin Notification Bell
                        Box {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SlateElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, RoyalViolet),
                            ) {
                                Box(modifier = Modifier.padding(7.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = if (unreadNotificationsCount > 0) RoyalVioletLight else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (unreadNotificationsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(RoyalViolet),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = unreadNotificationsCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Language Switcher (EN / عربي)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier
                            .testTag("lang_toggle_btn")
                            .clickable(onClick = onToggleLanguage)
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ARABIC) "EN" else "عربي",
                            color = EnergyCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SlateBorder)
            )
        }
    }
}

@Composable
fun LuxuryBottomBar(
    currentUser: User?,
    currentTab: AppNavTab,
    currentLanguage: AppLanguage,
    cartItemCount: Int,
    onTabSelected: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = currentUser?.role == UserRole.ADMIN

    // Beautiful 3D Floating / Elevated Navigation Bar with Ambient Shadow
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = CharcoalSurface.copy(alpha = 0.97f),
            border = androidx.compose.foundation.BorderStroke(
                1.2.dp,
                Brush.verticalGradient(
                    listOf(
                        if (isAdmin) RoyalVioletLight.copy(alpha = 0.5f) else ChampagneGold.copy(alpha = 0.5f),
                        SlateBorder
                    )
                )
            ),
            shadowElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color(0x35000000),
                    spotColor = if (isAdmin) RoyalViolet.copy(alpha = 0.3f) else ChampagneGold.copy(alpha = 0.25f)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAdmin) {
                    BottomNavItem3D(
                        label = Strings.tabAdminDashboard(currentLanguage),
                        icon = Icons.Default.Dashboard,
                        isSelected = currentTab == AppNavTab.ADMIN_DASHBOARD,
                        highlightColor = RoyalVioletLight,
                        onClick = { onTabSelected(AppNavTab.ADMIN_DASHBOARD) }
                    )

                    BottomNavItem3D(
                        label = Strings.tabAdminUsers(currentLanguage),
                        icon = Icons.Default.AdminPanelSettings,
                        isSelected = currentTab == AppNavTab.ADMIN_USERS,
                        highlightColor = RoyalVioletLight,
                        onClick = { onTabSelected(AppNavTab.ADMIN_USERS) }
                    )

                    BottomNavItem3D(
                        label = Strings.tabAdminOrders(currentLanguage),
                        icon = Icons.Default.Assignment,
                        isSelected = currentTab == AppNavTab.ADMIN_ORDERS,
                        highlightColor = RoyalVioletLight,
                        onClick = { onTabSelected(AppNavTab.ADMIN_ORDERS) }
                    )

                    BottomNavItem3D(
                        label = Strings.tabAdminCustomization(currentLanguage),
                        icon = Icons.Default.Palette,
                        isSelected = currentTab == AppNavTab.ADMIN_CUSTOMIZATION,
                        highlightColor = RoyalVioletLight,
                        onClick = { onTabSelected(AppNavTab.ADMIN_CUSTOMIZATION) }
                    )
                } else {
                    BottomNavItem3D(
                        label = Strings.tabDiscover(currentLanguage),
                        icon = Icons.Default.Storefront,
                        isSelected = currentTab == AppNavTab.DISCOVER,
                        onClick = { onTabSelected(AppNavTab.DISCOVER) }
                    )

                    BottomNavItem3D(
                        label = Strings.tabWishlist(currentLanguage),
                        icon = Icons.Default.Favorite,
                        isSelected = currentTab == AppNavTab.WISHLIST,
                        onClick = { onTabSelected(AppNavTab.WISHLIST) }
                    )

                    BottomNavItem3D(
                        label = Strings.tabOrders(currentLanguage),
                        icon = Icons.Default.LocalShipping,
                        isSelected = currentTab == AppNavTab.ORDERS,
                        onClick = { onTabSelected(AppNavTab.ORDERS) }
                    )

                    BottomNavItem3D(
                        label = Strings.tabCart(currentLanguage),
                        icon = Icons.Default.ShoppingCart,
                        isSelected = currentTab == AppNavTab.CART,
                        badgeCount = cartItemCount,
                        onClick = { onTabSelected(AppNavTab.CART) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem3D(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    highlightColor: Color = ChampagneGold
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tab_scale"
    )

    val offsetY by animateDpAsState(
        targetValue = if (isSelected) (-3).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tab_offset"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) highlightColor else TextSecondary,
        label = "tab_color"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = offsetY.toPx()
            }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .then(
                    if (isSelected) {
                        Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        highlightColor.copy(alpha = 0.22f),
                                        highlightColor.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        highlightColor.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                ),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    } else {
                        Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(DangerRed)
                        .shadow(2.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

/**
 * 3D Multi-color Stylized Brand Logo (No borders, No icons, No parentheses).
 */
@Composable
fun Store3DLogo(
    storeName: String,
    modifier: Modifier = Modifier
) {
    // 3D Vibrant Multi-Color Gradient
    val bobGoldCyanGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFD54F), // Radiant Champagne Gold
            Color(0xFFFF7043), // Vibrant 3D Sunset Orange
            Color(0xFF00E5FF)  // Electric Cyan
        ),
        start = Offset(0f, 0f),
        end = Offset(120f, 60f)
    )

    val cleanName = storeName.replace("(", "").replace(")", "").trim()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 2.dp, horizontal = 2.dp)
    ) {
        if (cleanName.contains("BoB", ignoreCase = true)) {
            val parts = cleanName.split(" ", limit = 2)
            val firstWord = parts.firstOrNull() ?: "BoB"
            val secondWord = if (parts.size > 1) parts[1] else ""

            // "BoB" in glowing 3D multi-color gradient
            Text(
                text = firstWord,
                style = TextStyle(
                    brush = bobGoldCyanGradient,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(2f, 3f),
                        blurRadius = 4f
                    )
                )
            )

            if (secondWord.isNotBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                // "store" with complementary 3D cyber-cyan/silver gradient
                Text(
                    text = secondWord,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00E5FF),
                                Color(0xFF80D8FF),
                                Color(0xFFE2E8F0)
                            )
                        ),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.75f),
                            offset = Offset(1.5f, 2.5f),
                            blurRadius = 3f
                        )
                    )
                )
            }
        } else {
            // Customized store name (Arabic or English) with rich 3D gradient colors & dimensional shadow
            Text(
                text = cleanName,
                style = TextStyle(
                    brush = bobGoldCyanGradient,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(2f, 3f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

