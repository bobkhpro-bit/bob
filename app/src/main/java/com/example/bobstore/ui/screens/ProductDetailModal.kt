package com.example.bobstore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.DialogProperties
import com.example.bobstore.data.model.Product
import com.example.bobstore.data.model.ProductReview
import com.example.bobstore.ui.components.ColorDot
import com.example.bobstore.ui.components.LuxuryBadge
import com.example.bobstore.ui.components.ProductImageThumbnail
import com.example.bobstore.ui.components.WhatsAppGreen
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.bobstore.util.WhatsAppHelper
import com.example.ui.theme.*

@Composable
fun ProductDetailModal(
    product: Product,
    viewModel: BoBViewModel? = null,
    currentLang: AppLanguage,
    formattedPrice: String,
    similarProducts: List<Product> = emptyList(),
    onDismiss: () -> Unit,
    onSelectSimilarProduct: (Product) -> Unit = {},
    onAddToCart: (color: String, size: String) -> Unit,
    onBuyNow: (color: String, size: String) -> Unit
) {
    val context = LocalContext.current
    var selectedColor by remember(product) { mutableStateOf(product.colors.firstOrNull() ?: "#D4AF37") }
    var selectedSize by remember(product) { mutableStateOf(product.sizes.firstOrNull() ?: "Universal") }
    var displayedImage by remember(product) { mutableStateOf(product.imageRes) }

    // Real Customer Reviews state
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var userRatingInput by remember { mutableStateOf(5) }
    var userCommentInput by remember { mutableStateOf("") }
    val productReviews = remember(product, viewModel) {
        viewModel?.getProductReviews(product.id) ?: listOf(
            ProductReview(
                productId = product.id,
                userName = "سامر حيدر",
                rating = 5,
                comment = "قطعة فخمة جداً، التيتانيوم متين وأصلي 100% ويستاهل كل ليرة!",
                date = "منذ يومين"
            ),
            ProductReview(
                productId = product.id,
                userName = "نور الدين عازار",
                rating = 5,
                comment = "الشحن كان سريع جداً والباكج راقي، شكراً متجر BoB.",
                date = "منذ 4 أيام"
            )
        )
    }

    val name = if (currentLang == AppLanguage.ARABIC) product.nameAr else product.nameEn
    val desc = if (currentLang == AppLanguage.ARABIC) product.descAr else product.descEn
    val badge = if (currentLang == AppLanguage.ARABIC) product.badgeAr else product.badgeEn

    val allGalleryImages = remember(product) {
        listOf(product.imageRes) + product.additionalImages
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.2.dp, ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
            color = CharcoalSurface,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!badge.isNullOrBlank()) {
                            LuxuryBadge(text = badge, isGold = true)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = Strings.inStock(currentLang, product.stock),
                            color = SuccessGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SlateElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Clean Flat Product Image Showcase
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SlateElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        ProductImageThumbnail(
                            imageResName = displayedImage,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gallery Thumbnails (if multiple images)
                    if (allGalleryImages.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allGalleryImages) { img ->
                                val isSelected = displayedImage == img
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SlateElevated,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.5.dp,
                                        if (isSelected) ChampagneGold else SlateBorder
                                    ),
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clickable { displayedImage = img }
                                ) {
                                    ProductImageThumbnail(imageResName = img, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Product Title & Rating
                    Text(
                        text = name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 23.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = formattedPrice,
                            color = ChampagneGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = ChampagneGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${product.rating} (${product.reviewsCount} تقييم)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Size / Device Model Selector
                    Text(
                        text = Strings.selectSize(currentLang),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(product.sizes) { size ->
                            val isSelected = selectedSize == size
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ChampagneGold else SlateElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ChampagneGoldLight else SlateBorder
                                ),
                                modifier = Modifier.clickable { selectedSize = size }
                            ) {
                                Text(
                                    text = size,
                                    color = if (isSelected) ObsidianBlack else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Picker
                    Text(
                        text = Strings.selectColor(currentLang),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        product.colors.forEach { hex ->
                            ColorDot(
                                colorHex = hex,
                                isSelected = selectedColor == hex,
                                onClick = { selectedColor = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Direct WhatsApp Inquiry Button (with prefilled product query)
                    Button(
                        onClick = {
                            WhatsAppHelper.openProductInquiry(context, name, formattedPrice)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("whatsapp_inquire_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.whatsappInquireProduct(currentLang),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = Strings.specifications(currentLang),
                                color = ChampagneGoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = desc,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // SIMILAR PRODUCTS SECTION (منتجات مشابهة)
                    if (similarProducts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "منتجات مشابهة قد تعجبك" else "Similar Products You May Like",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(similarProducts) { sim ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SlateElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clickable { onSelectSimilarProduct(sim) }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(75.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(CharcoalSurface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ProductImageThumbnail(
                                                imageResName = sim.imageRes,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (currentLang == AppLanguage.ARABIC) sim.nameAr else sim.nameEn,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$${sim.price}",
                                            color = ChampagneGold,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // REAL CUSTOMER REVIEWS SECTION (تقييمات الزبائن الحقيقية)
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RateReview,
                                        contentDescription = null,
                                        tint = ChampagneGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLang == AppLanguage.ARABIC) "تقييمات الزبائن الحقيقية (${productReviews.size})" else "Verified Customer Reviews (${productReviews.size})",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Button for customer to add real review
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ChampagneGold,
                                    modifier = Modifier.clickable { showAddReviewDialog = true }
                                ) {
                                    Text(
                                        text = if (currentLang == AppLanguage.ARABIC) "+ أضف تقييمك" else "+ Add Review",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Reviews List
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                productReviews.forEach { rev ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = CharcoalSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountCircle,
                                                        contentDescription = null,
                                                        tint = TextSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = rev.userName,
                                                        color = TextPrimary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    repeat(5) { starIndex ->
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = if (starIndex < rev.rating) Color(0xFFF59E0B) else SoftGrey,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = rev.date,
                                                        color = TextSecondary,
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = rev.comment,
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Action Buttons: Add To Cart & Buy Now
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAddToCart(selectedColor, selectedSize) },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateElevated),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("detail_add_to_cart_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            tint = ChampagneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.addToCart(currentLang),
                            color = ChampagneGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onBuyNow(selectedColor, selectedSize) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(46.dp)
                            .testTag("detail_buy_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = ObsidianBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.buyNow(currentLang),
                            color = ObsidianBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    // Add Review Dialog
    if (showAddReviewDialog) {
        Dialog(onDismissRequest = { showAddReviewDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentLang == AppLanguage.ARABIC) "أضف تقييمك الحقيقي للمنتج" else "Add Your Verified Review",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5-Star interactive selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i stars",
                                tint = if (i <= userRatingInput) Color(0xFFF59E0B) else SoftGrey,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { userRatingInput = i }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = userCommentInput,
                        onValueChange = { userCommentInput = it },
                        placeholder = {
                            Text(
                                if (currentLang == AppLanguage.ARABIC) "اكتب رأيك الصادق في جودة المنتج والخامات..." else "Write your honest feedback on product quality...",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddReviewDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Strings.cancel(currentLang), color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                if (userCommentInput.isNotBlank()) {
                                    viewModel?.addProductReview(product.id, userRatingInput, userCommentInput)
                                    showAddReviewDialog = false
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (currentLang == AppLanguage.ARABIC) "نشر التقييم" else "Post Review",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
