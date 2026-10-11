package com.example.bobstore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobstore.data.model.Order
import com.example.bobstore.ui.components.Luxury3DCard
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.ui.theme.*

@Composable
fun OrdersTrackingScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val orders by viewModel.orders.collectAsState()
    var searchKeyword by remember { mutableStateOf("") }

    val filteredOrders = remember(orders, searchKeyword) {
        if (searchKeyword.isBlank()) orders
        else orders.filter {
            it.id.contains(searchKeyword, ignoreCase = true) ||
                    it.trackingNumber.contains(searchKeyword, ignoreCase = true) ||
                    it.customerName.contains(searchKeyword, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Text(
                text = Strings.trackTitle(currentLang),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Search Input
        item {
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = { searchKeyword = it },
                placeholder = {
                    Text(
                        text = Strings.searchTrackingPlaceholder(currentLang),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = ChampagneGold
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SlateElevated,
                    unfocusedContainerColor = CharcoalSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ChampagneGold,
                    unfocusedBorderColor = SlateBorder
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("track_search_input")
            )
        }

        if (filteredOrders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "لا توجد شحنات مطابقة" else "No matching orders found",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredOrders, key = { it.id }) { order ->
                OrderTrackingCard(
                    order = order,
                    currentLang = currentLang,
                    formattedTotal = viewModel.formatPrice(order.totalAmount),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun OrderTrackingCard(
    order: Order,
    currentLang: AppLanguage,
    formattedTotal: String,
    modifier: Modifier = Modifier
) {
    Luxury3DCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        elevation = 6.dp,
        interactiveTilt = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: ID & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.id,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${Strings.trackingNumLabel(currentLang)} ${order.trackingNumber}",
                        color = ChampagneGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getStatusColor(order.status).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, getStatusColor(order.status))
                ) {
                    Text(
                        text = getLocalizedStatus(order.status, currentLang),
                        color = getStatusColor(order.status),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Timeline
            TrackingTimeline(status = order.status, currentLang = currentLang)

            Spacer(modifier = Modifier.height(14.dp))

            // Items Summary
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SlateElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.productName}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${item.unitPrice * item.quantity}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = order.customerAddress + " (${order.city})",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = formattedTotal,
                            color = ChampagneGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrackingTimeline(
    status: String,
    currentLang: AppLanguage
) {
    val steps = listOf("PENDING", "PROCESSING", "SHIPPED", "DELIVERED")
    val currentIdx = steps.indexOf(status).let { if (it >= 0) it else 0 }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, s ->
            val isDone = idx <= currentIdx
            val isCurrent = idx == currentIdx

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrent) ChampagneGold
                            else if (isDone) SuccessGreen
                            else SlateBorder
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ObsidianBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (s) {
                        "PENDING" -> if (currentLang == AppLanguage.ARABIC) "مراجعة" else "Pending"
                        "PROCESSING" -> if (currentLang == AppLanguage.ARABIC) "تجهيز" else "Process"
                        "SHIPPED" -> if (currentLang == AppLanguage.ARABIC) "شحن" else "Shipped"
                        else -> if (currentLang == AppLanguage.ARABIC) "تسليم" else "Done"
                    },
                    color = if (isDone) TextPrimary else TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "DELIVERED" -> SuccessGreen
        "SHIPPED" -> EnergyCyan
        "PROCESSING" -> ChampagneGold
        "CANCELLED" -> DangerRed
        else -> TextSecondary
    }
}

fun getLocalizedStatus(status: String, lang: AppLanguage): String {
    return when (status) {
        "PENDING" -> Strings.statusPending(lang)
        "PROCESSING" -> Strings.statusProcessing(lang)
        "SHIPPED" -> Strings.statusShipped(lang)
        "DELIVERED" -> Strings.statusDelivered(lang)
        else -> status
    }
}
