package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EventRecord
import com.example.data.Person
import com.example.data.RelationshipReminder
import com.example.viewmodel.GyeongjosaViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GyeongjosaViewModel,
    onNavigateToPersonAdd: () -> Unit,
    onNavigateToRecordAdd: () -> Unit,
    onNavigateToPersonDetail: (Int) -> Unit,
    onNavigateToReminders: () -> Unit
) {
    val persons by viewModel.persons.collectAsState()
    val records by viewModel.records.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    // 1. Calculations
    val totalSent = remember(records) { records.sumOf { it.amountGiven } }
    val totalReceived = remember(records) { records.sumOf { it.amountReceived } }

    val currentCalendar = Calendar.getInstance()
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH) + 1 // 1-12
    val currentMonthString = String.format("%02d", currentMonth)

    // People to celebrate this month (birthday in this month)
    val thisMonthBirthdays = remember(persons) {
        persons.filter { person ->
            if (person.birthday.isNotBlank() && person.birthday.contains("-")) {
                val parts = person.birthday.split("-")
                parts.size >= 2 && parts[1] == currentMonthString
            } else {
                false
            }
        }
    }

    // Upcoming reminders
    val activeReminders = remember(reminders, persons) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        reminders.filter { !it.isDone && it.reminderDate >= todayStr }
            .take(3)
    }

    // Recent 4 records
    val recentRecords = remember(records) {
        records.take(4)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "경조사 인맥 관리",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextLight,
                                fontSize = 23.sp
                            )
                        )
                        Text(
                            text = "4050 아빠의 스마트한 인맥 수첩",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SoftBeigeSurface
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDark,
                    titleContentColor = TextLight
                )
            )
        },
        containerColor = WarmBeigeBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // Safe privacy warning notice
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "보안공지",
                            tint = GoldPoint,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "개인정보 안심 안내: 이 앱의 모든 소중한 내역과 연락처 정보는 기기 내부(로컬 DB)에만 안전하게 암호화 보관되며, 어디로도 전송되지 않습니다.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextDark,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }

            // Gifting summary Card (Navy dashboard theme)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "누적 경조사 장부 요약",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = SoftBeigeSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CondolenceRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "보낸 금액 (지출)",
                                        style = MaterialTheme.typography.labelLarge.copy(color = SoftBeigeSurface)
                                    )
                                }
                                Text(
                                    text = FormatUtils.formatMoney(totalSent),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = TextLight,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(DeepGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "받은 금액 (수입)",
                                        style = MaterialTheme.typography.labelLarge.copy(color = SoftBeigeSurface)
                                    )
                                }
                                Text(
                                    text = FormatUtils.formatMoney(totalReceived),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = GoldPoint,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp
                                    )
                                )
                            }
                        }

                        // Net spending
                        val net = totalReceived - totalSent
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = SlateBlue, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "상쇄 정산액 (수입 - 지출)",
                                style = MaterialTheme.typography.bodySmall.copy(color = SoftBeigeSurface)
                            )
                            Text(
                                text = (if (net >= 0) "+" else "") + FormatUtils.formatMoney(net),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (net >= 0) DeepGreen else CondolenceRed,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Quick button rows
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToPersonAdd,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "등록")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "새 지인 등록",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                        )
                    }

                    Button(
                        onClick = onNavigateToRecordAdd,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "기록")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "경조사비 기록",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                        )
                    }
                }
            }

            // This month's celebrations (이번 달 챙길 사람)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎂 이번 달 생신 지인 (${thisMonthBirthdays.size}명)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (thisMonthBirthdays.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftBeigeSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "이번 달에 등록된 생일이 없습니다.\n인맥 정보 등록 시 생일을 추가해 보세요.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextGray,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            thisMonthBirthdays.forEach { person ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigateToPersonDetail(person.id) },
                                    colors = CardDefaults.cardColors(containerColor = CardBeige)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(FormatUtils.getRelationBgColor(person.relationType))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = person.relationType,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = FormatUtils.getRelationColor(person.relationType),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            Text(
                                                text = person.name,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "생일: ${person.birthday.substring(5)}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = GoldPoint,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = "전화",
                                                tint = NavyLight,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Upcoming schedule (다가오는 일정)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 다가오는 수첩 일정",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        )
                        Text(
                            text = "전체보기",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NavyLight,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .clickable { onNavigateToReminders() }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (activeReminders.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftBeigeSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "예정된 일정이 없습니다.\n기념일이나 경조사 챙김 알림을 계획해 보세요.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextGray,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeReminders.forEach { reminder ->
                                val associatedPerson = persons.firstOrNull { it.id == reminder.personId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBeige)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = reminder.title,
                                                style = MaterialTheme.typography.bodyLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextDark
                                                )
                                            )
                                            if (associatedPerson != null) {
                                                Text(
                                                    text = "대상: ${associatedPerson.name} (${associatedPerson.relationType})",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                                )
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = reminder.reminderDate,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = NavyLight,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(SoftGoldBg)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "알림 대기",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = GoldPoint,
                                                        fontSize = 11.sp
                                                    )
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

            // Recent history ledger
            item {
                Column {
                    Text(
                        text = "✍️ 최근 기록된 경조사비 내역",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyDark
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (recentRecords.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftBeigeSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "아직 기록된 장부가 없습니다.\n기록 및 정리를 시작해 보세요!",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextGray,
                                        lineHeight = 22.sp
                                    )
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentRecords.forEach { record ->
                                val associatedPerson = persons.firstOrNull { it.id == record.personId }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (associatedPerson != null) {
                                                onNavigateToPersonDetail(associatedPerson.id)
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = CardBeige)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(FormatUtils.getEventBgColor(record.eventType))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = record.eventType,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = FormatUtils.getEventColor(record.eventType),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                                Text(
                                                    text = associatedPerson?.name ?: "지인",
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextDark
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${record.date} • ${record.location.ifBlank { "장소 미기재" }}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            if (record.amountGiven > 0) {
                                                Text(
                                                    text = "보냄: ${FormatUtils.formatMoneyKoreanText(record.amountGiven)}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = CondolenceRed,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            if (record.amountReceived > 0) {
                                                Text(
                                                    text = "받음: ${FormatUtils.formatMoneyKoreanText(record.amountReceived)}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = DeepGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
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
