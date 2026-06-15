package com.example.data

import kotlinx.coroutines.flow.Flow

class GyeongjosaRepository(
    private val personDao: PersonDao,
    private val eventRecordDao: EventRecordDao,
    private val reminderDao: RelationshipReminderDao
) {
    val allPersons: Flow<List<Person>> = personDao.getAllPersons()
    val allEventRecords: Flow<List<EventRecord>> = eventRecordDao.getAllEventRecords()
    val allReminders: Flow<List<RelationshipReminder>> = reminderDao.getAllReminders()

    fun getPersonById(id: Int): Flow<Person?> = personDao.getPersonById(id)

    suspend fun getPersonByIdOneShot(id: Int): Person? = personDao.getPersonByIdOneShot(id)

    fun getEventRecordsForPerson(personId: Int): Flow<List<EventRecord>> =
        eventRecordDao.getEventRecordsForPerson(personId)

    fun getRemindersForPerson(personId: Int): Flow<List<RelationshipReminder>> =
        reminderDao.getRemindersForPerson(personId)

    suspend fun insertPerson(person: Person): Long = personDao.insertPerson(person)

    suspend fun updatePerson(person: Person) = personDao.updatePerson(person)

    suspend fun deletePerson(person: Person) {
        // Cascade delete records and reminders in the repository
        eventRecordDao.deleteEventRecordsForPerson(person.id)
        reminderDao.deleteRemindersForPerson(person.id)
        personDao.deletePerson(person)
    }

    suspend fun insertEventRecord(record: EventRecord): Long = eventRecordDao.insertEventRecord(record)

    suspend fun updateEventRecord(record: EventRecord) = eventRecordDao.updateEventRecord(record)

    suspend fun deleteEventRecord(record: EventRecord) = eventRecordDao.deleteEventRecord(record)

    suspend fun insertReminder(reminder: RelationshipReminder): Long = reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: RelationshipReminder) = reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: RelationshipReminder) = reminderDao.deleteReminder(reminder)
}
