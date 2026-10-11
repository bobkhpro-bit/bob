package com.example.bobstore.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bobstore.data.model.Category
import com.example.bobstore.data.model.PriceRangeFilter
import com.example.bobstore.data.model.ProductSortOption
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.ui.theme.*

/**
 * Filter and Sort Control Bar integrated into the product list header.
 * Provides instant 1-tap filtering for Sort, Brands, Price Ranges, and a full Filter Modal.
 */
@Composable
fun FilterAndSortHeader(
    sortOption: ProductSortOption,
    selectedBrand: String?,
    selectedPriceRange: PriceRangeFilter?,
    availableBrands: List<String>,
    priceRanges: List<PriceRangeFilter>,
    activeFilterCount: Int,
    currentLang: AppLanguage,
    onOpenFilterSheet: () -> Unit,
    onSelectSortOption: (ProductSortOption) -> Unit,
    onSelectBrand: (String?) -> Unit,
    onSelectPriceRange: (PriceRangeFilter?) -> Unit,
    onResetAllFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // TOP ROW: Filter Button (with badge) + Sort Dropdown Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Filter Sheet Trigger Button with active count badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (activeFilterCount > 0) ChampagneGold.copy(alpha = 0.15f) else SlateElevated,
                border = BorderStroke(
                    1.dp,
                    if (activeFilterCount > 0) ChampagneGold else SlateBorder
                ),
                modifier = Modifier
                    .testTag("filter_sheet_trigger_btn")
                    .clickable { onOpenFilterSheet() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = if (activeFilterCount > 0) ChampagneGold else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = Strings.filterButton(currentLang),
                        color = if (activeFilterCount > 0) ChampagneGold else TextPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (activeFilterCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(ChampagneGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeFilterCount.toString(),
                                color = ObsidianBlack,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 2. Sort Dropdown Pill Button
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SlateElevated,
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier
                        .testTag("sort_dropdown_btn")
                        .clickable { showSortMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Sort",
                            tint = ChampagneGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) sortOption.titleAr else sortOption.titleEn,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(SlateElevated)
                ) {
                    ProductSortOption.values().forEach { option ->
                        val isSelected = option == sortOption
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (currentLang == AppLanguage.ARABIC) option.titleAr else option.titleEn,
                                        color = if (isSelected) ChampagneGold else TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = ChampagneGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onSelectSortOption(option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        // PRICE RANGES HORIZONTAL ROW
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterPillChip(
                    label = Strings.allPrices(currentLang),
                    isSelected = selectedPriceRange == null,
                    onClick = { onSelectPriceRange(null) }
                )
            }
            items(priceRanges) { range ->
                val isSelected = selectedPriceRange == range
                FilterPillChip(
                    label = if (currentLang == AppLanguage.ARABIC) range.labelAr else range.labelEn,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelected) onSelectPriceRange(null) else onSelectPriceRange(range)
                    }
                )
            }
        }

        // BRANDS HORIZONTAL ROW (if available)
        if (availableBrands.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterPillChip(
                        label = Strings.allBrands(currentLang),
                        isSelected = selectedBrand == null,
                        onClick = { onSelectBrand(null) }
                    )
                }
                items(availableBrands) { brand ->
                    val isSelected = selectedBrand?.equals(brand, ignoreCase = true) == true
                    FilterPillChip(
                        label = brand,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) onSelectBrand(null) else onSelectBrand(brand)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Clean compact pill chip for quick filter options.
 */
@Composable
fun FilterPillChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) ChampagneGold.copy(alpha = 0.15f) else SlateElevated,
        border = BorderStroke(
            1.dp,
            if (isSelected) ChampagneGold else SlateBorder.copy(alpha = 0.7f)
        ),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (isSelected) ChampagneGold else TextSecondary,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

/**
 * Shows active removable chips for any currently active filters and a reset button.
 */
@Composable
fun ActiveFiltersSummaryRow(
    selectedCategoryId: String,
    selectedCategoryName: String?,
    selectedBrand: String?,
    selectedPriceRange: PriceRangeFilter?,
    sortOption: ProductSortOption,
    currentLang: AppLanguage,
    onClearCategory: () -> Unit,
    onClearBrand: () -> Unit,
    onClearPriceRange: () -> Unit,
    onClearSort: () -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = selectedCategoryId != "all" ||
            selectedBrand != null ||
            selectedPriceRange != null ||
            sortOption != ProductSortOption.DEFAULT

    if (!hasActiveFilters) return

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reset All Button
        item {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SlateElevated,
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.clickable(onClick = onResetAll)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear all",
                        tint = ChampagneGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = Strings.clearAllFilters(currentLang),
                        color = ChampagneGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Active Category
        if (selectedCategoryId != "all" && selectedCategoryName != null) {
            item {
                ActiveFilterTag(
                    label = selectedCategoryName,
                    onRemove = onClearCategory
                )
            }
        }

        // Active Brand
        if (selectedBrand != null) {
            item {
                ActiveFilterTag(
                    label = selectedBrand,
                    onRemove = onClearBrand
                )
            }
        }

        // Active Price Range
        if (selectedPriceRange != null) {
            item {
                val label = if (currentLang == AppLanguage.ARABIC) selectedPriceRange.labelAr else selectedPriceRange.labelEn
                ActiveFilterTag(
                    label = label,
                    onRemove = onClearPriceRange
                )
            }
        }

        // Active Sort (if non-default)
        if (sortOption != ProductSortOption.DEFAULT) {
            item {
                val label = if (currentLang == AppLanguage.ARABIC) sortOption.titleAr else sortOption.titleEn
                ActiveFilterTag(
                    label = label,
                    onRemove = onClearSort
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterTag(
    label: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SlateElevated,
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f)),
        modifier = Modifier.clickable(onClick = onRemove)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = TextSecondary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Comprehensive Bottom Sheet for fine-tuning Category, Brand, Price Range, and Sort options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterAndSortBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    categories: List<Category>,
    selectedCategoryId: String,
    onSelectCategory: (String) -> Unit,
    availableBrands: List<String>,
    selectedBrand: String?,
    onSelectBrand: (String?) -> Unit,
    priceRanges: List<PriceRangeFilter>,
    selectedPriceRange: PriceRangeFilter?,
    onSelectPriceRange: (PriceRangeFilter?) -> Unit,
    sortOption: ProductSortOption,
    onSelectSortOption: (ProductSortOption) -> Unit,
    matchingProductCount: Int,
    currentLang: AppLanguage,
    onResetAll: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = Strings.filterSheetTitle(currentLang),
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Divider(color = SlateBorder)

            // 1. Sort By Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = Strings.sortByTitle(currentLang),
                    color = ChampagneGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRowLayout(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp
                ) {
                    ProductSortOption.values().forEach { option ->
                        val isSelected = option == sortOption
                        FilterPillChip(
                            label = if (currentLang == AppLanguage.ARABIC) option.titleAr else option.titleEn,
                            isSelected = isSelected,
                            onClick = { onSelectSortOption(option) }
                        )
                    }
                }
            }

            // 2. Price Range Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = Strings.priceRangeTitle(currentLang),
                    color = ChampagneGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRowLayout(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp
                ) {
                    FilterPillChip(
                        label = Strings.allPrices(currentLang),
                        isSelected = selectedPriceRange == null,
                        onClick = { onSelectPriceRange(null) }
                    )
                    priceRanges.forEach { range ->
                        val isSelected = selectedPriceRange == range
                        FilterPillChip(
                            label = if (currentLang == AppLanguage.ARABIC) range.labelAr else range.labelEn,
                            isSelected = isSelected,
                            onClick = {
                                if (isSelected) onSelectPriceRange(null) else onSelectPriceRange(range)
                            }
                        )
                    }
                }
            }

            // 3. Brand Section
            if (availableBrands.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = Strings.brandsTitle(currentLang),
                        color = ChampagneGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRowLayout(
                        horizontalSpacing = 8.dp,
                        verticalSpacing = 8.dp
                    ) {
                        FilterPillChip(
                            label = Strings.allBrands(currentLang),
                            isSelected = selectedBrand == null,
                            onClick = { onSelectBrand(null) }
                        )
                        availableBrands.forEach { brand ->
                            val isSelected = selectedBrand?.equals(brand, ignoreCase = true) == true
                            FilterPillChip(
                                label = brand,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) onSelectBrand(null) else onSelectBrand(brand)
                                }
                            )
                        }
                    }
                }
            }

            // 4. Category Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (currentLang == AppLanguage.ARABIC) "الفئات" else "Categories",
                    color = ChampagneGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                FlowRowLayout(
                    horizontalSpacing = 8.dp,
                    verticalSpacing = 8.dp
                ) {
                    FilterPillChip(
                        label = Strings.allCategories(currentLang),
                        isSelected = selectedCategoryId == "all",
                        onClick = { onSelectCategory("all") }
                    )
                    categories.forEach { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        val name = if (currentLang == AppLanguage.ARABIC) cat.nameAr else cat.nameEn
                        FilterPillChip(
                            label = name,
                            isSelected = isSelected,
                            onClick = { onSelectCategory(cat.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // FOOTER: Reset and Apply buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onResetAll,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateElevated)
                ) {
                    Text(
                        text = Strings.clearAllFilters(currentLang),
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold)
                ) {
                    Text(
                        text = Strings.showResults(currentLang, matchingProductCount),
                        color = ObsidianBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Minimal lightweight FlowRow helper for chip wrapping without experimental warnings.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRowLayout(
    horizontalSpacing: androidx.compose.ui.unit.Dp,
    verticalSpacing: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        content()
    }
}
