package com.tdcolvin.passkeyauthdemo.ui.signedout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SignedOutScreen(
    modifier: Modifier = Modifier,
    username: String,
    navigateToSignUp: () -> Unit
) {
    SignedOutScreenContent(
        modifier = modifier,
        username = username,
        navigateToSignUp = navigateToSignUp
    )
}
@Composable
fun SignedOutScreenContent(
    modifier: Modifier = Modifier,
    username: String,
    navigateToSignUp: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Passkey demo",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(text = "Authenticating to auth.tomcolvin.co.uk", textAlign = TextAlign.Center)

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Your randomly generated username: $username",
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = navigateToSignUp
        ) {
            Text("Sign up (=register) with Passkey")
        }
    }
}