package com.app.appbitowear

import android.app.Application
import com.app.appbitowear.di.AppContainer

class AppbitoApplication: Application() {
    val container by lazy { AppContainer(this) }
}