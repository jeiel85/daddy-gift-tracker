package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Person
import com.example.viewmodel.GyeongjosaViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonRegisterScreen(
    viewModel: GyeongjosaViewModel,
    personId: Int? = null, // If non-null and > 0, we are in Edit Mode
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val persons by viewModel.persons.collectAsState()

    // 1. Identify if we are in Edit Mode
    val existingPerson = remember(personId, persons) {
        if (personId != null && personId > 0) {
            persons.firstOrNull { it.id == personId }
        } else {
            null
        }
    }

    val isEditMode = existingPerson != null

    // Form states
    var name by remember { mutableStateFlowOf(existingPerson?.name ?: "") }
    var relationType by remember { mutableStateOf(existingPerson?.relationType ?: "가족") }
    var groupName by remember { mutableStateOf(existingPerson?.groupName ?: "") }
    var phone by remember { mutableStateOf(existingPerson?.phone ?: "") }
    var memo by remember { mutableStateOf(existingPerson?.memo ?: "") }
    var importance by remember { mutableStateOf(existingPerson?.importance ?: 3) }
    var birthday by remember { mutableStateOf(existingPerson?.birthday ?: "") }

    // Sync state when existingPerson changes (e.g. database fully loads)
    LaunchedEffect(existingPerson) {
        existingPerson?.let {
            name = it.name
            relationType = it.relationType
            groupName = it.groupName
            phone = it.phone
            memo = it.memo
            importance = it.importance
            birthday = it.birthday
        }
    }

    val relations = listOf("가족", "친척", "회사", "동창", "친구", "교회", "기타")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "지인 인맥 수정" else "새 지인 인맥 등록",
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Helper explanation card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftGoldBg),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Text(
                    text = "💡 지인 정보는 완전히 개인 오프라인 기기 내부에만 보관됩니다. 생일이나 기념일을 입력해 두시면 해당 날짜 오전 9시에 알림이 전달됩니다.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDark, lineHeight = 18.sp)
                )
            }

            // Name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "* 이름",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("예: 홍길동") },
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

            // Relationship selections (가족, 친척, 회사, 동창, 친구, 교회, 기타)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "* 관계 구분",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val firstRow = relations.take(4)
                    firstRow.forEach { rel ->
                        val isSelected = relationType == rel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NavyDark else SoftBeigeSurface)
                                .clickable { relationType = rel }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rel,
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val secondRow = relations.drop(4)
                    secondRow.forEach { rel ->
                        val isSelected = relationType == rel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NavyDark else SoftBeigeSurface)
                                .clickable { relationType = rel }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isSelected) TextLight else TextDark,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                    // Spacer for grid balance
                    Box(modifier = Modifier.weight(1f))
                }
            }

            // Affiliation/Group/Company
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "소속/모임명",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = { Text("예: 85동창회, 기획팀, 중앙교회") },
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

            // Phone
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "전화번호",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("예: 010-1234-5678") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
            }

            // Birthday Or Anniversary
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "생일 / 음력/양력 기념일",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    placeholder = { Text("예: 1974-05-24") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(text = "YYYY-MM-DD 형식으로 입력하세요 (예: 1980-06-15)")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
            }

            // Importance Rating (1 ~ 5 Stars)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "중요도 (관리 정밀도)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardBeige)
                        .border(1.dp, SlateBlue, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        val isStarred = i <= importance
                        IconButton(
                            onClick = { importance = i },
                            modifier = Modifier.size(44.dp) // Touch target 48dp comfort
                        ) {
                            Icon(
                                imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "${i}성",
                                tint = if (isStarred) GoldPoint else TextGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // Memo
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "지인 관련 메모",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = NavyDark,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    placeholder = { Text("예: 등산을 좋아함, 대학 동창 회장, 고향 선배") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NavyDark,
                        unfocusedBorderColor = SlateBlue,
                        focusedContainerColor = CardBeige,
                        unfocusedContainerColor = CardBeige
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save submit button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        Toast.makeText(context, "지인 이름을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (birthday.isNotBlank()) {
                        val parts = birthday.split("-")
                        if (parts.size != 3 || parts[0].length != 4 || parts[1].length != 2 || parts[2].length != 2) {
                            Toast.makeText(context, "생일 날짜 형식이 올바르지 않습니다. (예: 1980-06-15)", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                    }

                    if (isEditMode && existingPerson != null) {
                        val updated = existingPerson.copy(
                            name = name,
                            relationType = relationType,
                            groupName = groupName,
                            phone = phone,
                            birthday = birthday,
                            importance = importance,
                            memo = memo
                        )
                        viewModel.updatePerson(updated)
                        Toast.makeText(context, "인맥 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        val info = Person(
                            name = name,
                            relationType = relationType,
                            groupName = groupName,
                            phone = phone,
                            birthday = birthday,
                            importance = importance,
                            memo = memo
                        )
                        viewModel.insertPerson(info)
                        Toast.makeText(context, "인맥이 새롭게 등록되었습니다.", Toast.LENGTH_SHORT).show()
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
                    text = if (isEditMode) "수정 완료" else "새로운 인맥 저장하기",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp, color = TextLight)
                )
            }
        }
    }
}

// Simple text flow helper since mutableStateFlowOf is not default, let's make it standard mutableStateOf
fun <T> mutableStateFlowOf(value: T): MutableState<T> = mutableStateOf(value)
