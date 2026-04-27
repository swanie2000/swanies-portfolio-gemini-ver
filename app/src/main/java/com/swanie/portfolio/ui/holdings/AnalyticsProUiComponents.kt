package com.swanie.portfolio.ui.holdings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swanie.portfolio.ui.theme.ProPalette

@Composable
fun ProInsightMiniCard(
    title: String,
    textColor: Color,
    containerColor: Color = ProPalette.SurfaceElevated,
    accentColor: Color = ProPalette.Accent,
    borderColor: Color = ProPalette.NeutralBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val compact = LocalConfiguration.current.screenWidthDp < 390
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 11.dp else 14.dp,
                vertical = if (compact) 9.dp else 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = accentColor,
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                HorizontalDivider(
                    color = textColor.copy(alpha = 0.12f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(2.dp))
                content()
            }
        )
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    textColor: Color,
    accentColor: Color
) {
    val compact = LocalConfiguration.current.screenWidthDp < 390
    Surface(
        color = textColor.copy(alpha = 0.06f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 5.dp else 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label ",
                color = textColor.copy(alpha = 0.75f),
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = accentColor,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun PremiumHeroStripe(
    title: String,
    subtitle: String,
    textColor: Color,
    accentColor: Color,
    borderColor: Color
) {
    val compact = LocalConfiguration.current.screenWidthDp < 390
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = textColor.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (compact) 11.dp else 14.dp,
                vertical = if (compact) 9.dp else 12.dp
            )
        ) {
            Text(
                text = title,
                color = accentColor,
                fontSize = if (compact) 11.sp else 12.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                color = textColor.copy(alpha = 0.8f),
                fontSize = if (compact) 11.sp else 12.sp,
                lineHeight = if (compact) 15.sp else 17.sp
            )
        }
    }
}

@Composable
fun ModelChip(
    label: String,
    textColor: Color,
    accentColor: Color
) {
    val compact = LocalConfiguration.current.screenWidthDp < 390
    Surface(
        color = accentColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 5.dp else 6.dp
            ),
            color = textColor,
            fontSize = if (compact) 9.sp else 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.4.sp
        )
    }
}
