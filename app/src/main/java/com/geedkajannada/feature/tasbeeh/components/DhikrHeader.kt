package com.geedkajannada.feature.tasbeeh.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.geedkajannada.data.Dhikr
import com.geedkajannada.ui.theme.BrownInk
import com.geedkajannada.ui.theme.BrownMid
import com.geedkajannada.ui.theme.arabicStyle

/** Carabi (Amiri 34sp), Ingiriisi bold 22sp, Soomaali 16sp, macne 13sp. Beddelka: fade 800ms. */
@Composable
fun DhikrHeader(dhikr: Dhikr, modifier: Modifier = Modifier) {
    Crossfade(dhikr, modifier, tween(800), label = "dhikr") { d ->
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(d.arabic, style = arabicStyle(32), color = BrownInk, textAlign = TextAlign.Center)
            Text(stringResource(d.englishRes), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrownInk)
            Text(stringResource(d.nameRes), fontSize = 16.sp, color = BrownInk, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(d.meaningEnRes) + " · " + stringResource(d.meaningSoRes),
                fontSize = 13.sp,
                color = BrownMid,
                textAlign = TextAlign.Center,
            )
        }
    }
}
