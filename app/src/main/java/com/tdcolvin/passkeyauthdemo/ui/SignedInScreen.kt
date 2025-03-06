package com.tdcolvin.passkeyauthdemo.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tdcolvin.passkeyauthdemo.ui.theme.PasskeyAuthDemoAndroidTheme

@Composable
fun SignedInScreen(
    modifier: Modifier = Modifier,
    username: String,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            textAlign = TextAlign.Center,
            text = "Successfully signed in as $username",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            modifier = Modifier.padding(top = 20.dp),
            textAlign = TextAlign.Center,
            text = "Registered and authenticated to auth.tomcolvin.co.uk",
        )

        Button(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            onClick = onSignOut
        ) {
            Text("Sign out")
        }
    }
}

@Preview(showBackground = false)
@Composable
fun SignedInScreenPreview_Light() {
    PasskeyAuthDemoAndroidTheme {
        Surface {
            SignedInScreen(username = "John Doe", onSignOut = {})
        }
    }
}

@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignedInScreenPreview_Dark() {
    PasskeyAuthDemoAndroidTheme {
        Surface {
            SignedInScreen(username = "John Doe", onSignOut = {})
        }
    }
}