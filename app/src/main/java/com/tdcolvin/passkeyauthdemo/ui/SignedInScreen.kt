package com.tdcolvin.passkeyauthdemo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SignedInScreen(
    modifier: Modifier = Modifier,
    username: String,
    onSignOut: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.weight(1f),
            text = "Successfully signed in as $username"
        )

        Button(onClick = onSignOut) {
            Text("Sign out")
        }
    }
}