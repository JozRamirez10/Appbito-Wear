@file:Suppress("UNCHECKED_CAST")

package com.app.appbitowear.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

inline fun <reified VM : ViewModel> buildViewModelFactory(crossinline creator: () -> VM)
    : ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return creator() as T
        }
    }
}