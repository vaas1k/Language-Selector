package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.theme.Ui

@Composable
fun ShizukuRequiredWarning(
    onClickContinue: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        shape = Ui.CardShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onClickContinue() }
            ) { Text(stringResource(id = R.string.proceed)) }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.permissions_required),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.shizuku_required),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
