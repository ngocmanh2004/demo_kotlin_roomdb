package com.example.myapplication

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    // Lấy Application để tạo ViewModel
                    val application = application as Application
                    val contactViewModel: ContactViewModel = viewModel(
                        factory = ContactViewModelFactory(application)
                    )

                    AppNavHost(
                        navController = navController,
                        viewModel = contactViewModel
                    )
                }
            }
        }
    }
}