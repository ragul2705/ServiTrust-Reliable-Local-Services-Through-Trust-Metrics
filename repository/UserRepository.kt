package com.servitrust.app.repository

import com.servitrust.app.model.LoginRequest
import com.servitrust.app.model.User
import com.servitrust.app.network.ApiClient

class UserRepository {
    suspend fun login(email: String, password: String): User {
        return ApiClient.api.login(LoginRequest(email, password))
    }
}
