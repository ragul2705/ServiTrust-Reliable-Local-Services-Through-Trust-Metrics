package com.servitrust.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servitrust.app.model.RegisterRequest
import com.servitrust.app.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val loading: Boolean = false,
    val message: String = "",
    val isSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun setMessage(msg: String) {
        _uiState.value = _uiState.value.copy(message = msg)
    }

    fun register(
        name: String,
        email: String,
        password: String,
        location: String?,
        latitude: Double?,
        longitude: Double?
    ) {
        _uiState.value = RegisterUiState(loading = true)

        viewModelScope.launch {
            try {
                ApiClient.api.register(
                    RegisterRequest(
                        name = name,
                        email = email,
                        password = password,
                        location = location,
                        latitude = latitude,
                        longitude = longitude
                    )
                )
                _uiState.value = RegisterUiState(
                    loading = false,
                    message = "Registered Successfully",
                    isSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(
                    loading = false,
                    message = "Register failed ${e.message}",
                    isSuccess = false
                )
            }
        }
    }
}