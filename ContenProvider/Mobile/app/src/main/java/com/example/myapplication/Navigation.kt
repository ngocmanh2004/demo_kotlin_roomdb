
package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api

// ...existing imports above...

/**
 * Navigation.kt - Điều hướng trong ứng dụng.
 *
 * Ở đây dùng Navigation Compose nhưng chỉ có một màn hình chính.
 * Mục đích là thể hiện cách khai báo NavHost đơn giản đúng theo yêu cầu bài.
 */
object Destinations {
    const val CONTACT_LIST = "contact_list"
    const val ADD_CONTACT = "add_contact"
    const val EDIT_CONTACT = "edit_contact/{contactId}"
    const val CONTACT_DETAIL = "contact_detail/{contactId}"
    const val CALL = "call/{contactId}"
    const val SMS = "sms/{contactId}"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: ContactViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.CONTACT_LIST
    ) {
        composable(Destinations.CONTACT_LIST) {
            ContactScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = Destinations.CONTACT_DETAIL,
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.toIntOrNull()
            if (contactId != null) {
                ContactDetailScreen(viewModel = viewModel, navController = navController, contactId = contactId)
            }
        }
        composable(Destinations.ADD_CONTACT) {
            AddContactScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = "edit_contact/{contactId}",
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.toIntOrNull()
            if (contactId != null) {
                EditContactScreen(viewModel = viewModel, navController = navController, contactId = contactId)
            }
        }
        composable(
            route = "call/{contactId}",
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.toIntOrNull()
            if (contactId != null) {
                CallScreen(viewModel = viewModel, navController = navController, contactId = contactId)
            }
        }
        composable(
            route = "sms/{contactId}",
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")?.toIntOrNull()
            if (contactId != null) {
                SmsScreen(viewModel = viewModel, navController = navController, contactId = contactId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(viewModel: ContactViewModel, navController: NavController, contactId: Int) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (contact == null) {
        Text("Không tìm thấy liên hệ")
        return
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TopAppBar with back button
        androidx.compose.material3.TopAppBar(
            title = { Text("Chi tiết liên hệ") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Avatar tròn với ký tự đầu
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(contact.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(contact.phoneNumber, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    // Gọi hệ thống
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:${contact.phoneNumber}")
                    }
                    navController.context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Call, contentDescription = "Gọi", tint = MaterialTheme.colorScheme.primary)
                }
                Text("Gọi", fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    // Nhắn tin hệ thống
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("smsto:${contact.phoneNumber}")
                    }
                    navController.context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Email, contentDescription = "Nhắn tin", tint = MaterialTheme.colorScheme.primary)
                }
                Text("Nhắn tin", fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    navController.navigate("edit_contact/${contact.id}")
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = MaterialTheme.colorScheme.primary)
                }
                Text("Sửa", fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Xoá", tint = MaterialTheme.colorScheme.error)
                }
                Text("Xoá", fontSize = 14.sp)
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xoá") },
            text = { Text("Bạn có chắc muốn xoá liên hệ này không?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteContact(contact)
                    showDeleteDialog = false
                    navController.popBackStack()
                }) {
                    Text("Xoá")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Huỷ")
                }
            }
        )
    }
}

@Composable
fun CallScreen(viewModel: ContactViewModel, navController: NavController, contactId: Int) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Đang gọi tới", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(contact?.name ?: "", fontSize = 18.sp)
        Text(contact?.phoneNumber ?: "", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { /* Tắt/mở loa giả lập */ }) { Text("Loa") }
            Button(onClick = { /* Tắt/mở mic giả lập */ }) { Text("Mic") }
            Button(onClick = { navController.popBackStack() }) { Text("Kết thúc") }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("(Chỉ là giao diện mô phỏng, không thực hiện cuộc gọi thật)", fontSize = 12.sp)
    }
}

@Composable
fun SmsScreen(viewModel: ContactViewModel, navController: NavController, contactId: Int) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    var message by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Nhắn tin tới", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(contact?.name ?: "", fontSize = 18.sp)
        Text(contact?.phoneNumber ?: "", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Nội dung tin nhắn") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { /* Gửi tin nhắn giả lập */ message = "" }) { Text("Gửi") }
            Button(onClick = { navController.popBackStack() }) { Text("Thoát") }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("(Chỉ là giao diện mô phỏng, không gửi tin nhắn thật)", fontSize = 12.sp)
    }
// ...existing code...
}

@Composable
fun EditContactScreen(viewModel: ContactViewModel, navController: NavController, contactId: Int) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val contact = contacts.firstOrNull { it.id == contactId }
    var name by remember(contact) { mutableStateOf(contact?.name ?: "") }
    var phone by remember(contact) { mutableStateOf(contact?.phoneNumber ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Sửa liên hệ")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Số điện thoại") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                if (contact != null && name.isNotBlank() && phone.isNotBlank()) {
                    viewModel.updateContact(contact.copy(name = name, phoneNumber = phone))
                    navController.popBackStack()
                }
            }) {
                Text("Cập nhật")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Huỷ")
            }
        }
    }
}


@Composable
fun AddContactScreen(viewModel: ContactViewModel, navController: NavController) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Thêm liên hệ mới")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Số điện thoại") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = {
                if (name.isNotBlank() && phone.isNotBlank()) {
                    viewModel.addContact(name, phone)
                    navController.popBackStack()
                }
            }) {
                Text("Thêm")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Huỷ")
            }
        }
    }
}