package dev.trimpsuz.sealfin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import dev.trimpsuz.sealfin.ui.nav.SealfinNavHost
import dev.trimpsuz.sealfin.ui.theme.AppTheme
import dev.trimpsuz.sealfin.ui.theme.SealfinTheme
import dev.trimpsuz.sealfin.ui.theme.ThemeViewModel
import androidx.activity.viewModels

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val currentTheme by themeViewModel.theme.collectAsState()

            val useDarkTheme = when(currentTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }
            SealfinTheme(
                darkTheme = useDarkTheme
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SealfinNavHost()
                }
            }
        }
    }
}
