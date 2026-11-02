package com.example.project_auth.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(email)
    }
}
