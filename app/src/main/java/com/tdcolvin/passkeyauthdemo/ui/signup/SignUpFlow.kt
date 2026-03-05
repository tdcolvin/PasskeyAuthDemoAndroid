package com.tdcolvin.passkeyauthdemo.ui.signup

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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SignUpFlow(
        modifier = modifier,
        getRegistrationOptionsJsonResult = state.getRegistrationOptionsJsonResult,
        isGettingRegistrationOptionsJson = state.isGettingRegistrationOptionsJson,
//        passkeyJson = state.passkeyJson,
        getRegistrationOptionsJson = { viewModel.getPasskeyRegistrationOptionsJson(username) },
//        createPasskey = { },
        sendPasskeyDetailsToServer = { }
    )
}
@Composable
fun SignUpFlow(
    modifier: Modifier = Modifier,
    getRegistrationOptionsJsonResult: Result<String>?,
    isGettingRegistrationOptionsJson: Boolean,
//    passkeyJson: String?,
    getRegistrationOptionsJson: () -> Unit,
//    createPasskey: () -> Unit,
    sendPasskeyDetailsToServer: () -> Unit
) {
    Column(modifier = modifier) {
        SignUpGetRegisterRequestStage(
            getRegistrationOptionsJsonResult = getRegistrationOptionsJsonResult,
            isGettingRegistrationOptionsJson = isGettingRegistrationOptionsJson,
            getRegistrationOptionsJson = getRegistrationOptionsJson
        )

//        if (getRegistrationOptionsJsonResult?.isSuccess == true) {
//            Spacer(Modifier.height(20.dp))
//
//            SignUpCreatePasskeyStage(
//                createPasskeyDetailsJsonResult = passkeyJson,
//                createPasskey = createPasskey
//            )
//        }

//        if (passkeyJson != null) {
//            Spacer(Modifier.height(20.dp))
//
//            SignUpSendPasskeyDetailsToServerStage(
//                sendPasskeyDetailsToServer = sendPasskeyDetailsToServer
//            )
//        }
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

//@Composable
//fun SignUpCreatePasskeyStage(
//    createPasskeyDetailsJsonResult: Result<String>?,
//    creatingPasskeyDetailsJson: Boolean,
//    createPasskey: () -> Unit
//) {
//    Button(onClick = createPasskey) {
//        Text("Create Passkey From Registration Options")
//    }
//
//    Spacer(Modifier.height(10.dp))
//
//    if (passkeyDetailsJson != null) {
//        Text("Passkey details to send to server:")
//        TextField(
//            modifier = Modifier.fillMaxWidth(),
//            minLines = 5,
//            textStyle = TextStyle(fontFamily = FontFamily.Monospace),
//            value = passkeyJson,
//            readOnly = true,
//            onValueChange = { }
//        )
//    }
//}

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
//            passkeyJson = "Moo\nBoo\nRoo",
            getRegistrationOptionsJson = { },
//            createPasskey = { },
            sendPasskeyDetailsToServer = { }
        )
    }
}