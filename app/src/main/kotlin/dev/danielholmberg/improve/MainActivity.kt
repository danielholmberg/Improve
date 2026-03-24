package dev.danielholmberg.improve

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import dev.danielholmberg.improve.domain.usecase.auth.GetCurrentUserUseCase
import dev.danielholmberg.improve.navigation.ImproveNavGraph
import dev.danielholmberg.improve.navigation.Screen
import dev.danielholmberg.improve.ui.theme.ImproveTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImproveTheme {
                val user by getCurrentUserUseCase().collectAsState(initial = null)
                val startDestination = if (user != null) Screen.Notes.route else Screen.SignIn.route
                ImproveNavGraph(startDestination = startDestination)
            }
        }
    }
}
