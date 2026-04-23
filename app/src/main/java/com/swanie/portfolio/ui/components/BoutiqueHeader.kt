package com.swanie.portfolio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.R

@Composable
fun BoutiqueHeader(
    title: String,
    onBack: () -> Unit,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    actionLabel: String? = null,
    textColor: Color = Color.White,
    /**
     * Placed below the top bar (back / title / action), visually separated from navigation.
     * Widget manager passes the portfolio picker and sub-page tabs here.
     */
    belowBrandingContent: (@Composable () -> Unit)? = null,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp
    val scaleFactor = if (screenWidth < 360) 0.85f else 1f
    
    // Boutique Density Clamp: Prevent title from expanding into button safe-zones at high font scales
    val clampedFontSize = with(density) { 
        ((24 * scaleFactor).sp.toPx() / fontScale.coerceAtLeast(1.0f)).toSp()
    }

    val bottomPadding = when {
        belowBrandingContent != null -> 4.dp
        actionLabel != null -> 20.dp
        else -> 8.dp
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = bottomPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            // Back Button (Safe Zone: Start)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }

            // Branding (Swan + Title)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp), // Hard boundary to prevent button overlap
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.swanie_foreground),
                    contentDescription = "Swan Logo",
                    modifier = Modifier.size((80 * scaleFactor).dp)
                )

                Text(
                    text = title,
                    fontSize = clampedFontSize,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            // Action Button (Safe Zone: End)
            if (onAction != null) {
                if (actionLabel != null) {
                    Text(
                        text = actionLabel,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.6.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 12.dp)
                            .clickable { onAction() }
                    )
                } else if (actionIcon != null) {
                    IconButton(
                        onClick = onAction,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = "Action",
                            tint = textColor
                        )
                    }
                }
            }
        }

        if (belowBrandingContent != null) {
            Spacer(modifier = Modifier.height(10.dp))
            belowBrandingContent()
        }
    }
}
