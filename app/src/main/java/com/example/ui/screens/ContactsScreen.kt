package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Person
import com.example.viewmodel.GyeongjosaViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel: GyeongjosaViewModel,
    onNavigateToPersonAdd: () -> Unit,
    onNavigateToPersonDetail: (Int) -> Unit
) {
    val persons by viewModel.persons.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRelationFilter by remember { mutableStateOf("전체") }

    val filters = listOf("전체", "가족", "친척", "회사", "동창", "친구", "교회", "기타")

    // Filtered persons
    val filteredPersons = remember(persons, searchQuery, selectedRelationFilter) {
        persons.filter { p ->
            val matchQuery = p.name.contains(searchQuery, ignoreCase = true) || 
                             p.groupName.contains(searchQuery, ignoreCase = true) ||
                             p.memo.contains(searchQuery, ignoreCase = true)
            val matchRelation = selectedRelationFilter == "전체" || p.relationType == selectedRelationFilter
            matchQuery && matchRelation
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "전체 인맥 장부 (${persons.size}명)",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPersonAdd,
                containerColor = NavyDark,
                contentColor = TextLight,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = WarmBeigeBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("이름 또는 소속/모임명 검색") },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "돋보기", tint = NavyLight)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyDark,
                    unfocusedBorderColor = SlateBlue,
                    focusedContainerColor = CardBeige,
                    unfocusedContainerColor = CardBeige
                )
            )

            // Category choice chips scrollable row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Let's create a scrollable low-cost layout for our 8 filter chips
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { flt ->
                        val isSelected = selectedRelationFilter == flt
                        val color = if (isSelected) TextLight else FormatUtils.getRelationColor(flt)
                        val bg = if (isSelected) NavyDark else FormatUtils.getRelationBgColor(flt)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(bg)
                                .clickable { selectedRelationFilter = flt }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = flt,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = color,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Listing persons
            if (filteredPersons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (persons.isEmpty()) "아직 등록된 지인이 없습니다.\n오른쪽 아래 주황색 '+' 버튼을 눌러 추가하세요."
                               else "검색 조건에 일치하는 지인이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextGray)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPersons) { person ->
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
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(FormatUtils.getRelationBgColor(person.relationType))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = person.relationType,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = FormatUtils.getRelationColor(person.relationType),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = person.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextDark
                                            )
                                        )
                                        if (person.groupName.isNotBlank()) {
                                            Text(
                                                text = person.groupName,
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextGray)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Sledge Stars indicator for importance
                                    Row {
                                        for (i in 1..person.importance) {
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = "*",
                                                tint = GoldPoint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Search, // custom arrow replacement
                                        contentDescription = "상세보기",
                                        tint = SlateBlue,
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
}
