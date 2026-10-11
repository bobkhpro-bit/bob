package com.example.bobstore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bobstore.data.model.CartItem
import com.example.bobstore.data.model.Order
import com.example.bobstore.ui.components.LuxuryCard
import com.example.bobstore.ui.components.ProductImageThumbnail
import com.example.bobstore.ui.components.WhatsAppGreen
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.bobstore.util.WhatsAppHelper
import com.example.ui.theme.*

@Composable
fun CartScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    var promoInput by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.product.price * it.quantity }
    val discount = if (appliedCoupon != null) subtotal * 0.10 else 0.0
    val vat = (subtotal - discount) * 0.15
    val total = (subtotal - discount) + vat

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Text(
                text = Strings.cartTitle(currentLang),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (cartItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(SlateElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveShoppingCart,
                                contentDescription = null,
                                tint = ChampagneGold,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Strings.emptyCart(currentLang),
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = Strings.emptyCartSubtitle(currentLang),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(cartItems, key = { it.product.id + it.selectedColor + it.selectedSize }) { item ->
                CartItemRow(
                    item = item,
                    currentLang = currentLang,
                    formattedPrice = viewModel.formatPrice(item.product.price * item.quantity),
                    onIncrease = {
                        viewModel.updateCartQuantity(item.product.id, item.selectedColor, item.selectedSize, item.quantity + 1)
                    },
                    onDecrease = {
                        viewModel.updateCartQuantity(item.product.id, item.selectedColor, item.selectedSize, item.quantity - 1)
                    },
                    onRemove = {
                        viewModel.removeFromCart(item.product.id, item.selectedColor, item.selectedSize)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
            }

            // Coupon Code Row
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CharcoalSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            placeholder = {
                                Text(
                                    text = Strings.voucherCode(currentLang),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateElevated,
                                unfocusedContainerColor = SlateElevated,
                                focusedBorderColor = ChampagneGold,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("coupon_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (viewModel.applyCoupon(promoInput)) {
                                    promoInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("apply_coupon_btn")
                        ) {
                            Text(
                                text = Strings.applyCoupon(currentLang),
                                color = ObsidianBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Price Breakdown Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                LuxuryCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    elevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PriceRow(
                            label = Strings.subtotal(currentLang),
                            value = viewModel.formatPrice(subtotal)
                        )

                        if (appliedCoupon != null) {
                            PriceRow(
                                label = Strings.couponApplied(currentLang),
                                value = "- ${viewModel.formatPrice(discount)}",
                                isHighlight = true
                            )
                        }

                        PriceRow(
                            label = Strings.shipping(currentLang),
                            value = Strings.freeShipping(currentLang),
                            isHighlight = true
                        )

                        PriceRow(
                            label = Strings.vatTax(currentLang),
                            value = viewModel.formatPrice(vat)
                        )

                        Divider(
                            color = SlateBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Strings.total(currentLang),
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.formatPrice(total),
                                color = ChampagneGold,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Checkout CTA Button
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { viewModel.setShowCheckout(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("checkout_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ObsidianBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.proceedToCheckout(currentLang),
                        color = ObsidianBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    currentLang: AppLanguage,
    formattedPrice: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = if (currentLang == AppLanguage.ARABIC) item.product.nameAr else item.product.nameEn

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateElevated),
                contentAlignment = Alignment.Center
            ) {
                ProductImageThumbnail(
                    imageResName = item.product.imageRes,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Size Badge
                Text(
                    text = "الموديل: ${item.selectedSize}",
                    color = EnergyCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val colorObj = remember(item.selectedColor) {
                        try {
                            Color(android.graphics.Color.parseColor(item.selectedColor))
                        } catch (e: Exception) {
                            ChampagneGold
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colorObj)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedPrice,
                        color = ChampagneGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(SlateElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Minus",
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = item.quantity.toString(),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(SlateElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Plus",
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PriceRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = if (isHighlight) EnergyCyan else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = if (isHighlight) EnergyCyan else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CheckoutDialog(
    currentLang: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, address: String, city: String, payment: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("بيروت") }
    var address by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf("Whish Money") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.2.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            color = CharcoalSurface,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.checkoutTitle(currentLang),
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Strings.fullName(currentLang)) },
                    placeholder = { Text("مثال: علي خوري") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateElevated,
                        unfocusedContainerColor = SlateElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ChampagneGold,
                        unfocusedBorderColor = SlateBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Strings.phoneNumber(currentLang)) },
                    placeholder = { Text("03xxxxxx / 70xxxxxx") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateElevated,
                        unfocusedContainerColor = SlateElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ChampagneGold,
                        unfocusedBorderColor = SlateBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("checkout_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text(Strings.city(currentLang)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateElevated,
                            unfocusedContainerColor = SlateElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ChampagneGold,
                            unfocusedBorderColor = SlateBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("checkout_city_input")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(Strings.deliveryAddress(currentLang)) },
                        placeholder = { Text("الشارع، البناية...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateElevated,
                            unfocusedContainerColor = SlateElevated,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ChampagneGold,
                            unfocusedBorderColor = SlateBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("checkout_address_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = Strings.paymentMethod(currentLang),
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Payment Options with prominent Whish Money
                val payments = listOf("Whish Money", "Cash on Delivery", "Credit Card")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    payments.forEach { p ->
                        val isSelected = selectedPayment == p
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) ChampagneGold.copy(alpha = 0.15f) else SlateElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) (if (p == "Whish Money") EnergyCyan else ChampagneGold) else SlateBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPayment = p }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (p) {
                                            "Whish Money" -> Icons.Default.AccountBalanceWallet
                                            "Credit Card" -> Icons.Default.CreditCard
                                            else -> Icons.Default.LocalAtm
                                        },
                                        contentDescription = null,
                                        tint = if (p == "Whish Money") EnergyCyan else ChampagneGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = when (p) {
                                                "Whish Money" -> Strings.payWhishMoney(currentLang)
                                                "Credit Card" -> Strings.payCard(currentLang)
                                                else -> Strings.payCod(currentLang)
                                            },
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        if (p == "Whish Money") {
                                            Text(
                                                text = Strings.whishMoneyNote(currentLang),
                                                color = EnergyCyan,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPayment = p },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = if (p == "Whish Money") EnergyCyan else ChampagneGold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val finalName = if (name.isBlank()) "زبون BoB Store" else name
                        val finalPhone = if (phone.isBlank()) "0096170198429" else phone
                        val finalAddress = if (address.isBlank()) "استلام عبر المتجر" else address
                        onConfirm(finalName, finalPhone, finalAddress, city, selectedPayment)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_order_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = ObsidianBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.confirmOrderBtn(currentLang),
                        color = ObsidianBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderSuccessDialog(
    order: Order,
    currentLang: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        LuxuryCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            elevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ObsidianBlack,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = Strings.orderSuccessTitle(currentLang),
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = Strings.trackingNumLabel(currentLang),
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SlateElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold)
                ) {
                    Text(
                        text = order.trackingNumber,
                        color = ChampagneGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "طريقة الدفع: ${order.paymentMethod}",
                    color = EnergyCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send Order to WhatsApp Button
                Button(
                    onClick = {
                        WhatsAppHelper.sendOrderToWhatsApp(context, order, "$${order.totalAmount}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Strings.sendOrderWhatsappBtn(currentLang),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "متابعة التسوق" else "Continue Shopping",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
