package com.example.bobstore.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bobstore.util.WhatsAppHelper
import com.example.ui.theme.*

// Official WhatsApp Green
val WhatsAppGreen = Color(0xFF25D366)
val WhatsAppDarkGreen = Color(0xFF128C7E)

@Composable
fun Luxury3DCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 4.dp,
    interactiveTilt: Boolean = false, // Disabled 3D tilt per user request
    content: @Composable BoxScope.() -> Unit
) {
    LuxuryCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        elevation = elevation,
        content = content
    )
}

@Composable
fun LuxuryCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 4.dp,
    borderColor: Color = SlateBorder,
    backgroundColor: Color = CharcoalSurface,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            ChampagneGold.copy(alpha = 0.35f),
            SlateBorder,
            SlateBorder.copy(alpha = 0.7f)
        ),
        start = Offset(0f, 0f),
        end = Offset(300f, 300f)
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x14000000),
                spotColor = Color(0x0F000000)
            )
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clip(shape)
            .background(backgroundColor)
    ) {
        content()
    }
}

@Composable
fun FloatingWhatsAppButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = WhatsAppGreen,
        shadowElevation = 10.dp,
        modifier = modifier
            .testTag("floating_whatsapp_btn")
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "WhatsApp",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "واتساب المتجر",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProductImageThumbnail(
    imageResName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val resId = remember(imageResName) {
        if (!imageResName.startsWith("content://") &&
            !imageResName.startsWith("file://") &&
            !imageResName.startsWith("http://") &&
            !imageResName.startsWith("https://")
        ) {
            context.resources.getIdentifier(imageResName, "drawable", context.packageName)
        } else 0
    }

    val model = remember(imageResName, resId) {
        coil.request.ImageRequest.Builder(context)
            .data(if (resId != 0) resId else imageResName)
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = model,
        contentDescription = "Accessory image",
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun LuxuryBadge(
    text: String,
    modifier: Modifier = Modifier,
    isGold: Boolean = true
) {
    val bgBrush = if (isGold) {
        Brush.horizontalGradient(listOf(ChampagneGoldDark, ChampagneGold))
    } else {
        Brush.horizontalGradient(listOf(RoyalViolet, RoyalVioletLight))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgBrush)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ColorDot(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val parsedColor = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            ChampagneGold
        }
    }

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) SlateElevated else Color.Transparent
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ChampagneGold else SlateBorder,
                shape = CircleShape
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(parsedColor)
            .clickable(onClick = onClick)
    )
}
