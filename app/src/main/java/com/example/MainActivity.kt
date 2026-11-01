package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clipforge.ui.navigation.AppNavigation
import com.example.clipforge.ui.theme.ClipForgeTheme
import com.example.clipforge.ui.viewmodel.ClipForgeViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "ClipForgeAppCheck"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
  try {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    if (BuildConfig.DEBUG) {
      val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
      if (!debugToken.isNullOrEmpty()) {
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val prefs = context.getSharedPreferences(
          "$DEBUG_STORE_PREFIX$persistenceKey",
          Context.MODE_PRIVATE
        )
        prefs.edit().putString(DEBUG_SECRET_KEY, debugToken).commit()
        Log.d(TAG, "App Check debug token stored successfully.")
      }
      FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
      )
    } else {
      val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
      if (resId != 0) {
        val siteKey = context.getString(resId)
        if (siteKey.isNotBlank()) {
          FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            RecaptchaAppCheckProviderFactory.getInstance(siteKey)
          )
        }
      }
    }
  } catch (e: Exception) {
    Log.w(TAG, "Firebase App Check initialization info: ${e.message}")
  }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure Firebase App Check with launch intent before UI initialization
    configureAppCheck(this, intent)

    enableEdgeToEdge()

    setContent {
      ClipForgeTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val viewModel: ClipForgeViewModel = viewModel()
          AppNavigation(viewModel = viewModel)
        }
      }
    }
  }
}
