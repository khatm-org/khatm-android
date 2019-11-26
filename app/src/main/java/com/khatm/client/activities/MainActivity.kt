package com.khatm.client.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.khatm.client.R
import com.khatm.client.extensions.AsyncActivityExtension
import com.khatm.client.viewmodels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AsyncActivityExtension() {

    private lateinit var authViewModel: AuthViewModel

    lateinit var googleSignInButton: SignInButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authViewModel = ViewModelProviders.of(this).get(AuthViewModel::class.java)
        authViewModel.setupFor(this)

        setContentView(R.layout.activity_main)
        googleSignInButton = findViewById(R.id.button_sign_in_google)
        googleSignInButton.setOnClickListener {
            signInGoogleAction()
        }
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch(Dispatchers.Main) {
            val user = authViewModel.authorizedUserAsync().await()

            user?.access?.let {
                if (it.isNotBlank()) {
                    Log.d("MainActivity", "Automatically Login")

                    goToNextScreen()
                }
            }
        }
    }

    private fun signInGoogleAction() {
        GlobalScope.launch(Dispatchers.Main) {
            val result = launchIntentAsync(authViewModel.signInIntent).await()

            result?.data?.let {
                try {
                    val user = authViewModel.authorizeWithServerAsync(it).await()

                    if (user?.access != null) {
                        authViewModel.saveAuthorizedUserAsync(user).await()

                        Log.d("MainActivity", "Login successful")

                        goToNextScreen()
                    }
                }
                catch (e: ApiException) {
                    Log.d("MainActivity", "Failed: $e")
                    Toast.makeText(this@MainActivity, "Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun goToNextScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}