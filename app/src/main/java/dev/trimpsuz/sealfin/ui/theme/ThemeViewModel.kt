package dev.trimpsuz.sealfin.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.trimpsuz.sealfin.data.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppTheme { LIGHT, DARK, SYSTEM }

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _theme = MutableStateFlow(AppTheme.SYSTEM)
    val theme: StateFlow<AppTheme> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.themeFlow.collect { stored ->
                _theme.value = AppTheme.valueOf(stored)
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            _theme.value = theme
            dataStore.saveTheme(theme.name)
        }
    }
}
