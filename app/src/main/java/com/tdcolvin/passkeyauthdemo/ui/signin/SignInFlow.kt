package com.tdcolvin.passkeyauthdemo.ui.signin

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tdcolvin.passkeyauthdemo.ui.theme.PasskeyAuthDemoAndroidTheme
import com.tdcolvin.passkeyauthdemo.util.prettyPrintJson

@Composable
fun SignInScreen(
    modifier: Modifier = Modifier,
    username: String,
    viewModel: SignInViewModel = viewModel()
) {
    val localActivity = LocalActivity.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SignInFlow(
        modifier = modifier,
        username = username,
        getAuthenticationOptionsJsonResult = state.getAuthenticationOptionsJsonResult,
        isGettingAuthenticationOptionsJson = state.isGettingAuthenticationOptionsJson,
        getCredentialResult = state.getCredentialResult,
        isGettingCredential = state.isGettingCredential,
        sendAuthenticationResponseToServerResult = state.sendAuthenticationResponseToServerResult,
        isSendingAuthenticationResponseToServer = state.isSendingAuthenticationResponseToServer,
        getAuthenticationOptionsJson = { viewModel.getPasskeyAuthenticationRequest(username) },
        doSignIn = { viewModel.getCredential(localActivity ?: throw Exception("No activity")) },
        sendAuthenticationResponseToServer = viewModel::sendAuthenticationResponse
    )
}

@Composable
fun SignInFlow(
    modifier: Modifier = Modifier,
    username: String,
    getAuthenticationOptionsJsonResult: Result<String>?,
    isGettingAuthenticationOptionsJson: Boolean,
    getCredentialResult: Result<String>?,
    isGettingCredential: Boolean,
    sendAuthenticationResponseToServerResult: Result<Unit>?,
    isSendingAuthenticationResponseToServer: Boolean,
    getAuthenticationOptionsJson: () -> Unit,
    doSignIn: () -> Unit,
    sendAuthenticationResponseToServer: () -> Unit
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(
        getAuthenticationOptionsJsonResult?.isSuccess,
        getCredentialResult?.isSuccess,
        sendAuthenticationResponseToServerResult?.isSuccess
    ) {
        scrollState.animateScrollTo(Int.MAX_VALUE)
    }

    Column(modifier = modifier.verticalScroll(scrollState)) {
        SignInGetAuthenticationRequestStage(
            getAuthenticationOptionsJsonResult = getAuthenticationOptionsJsonResult,
            isGettingAuthenticationOptionsJson = isGettingAuthenticationOptionsJson,
            getAuthenticationOptionsJson = getAuthenticationOptionsJson
        )

        if (getAuthenticationOptionsJsonResult?.isSuccess == true) {
            Spacer(Modifier.height(20.dp))

            SignInGetCredentialStage(
                getCredentialResult = getCredentialResult,
                isGettingCredential = isGettingCredential,
                doSignIn = doSignIn
            )
        }

        if (getCredentialResult?.isSuccess == true) {
            Spacer(Modifier.height(20.dp))

            SignInSendAuthenticationResponseToServerStage(
                sendAuthenticationResponseToServerResult = sendAuthenticationResponseToServerResult,
                isSendingAuthenticationResponseToServer = isSendingAuthenticationResponseToServer,
                sendAuthenticationResponseToServer = sendAuthenticationResponseToServer
            )
        }

        if (sendAuthenticationResponseToServerResult?.isSuccess == true) {
            Spacer(Modifier.height(20.dp))

            SignInSuccessStage(username = username)
        }
    }
}

@Composable
fun SignInGetAuthenticationRequestStage(
    getAuthenticationOptionsJsonResult: Result<String>?,
    isGettingAuthenticationOptionsJson: Boolean,
    getAuthenticationOptionsJson: () -> Unit
) {
    val authenticationOptionsPrettyJson = remember(getAuthenticationOptionsJsonResult) {
        getAuthenticationOptionsJsonResult?.getOrNull()?.prettyPrintJson()
    }

    Column {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = getAuthenticationOptionsJson,
                enabled = !isGettingAuthenticationOptionsJson
            ) {
                Text("Get Authentication Options From Server")
            }

            if (isGettingAuthenticationOptionsJson) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (authenticationOptionsPrettyJson != null) {
            Text("Authentication options from server:")
            TextField(
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                value = authenticationOptionsPrettyJson,
                onValueChange = { }
            )
        }
    }
}

@Composable
fun SignInGetCredentialStage(
    getCredentialResult: Result<String>?,
    isGettingCredential: Boolean,
    doSignIn: () -> Unit
) {
    val credentialPrettyJson = remember(getCredentialResult) {
        getCredentialResult?.getOrNull()?.prettyPrintJson()
    }

    Column {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = doSignIn,
                enabled = !isGettingCredential
            ) {
                Text("Get Credential")
            }

            if (isGettingCredential) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    if (credentialPrettyJson != null) {
        Text("Credential details to send to server:")
        TextField(
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            value = credentialPrettyJson,
            readOnly = true,
            onValueChange = { }
        )
    }
}

@Composable
fun SignInSendAuthenticationResponseToServerStage(
    sendAuthenticationResponseToServerResult: Result<Unit>?,
    isSendingAuthenticationResponseToServer: Boolean,
    sendAuthenticationResponseToServer: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = sendAuthenticationResponseToServer,
                enabled = !isSendingAuthenticationResponseToServer
            ) {
                Text("Send Authentication Details To Server")
            }

            if (isSendingAuthenticationResponseToServer) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }

        sendAuthenticationResponseToServerResult?.exceptionOrNull()?.let { exception ->
            Text("Error sending authentication response to server: ${exception.message}")
        }
    }
}

@Composable
fun SignInSuccessStage(
    username: String
) {
    Text("Woohoo!", style = MaterialTheme.typography.displayLarge)
    Text("You have successfully signed in as $username")
}

@Preview
@Composable
fun SignInFlow_Preview_ErrorAtSend() {
    PasskeyAuthDemoAndroidTheme {
        SignInFlow(
            username = "abc123",
            getAuthenticationOptionsJsonResult = Result.success("Booo\nBoo\nBoo\nBoo"),
            isGettingAuthenticationOptionsJson = false,
            getCredentialResult = Result.success("{ }"),
            isGettingCredential = false,
            sendAuthenticationResponseToServerResult = Result.failure(Exception("No server")),
            isSendingAuthenticationResponseToServer = false,
            getAuthenticationOptionsJson = { },
            doSignIn = { },
            sendAuthenticationResponseToServer = { },
        )
    }
}

@Preview
@Composable
fun SignInFlow_Preview_Success() {
    PasskeyAuthDemoAndroidTheme {
        SignInFlow(
            username = "abc123",
            getAuthenticationOptionsJsonResult = Result.success("Booo\nBoo\nBoo\nBoo"),
            isGettingAuthenticationOptionsJson = false,
            getCredentialResult = Result.success("{ }"),
            isGettingCredential = false,
            sendAuthenticationResponseToServerResult = Result.success(Unit),
            isSendingAuthenticationResponseToServer = false,
            getAuthenticationOptionsJson = { },
            doSignIn = { },
            sendAuthenticationResponseToServer = { },
        )
    }
}
