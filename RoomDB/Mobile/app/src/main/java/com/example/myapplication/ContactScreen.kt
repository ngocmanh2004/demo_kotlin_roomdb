package com.example.myapplication

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Màn hình chính của ứng dụng danh bạ.
 *
 * Có các chức năng:
 *  - Thêm liên hệ
 *  - Sửa, xoá liên hệ
 *  - Tìm kiếm theo tên hoặc số điện thoại
 *  - Gọi điện
 *  - Gửi tin nhắn
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    viewModel: ContactViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    val allContacts by viewModel.contacts.collectAsState()
    val contacts = remember(searchText, allContacts) {
        if (searchText.isBlank()) allContacts
        else allContacts.filter {
            it.name.contains(searchText, ignoreCase = true) ||
            it.phoneNumber.contains(searchText, ignoreCase = true)
        }
    }

    var contactToDelete by remember { mutableStateOf<Contact?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Danh bạ điện thoại") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("add_contact")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Thêm liên hệ")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Ô tìm kiếm
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Tìm kiếm (tên hoặc số)") },
                modifier = Modifier.fillMaxWidth()
                // Không dùng .focusRequester hoặc .requestFocus để tránh auto-focus
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Danh sách liên hệ",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Danh sách liên hệ
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(contacts) { contact ->
                    ContactItem(
                        contact = contact,
                        onClick = {
                            navController.navigate("contact_detail/${contact.id}")
                        }
                    )
                }
            }
        }

        // Dialog xác nhận xoá
        if (contactToDelete != null) {
            AlertDialog(
                onDismissRequest = { contactToDelete = null },
                title = { Text("Xác nhận xoá") },
                text = { Text("Bạn có chắc muốn xoá liên hệ này không?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteContact(contactToDelete!!)
                        contactToDelete = null
                    }) {
                        Text("Xoá")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { contactToDelete = null }) {
                        Text("Huỷ")
                    }
                }
            )
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = contact.phoneNumber,
                    fontSize = 14.sp
                )
            }
        }
    }
}