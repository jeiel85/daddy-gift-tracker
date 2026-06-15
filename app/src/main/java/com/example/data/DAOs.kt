package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    fun getPersonById(id: Int): Flow<Person?>

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    suspend fun getPersonByIdOneShot(id: Int): Person?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)
}

@Dao
interface EventRecordDao {
    @Query("SELECT * FROM event_records ORDER BY date DESC, id DESC")
    fun getAllEventRecords(): Flow<List<EventRecord>>

    @Query("SELECT * FROM event_records WHERE personId = :personId ORDER BY date DESC, id DESC")
    fun getEventRecordsForPerson(personId: Int): Flow<List<EventRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventRecord(record: EventRecord): Long

    @Update
    suspend fun updateEventRecord(record: EventRecord)

    @Delete
    suspend fun deleteEventRecord(record: EventRecord)

    @Query("DELETE FROM event_records WHERE personId = :personId")
    suspend fun deleteEventRecordsForPerson(personId: Int)
}

@Dao
interface RelationshipReminderDao {
    @Query("SELECT * FROM relationship_reminders ORDER BY reminderDate ASC")
    fun getAllReminders(): Flow<List<RelationshipReminder>>

    @Query("SELECT * FROM relationship_reminders WHERE personId = :personId ORDER BY reminderDate ASC")
    fun getRemindersForPerson(personId: Int): Flow<List<RelationshipReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: RelationshipReminder): Long

    @Update
    suspend fun updateReminder(reminder: RelationshipReminder)

    @Delete
    suspend fun deleteReminder(reminder: RelationshipReminder)

    @Query("DELETE FROM relationship_reminders WHERE personId = :personId")
    suspend fun deleteRemindersForPerson(personId: Int)
}
