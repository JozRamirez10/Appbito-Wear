/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.app.appbitowear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material3.MaterialTheme
import com.app.appbitowear.AppbitoApplication
import com.app.appbitowear.presentation.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        val appContainer = (application as AppbitoApplication).container

        setContent {
            MaterialTheme {
                AppNavigation(
                    authRepository = appContainer.authRepository,
                    habitRepository = appContainer.habitRepository,
                    habitProgressRepository = appContainer.habitProgressRepository,
                    userRepository = appContainer.userRepository,
                    tokenManager = appContainer.tokenManager
                )
            }
        }
    }
}