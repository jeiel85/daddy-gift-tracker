package com.jeiel.daddygifttracker.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel.daddygifttracker.data.Person
import com.jeiel.daddygifttracker.data.RelationshipReminder
import com.jeiel.daddygifttracker.viewmodel.GyeongjosaViewModel
import com.jeiel.daddygifttracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleRemindersScreen(
    viewModel: GyeongjosaViewModel,
    onNavigateToPersonDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val persons by viewModel.persons.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: 예정된 일정, 1: 완료된 일정, 2: 🌾 명절 인사 관리기

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Split reminders
    val upcomingReminders = remember(reminders) {
        reminders.filter { !it.isDone }
    }

    val pastReminders = remember(reminders) {
        reminders.filter { it.isDone }
    }

    // Holiday Greetings parameters
    var selectedHoliday by remember { mutableStateOf("설날 (구정)") }
    val holidays = listOf("설날 (구정)", "추석 (한가위)", "가정의 달 (5월)", "연말연시 (송년)")

    // High importance persons (importance >= 3)
    val importantPersonsForGreeting = remember(persons) {
        persons.filter { it.importance >= 3 }
    }

    // Since we need to keep track of holiday greeting checks fully offline without complex tables,
    // we can use standard client side mapping or store it in state, using an in-memory or persisted map.
    // To make it fully functional and reliable, we can use a local mutableStateMap to track who received greetings
    // for the selected holiday, persisting temporarily or initializing neatly.
    val greetingTracker = remember { mutableStateMapOf<String, Boolean>() } // Key: "holiday_personId" -> isGreeted

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "일정 및 알림 챙겨보기",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDark)
                    .padding(bottom = 6.dp)
            ) {
                TabItem(
                    title = "예정된 알림 (${upcomingReminders.size})",
                    isSelected = activeTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = 0 }
                )
                TabItem(
                    title = "완료된 일정 (${pastReminders.size})",
                    isSelected = activeTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = 1 }
                )
                TabItem(
                    title = "🌾 명절 인사 수첩",
                    isSelected = activeTab == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { activeTab = 2 }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeTab) {
                0 -> { // Upcoming Schedule List
                    if (upcomingReminders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "예정된 스마트 알림이 비어 있습니다명.\n인맥 프로필에서 생년월일을 기재하거나 \n'새 일정 예약' 버튼을 통해 알림을 만들어 보세요.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextGray,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(upcomingReminders) { rem ->
                                val assocPerson = persons.firstOrNull { it.id == rem.personId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Checkbox(
                                                checked = rem.isDone,
                                                onCheckedChange = {
                                                    viewModel.toggleReminderDone(rem)
                                                    Toast.makeText(context, "일정을 처리 완료했습니다.", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                            Column {
                                                Text(
                                                    text = rem.title,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextDark
                                                    )
                                                )
                                                if (assocPerson != null) {
                                                    Text(
                                                        text = "지인: ${assocPerson.name} (${assocPerson.relationType} / ${assocPerson.groupName})",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray),
                                                        modifier = Modifier.clickable { onNavigateToPersonDetail(assocPerson.id) }
                                                    )
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = rem.reminderDate,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = if (rem.reminderDate < todayStr) CondolenceRed else NavyDark,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            if (rem.reminderDate < todayStr) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(SoftRedBg)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "기한 지남",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = CondolenceRed,
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

                1 -> { // Past Done List
                    if (pastReminders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "아직 완료 표기된 일정이 없습니다.\n일정들을 처리하며 체크해 보세요.",
                                style = MaterialTheme.typography.bodyLarge.copy(color = TextGray)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            items(pastReminders) { rem ->
                                val assocPerson = persons.firstOrNull { it.id == rem.personId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = SoftBeigeSurface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Checkbox(
                                                checked = rem.isDone,
                                                onCheckedChange = { viewModel.toggleReminderDone(rem) }
                                            )
                                            Column {
                                                Text(
                                                    text = rem.title,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextGray
                                                    )
                                                )
                                                if (assocPerson != null) {
                                                    Text(
                                                        text = "인맥: ${assocPerson.name} (${assocPerson.relationType})",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(onClick = { viewModel.deleteReminder(rem) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "삭제",
                                                tint = CondolenceRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> { // Holiday Greeting checklist screen (명절 인사 챙김이)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Instruction
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "명절",
                                    tint = GoldPoint,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "설날, 추석 등 명절에 꼭 안부 전화를 드려야 하는 중요 명단입니다 (중요도 3성 이상 필터링). 통화 전송 후 클릭하여 체크하세요.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextDark,
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }

                        // Holiday Choice Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            holidays.take(2).forEach { hol ->
                                val isSelected = selectedHoliday == hol
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NavyDark else SoftBeigeSurface)
                                        .clickable { selectedHoliday = hol }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hol,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) TextLight else TextDark,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            holidays.drop(2).forEach { hol ->
                                val isSelected = selectedHoliday == hol
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NavyDark else SoftBeigeSurface)
                                        .clickable { selectedHoliday = hol }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = hol,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isSelected) TextLight else TextDark,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }

                        // Listing important persons
                        Text(
                            text = "🌾 $selectedHoliday 안부 전화 체크리스트 (${importantPersonsForGreeting.size}명)",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyDark
                            )
                        )

                        if (importantPersonsForGreeting.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "중요도 별 3개 이상의 지인이 없습니다.\n지인 정보 등록 시 중요도를 지정해 보세요.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(importantPersonsForGreeting) { p ->
                                    val trackerKey = "${selectedHoliday}_${p.id}"
                                    val isGreeted = greetingTracker[trackerKey] ?: false

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isGreeted) SoftGreenBg else CardBeige
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Checkbox(
                                                    checked = isGreeted,
                                                    onCheckedChange = { checked ->
                                                        greetingTracker[trackerKey] = checked
                                                    }
                                                )
                                                Column {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = p.name,
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isGreeted) TextGray else TextDark
                                                            )
                                                        )
                                                        Text(
                                                            text = p.relationType,
                                                            style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                                        )
                                                    }
                                                    Text(
                                                        text = "소속: ${p.groupName.ifBlank { "소속없음" }} • ${p.phone}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                                    )
                                                }
                                            }

                                            // Trigger dial directly
                                            if (p.phone.isNotBlank()) {
                                                Button(
                                                    onClick = {
                                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                                            data = Uri.parse("tel:${p.phone}")
                                                        }
                                                        context.startActivity(intent)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (isGreeted) SlateBlue else NavyLight
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(text = "전화 걸기", style = MaterialTheme.typography.labelSmall)
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

@Composable
fun TabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSelected) TextLight else SoftBeigeSurface,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(3.dp)
                        .background(GoldPoint)
                )
            }
        }
    }
}

