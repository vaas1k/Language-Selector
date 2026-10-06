package vegabobo.languageselector.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun BackButton(
    onClick: () -> Unit
){
    IconButton(onClick = { onClick() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Back arrow"
        )
    }
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.offset(x = (-12).dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(onBack)
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
