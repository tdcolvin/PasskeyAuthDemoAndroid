package com.tdcolvin.passkeyauthdemo.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.tdcolvin.passkeyauthdemo.PasskeyDemoViewModel
import com.tdcolvin.passkeyauthdemo.ui.signedout.SignedOutScreen
import com.tdcolvin.passkeyauthdemo.ui.signup.SignUpScreen

data object SignedOutNavState
data class SignUpNavState(val username: String)
data class SignInNavState(val username: String)
data object SignedInNavState

@Composable
fun PasskeyDemoNav(
    modifier: Modifier = Modifier,
    viewModel: PasskeyDemoViewModel = viewModel()
) {
    val localContext = LocalContext.current

    val credentialManager = remember { CredentialManager.create(localContext) }

    val backStack = remember { mutableStateListOf<Any>(SignedOutNavState) }

    val username = remember {
        String((0..5).map { "abcdefghijklmnopqrstuvwxyz0123456789".random() }.toCharArray())
    }

    NavDisplay (
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<SignedOutNavState> {
                SignedOutScreen(
                    modifier = modifier.fillMaxSize().padding(20.dp),
                    username = username,
                    navigateToSignUp = { backStack.add(SignUpNavState(username)) },
                    navigateToSignIn = { backStack.add(SignInNavState(username)) }
                )
            }

            entry<SignUpNavState> {
                SignUpScreen(
                    modifier = modifier.fillMaxSize().padding(20.dp),
                    username = username
                )
            }
        }
    )

    var signedIn by remember { mutableStateOf(false) }

    if (signedIn) {
        SignedInScreen(
            modifier = modifier.fillMaxSize().padding(20.dp),
            username = username,
            onSignOut = { signedIn = false }
        )
    }
    else {
        /*SignedOutScreen(
            modifier = modifier.fillMaxSize().padding(20.dp),
            username = username,
            credentialManager = credentialManager,
            getPasskeyRegisterRequestJson = viewModel::getPasskeyRegisterRequestJson,
            sendRegistrationResponse = viewModel::sendRegistrationResponse,
            getPasskeyAuthenticationRequestJson = viewModel::getPasskeyAuthenticationRequestJson,
            sendAuthenticationResponse = viewModel::sendAuthenticationResponse,
            onSignedIn = { signedIn = true }
        )*/
    }
}