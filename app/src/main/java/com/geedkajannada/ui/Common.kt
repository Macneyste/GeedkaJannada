package com.geedkajannada.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.compose.ui.platform.LocalContext
import com.geedkajannada.AppContainer
import com.geedkajannada.container
import com.geedkajannada.ui.theme.Dimens
import com.geedkajannada.ui.theme.Line
import com.geedkajannada.ui.theme.White

@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM,
): VM {
    val c = LocalContext.current.container
    return viewModel(key = key, factory = viewModelFactory { initializer { create(c) } })
}

@Composable
fun GeedkaCard(
    modifier: Modifier = Modifier,
    border: BorderStroke = BorderStroke(1.dp, Line),
    containerColor: Color = White,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Column(Modifier.padding(Dimens.Spacing), content = content)
    }
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Box(modifier.heightIn(min = Dimens.TouchMin), contentAlignment = Alignment.CenterStart) {
        Text(text, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}
