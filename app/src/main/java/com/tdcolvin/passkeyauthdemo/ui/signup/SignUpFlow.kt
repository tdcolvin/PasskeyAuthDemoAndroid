package com.tdcolvin.passkeyauthdemo.ui.signup

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tdcolvin.passkeyauthdemo.ui.theme.PasskeyAuthDemoAndroidTheme

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    username: String,
    viewModel: SignUpViewModel = viewModel()
) {
    val localActivity = LocalActivity.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SignUpFlow(
        modifier = modifier,
        getRegistrationOptionsJsonResult = state.getRegistrationOptionsJsonResult,
        isGettingRegistrationOptionsJson = state.isGettingRegistrationOptionsJson,
        createPasskeyResult = state.createPasskeyResult,
        isCreatingPasskey = state.isCreatingPasskey,
        getRegistrationOptionsJson = { viewModel.getPasskeyRegistrationOptionsJson(username) },
        createPasskey = { viewModel.createPasskeyFromRegistrationOptions(localActivity ?: throw Exception("No activity")) },
        sendPasskeyDetailsToServer = { }
    )
}
@Composable
fun SignUpFlow(
    modifier: Modifier = Modifier,
    getRegistrationOptionsJsonResult: Result<String>?,
    isGettingRegistrationOptionsJson: Boolean,
    createPasskeyResult: Result<String>?,
    isCreatingPasskey: Boolean,
    getRegistrationOptionsJson: () -> Unit,
    createPasskey: () -> Unit,
    sendPasskeyDetailsToServer: () -> Unit
) {
    Column(modifier = modifier) {
        SignUpGetRegisterRequestStage(
            getRegistrationOptionsJsonResult = getRegistrationOptionsJsonResult,
            isGettingRegistrationOptionsJson = isGettingRegistrationOptionsJson,
            getRegistrationOptionsJson = getRegistrationOptionsJson
        )

        if (getRegistrationOptionsJsonResult?.isSuccess == true) {
            Spacer(Modifier.height(20.dp))

            SignUpCreatePasskeyStage(
                createPasskeyResult = createPasskeyResult,
                isCreatingPasskey = isCreatingPasskey,
                createPasskey = createPasskey
            )
        }

        if (createPasskeyResult?.isSuccess == true) {
            Spacer(Modifier.height(20.dp))

            SignUpSendPasskeyDetailsToServerStage(
                sendPasskeyDetailsToServer = sendPasskeyDetailsToServer
            )
        }
    }
}

@Composable
fun SignUpGetRegisterRequestStage(
    getRegistrationOptionsJsonResult: Result<String>?,
    isGettingRegistrationOptionsJson: Boolean,
    getRegistrationOptionsJson: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = getRegistrationOptionsJson,
                enabled = !isGettingRegistrationOptionsJson
            ) {
                Text("Get Registration Options From Server")
            }

            if (isGettingRegistrationOptionsJson) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        getRegistrationOptionsJsonResult?.getOrNull()?.let { registrationOptionsJson ->
            Text("Registration options from server:")
            TextField(
                modifier = Modifier.fillMaxWidth(),
                minLines = 5,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                value = registrationOptionsJson,
                onValueChange = { }
            )
        }
    }
}

@Composable
fun SignUpCreatePasskeyStage(
    createPasskeyResult: Result<String>?,
    isCreatingPasskey: Boolean,
    createPasskey: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = createPasskey,
                enabled = !isCreatingPasskey
            ) {
                Text("Create Passkey From Registration Options")
            }

            if (isCreatingPasskey) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    createPasskeyResult?.getOrNull()?.let { passkeyDetailsJson ->
        Text("Passkey details to send to server:")
        TextField(
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
            textStyle = TextStyle(fontFamily = FontFamily.Monospace),
            value = passkeyDetailsJson,
            readOnly = true,
            onValueChange = { }
        )
    }
}

@Composable
fun SignUpSendPasskeyDetailsToServerStage(
    sendPasskeyDetailsToServer: () -> Unit
)  {
    Button(onClick = sendPasskeyDetailsToServer) {
        Text("Send Passkey Details To Server")
    }
}

@Preview
@Composable
fun SignUpFlow_Preview() {
    PasskeyAuthDemoAndroidTheme {
        SignUpFlow(
            getRegistrationOptionsJsonResult = Result.success("Booo\nBoo\nBoo\nBoo"),
            isGettingRegistrationOptionsJson = false,
            createPasskeyResult = Result.success("{ }"),
            isCreatingPasskey = false,
            getRegistrationOptionsJson = { },
            createPasskey = { },
            sendPasskeyDetailsToServer = { }
        )
    }
}