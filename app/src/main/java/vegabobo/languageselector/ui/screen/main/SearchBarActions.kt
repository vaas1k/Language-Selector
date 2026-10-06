package vegabobo.languageselector.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import vegabobo.languageselector.R
import vegabobo.languageselector.ui.theme.pressClickable

@Composable
fun SearchBarActions(
    isDropdownVisible: Boolean = false,
    isShowingSystemApps: Boolean = false,
    onClickToggleDropdown: () -> Unit,
    onToggleDropdown: () -> Unit,
    onClickToggleSystemApps: () -> Unit,
    onClickAbout: () -> Unit,
    onClickOwnLanguage: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val description = stringResource(R.string.own_language)
        Row(
            modifier = Modifier
                .pressClickable(CircleShape, Color.Transparent, onClick = onClickOwnLanguage)
                .semantics(mergeDescendants = true) { contentDescription = description; role = Role.Button }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = LocalConfiguration.current.locales[0].language.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Box(
            modifier = Modifier.wrapContentSize(Alignment.Center)
        ) {
            ToolbarNormal(
                onToggleDropdown = { onToggleDropdown() }
            )

            DropdownMenu(
                expanded = isDropdownVisible,
                onDismissRequest = { onClickToggleDropdown() }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (isShowingSystemApps)
                                stringResource(R.string.show_only_user_apps)
                            else
                                stringResource(R.string.show_system_apps)
                        )
                    },
                    onClick = { onClickToggleSystemApps() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.about)) },
                    onClick = { onClickAbout(); onClickToggleDropdown() }
                )
            }
        }
    }
}

@Composable
fun ToolbarNormal(
    onToggleDropdown: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onToggleDropdown() }) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More icon"
            )
        }
    }
}
