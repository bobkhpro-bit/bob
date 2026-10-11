package com.example.bobstore.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.bobstore.data.model.Product
import com.example.bobstore.ui.components.ProductImageThumbnail
import com.example.bobstore.ui.i18n.AppLanguage
import com.example.bobstore.ui.i18n.Strings
import com.example.bobstore.ui.viewmodel.BoBViewModel
import com.example.bobstore.ui.viewmodel.VisualSearchUiState
import com.example.ui.theme.*

@Composable
fun VisualSearchModal(
    viewModel: BoBViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val visualState by viewModel.visualSearchState.collectAsState()

    // Camera Launcher for capturing photo directly
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { capturedBitmap ->
        if (capturedBitmap != null) {
            viewModel.performVisualSearch(capturedBitmap)
        }
    }

    // Gallery Photo Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                null
            }
            if (bitmap != null) {
                viewModel.performVisualSearch(bitmap)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, ChampagneGold.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
            color = ObsidianBlack,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(ChampagneGoldDark, EnergyCyan)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = Strings.visualSearchTitle(currentLang),
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Gemini Multimodal AI Vision",
                                color = EnergyCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(CharcoalSurface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (val state = visualState) {
                    is VisualSearchUiState.Idle -> {
                        VisualSearchIdleView(
                            currentLang = currentLang,
                            onTakePhoto = { cameraLauncher.launch(null) },
                            onPickGallery = {
                                galleryLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        )
                    }

                    is VisualSearchUiState.Analyzing -> {
                        VisualSearchAnalyzingView(
                            bitmap = state.previewBitmap,
                            currentLang = currentLang
                        )
                    }

                    is VisualSearchUiState.Success -> {
                        VisualSearchResultView(
                            result = state.result,
                            bitmap = state.image,
                            currentLang = currentLang,
                            viewModel = viewModel,
                            onRetake = {
                                viewModel.openVisualSearchModal()
                            },
                            onApply = {
                                viewModel.applyVisualSearchResultToStore(state.result)
                            }
                        )
                    }

                    is VisualSearchUiState.Error -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.message,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.openVisualSearchModal() },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold)
                            ) {
                                Text(Strings.retakePhoto(currentLang), color = ChampagneGold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualSearchIdleView(
    currentLang: AppLanguage,
    onTakePhoto: () -> Unit,
    onPickGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = Strings.visualSearchSubtitle(currentLang),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }

            // Central Visual Art / Scanner Illustration
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                ChampagneGoldDark.copy(alpha = 0.35f),
                                CharcoalSurface
                            )
                        )
                    )
                    .border(2.dp, ChampagneGold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = ChampagneGoldLight,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Features Bullet Points
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SlateElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EnergyCyan, modifier = Modifier.size(16.dp))
                        Text("التعرف على الكفرات ومواد الصنع (تيتانيوم، كربون، جلد)", color = TextPrimary, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EnergyCyan, modifier = Modifier.size(16.dp))
                        Text("كشف الشواحن اللاسلكية ومنصات Qi2 و MagSafe", color = TextPrimary, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EnergyCyan, modifier = Modifier.size(16.dp))
                        Text("مطابقة فورية لأقوى البدائل والمنتجات المماثلة في المتجر", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Action Buttons Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onTakePhoto,
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("visual_search_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.takePhotoAction(currentLang),
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onPickGallery,
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("visual_search_gallery_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = EnergyCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.pickGalleryAction(currentLang),
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun VisualSearchAnalyzingView(
    bitmap: Bitmap?,
    currentLang: AppLanguage
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (bitmap != null) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, ChampagneGold, RoundedCornerShape(20.dp))
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Scanning",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Laser scan line overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (220 * laserOffset).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, EnergyCyan, Color.Transparent)
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(
            color = ChampagneGold,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Strings.analyzingImage(currentLang),
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gemini AI Vision Multimodal Processing...",
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun VisualSearchResultView(
    result: com.example.bobstore.data.remote.gemini.VisualAnalysisResult,
    bitmap: Bitmap,
    currentLang: AppLanguage,
    viewModel: BoBViewModel,
    onRetake: () -> Unit,
    onApply: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Recognition Summary Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "User Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp))
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = Strings.productRecognized(currentLang),
                                color = SuccessGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (currentLang == AppLanguage.ARABIC) result.identifiedItemAr else result.identifiedItemEn,
                            color = ChampagneGoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = result.descriptionAr,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Detected Features Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                result.features.take(4).forEach { feat ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SlateElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                    ) {
                        Text(
                            text = "✦ $feat",
                            color = EnergyCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Section Title: Matched Products
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.similarStoreProducts(currentLang),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${result.matchedProducts.size} منتجات متطابقة",
                    color = ChampagneGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Matched Products List
        items(result.matchedProducts, key = { it.id }) { product ->
            VisualMatchedProductCard(
                product = product,
                currentLang = currentLang,
                viewModel = viewModel,
                onViewProduct = { viewModel.selectProduct(product) },
                onAddToCart = { viewModel.addToCart(product) }
            )
        }

        // Footer Actions
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRetake,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Strings.retakePhoto(currentLang), color = TextPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        Strings.applySearchInStore(currentLang),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun VisualMatchedProductCard(
    product: Product,
    currentLang: AppLanguage,
    viewModel: BoBViewModel,
    onViewProduct: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SlateElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewProduct)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductImageThumbnail(
                imageResName = product.imageRes,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalSurface)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (currentLang == AppLanguage.ARABIC) product.nameAr else product.nameEn,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = viewModel.formatPrice(product.price),
                    color = ChampagneGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${product.rating} (${product.reviewsCount})",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CharcoalSurface
                    ) {
                        Text(
                            text = "متطابق 98%",
                            color = EnergyCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Quick Add to Cart
            IconButton(
                onClick = onAddToCart,
                modifier = Modifier
                    .size(38.dp)
                    .background(CharcoalSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = "Add to Cart",
                    tint = ChampagneGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
