package com.servitrust.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servitrust.app.repository.UserRepository
import com.servitrust.app.ui.screens.LoginUiState
import com.servitrust.app.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel : ViewModel() {

    private val repository = UserRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(context: Context, email: String, password: String) {
        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                loading = true,
                message = "",
                isSuccess = false
            )

            try {
                val user = repository.login(email, password)

                SessionManager.saveUserSession(
                    context = context,
                    userId = user.id,
                    userName = user.name
                )

                _uiState.value = _uiState.value.copy(
                    loading = false,
                    message = "LOGIN SUCCESS Welcome ${user.name}",
                    isSuccess = true
                )

            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    message = "LOGIN FAILED (HTTP ${e.code()})",
                    isSuccess = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    message = e.message ?: "LOGIN FAILED Invalid email or password",
                    isSuccess = false
                )
            }
        }
    }
}