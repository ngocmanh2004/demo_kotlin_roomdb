package com.example.myapplication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ContactRepository - lớp trung gian giữa ViewModel và DAO.
 *
 * Mục đích: ẩn chi tiết Room, cung cấp API đơn giản cho ViewModel.
 */
class ContactRepository(
    private val contactDao: ContactDao
) {

    /** Dòng dữ liệu tất cả liên hệ, tự động update khi database thay đổi */
    val allContacts: Flow<List<Contact>> =
        contactDao.getAllContacts().map { list -> list.map { it.toContact() } }

    /** Tìm kiếm theo tên hoặc số điện thoại */
    fun searchContacts(query: String): Flow<List<Contact>> =
        if (query.isBlank()) {
            allContacts
        } else {
            contactDao.searchContacts(query).map { list ->
                list.map { it.toContact() }
            }
        }

    suspend fun insert(contact: Contact) {
        contactDao.insertContact(contact.toEntity())
    }

    suspend fun update(contact: Contact) {
        contactDao.updateContact(contact.toEntity())
    }

    suspend fun delete(contact: Contact) {
        contactDao.deleteContact(contact.toEntity())
    }

    suspend fun deleteAll() {
        contactDao.deleteAllContacts()
    }
}