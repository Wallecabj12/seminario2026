package com.geovanivargas.planetamusica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.geovanivargas.planetamusica.ui.auth.AuthNavGraph
import com.geovanivargas.planetamusica.ui.theme.SoundCraftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoundCraftTheme {
                AuthNavGraph()
            }
        }
    }
}