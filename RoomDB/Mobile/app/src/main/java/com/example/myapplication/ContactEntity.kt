package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ContactEntity - bảng lưu thông tin danh bạ trong Room database.
 *
 * Chỉ lưu những thông tin cô yêu cầu:
 *  - id: khoá chính, tự tăng
 *  - name: tên liên hệ
 *  - phoneNumber: số điện thoại
 */
@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String
)

/**
 * Model dùng cho UI. Tách riêng entity để sau này có thể thay đổi database
 * mà không ảnh hưởng tới giao diện.
 */
data class Contact(
    val id: Int = 0,
    val name: String,
    val phoneNumber: String
)

/** Chuyển từ Entity sang model UI */
fun ContactEntity.toContact() = Contact(
    id = id,
    name = name,
    phoneNumber = phoneNumber
)

/** Chuyển từ model UI sang Entity để lưu database */
fun Contact.toEntity() = ContactEntity(
    id = id,
    name = name,
    phoneNumber = phoneNumber
)