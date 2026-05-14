package com.nammahomestay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nammahomestay.ui.NavGraph
import com.nammahomestay.ui.theme.NammaHomeStayTheme
import com.nammahomestay.viewmodel.AuthViewModel
import com.nammahomestay.viewmodel.HomeStayViewModel

class MainActivity : ComponentActivity() {

    // ViewModels scoped to the Activity so they survive recompositions
    private val authViewModel: AuthViewModel by viewModels()
    private val homeStayViewModel: HomeStayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NammaHomeStayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        authViewModel = authViewModel,
                        homeStayViewModel = homeStayViewModel
                    )
                }
            }
        }
    }
}
