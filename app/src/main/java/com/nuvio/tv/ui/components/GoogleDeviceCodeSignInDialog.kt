@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.R
import com.nuvio.tv.core.device.DeviceFormFactor
import com.nuvio.tv.core.qr.QrCodeGenerator
import com.nuvio.tv.livetv.sync.SignInPrompt
import com.nuvio.tv.ui.screens.settings.SettingsDialogActionButton
import com.nuvio.tv.ui.screens.settings.SettingsDialogActionRow
import com.nuvio.tv.ui.theme.NuvioTheme
import kotlinx.coroutines.delay

@Composable
fun GoogleDeviceCodeSignInDialog(
    title: String,
    prompt: SignInPrompt,
    instruction: String,
    waitingMessage: String,
    onCancel: () -> Unit
) {
    val cancelFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(80)
        runCatching { cancelFocus.requestFocus() }
    }
    val context = LocalContext.current
    val touchPreferred = DeviceFormFactor.prefersTouchGuide(context)
    var showQr by remember(touchPreferred) { mutableStateOf(!touchPreferred) }
    val openUrl = rememberOpenExternalUrl()
    val qrBitmap = remember(prompt.qrUrl, showQr) {
        if (!showQr) null
        else runCatching { QrCodeGenerator.generate(prompt.qrUrl, 420, margin = 1) }.getOrNull()
    }
    val shortUrl = prompt.url.removePrefix("https://").removePrefix("http://")
    val bodyInstruction = if (touchPreferred) {
        stringResource(R.string.drive_device_code_instruction_handheld)
    } else {
        instruction
    }
    NuvioDialog(onDismiss = onCancel, title = title, width = 560.dp) {
        Text(
            text = bodyInstruction,
            style = MaterialTheme.typography.bodyMedium,
            color = NuvioTheme.colors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (touchPreferred) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TouchMaterial3Button(
                    text = stringResource(R.string.drive_sign_in_open_browser),
                    onClick = { openUrl(prompt.qrUrl) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NuvioTheme.colors.Secondary,
                        contentColor = NuvioTheme.colors.OnSecondary
                    ),
                    labelColor = NuvioTheme.colors.OnSecondary,
                )
                M3Text(
                    text = if (showQr) {
                        stringResource(R.string.auth_sign_in_hide_qr)
                    } else {
                        stringResource(R.string.auth_sign_in_show_qr)
                    },
                    modifier = Modifier
                        .clickable { showQr = !showQr }
                        .padding(vertical = 4.dp),
                    color = NuvioTheme.colors.TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (showQr && qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.cd_qr_code),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(196.dp),
                contentScale = ContentScale.Fit
            )
        }
        if (showQr || !touchPreferred) {
            Text(
                text = prompt.userCode,
                style = MaterialTheme.typography.displaySmall,
                color = NuvioTheme.colors.Secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = shortUrl,
                style = MaterialTheme.typography.titleMedium,
                color = NuvioTheme.colors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = waitingMessage,
            style = MaterialTheme.typography.bodySmall,
            color = NuvioTheme.colors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        SettingsDialogActionRow(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.focusRequester(cancelFocus)) {
                SettingsDialogActionButton(
                    text = stringResource(R.string.action_cancel),
                    onClick = onCancel
                )
            }
        }
    }
}
