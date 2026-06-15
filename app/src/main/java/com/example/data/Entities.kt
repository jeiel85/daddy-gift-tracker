package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relationType: String, // 가족, 친척, 회사, 동창, 친구, 교회, 기타
    val groupName: String,    // 소속/모임
    val phone: String,
    val memo: String,
    val importance: Int,      // 1 ~ 5 중요도
    val birthday: String,     // YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "event_records")
data class EventRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int,
    val eventType: String,    // 결혼, 장례, 돌잔치, 생일, 명절, 병문안, 기타
    val amountGiven: Long,    // 내가 보낸 금액
    val amountReceived: Long, // 내가 받은 금액
    val date: String,         // YYYY-MM-DD
    val location: String,
    val memo: String
)

@Entity(tableName = "relationship_reminders")
data class RelationshipReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int,
    val title: String,
    val reminderDate: String, // YYYY-MM-DD
    val isDone: Boolean = false
)
