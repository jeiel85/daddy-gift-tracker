package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import java.text.DecimalFormat

object FormatUtils {
    private val decimalFormat = DecimalFormat("#,###")

    fun formatMoney(amount: Long): String {
        return decimalFormat.format(amount) + "원"
    }

    fun formatMoneyKoreanText(amount: Long): String {
        if (amount == 0L) return "0원"
        val man = amount / 10000
        val remaining = amount % 10000
        return when {
            man > 0 && remaining > 0 -> "${decimalFormat.format(man)}만 ${decimalFormat.format(remaining)}원"
            man > 0 -> "${decimalFormat.format(man)}만 원"
            else -> "${decimalFormat.format(amount)}원"
        }
    }

    @Composable
    fun getRelationColor(relation: String): Color {
        return when (relation) {
            "가족" -> GoldPoint
            "친척" -> SlateBlue
            "회사" -> NavyLight
            "동창" -> DeepGreen
            "친구" -> Color(0xFF0284C7) // Sky blue
            "교회" -> Color(0xFF6366F1) // Indigo/Church blue
            else -> TextGray
        }
    }

    @Composable
    fun getRelationBgColor(relation: String): Color {
        return when (relation) {
            "가족" -> SoftGoldBg
            "친척" -> CardBeige
            "회사" -> Color(0xFFEFF6FF)
            "동창" -> SoftGreenBg
            "친구" -> Color(0xFFF0F9FF)
            "교회" -> Color(0xFFEEF2FF)
            else -> Color(0xFFF3F4F6)
        }
    }

    @Composable
    fun getEventColor(eventType: String): Color {
        return when (eventType) {
            "결혼" -> GoldPoint
            "장례" -> Color(0xFF374151) // Charcoal dark gray
            "돌잔치" -> Color(0xFF0EA5E9)
            "생일" -> Color(0xFFEC4899) // Pink
            "명절" -> DeepGreen
            "병문안" -> CondolenceRed
            else -> SlateBlue
        }
    }

    @Composable
    fun getEventBgColor(eventType: String): Color {
        return when (eventType) {
            "결혼" -> SoftGoldBg
            "장례" -> Color(0xFFF3F4F6)
            "돌잔치" -> Color(0xFFF0F9FF)
            "생일" -> Color(0xFFFDF2F8)
            "명절" -> SoftGreenBg
            "병문안" -> SoftRedBg
            else -> Color(0xFFF1F5F9)
        }
    }

    fun getTodayString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-DD", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}
