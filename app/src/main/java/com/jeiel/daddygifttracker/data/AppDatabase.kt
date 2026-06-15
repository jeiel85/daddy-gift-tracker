package com.jeiel.daddygifttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Person::class, EventRecord::class, RelationshipReminder::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
    abstract fun eventRecordDao(): EventRecordDao
    abstract fun relationshipReminderDao(): RelationshipReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gyeongjosa_database"
                )
                .fallbackToDestructiveMigration() // Let's simplify DB upgrades of prototype
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

