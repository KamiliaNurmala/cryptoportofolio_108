// data/repository/AuthRepository.kt
package com.example.cryptoportfolio.data.repository

import com.example.cryptoportfolio.data.local.UserPreferences
import com.example.cryptoportfolio.data.local.dao.UserDao
import com.example.cryptoportfolio.data.local.entity.User
import com.example.cryptoportfolio.util.PasswordUtil
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val userDao: UserDao,
    private val userPreferences: UserPreferences
) {

    val currentUserId: Flow<Int?> = userPreferences.userIdFlow

    val currentUsername: Flow<String?> = userPreferences.usernameFlow
    val currentEmail: Flow<String?> = userPreferences.emailFlow

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val hashedPassword = PasswordUtil.hashPassword(password)
            val user = userDao.login(email, hashedPassword)

            if (user != null) {
                userPreferences.saveUser(user.id, user.username, user.email)
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(username: String, email: String, password: String): Result<User> {
        return try {
            // Check if email already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("Email already registered"))
            }

            val hashedPassword = PasswordUtil.hashPassword(password)
            val user = User(
                username = username,
                email = email,
                password = hashedPassword
            )

            val userId = userDao.register(user)
            val newUser = user.copy(id = userId.toInt())

            userPreferences.saveUser(newUser.id, newUser.username, newUser.email)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPreferences.clearUser()
    }
}