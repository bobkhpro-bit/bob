package com.example.bobstore.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobstore.data.model.*
import com.example.bobstore.ui.components.*
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.bobstore.ui.viewmodel.VisualSearchUiState
import com.example.bobstore.util.WhatsAppHelper
import com.example.ui.theme.*

@Composable
fun CustomerHomeScreen(
    viewModel: BoBViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val storeCustomization by viewModel.storeCustomization.collectAsState()
    val isAiSearching by viewModel.isAiSearching.collectAsState()
    val aiSearchInsight by viewModel.aiSearchInsight.collectAsState()
    val isVisualSearchModalOpen by viewModel.isVisualSearchModalOpen.collectAsState()
    val visualSearchState by viewModel.visualSearchState.collectAsState()

    // Filter & Sort States
    val selectedBrand by viewModel.selectedBrand.collectAsState()
    val selectedPriceRange by viewModel.selectedPriceRange.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val availableBrands by viewModel.availableBrands.collectAsState()
    val activeFilterCount by viewModel.activeFilterCount.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }

    val quickPriceRanges = remember {
        listOf(
            PriceRangeFilter(minPrice = null, maxPrice = 30.0, labelAr = "أقل من 30$", labelEn = "< $30"),
            PriceRangeFilter(minPrice = 30.0, maxPrice = 60.0, labelAr = "30$ - 60$", labelEn = "$30 - $60"),
            PriceRangeFilter(minPrice = 60.0, maxPrice = 90.0, labelAr = "60$ - 90$", labelEn = "$60 - $90"),
            PriceRangeFilter(minPrice = 90.0, maxPrice = null, labelAr = "+90$", labelEn = "$90+")
        )
    }

    val isAiProcessing = isAiSearching

    // AI Search Bar Subtle Loading Animation
    val infiniteTransition = rememberInfiniteTransition(label = "ai_search_bar_loading")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Hero Luxury Banner (Flat & Clean, No 3D, Custom Branding)
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroCleanBanner(
                    currentLang = currentLang,
                    customization = storeCustomization,
                    onExploreClick = { viewModel.setCategory("all") },
                    onWhatsAppClick = { WhatsAppHelper.openGeneralInquiry(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            // AI-Integrated Smart Search Box & Visual Search Trigger
            // AI-Integrated Interactive Search Bar (Clickable search icon, instant results, no camera, no shortcuts)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    // Header Badge with live AI feedback
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isAiProcessing) EnergyCyan else ChampagneGold,
                                modifier = Modifier
                                    .size(15.dp)
                                    .then(if (isAiProcessing) Modifier.graphicsLayer(scaleX = glowScale, scaleY = glowScale) else Modifier)
                            )
                            Text(
                                text = Strings.aiSearchBadge(currentLang),
                                color = if (isAiProcessing) EnergyCyan else ChampagneGoldLight,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isAiProcessing) {
                            // Live animated status micro-chip for subtle feedback
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EnergyCyan.copy(alpha = 0.15f))
                                    .border(1.dp, EnergyCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EnergyCyan.copy(alpha = pulseAlpha))
                                )
                                Text(
                                    text = Strings.aiProcessingText(currentLang),
                                    color = EnergyCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = if (currentLang == AppLanguage.ARABIC) "مساعد BoB الذكي" else "BoB AI Engine",
                                color = TextSecondary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // AI Search Bar Container with Dynamic Interactive Glowing Border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                color = CharcoalSurface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .then(
                                if (isAiProcessing) {
                                    Modifier.border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                EnergyCyan.copy(alpha = 0.3f),
                                                EnergyCyan,
                                                ChampagneGold,
                                                EnergyCyan
                                            ),
                                            start = Offset(shimmerPhase * 800f, 0f),
                                            end = Offset((shimmerPhase + 1f) * 800f, 300f)
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                } else {
                                    Modifier.border(
                                        width = 1.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                SlateBorder,
                                                ChampagneGold.copy(alpha = 0.35f),
                                                SlateBorder
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            )
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = {
                                Text(
                                    text = Strings.searchPlaceholder(currentLang),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                // Interactive Clickable Search Icon: Tapping directly outputs results
                                IconButton(
                                    onClick = { viewModel.triggerAiSearch(searchQuery) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .testTag("search_icon_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "بحث",
                                        tint = if (isAiProcessing) EnergyCyan else ChampagneGold,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .then(if (isAiProcessing) Modifier.graphicsLayer(scaleX = glowScale, scaleY = glowScale) else Modifier)
                                    )
                                }
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.clearAiSearch() },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Interactive AI Search Action Trigger
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isAiProcessing) EnergyCyan.copy(alpha = 0.18f) else SlateElevated,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isAiProcessing) EnergyCyan else ChampagneGold.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier
                                            .clickable { viewModel.triggerAiSearch(searchQuery) }
                                            .testTag("ai_search_submit_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "AI Search",
                                                tint = if (isAiProcessing) EnergyCyan else ChampagneGold,
                                                modifier = Modifier
                                                    .size(15.dp)
                                                    .then(if (isAiProcessing) Modifier.graphicsLayer(rotationZ = rotationAngle) else Modifier)
                                            )
                                            Text(
                                                text = if (isAiProcessing) {
                                                    if (currentLang == AppLanguage.ARABIC) "جاري التحليل..." else "Thinking..."
                                                } else {
                                                    if (currentLang == AppLanguage.ARABIC) "بحث ذكي" else "AI Search"
                                                },
                                                color = if (isAiProcessing) EnergyCyan else TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.triggerAiSearch(searchQuery) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input")
                        )

                        // Subtle glowing light beam at the bottom edge while processing
                        val beamAlpha by animateFloatAsState(
                            targetValue = if (isAiProcessing) 1f else 0f,
                            animationSpec = tween(300),
                            label = "beam_alpha"
                        )
                        if (beamAlpha > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp)
                                    .align(Alignment.BottomCenter)
                                    .graphicsLayer(alpha = beamAlpha)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                EnergyCyan.copy(alpha = 0.2f),
                                                EnergyCyan,
                                                EnergyCyan.copy(alpha = 0.8f),
                                                EnergyCyan,
                                                EnergyCyan.copy(alpha = 0.2f),
                                                Color.Transparent
                                            ),
                                            startX = (shimmerPhase - 0.25f) * 1200f,
                                            endX = (shimmerPhase + 0.25f) * 1200f
                                        )
                                    )
                            )
                        }
                    }

                    // Interactive AI Insight Banner (Shown when AI search outputs results)
                    if (aiSearchInsight != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CharcoalSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, EnergyCyan.copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = EnergyCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = aiSearchInsight ?: "",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = if (currentLang == AppLanguage.ARABIC) "✨ تم استخراج النتائج بالذكاء الاصطناعي بنجاح" else "AI matched matching store items",
                                            color = ChampagneGoldLight,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.clearAiSearch() },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                CategorySelectorRow(
                    categories = categories,
                    selectedCategoryId = selectedCatId,
                    currentLang = currentLang,
                    onSelectCategory = { viewModel.setCategory(it) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Integrated Filtering and Sorting Control Header (Sort, Brand, Price Range, Modal trigger)
            item(span = { GridItemSpan(maxLineSpan) }) {
                FilterAndSortHeader(
                    sortOption = sortOption,
                    selectedBrand = selectedBrand,
                    selectedPriceRange = selectedPriceRange,
                    availableBrands = availableBrands,
                    priceRanges = quickPriceRanges,
                    activeFilterCount = activeFilterCount,
                    currentLang = currentLang,
                    onOpenFilterSheet = { showFilterSheet = true },
                    onSelectSortOption = { viewModel.setSortOption(it) },
                    onSelectBrand = { viewModel.setBrand(it) },
                    onSelectPriceRange = { viewModel.setPriceRange(it) },
                    onResetAllFilters = { viewModel.resetAllFilters() }
                )
            }

            // Active Filters Summary Strip (Shown when any filter or non-default sort is active)
            item(span = { GridItemSpan(maxLineSpan) }) {
                val selectedCatName = categories.find { it.id == selectedCatId }?.let {
                    if (currentLang == AppLanguage.ARABIC) it.nameAr else it.nameEn
                }
                ActiveFiltersSummaryRow(
                    selectedCategoryId = selectedCatId,
                    selectedCategoryName = selectedCatName,
                    selectedBrand = selectedBrand,
                    selectedPriceRange = selectedPriceRange,
                    sortOption = sortOption,
                    currentLang = currentLang,
                    onClearCategory = { viewModel.setCategory("all") },
                    onClearBrand = { viewModel.setBrand(null) },
                    onClearPriceRange = { viewModel.setPriceRange(null) },
                    onClearSort = { viewModel.setSortOption(ProductSortOption.DEFAULT) },
                    onResetAll = { viewModel.resetAllFilters() }
                )
            }

            // Section Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Strings.featuredSection(currentLang),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${products.size} ${if (currentLang == AppLanguage.ARABIC) "قطعة" else "items"}",
                        color = ChampagneGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Products Grid
            if (products.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (currentLang == AppLanguage.ARABIC) "لم يتم العثور على قطع تطابق بحثك" else "No matching accessories found",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(products, key = { it.id }) { product ->
                    ProductGridCard(
                        product = product,
                        currentLang = currentLang,
                        isFavorite = wishlistIds.contains(product.id),
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Floating WhatsApp Action Button in Bottom Corner
        FloatingWhatsAppButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp),
            onClick = {
                WhatsAppHelper.openGeneralInquiry(context)
            }
        )

        // AI Visual Search Modal Dialog
        if (isVisualSearchModalOpen) {
            VisualSearchModal(
                viewModel = viewModel,
                onDismiss = { viewModel.closeVisualSearchModal() }
            )
        }

        // Interactive Filter & Sort Bottom Sheet
        FilterAndSortBottomSheet(
            isOpen = showFilterSheet,
            onDismiss = { showFilterSheet = false },
            categories = categories,
            selectedCategoryId = selectedCatId,
            onSelectCategory = { viewModel.setCategory(it) },
            availableBrands = availableBrands,
            selectedBrand = selectedBrand,
            onSelectBrand = { viewModel.setBrand(it) },
            priceRanges = quickPriceRanges,
            selectedPriceRange = selectedPriceRange,
            onSelectPriceRange = { viewModel.setPriceRange(it) },
            sortOption = sortOption,
            onSelectSortOption = { viewModel.setSortOption(it) },
            matchingProductCount = products.size,
            currentLang = currentLang,
            onResetAll = { viewModel.resetAllFilters() }
        )
    }
}

@Composable
fun HeroCleanBanner(
    currentLang: AppLanguage,
    customization: com.example.bobstore.data.model.StoreCustomization? = null,
    onExploreClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (currentLang == AppLanguage.ARABIC) {
        customization?.bannerTitleAr ?: "إكسسوارات هواتف حصرية"
    } else {
        customization?.bannerTitleEn ?: "Elite Smartphone Gear"
    }

    val subtitle = if (currentLang == AppLanguage.ARABIC) {
        customization?.bannerSubtitleAr ?: "كفرات أصلية، منصات شحن Qi2، ودروع حماية"
    } else {
        customization?.bannerSubtitleEn ?: "Titanium cases, Qi2 chargers & camera armor"
    }

    val bannerImg = customization?.bannerImageRes ?: "hero_luxury_case"

    LuxuryCard(
        modifier = modifier.height(165.dp),
        cornerRadius = 18.dp,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                LuxuryBadge(
                    text = if (currentLang == AppLanguage.ARABIC) "مجموعة BoB الأصلية" else "AUTHENTIC BoB GEAR",
                    isGold = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    color = ChampagneGoldLight,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) "تصفح المتجر" else "Shop Now",
                            color = ObsidianBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onWhatsAppClick,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateElevated),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "واتساب",
                            color = WhatsAppGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                ProductImageThumbnail(
                    imageResName = bannerImg,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}

@Composable
fun CategorySelectorRow(
    categories: List<Category>,
    selectedCategoryId: String,
    currentLang: AppLanguage,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val isSelected = selectedCategoryId == "all"
            CategoryChip(
                label = Strings.allCategories(currentLang),
                isSelected = isSelected,
                onClick = { onSelectCategory("all") }
            )
        }

        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCategoryId == cat.id
            val name = if (currentLang == AppLanguage.ARABIC) cat.nameAr else cat.nameEn
            CategoryChip(
                label = name,
                isSelected = isSelected,
                onClick = { onSelectCategory(cat.id) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) ChampagneGold else SlateElevated,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) ChampagneGoldLight else SlateBorder
        ),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) ObsidianBlack else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    currentLang: AppLanguage,
    isFavorite: Boolean,
    formattedPrice: String,
    onProductClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToCart: () -> Unit,
    onWhatsAppInquire: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = if (currentLang == AppLanguage.ARABIC) product.nameAr else product.nameEn
    val desc = if (currentLang == AppLanguage.ARABIC) product.descAr else product.descEn
    val badge = if (currentLang == AppLanguage.ARABIC) product.badgeAr else product.badgeEn

    LuxuryCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick)
            .testTag("product_card_${product.id}"),
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Top Row: Badge & Wishlist Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!badge.isNullOrBlank()) {
                    LuxuryBadge(text = badge, isGold = product.isFeatured == 1)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isFavorite) DangerRed else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product Image Thumbnail loaded via Coil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateElevated),
                contentAlignment = Alignment.Center
            ) {
                ProductImageThumbnail(
                    imageResName = product.imageRes,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Name & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = product.rating.toString(),
                        color = ChampagneGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Short Description
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 10.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price & Original Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formattedPrice,
                    color = ChampagneGold,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (product.originalPrice != null) {
                    Text(
                        text = "$${product.originalPrice}",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row: WhatsApp & Add to Cart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Inquire Icon
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WhatsAppGreen.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(onClick = onWhatsAppInquire)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp Inquire",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Add to Cart Button
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = SlateElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("add_to_cart_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = Strings.addToCart(currentLang),
                        color = ChampagneGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCleanCard(
    product: Product,
    currentLang: AppLanguage,
    isFavorite: Boolean,
    formattedPrice: String,
    onProductClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddToCart: () -> Unit,
    onWhatsAppInquire: () -> Unit,
    modifier: Modifier = Modifier
) {
    val name = if (currentLang == AppLanguage.ARABIC) product.nameAr else product.nameEn
    val desc = if (currentLang == AppLanguage.ARABIC) product.descAr else product.descEn
    val badge = if (currentLang == AppLanguage.ARABIC) product.badgeAr else product.badgeEn

    LuxuryCard(
        modifier = modifier
            .clickable(onClick = onProductClick)
            .testTag("product_card_${product.id}"),
        cornerRadius = 18.dp,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Badges & Wishlist Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!badge.isNullOrBlank()) {
                    LuxuryBadge(text = badge, isGold = product.isFeatured == 1)
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isFavorite) DangerRed else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Product Visual Showcase (Crisp and Clean)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SlateElevated),
                contentAlignment = Alignment.Center
            ) {
                ProductImageThumbnail(
                    imageResName = product.imageRes,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product Name & Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = ChampagneGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = product.rating.toString(),
                        color = ChampagneGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = desc,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Available Sizes Count / Compatibility
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SlateElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                ) {
                    Text(
                        text = "${product.sizes.size} مقاسات وموديلات",
                        color = EnergyCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Price & Actions (Add to Cart + WhatsApp Inquire)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formattedPrice,
                        color = ChampagneGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (product.originalPrice != null) {
                        Text(
                            text = "$${product.originalPrice}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Quick WhatsApp Inquiry Icon Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WhatsAppGreen.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppGreen),
                        modifier = Modifier
                            .clickable(onClick = onWhatsAppInquire)
                            .size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp Inquire",
                                tint = WhatsAppGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Add to Cart Button
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("add_to_cart_${product.id}")
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
                }
            }
        }
    }
}
