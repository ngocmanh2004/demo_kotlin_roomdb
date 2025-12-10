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

        /**
         * Đồng bộ các liên hệ từ Room database của app lên danh bạ hệ thống Android.
         * Chỉ thêm các liên hệ chưa có (trùng cả tên và số thì bỏ qua).
         */
        fun syncContactsToSystem(context: android.content.Context, onSuccess: (() -> Unit)? = null) {
            viewModelScope.launch {
                val contentResolver = context.contentResolver
                // Lấy toàn bộ liên hệ hệ thống hiện tại
                val systemContacts = mutableSetOf<Pair<String, String>>()
                val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val projection = arrayOf(
                    android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                )
                val cursor = contentResolver.query(uri, projection, null, null, null)
                cursor?.use {
                    while (it.moveToNext()) {
                        val name = it.getString(it.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        val number = it.getString(it.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))
                        systemContacts.add(name to number)
                    }
                }
                // Lấy toàn bộ liên hệ trong app
                val appContacts = contacts.value
                // Thêm vào hệ thống các liên hệ chưa có
                val toAdd = appContacts.filter { (it.name to it.phoneNumber) !in systemContacts }
                toAdd.forEach { contact ->
                        val ops = ArrayList<android.content.ContentProviderOperation>()
                        val rawContactInsertIndex = ops.size
                        ops.add(
                            android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                .withValue(android.provider.ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                .build()
                        )
                        // Tên
                        ops.add(
                            android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                .withValue(android.provider.ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                                .build()
                        )
                        // Số điện thoại
                        ops.add(
                            android.content.ContentProviderOperation.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(android.provider.ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                                .withValue(android.provider.ContactsContract.Data.MIMETYPE, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, contact.phoneNumber)
                                .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.TYPE, android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                .build()
                        )
                        try {
                            contentResolver.applyBatch(android.provider.ContactsContract.AUTHORITY, ops)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                if (toAdd.isNotEmpty()) {
                    onSuccess?.invoke()
                }
            }
        }
    /**
     * Đồng bộ danh bạ từ hệ thống Android vào Room database của app.
     */
    fun syncContactsFromSystem(context: android.content.Context, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val contentResolver = context.contentResolver
            val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val cursor = contentResolver.query(uri, projection, null, null, null)
            val newContacts = mutableListOf<Contact>()
            cursor?.use {
                while (it.moveToNext()) {
                    val name = it.getString(it.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val number = it.getString(it.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))
                    newContacts.add(Contact(name = name, phoneNumber = number))
                }
            }
            // Lấy danh sách liên hệ đã có trong DB (theo cả tên và số)
            val currentContacts = contacts.value
            val currentPairs = currentContacts.map { it.name to it.phoneNumber }.toSet()
            // Chỉ thêm những liên hệ chưa có đủ cả tên và số
            val toAdd = newContacts.filter { (it.name to it.phoneNumber) !in currentPairs }
            toAdd.forEach { repository.insert(it) }
            if (toAdd.isNotEmpty()) {
                onSuccess?.invoke()
            }
        }
    }

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