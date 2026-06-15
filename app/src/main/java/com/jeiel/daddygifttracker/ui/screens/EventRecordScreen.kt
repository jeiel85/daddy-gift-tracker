package com.jeiel.daddygifttracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel.daddygifttracker.data.EventRecord
import com.jeiel.daddygifttracker.viewmodel.GyeongjosaViewModel
import com.jeiel.daddygifttracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRecordScreen(
    viewModel: GyeongjosaViewModel,
    personId: Int? = null,      // If pre-selected from Person Detail
    recordId: Int? = null,      // If editing an existing record
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val persons by viewModel.persons.collectAsState()
    val records by viewModel.records.collectAsState()

    // 1. Identify existing record in Edit Mode
    val existingRecord = remember(recordId, records) {
        if (recordId != null && recordId > 0) {
            records.firstOrNull { it.id == recordId }
        } else {
            null
        }
    }
    val isEditMode = existingRecord != null

    // Determine initial person
    val initialPersonId = existingRecord?.personId ?: personId ?: 0

    // Form fields state
    var selectedPersonId by remember { mutableStateOf(initialPersonId) }
    var eventType by remember { mutableStateOf(existingRecord?.eventType ?: "결혼") }
    var amountGivenStr by remember { mutableStateOf(existingRecord?.amountGiven?.toString() ?: "") }
    var amountReceivedStr by remember { mutableStateOf(existingRecord?.amountReceived?.toString() ?: "") }
    
    // Default date is today in YYYY-MM-DD
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var date by remember { mutableStateOf(existingRecord?.date ?: todayStr) }
    var location by remember { mutableStateOf(existingRecord?.location ?: "") }
    var memo by remember { mutableStateOf(existingRecord?.memo ?: "") }

    // Dropdown state for person selection
    var showPersonDropdown by remember { mutableStateOf(false) }

    // Synchronize states on database load
    LaunchedEffect(existingRecord) {
        existingRecord?.let {
            selectedPersonId = it.personId
            eventType = it.eventType
            amountGivenStr = it.amountGiven.toString()
            amountReceivedStr = it.amountReceived.toString()
            date = it.date
            location = it.location
            memo = it.memo
        }
    }

    val eventTypes = listOf("결혼", "장례", "돌잔치", "생일", "명절", "병문안", "기타")

    val selectedPersonObj = remember(selectedPersonId, persons) {
        persons.firstOrNull { it.id == selectedPersonId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "장부 기록 수정" else "경조사비 장부 남기기",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        containerColor = WarmBeigeBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Safe helper card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 보낸 금액(지금까지 지출한 돈)과 받은 금액(지금까지 상대방이 보내온 돈)을 분리해 기록하면 나중에 품앗이 계산 시 무척 유용합니다.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark, lineHeight = 18.sp)
                )
            }

            // Target Person choosing dropdown/box
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "* 대상 지인 선택",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (persons.isEmpty()) {
                    Text(
                        text = "⚠️ 등록된 지인이 없습니다. 버튼을 눌러 인맥을 먼저 등록해 주세요.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CondolenceRed)
                    )
                } else {
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardBeige)
                                .clickable {
                                    if (!isEditMode && personId == null) {
                                        showPersonDropdown = true
                                    }
                                }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPersonObj?.let { "${it.name} (${it.relationType} / ${it.groupName})" }
                                    ?: "지인을 선택해 주세요.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedPersonObj != null) TextDark else TextGray
                                )
                            )
                            if (!isEditMode && personId == null) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "드롭다운",
                                    tint = NavyLight
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showPersonDropdown,
                            onDismissRequest = { showPersonDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(CardBeige)
                        ) {
                            persons.forEach { person ->
                                DropdownMenuItem(
                                    text = {
                                        Text(text = "${person.name} (${person.relationType} • ${person.groupName})")
                                    },
                                    onClick = {
                                        selectedPersonId = person.id
                                        showPersonDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Event Type selections
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "* 주 유형",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val firstRow = eventTypes.take(4)
                    firstRow.forEach { type ->
                        val isSelected = eventType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NavyDark else SoftBeigeSurface)
                                .clickable { eventType = type }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) TextLight else TextDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val secondRow = eventTypes.drop(4)
                    secondRow.forEach { type ->
                        val isSelected = eventType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NavyDark else SoftBeigeSurface)
                                .clickable { eventType = type }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) TextLight else TextDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) // balancer spacer
                }
            }

            // Amount Sent
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "내가 보낸 금액 (지출)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = amountGivenStr,
                    onValueChange = { amountGivenStr = it },
                    placeholder = { Text("보낸 돈이 없다면 비워두거나 0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
                val givenAmt = amountGivenStr.toLongOrNull() ?: 0L
                if (givenAmt > 0) {
                    Text(
                        text = "👉 한글 금액 표시: ${FormatUtils.formatMoneyKoreanText(givenAmt)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CondolenceRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Amount Received
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "내가 받은 금액 (축의금/조의금 조달 수지)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = amountReceivedStr,
                    onValueChange = { amountReceivedStr = it },
                    placeholder = { Text("받은 돈이 없다면 비워두거나 0") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
                val recAmt = amountReceivedStr.toLongOrNull() ?: 0L
                if (recAmt > 0) {
                    Text(
                        text = "👉 한글 금액 표시: ${FormatUtils.formatMoneyKoreanText(recAmt)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DeepGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Date
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "* 날짜",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    placeholder = { Text("예: 2026-06-15") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(text = "YYYY-MM-DD 형식으로 작성해 주세요.")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
            }

            // Location
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "장소",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("예: 세종문화회관, 한솔웨딩홀, 서울성모장례식장") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
            }

            // Event Memo
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "참석 및 특이메모",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = { Text("예: 대리 참석함, 축하 화환 보냄, 아들과 동석") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save records button
            Button(
                onClick = {
                    if (selectedPersonId <= 0) {
                        Toast.makeText(context, "대상 지인을 반드시 명시해야 합니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val givenValue = amountGivenStr.toLongOrNull() ?: 0L
                    val recValue = amountReceivedStr.toLongOrNull() ?: 0L
                    
                    if (givenValue == 0L && recValue == 0L) {
                        Toast.makeText(context, "금액(보낸액 또는 받은액) 중 최소 하나는 채워주셔야 합니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Date format validation
                    val parts = date.split("-")
                    if (parts.size != 3 || parts[0].length != 4 || parts[1].length != 2 || parts[2].length != 2) {
                        Toast.makeText(context, "장부 날짜 형식이 올바르지 않습니다. (예: 2026-06-15)", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (isEditMode && existingRecord != null) {
                        val updated = existingRecord.copy(
                            personId = selectedPersonId,
                            eventType = eventType,
                            amountGiven = givenValue,
                            amountReceived = recValue,
                            date = date,
                            location = location,
                            memo = memo
                        )
                        viewModel.updateEventRecord(updated)
                        Toast.makeText(context, "장부 기록이 완벽히 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val record = EventRecord(
                            personId = selectedPersonId,
                            eventType = eventType,
                            amountGiven = givenValue,
                            amountReceived = recValue,
                            date = date,
                            location = location,
                            memo = memo
                        )
                        viewModel.insertEventRecord(record)
                        Toast.makeText(context, "장부에 새 기록이 편철되었습니다.", Toast.LENGTH_SHORT).show()
                    }

                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
            ) {
                Text(
                    text = if (isEditMode) "기록지 수정 보관" else "장부에 든든히 기록하기",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp, color = TextLight)
                )
            }
        }
    }
}

