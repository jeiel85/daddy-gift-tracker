package com.jeiel.daddygifttracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeiel.daddygifttracker.viewmodel.GyeongjosaViewModel
import com.jeiel.daddygifttracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: GyeongjosaViewModel) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showEtiquetteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "환경 설정 및 도구",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Privacy Assurance Shield Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftGreenBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "방패망",
                        tint = DeepGreen,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "🔒 기기 로컬 100% 오프라인 보관",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepGreen)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "본 어플리케이션은 서버, 가입, 동기화가 완전히 없으므로 입력하신 모든 동창, 회사, 친척 경조사지 비용 기록은 본인 핸드폰 밖으로 한 글자도 절대 유출될 수 없이 안전합니다.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDark, lineHeight = 18.sp)
                        )
                    }
                }
            }

            // CSV Export tools
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBeige),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📥 장부 데이터 CSV 백업 내보내기",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "수첩에 적어놓은 지인 연명부와 경조사비 정산 지출 내역 전체를 엑셀(CSV) 형식으로 저장하고, 즉시 복사하여 백업합니다.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.exportToCSV { success, pathMsg ->
                                if (success) {
                                    Toast.makeText(context, "백업 완료! 클립보드에 복사되었습니다.", Toast.LENGTH_LONG).show()
                                    // Pop dialog with detailed info
                                    val alert = android.app.AlertDialog.Builder(context)
                                        .setTitle("엑셀 파일 내보내기 완료")
                                        .setMessage(pathMsg)
                                        .setPositiveButton("확인", null)
                                        .create()
                                    alert.show()
                                } else {
                                    Toast.makeText(context, pathMsg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "공유")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "CSV 엑셀로 백업하기 (클립보드 자동복사)", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Etiquette guide card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBeige),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📕 아빠를 위한 경조사비 상식 사전",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "한국 사회의 결혼, 장례, 돌잔치 등 상황별 standard 축의금 및 조의금 매너 가이드라인을 확인해 보세요.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { showEtiquetteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = "매너")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "경조사비 예절 기준표 읽기", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Category summary reference lists (설계에 명시된 관계 및 행사 유형 고정)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBeige),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 관리 유형 기준표",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 관계 카테고리: 가족 • 친척 • 회사 • 동창 • 친구 • 교회 • 기타",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                    )
                    Text(
                        text = "• 경조사 행사 유형: 결혼 • 장례 • 돌잔치 • 생일 • 명절 • 병문안 • 기타",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextDark)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "* MVP 버전은 혼선을 방지하기 위해 한국 전통 7대 대형 카테고리로 고정 설계되어 작동됩니다.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray, fontSize = 12.sp)
                    )
                }
            }

            // Danger Reset utilities
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBeige),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ 전 기기 데이터 초기화",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CondolenceRed)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "어플리케이션에 편철된 모든 인명 정보, 누적 지출/수입 경조사 회계 기록을 완전히 삭제하고, 깨끗한 초기 백지 장부 상태로 초기화합니다. (기기 내 보관 파일 및 모든 알림이 자동 파기됩니다.)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGray, lineHeight = 18.sp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CondolenceRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "초기화")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "모든 데이터 완전 삭제하기", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    // Modal Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "⚠️ 수첩 백지 초기화 경고") },
            text = { Text(text = "정말로 이 기기의 모든 인맥 및 경조사비 정산 데이터를 초기화할까요? 이 작업은 절대 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData {
                            Toast.makeText(context, "장부가 완전히 깨끗해졌습니다.", Toast.LENGTH_SHORT).show()
                            showResetDialog = false
                        }
                    }
                ) {
                    Text(text = "예, 영구 삭제", color = CondolenceRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = "취소", color = NavyDark)
                }
            }
        )
    }

    // Modal Etiquette Guide Dialog
    if (showEtiquetteDialog) {
        AlertDialog(
            onDismissRequest = { showEtiquetteDialog = false },
            title = { Text(text = "📕 경조사비 금액 가이드라인") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "1. 기본 축의금/조의금 매너 기준",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                    )
                    Text(
                        text = "• 그냥 얼굴만 아는 사이 (회사 타 팀 등):\n  - 5만원권 1장이 기본\n• 자주 식사하거나 연락하는 직장 동료, 지인:\n  - 10만원 (결혼식 식대가 비싼 경우 10만원이 표준)\n• 특별히 아끼거나 은혜를 입은 가까운 지인, 선후배:\n  - 15만원 ~ 20만원\n• 정말 친한 베스트 프렌드, 동창, 핵심 친척:\n  - 20만원 ~ 50만원 이상 + 상호 합의 품앗이",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, color = TextDark)
                    )
                    Divider(color = SoftBeigeSurface)
                    Text(
                        text = "2. 장례식 (조의금) 특별 규칙",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = NavyDark)
                    )
                    Text(
                        text = "• 장례식 조의금은 홀수 금액(3만, 5만, 7만, 10만)으로 맞추며, 보통 5만원 혹은 10만원으로 통일하는 추세입니다.\n• 봉투 뒤 편에는 소속과 이름을 세로로 명시합니다.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, color = TextDark)
                    )
                    Divider(color = SoftBeigeSurface)
                    Text(
                        text = "3. 품앗이 정신 강조",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp, color = TextGray)
                    )
                    Text(
                        text = "경조사비의 가장 큰 원칙은 '상대가 내게 보냈던 액수만큼은 똑같이 환원해 돌려준다' 입니다. 이 수첩을 통해 상대가 보낸 축하/조의금을 기록해두고 잊지 말고 갚아 주도록 하십시오.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, color = TextDark)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showEtiquetteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyDark)
                ) {
                    Text(text = "잘 배웠습니다", color = TextLight)
                }
            }
        )
    }
}

