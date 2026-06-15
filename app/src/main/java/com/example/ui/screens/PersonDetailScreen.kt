package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun PersonDetailScreen(
    viewModel: GyeongjosaViewModel,
    personId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEditPerson: (Int) -> Unit,
    onNavigateToAddRecordForPerson: (Int) -> Unit,
    onNavigateToEditRecord: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val persons by viewModel.persons.collectAsState()
    val records by viewModel.records.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    val person = remember(personId, persons) {
        persons.firstOrNull { it.id == personId }
    }

    if (person == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "지인 정보를 불러오는 중이거나 찾을 수 없습니다.")
        }
        return
    }

    // Filtered data
    val personRecords = remember(records, personId) {
        records.filter { it.personId == personId }
    }

    val personReminders = remember(reminders, personId) {
        reminders.filter { it.personId == personId }
    }

    // Financial totals
    val totalSent = remember(personRecords) { personRecords.sumOf { it.amountGiven } }
    val totalReceived = remember(personRecords) { personRecords.sumOf { it.amountReceived } }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Last Contacted Date (newest past event date)
    val lastContactDate = remember(personRecords, todayStr) {
        personRecords.filter { it.date <= todayStr }
            .maxOfOrNull { it.date } ?: "기록 없음"
    }

    // Next Scheduled Date (earliest future reminder or birthday)
    val nextScheduleDate = remember(personReminders, person, todayStr) {
        val nextRem = personReminders.filter { !it.isDone && it.reminderDate >= todayStr }
            .minOfOrNull { it.reminderDate }
        val bday = person.birthday
        
        when {
            nextRem != null && bday.isNotBlank() -> {
                // Return whichever is closer
                if (nextRem < bday) nextRem else bday
            }
            nextRem != null -> nextRem
            bday.isNotBlank() -> bday
            else -> "검출된 일정 없음"
        }
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Quick add reminder inputs
    var showAddReminderDialog by remember { mutableStateOf(false) }
    var reminderTitle by remember { mutableStateOf("") }
    var reminderDate by remember { mutableStateOf(todayStr) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${person.name} 님의 기록상자",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            fontSize = 20.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = TextLight
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEditPerson(person.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "수정",
                            tint = TextLight
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "인맥삭제",
                            tint = TextLight
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // Profile block
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBeige),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FormatUtils.getRelationBgColor(person.relationType))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = person.relationType,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = FormatUtils.getRelationColor(person.relationType),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }

                                Text(
                                    text = person.name,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 24.sp,
                                        color = TextDark
                                    )
                                )
                            }

                            // Importance Stars indicator
                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "별",
                                        tint = if (i <= person.importance) GoldPoint else Color.LightGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Affiliation & Memo & Phone
                        if (person.groupName.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = "소속", tint = NavyLight, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "소속/모임: ${person.groupName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                                )
                            }
                        }

                        if (person.phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .clickable {
                                        // Standard Dial intent
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${person.phone}")
                                        }
                                        context.startActivity(intent)
                                    }
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "전화기", tint = DeepGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "전화번호: ${person.phone} (터치 시 통화)",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = NavyDark, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        if (person.birthday.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                                Icon(imageVector = Icons.Default.DateRange, contentDescription = "기념", tint = GoldPoint, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "생년월일(기념일): ${person.birthday}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextGray)
                                )
                            }
                        }

                        if (person.memo.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = SoftBeigeSurface, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "지인 특이메모:",
                                style = MaterialTheme.typography.labelLarge.copy(color = NavyDark)
                            )
                            Text(
                                text = person.memo,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextGray, lineHeight = 22.sp)
                            )
                        }
                    }
                }
            }

            // Quick calculations & contact summaries
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SoftBeigeSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "마지막 챙긴 날", style = MaterialTheme.typography.labelMedium.copy(color = TextGray))
                            Text(
                                text = lastContactDate,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = NavyDark),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SoftGoldBg)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "다음 챙길 일정", style = MaterialTheme.typography.labelMedium.copy(color = TextGray))
                            Text(
                                text = nextScheduleDate,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = GoldPoint),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Financial Summary Card specifically for the Person
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavyDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "서로 나눈 경조사비 정산",
                            style = MaterialTheme.typography.titleMedium.copy(color = SoftBeigeSurface)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "내가 보낸 돈 (지출)", style = MaterialTheme.typography.labelMedium.copy(color = SoftBeigeSurface))
                                Text(
                                    text = FormatUtils.formatMoney(totalSent),
                                    style = MaterialTheme.typography.titleLarge.copy(color = TextLight, fontWeight = FontWeight.Bold)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "내가 받은 돈 (수입)", style = MaterialTheme.typography.labelMedium.copy(color = SoftBeigeSurface))
                                Text(
                                    text = FormatUtils.formatMoney(totalReceived),
                                    style = MaterialTheme.typography.titleLarge.copy(color = GoldPoint, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Settlement balance logic
                        val balance = totalReceived - totalSent
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = SlateBlue, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "인맥 정산 지표:",
                                style = MaterialTheme.typography.bodySmall.copy(color = SoftBeigeSurface)
                            )
                            Text(
                                text = if (balance == 0L) "서로 균형을 이룸 (0원)"
                                else if (balance > 0) "${FormatUtils.formatMoneyKoreanText(balance)} 더 받음"
                                else "${FormatUtils.formatMoneyKoreanText(-balance)} 더 보냄 (품앗이)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (balance >= 0) DeepGreen else GoldPoint,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }
                }
            }

            // Interactive action buttons specifically for person detail
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigateToAddRecordForPerson(person.id) },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "보내기", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "경조사비 추가", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = { showAddReminderDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "알림", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "새 일정 예약", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Reminders section
            item {
                Text(
                    text = "⏰ 이분과의 약속/알림 설정 (${personReminders.size}건)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                )
            }

            if (personReminders.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBeige),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "등록된 전용 알약속/일정이 없습니다.\n새 일정 예약으로 챙길 약속이나 명절 안부를 더해보세요.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                            )
                        }
                    }
                }
            } else {
                items(personReminders) { rem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBeige),
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
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                            color = if (rem.isDone) TextGray else TextDark
                                        )
                                    )
                                    Text(text = "예정일시: ${rem.reminderDate}", style = MaterialTheme.typography.bodySmall.copy(color = TextGray))
                                }
                            }

                            IconButton(onClick = { viewModel.deleteReminder(rem) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "삭제", tint = CondolenceRed)
                            }
                        }
                    }
                }
            }

            // Historic records title section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "📜 이분과의 경조조사비 장부 내역 (${personRecords.size}건)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                )
            }

            if (personRecords.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBeige),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "기록된 경조사 내역이 없습니다.\n경조사비 기록 버튼을 눌러 첫 거래를 완벽히 남겨 보세요.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                            )
                        }
                    }
                }
            } else {
                items(personRecords) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBeige),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToEditRecord(personId, record.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        text = record.date,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDark, fontWeight = FontWeight.Bold)
                                    )
                                }
                                if (record.location.isNotBlank()) {
                                    Text(text = "장소: ${record.location}", style = MaterialTheme.typography.bodySmall.copy(color = TextGray))
                                }
                                if (record.memo.isNotBlank()) {
                                    Text(text = "메모: ${record.memo}", style = MaterialTheme.typography.bodySmall.copy(color = TextGray))
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (record.amountGiven > 0) {
                                    Text(
                                        text = "보냄: ${FormatUtils.formatMoneyKoreanText(record.amountGiven)}",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = CondolenceRed, fontWeight = FontWeight.Bold)
                                    )
                                }
                                if (record.amountReceived > 0) {
                                    Text(
                                        text = "받음: ${FormatUtils.formatMoneyKoreanText(record.amountReceived)}",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = DeepGreen, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(text = "인맥 정보 완결 삭제") },
            text = { Text(text = "${person.name} 님을 수첩에서 완전히 삭제할까요? 관련 경조사비 내역과 예정 일정 ${personRecords.size + personReminders.size}건이 모두 지워지며 이 작업은 돌이킬 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePerson(person)
                        Toast.makeText(context, "${person.name} 님이 인맥 수첩에서 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        showDeleteConfirmation = false
                        onNavigateBack()
                    }
                ) {
                    Text(text = "예, 완전히 삭제", color = CondolenceRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "취소", color = NavyDark)
                }
            }
        )
    }

    // Modal Add Reminder Dialog
    if (showAddReminderDialog) {
        AlertDialog(
            onDismissRequest = { showAddReminderDialog = false },
            title = { Text(text = "새 일정/알림 등록") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "${person.name} 님을 위해 알림을 받을 일정을 작성하세요.")
                    OutlinedTextField(
                        value = reminderTitle,
                        onValueChange = { reminderTitle = it },
                        label = { Text("알림 제목 (예: 장남 결혼, 부친 구순, 명절인사)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reminderDate,
                        onValueChange = { reminderDate = it },
                        label = { Text("알림 일시 (YYYY-MM-DD)") },
                        singleLine = true,
                        supportingText = { Text("YYYY-MM-DD 포맷을 입력해 주세요.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reminderTitle.isBlank()) {
                            Toast.makeText(context, "알림 제목을 기재해 주세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val parts = reminderDate.split("-")
                        if (parts.size != 3 || parts[0].length != 4 || parts[1].length != 2 || parts[2].length != 2) {
                            Toast.makeText(context, "날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val reminder = RelationshipReminder(
                            personId = personId,
                            title = reminderTitle,
                            reminderDate = reminderDate,
                            isDone = false
                        )
                        viewModel.insertReminder(reminder)
                        Toast.makeText(context, "일정 알림 예약이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        reminderTitle = ""
                        showAddReminderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                ) {
                    Text(text = "알림 설정 완료")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReminderDialog = false }) {
                    Text(text = "취소")
                }
            }
        )
    }
}
