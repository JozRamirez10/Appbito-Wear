package com.app.appbitowear.data.network.interceptors

import android.content.Context
import androidx.core.content.edit
import com.app.appbitowear.constants.Auth
import com.app.appbitowear.constants.Constants
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class PersistentCookieJar(context: Context): CookieJar {

    private val prefs = context.getSharedPreferences(
        Constants.COOKIES_STORE,
        Context.MODE_PRIVATE
    )

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.firstOrNull{ it.name() == Auth.REFRESH_TOKEN }?.let { cookie ->
            prefs.edit{
                if (cookie.expiresAt() < System.currentTimeMillis()) {
                    remove(Auth.REFRESH_TOKEN)
                } else {
                    putString(Auth.REFRESH_TOKEN, cookie.value())
                }
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return prefs.getString(Auth.REFRESH_TOKEN, null)?.let { tokenValue ->
            listOf(
                Cookie.Builder()
                    .domain(url.host())
                    .name(Auth.REFRESH_TOKEN)
                    .value(tokenValue)
                    .build()
            )
        } ?: emptyList()
    }

    fun clearCookies() {
        prefs.edit {
            remove(Auth.REFRESH_TOKEN)
        }
    }
}