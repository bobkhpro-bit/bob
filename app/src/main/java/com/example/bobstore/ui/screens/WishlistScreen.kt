package com.example.bobstore.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.bobstore.util.WhatsAppHelper
import com.example.ui.theme.*

@Composable
fun WishlistScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val allProducts by viewModel.products.collectAsState()

    val favProducts = remember(wishlistIds, allProducts) {
        allProducts.filter { wishlistIds.contains(it.id) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        item {
            Text(
                text = Strings.tabWishlist(currentLang),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (favProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(SlateElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = DangerRed.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "قائمة المفضلة فارغة" else "Your Wishlist is Empty",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "اضغط على رمز القلب في أي قطعة لحفظها هنا" else "Tap the heart icon on any accessory to save it here",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(favProducts, key = { it.id }) { product ->
                ProductCleanCard(
                    product = product,
                    currentLang = currentLang,
                    isFavorite = true,
                    formattedPrice = viewModel.formatPrice(product.price),
                    onProductClick = { viewModel.selectProduct(product) },
                    onFavoriteToggle = { viewModel.toggleWishlist(product.id) },
                    onAddToCart = { viewModel.addToCart(product) },
                    onWhatsAppInquire = {
                        WhatsAppHelper.openProductInquiry(
                            context,
                            if (currentLang == AppLanguage.ARABIC) product.nameAr else product.nameEn,
                            viewModel.formatPrice(product.price)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
