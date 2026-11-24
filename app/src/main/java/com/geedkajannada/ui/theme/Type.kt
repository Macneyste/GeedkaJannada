package com.geedkajannada.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.geedkajannada.R

val AmiriFamily = FontFamily(Font(R.font.amiri_regular, FontWeight.Normal))

val AppTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 64.sp, lineHeight = 70.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
)

/** Qoraalka Carabiga (dikhri iyo Quraan) — Amiri. */
fun arabicStyle(sizeSp: Int, lineHeightFactor: Float = 1.8f) = TextStyle(
    fontFamily = AmiriFamily,
    fontSize = sizeSp.sp,
    lineHeight = (sizeSp * lineHeightFactor).sp,
)
