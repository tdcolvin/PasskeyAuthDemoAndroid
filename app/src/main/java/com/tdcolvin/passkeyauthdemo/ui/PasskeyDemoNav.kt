package com.tdcolvin.passkeyauthdemo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tdcolvin.passkeyauthdemo.PasskeyDemoViewModel

@Composable
fun PasskeyDemoNav(
    modifier: Modifier = Modifier,
    viewModel: PasskeyDemoViewModel = viewModel()
) {
    val localContext = LocalContext.current

    val credentialManager = remember { CredentialManager.create(localContext) }

    val username = remember {
        String((0..5).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.toCharArray())
    }
    var signedIn by remember { mutableStateOf(false) }

    if (signedIn) {
        SignedInScreen(
            modifier = modifier,
            username = username,
            onSignOut = { signedIn = false }
        )
    }
    else {
        SignedOutScreen(
            modifier = modifier,
            username = username,
            credentialManager = credentialManager,
            getPasskeyRegisterRequestJson = viewModel::getPasskeyRegisterRequestJson,
            sendRegistrationResponse = viewModel::sendRegistrationResponse,
            getPasskeyAuthenticationRequestJson = viewModel::getPasskeyAuthenticationRequestJson,
            sendAuthenticationResponse = viewModel::sendAuthenticationResponse,
            onSignedIn = { signedIn = true }
        )
    }
}