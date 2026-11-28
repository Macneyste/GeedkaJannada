package com.geedkajannada.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geedkajannada.R
import com.geedkajannada.ui.BackHeader
import com.geedkajannada.ui.GeedkaCard
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Indigo

data class MoreItem(val label: Int, val icon: Int, val onClick: () -> Unit)

/** Bogga "Kale": wax walba oo aan tab lahayn (waxaa laga furaa Hoyga). */
@Composable
fun MoreScreen(onBack: () -> Unit, items: List<MoreItem>) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BackHeader(stringResource(R.string.nav_more), onBack)
        items.forEach { item ->
            GeedkaCard(Modifier.clickable(onClick = item.onClick)) {
                Row(Modifier.heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(item.icon), contentDescription = null, tint = Indigo)
                    Text(
                        stringResource(item.label),
                        style = MaterialTheme.typography.titleMedium,
                        color = BrownInk,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
        }
    }
}

/** Ku saabsan iyo asturnaanta. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        BackHeader(stringResource(R.string.about_title), onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.about_app), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleMedium, color = Indigo)
            Text(stringResource(R.string.privacy_body), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.about_sources_title), style = MaterialTheme.typography.titleMedium, color = Indigo)
            Text(stringResource(R.string.about_sources), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
