package com.geedkajannada.feature.tasbeeh.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geedkajannada.R
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Night
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle

@Composable
fun CompletionCard(total: Int, goal: Int, rounds: Int, onRestart: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Night, RoundedCornerShape(Dimens.HeroRadius))
            .padding(Dimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(stringResource(R.string.tas_complete, total, goal), style = MaterialTheme.typography.titleMedium, color = Sun)
        Text("تَقَبَّلَ ٱللَّهُ مِنَّا وَمِنْكُمْ", style = arabicStyle(30), color = White, textAlign = TextAlign.Center)
        Text(stringResource(R.string.tas_complete_latin), style = MaterialTheme.typography.titleSmall, color = White, textAlign = TextAlign.Center)
        Text(stringResource(R.string.tas_complete_meaning), style = MaterialTheme.typography.bodyMedium, color = White, textAlign = TextAlign.Center)
        Text(stringResource(R.string.rounds_done, rounds), style = MaterialTheme.typography.bodySmall, color = White)
        Button(onClick = onRestart, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
            Text(stringResource(R.string.reset))
        }
    }
}
