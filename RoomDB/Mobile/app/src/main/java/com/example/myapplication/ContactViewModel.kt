package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ContactViewModel - chứa logic xử lý cho màn hình danh bạ.
 *
 * Chức năng:
 *  - Quan sát danh sách liên hệ từ repository
 *  - Lọc theo chuỗi tìm kiếm
 *  - Gọi insert / update / delete
 */
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository

    /** text tìm kiếm người dùng nhập */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** danh sách liên hệ sau khi áp dụng tìm kiếm */
    val contacts: StateFlow<List<Contact>>

    init {
        val dao = ContactDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(dao)

        // Kết hợp allContacts với searchQuery và dữ liệu từ database
        contacts = _searchQuery
            .debounce(200)
            .flatMapLatest { query -> repository.searchContacts(query) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addContact(name: String, phone: String) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            repository.insert(Contact(name = name.trim(), phoneNumber = phone.trim()))
        }
    }

    fun updateContact(contact: Contact) {
        viewModelScope.launch {
            repository.update(contact)
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.delete(contact)
        }
    }
}

/**
 * Factory để tạo ContactViewModel với tham số Application.
 * Dùng trong MainActivity khi gọi viewModel(factory = ...).
 */
class ContactViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}