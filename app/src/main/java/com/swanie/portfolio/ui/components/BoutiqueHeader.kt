package com.swanie.portfolio.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.R

@Composable
fun BoutiqueHeader(
    title: String,
    onBack: () -> Unit,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    textColor: Color = Color.White
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scaleFactor = if (screenWidth < 360) 0.85f else 1f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = textColor
            )
        }

        // Branding (Swan + Title)
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.swanie_foreground),
                contentDescription = "Swan Logo",
                modifier = Modifier.size((80 * scaleFactor).dp)
            )
            
            Text(
                text = title,
                fontSize = (24 * scaleFactor).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(top = 4.dp)
            )
        }

        // Action Button
        if (actionIcon != null && onAction != null) {
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
