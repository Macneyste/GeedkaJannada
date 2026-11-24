package com.geedkajannada

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.geedkajannada.ui.nav.AppNav
import com.geedkajannada.ui.theme.GeedkaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { GeedkaTheme { AppNav() } }
    }
}
