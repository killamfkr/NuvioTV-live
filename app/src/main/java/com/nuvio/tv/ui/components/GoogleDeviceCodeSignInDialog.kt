@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.nuvio.tv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nuvio.tv.R
import com.nuvio.tv.core.qr.QrCodeGenerator
import com.nuvio.tv.livetv.sync.SignInPrompt
import com.nuvio.tv.ui.components.NuvioDialog
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
    val qrBitmap = remember(prompt.qrUrl) {
        runCatching { QrCodeGenerator.generate(prompt.qrUrl, 420, margin = 1) }.getOrNull()
    }
    val shortUrl = prompt.url.removePrefix("https://").removePrefix("http://")
    NuvioDialog(onDismiss = onCancel, title = title, width = 560.dp) {
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            color = NuvioTheme.colors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.cd_qr_code),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(196.dp),
                contentScale = ContentScale.Fit
            )
        }
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
