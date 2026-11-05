package app.calendarmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.calendarmanager.R

/** Empty-state shell shown until each section's real content is built (Phases 3-6). */
@Composable
fun SectionScreen(tag: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.empty_state), style = MaterialTheme.typography.bodyLarge)
    }
}
