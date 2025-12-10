package com.example.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * ContactDatabase - Room database chứa bảng contacts.
 */
@Database(
    entities = [ContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: ContactDatabase? = null

        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Chèn dữ liệu mẫu khi tạo database lần đầu
                db.execSQL("INSERT INTO contacts (name, phoneNumber) VALUES ('Manh', '0779421219')")
                db.execSQL("INSERT INTO contacts (name, phoneNumber) VALUES ('Linh', '0901234567')")
                db.execSQL("INSERT INTO contacts (name, phoneNumber) VALUES ('Huy', '0987654321')")
                db.execSQL("INSERT INTO contacts (name, phoneNumber) VALUES ('Trang', '0911222333')")
                db.execSQL("INSERT INTO contacts (name, phoneNumber) VALUES ('Tuan', '0933444555')")
            }
        }
 
        /**
         * Lấy instance database dạng singleton.
         * Dùng applicationContext để không bị leak activity.
         */
        fun getDatabase(context: Context): ContactDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContactDatabase::class.java,
                    "contact_db"
                )
                    .addCallback(roomCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}