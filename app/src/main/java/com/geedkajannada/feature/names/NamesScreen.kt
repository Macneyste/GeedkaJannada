package com.geedkajannada.feature.names

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geedkajannada.R
import com.geedkajannada.ui.BackHeader
import com.geedkajannada.ui.ScreenTitle
import com.geedkajannada.ui.appViewModel
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo
import com.geedkajannada.ui.theme.IndigoLight
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.Night
import com.geedkajannada.ui.theme.Sun
import com.geedkajannada.ui.theme.White
import com.geedkajannada.ui.theme.arabicStyle

/** Barashada magacyada Alle: liis, raadin, horumar iyo kaar barasho. */
@Composable
fun NamesScreen(vm: NamesViewModel = appViewModel { NamesViewModel(it.names) }) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val open = state.open
    if (open != null) {
        BackHandler(onBack = vm::close)
        NameCard(open, state, vm)
    } else {
        NamesList(state, vm)
    }
}

@Composable
private fun NamesList(state: NamesUiState, vm: NamesViewModel) {
    Column(Modifier.fillMaxSize().padding(horizontal = Dimens.ScreenPadding)) {
        ScreenTitle(stringResource(R.string.names_title), Modifier.padding(top = 8.dp))
        Text(stringResource(R.string.names_progress, state.learned.size, state.total), color = BrownMid)
        LinearProgressIndicator(
            progress = { if (state.total == 0) 0f else state.learned.size.toFloat() / state.total },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            color = IndigoLight,
            trackColor = Line,
        )
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQuery,
            placeholder = { Text(stringResource(R.string.names_search)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(Dimens.CardRadiusSmall),
            modifier = Modifier.fillMaxWidth(),
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(104.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.names, key = { it.id }) { n ->
                val done = n.id in state.learned
                Column(
                    Modifier
                        .heightIn(min = 112.dp)
                        .background(if (done) Indigo else White, RoundedCornerShape(16.dp))
                        .border(1.dp, if (done) Indigo else Line, RoundedCornerShape(16.dp))
                        .clickable { vm.open(n.id) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(n.id.toString(), style = MaterialTheme.typography.labelSmall, color = if (done) Sun else BrownMid)
                    Text(n.arabic, style = arabicStyle(22), color = if (done) White else BrownInk, textAlign = TextAlign.Center)
                    Text(
                        n.latin,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (done) White else BrownInk,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun NameCard(name: AllahName, state: NamesUiState, vm: NamesViewModel) {
    val done = name.id in state.learned
    Column(Modifier.fillMaxSize()) {
        BackHeader(stringResource(R.string.names_title), vm::close)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 320.dp)
                    .background(Night, RoundedCornerShape(Dimens.HeroRadius))
                    .clickable(onClickLabel = stringResource(R.string.names_show_meaning), onClick = vm::showMeaning)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("${name.id} / ${state.total}", style = MaterialTheme.typography.labelLarge, color = Sun)
                Text(name.arabic, style = arabicStyle(48), color = White, textAlign = TextAlign.Center)
                Text(name.latin, style = MaterialTheme.typography.headlineSmall, color = White, textAlign = TextAlign.Center)
                // Marka hore isku day inaad xusuusato, kadibna taabo.
                Text(
                    if (state.meaningShown) name.meaning else stringResource(R.string.names_show_meaning),
                    style = if (state.meaningShown) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
                    color = if (state.meaningShown) Sun else White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
            val learnedModifier = Modifier.fillMaxWidth().heightIn(min = Dimens.TouchMin)
            if (done) {
                OutlinedButton(onClick = vm::toggleLearned, modifier = learnedModifier) {
                    Text(stringResource(R.string.names_unlearn))
                }
            } else {
                Button(onClick = vm::toggleLearned, modifier = learnedModifier) { Text(stringResource(R.string.names_learned)) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { vm.step(-1) }, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.adhkar_prev))
                }
                TextButton(onClick = { vm.step(1) }, modifier = Modifier.heightIn(min = Dimens.TouchMin)) {
                    Text(stringResource(R.string.adhkar_next))
                }
            }
            Text(
                stringResource(R.string.names_note),
                style = MaterialTheme.typography.bodySmall,
                color = BrownMid,
                textAlign = TextAlign.Center,
            )
        }
    }
}
