package com.example.myapplication

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * ContactDao - các hàm thao tác với bảng contacts trong Room.
 *
 * Bao gồm đúng các chức năng:
 *  - Lấy danh sách
 *  - Thêm
 *  - Sửa
 *  - Xoá
 *  - Tìm kiếm theo tên hoặc số điện thoại
 */
@Dao
interface ContactDao {

    /** Lấy tất cả liên hệ, sắp xếp theo tên */
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    /** Tìm kiếm theo tên hoặc số điện thoại */
    @Query(
        "SELECT * FROM contacts " +
        "WHERE name LIKE '%' || :query || '%' " +
        "OR phoneNumber LIKE '%' || :query || '%' " +
        "ORDER BY name ASC"
    )
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    /** Thêm liên hệ */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    /** Cập nhật liên hệ */
    @Update
    suspend fun updateContact(contact: ContactEntity)

    /** Xoá một liên hệ */
    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    /** Xoá tất cả (không bắt buộc nhưng hữu ích khi test) */
    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()
}