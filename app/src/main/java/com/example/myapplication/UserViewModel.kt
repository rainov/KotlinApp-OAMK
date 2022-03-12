package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class UserViewModel: ViewModel() {
    var isLoggedIn = mutableStateOf(false)

    fun logInUser(){
        isLoggedIn.value = true
    }
    fun logOutUser() {
        isLoggedIn.value = false
    }

}