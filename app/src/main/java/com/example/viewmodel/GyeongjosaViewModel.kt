package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GyeongjosaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = GyeongjosaRepository(
        db.personDao(),
        db.eventRecordDao(),
        db.relationshipReminderDao()
    )

    // Exposed Flows from Repo
    val persons: StateFlow<List<Person>> = repository.allPersons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val records: StateFlow<List<EventRecord>> = repository.allEventRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<RelationshipReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for detail flows or calculations
    private val _selectedPersonId = MutableStateFlow<Int?>(null)
    val selectedPersonId = _selectedPersonId.asStateFlow()

    // Screen navigation helpers
    fun selectPerson(personId: Int) {
        _selectedPersonId.value = personId
    }

    // Insert/Update/Delete operations with Notification Scheduling
    fun insertPerson(person: Person, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertPerson(person).toInt()
            // Schedule custom birthday reminder if birthday is present
            if (person.birthday.isNotBlank()) {
                NotificationHelper.scheduleReminder(
                    context = getApplication(),
                    reminderId = id + 100000, // offset
                    title = "🎂 생신 알림",
                    message = "${person.name} (${person.relationType})님의 생신입니다. 마음이 담긴 인사를 전해 보세요.",
                    dateString = person.birthday
                )
            }
            onComplete(id)
        }
    }

    fun updatePerson(person: Person) {
        viewModelScope.launch {
            repository.updatePerson(person)

            // Reschedule birthday alert if birthday is set
            if (person.birthday.isNotBlank()) {
                NotificationHelper.cancelReminder(getApplication(), person.id + 100000)
                NotificationHelper.scheduleReminder(
                    context = getApplication(),
                    reminderId = person.id + 100000,
                    title = "🎂 생신 알림",
                    message = "${person.name} (${person.relationType})님의 생신입니다. 마음이 담긴 인사를 전해 보세요.",
                    dateString = person.birthday
                )
            } else {
                NotificationHelper.cancelReminder(getApplication(), person.id + 100000)
            }
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            // Cancel notifications associated
            NotificationHelper.cancelReminder(getApplication(), person.id + 100000)
            repository.deletePerson(person)
        }
    }

    // Event operations
    fun insertEventRecord(record: EventRecord, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertEventRecord(record)
            onComplete()
        }
    }

    fun updateEventRecord(record: EventRecord) {
        viewModelScope.launch {
            repository.updateEventRecord(record)
        }
    }

    fun deleteEventRecord(record: EventRecord) {
        viewModelScope.launch {
            repository.deleteEventRecord(record)
        }
    }

    // Reminders
    fun insertReminder(reminder: RelationshipReminder, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder).toInt()
            
            // Generate notification content
            viewModelScope.launch {
                val person = repository.getPersonByIdOneShot(reminder.personId)
                val nm = person?.name ?: "인맥"
                val rel = person?.relationType ?: "기타"
                NotificationHelper.scheduleReminder(
                    context = getApplication(),
                    reminderId = id + 200000, // offset for general reminders
                    title = "📅 경조사/약속 일정 알림",
                    message = "$nm($rel) - ${reminder.title} 일정이 있습니다. 잊지 말고 챙기세요!",
                    dateString = reminder.reminderDate
                )
            }
            onComplete()
        }
    }

    fun toggleReminderDone(reminder: RelationshipReminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isDone = !reminder.isDone)
            repository.updateReminder(updated)
            if (updated.isDone) {
                NotificationHelper.cancelReminder(getApplication(), updated.id + 200000)
            } else {
                val person = repository.getPersonByIdOneShot(reminder.personId)
                val nm = person?.name ?: "인맥"
                val rel = person?.relationType ?: "기타"
                NotificationHelper.scheduleReminder(
                    context = getApplication(),
                    reminderId = updated.id + 200000,
                    title = "📅 경조사/약속 일정 알림",
                    message = "$nm($rel) - ${reminder.title} 일정이 있습니다. 잊지 말고 챙기세요!",
                    dateString = reminder.reminderDate
                )
            }
        }
    }

    fun deleteReminder(reminder: RelationshipReminder) {
        viewModelScope.launch {
            NotificationHelper.cancelReminder(getApplication(), reminder.id + 200000)
            repository.deleteReminder(reminder)
        }
    }

    // CSV Export Function
    fun exportToCSV(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val personList = persons.value
                val recordList = records.value

                val csvContent = StringBuilder()
                
                // Add Privacy warning & Header
                csvContent.append("# 경조사·인맥 장부 데이터 백업 파일\n")
                csvContent.append("# 인명 정보 및 경조사 지출 내역을 포함하고 있습니다. 소중히 관리하세요.\n\n")

                // Section 1: Persons
                csvContent.append("비밀 인맥 정보\n")
                csvContent.append("ID,이름,관계유형,소속/모임,전화번호,중요도,생년월일(기념일),메모\n")
                for (p in personList) {
                    csvContent.append("${p.id},\"${p.name}\",\"${p.relationType}\",\"${p.groupName}\",\"${p.phone}\",${p.importance},\"${p.birthday}\",\"${p.memo}\"\n")
                }

                csvContent.append("\n\n")

                // Section 2: Records
                csvContent.append("경조사 장부 기록\n")
                csvContent.append("기록ID,지인ID,지인이름,경조사유형,보낸금액,받은금액,날짜,장소,메모\n")
                for (r in recordList) {
                    val pName = personList.firstOrNull { it.id == r.personId }?.name ?: "알수없음"
                    csvContent.append("${r.id},${r.personId},\"${pName}\",\"${r.eventType}\",${r.amountGiven},${r.amountReceived},\"${r.date}\",\"${r.location}\",\"${r.memo}\"\n")
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "경조사장부백업_$timestamp.csv"
                
                // Write to external documents or app storage with zero permission requirement
                val directory = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                val file = File(directory, fileName)
                file.parentFile?.mkdirs()
                file.writeText(csvContent.toString(), Charsets.UTF_8)

                // Also copy to Clip board as fallback
                val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("gyeongjosa_csv_backup", csvContent.toString())
                clipboard.setPrimaryClip(clip)

                onResult(true, "클립보드 복사 및 내부 문서 폴더 저장 완료!\n경로: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e("GyeongjosaViewModel", "Failed to export CSV", e)
                onResult(false, "오류가 발생했습니다: ${e.localizedMessage}")
            }
        }
    }

    // Reset Data Function
    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            // Cancel all alarms first
            for (p in persons.value) {
                NotificationHelper.cancelReminder(getApplication(), p.id + 100000)
            }
            for (r in reminders.value) {
                NotificationHelper.cancelReminder(getApplication(), r.id + 200000)
            }

            // Clear tables
            db.clearAllTables()
            onComplete()
        }
    }
}
