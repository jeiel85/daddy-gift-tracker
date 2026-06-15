package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventRecord
import com.example.data.Person
import com.example.viewmodel.GyeongjosaViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: GyeongjosaViewModel) {
    val persons by viewModel.persons.collectAsState()
    val records by viewModel.records.collectAsState()

    val currentYearString = remember { SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()) }

    // 1. Annual filtering
    val thisYearRecords = remember(records, currentYearString) {
        records.filter { it.date.startsWith(currentYearString) }
    }

    val annualSent = remember(thisYearRecords) { thisYearRecords.sumOf { it.amountGiven } }
    val annualReceived = remember(thisYearRecords) { thisYearRecords.sumOf { it.amountReceived } }

    val allTimeSent = remember(records) { records.sumOf { it.amountGiven } }
    val allTimeReceived = remember(records) { records.sumOf { it.amountReceived } }

    // 2. Spending by Relation Category (All time)
    val spentByRelation = remember(records, persons) {
        val distribution = mutableMapOf<String, Long>()
        for (rec in records) {
            val p = persons.firstOrNull { it.id == rec.personId }
            val rel = p?.relationType ?: "기타"
            distribution[rel] = (distribution[rel] ?: 0L) + rec.amountGiven
        }
        distribution.toList().sortedByDescending { it.second }
    }

    val totalSpentDistribution = remember(spentByRelation) { spentByRelation.sumOf { it.second } }

    // 3. Spending by Month (Current Year)
    val monthlySpent = remember(thisYearRecords) {
        val monthlyMap = mutableMapOf<Int, Pair<Long, Long>>() // Month -> (Sent, Received)
        for (i in 1..12) {
            monthlyMap[i] = Pair(0L, 0L)
        }
        for (rec in thisYearRecords) {
            val parts = rec.date.split("-")
            if (parts.size >= 2) {
                val month = parts[1].toIntOrNull() ?: 1
                val pair = monthlyMap[month] ?: Pair(0L, 0L)
                monthlyMap[month] = Pair(pair.first + rec.amountGiven, pair.second + rec.amountReceived)
            }
        }
        monthlyMap.toList().sortedBy { it.first }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "지출 및 수입 통계분석",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextLight,
                            fontSize = 20.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        containerColor = WarmBeigeBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // Main Year Spending Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "올해 (${currentYearString}년) 경조사비 장부 현황",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = NavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "기록된 지출 (보냄)", style = MaterialTheme.typography.labelMedium.copy(color = TextGray))
                                Text(
                                    text = FormatUtils.formatMoney(annualSent),
                                    style = MaterialTheme.typography.titleLarge.copy(color = CondolenceRed, fontWeight = FontWeight.Bold)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "기록된 수입 (받음)", style = MaterialTheme.typography.labelMedium.copy(color = TextGray))
                                Text(
                                    text = FormatUtils.formatMoney(annualReceived),
                                    style = MaterialTheme.typography.titleLarge.copy(color = DeepGreen, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Received vs Sent Ratio Block
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚖️ 누적 보낸 돈 대 받은 돈 비율 (품앗이 수수율)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalMoney = allTimeSent + allTimeReceived
                        if (totalMoney == 0L) {
                            Text(
                                text = "통계를 위한 경조사비 기록이 부족합니다. 장부 기록을 더 많이 남겨보세요.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                            )
                        } else {
                            val sentPct = (allTimeSent.toFloat() / totalMoney.toFloat()) * 100
                            val receivedPct = (allTimeReceived.toFloat() / totalMoney.toFloat()) * 100

                            // Multi-colored bar indicator
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                if (allTimeSent > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(sentPct)
                                            .fillMaxHeight()
                                            .background(CondolenceRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${sentPct.toInt()}%",
                                            color = TextLight,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                                if (allTimeReceived > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(receivedPct)
                                            .fillMaxHeight()
                                            .background(DeepGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${receivedPct.toInt()}%",
                                            color = TextLight,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(CondolenceRed))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "보낸 돈 (${FormatUtils.formatMoneyKoreanText(allTimeSent)})", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(DeepGreen))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "받은 돈 (${FormatUtils.formatMoneyKoreanText(allTimeReceived)})", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // Spent by relationships
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 관계 유형별 보낸 돈 분배비율 (경조사비 누적)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (spentByRelation.isEmpty() || totalSpentDistribution == 0L) {
                            Text(
                                text = "관계별 분배 통계 정보가 전혀 없습니다. 장부 기록이 쌓이면 자동으로 집계됩니다.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                spentByRelation.forEach { (rel, amt) ->
                                    val percentage = (amt.toFloat() / totalSpentDistribution.toFloat()) * 100
                                    val color = FormatUtils.getRelationColor(rel)

                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "$rel • ${FormatUtils.formatMoneyKoreanText(amt)}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${percentage.toInt()}%",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = color, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        // Bar progress
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(SoftBeigeSurface)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(percentage / 100)
                                                    .fillMaxHeight()
                                                    .background(color)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Monthly breakdown list
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🗓️ 올해 (${currentYearString}년) 월별 수지 추이",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (thisYearRecords.isEmpty()) {
                            Text(
                                text = "올해 작성된 경조사 거래 내역이 아직 존재하지 않습니다.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                monthlySpent.forEach { (month, pair) ->
                                    val (sent, rec) = pair
                                    if (sent > 0 || rec > 0) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, SoftBeigeSurface, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${month}월",
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavyDark
                                                )
                                            )

                                            Column(horizontalAlignment = Alignment.End) {
                                                if (sent > 0L) {
                                                    Text(
                                                        text = "보냄: ${FormatUtils.formatMoney(sent)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(color = CondolenceRed, fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                                if (rec > 0L) {
                                                    Text(
                                                        text = "받음: ${FormatUtils.formatMoney(rec)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(color = DeepGreen, fontWeight = FontWeight.Bold)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
