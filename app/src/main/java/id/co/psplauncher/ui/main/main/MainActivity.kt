package id.co.psplauncher.ui.main.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import id.co.psplauncher.Utils.handleApiError
import id.co.psplauncher.Utils.startNewActivity
import id.co.psplauncher.Utils.visible
import id.co.psplauncher.data.network.Resource
import id.co.psplauncher.databinding.ActivityMainBinding
import id.co.psplauncher.ui.main.menu.MenuActivity

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLoginStatus()
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            btnLogin.setOnClickListener {
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()

                when {
                    username.isEmpty() -> {
                        etUsername.error = "Username tidak boleh kosong"
                        etUsername.requestFocus()
                    }

                    password.isEmpty() -> {
                        tilPassword.error = "Password tidak boleh kosong"
                        tilPassword.requestFocus()
                    }

                    else -> {

                        etUsername.error = null
                        tilPassword.error = null


                        viewModel.login(username, password)
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.loginResponse.observe(this) { resource ->
            with(binding) {
                when (resource) {
                    is Resource.Loading -> {
                        progressBar.visible(true)
                        btnLogin.isEnabled = false
                    }

                    is Resource.Success -> {
                        progressBar.visible(false)
                        btnLogin.isEnabled = true

                        Toast.makeText(this@MainActivity, "Login Berhasil!", Toast.LENGTH_SHORT)
                            .show()
                        startNewActivity(MenuActivity::class.java)

                    }

                    is Resource.Failure -> {
                        progressBar.visible(false)
                        btnLogin.isEnabled = true

                        handleApiError(root, resource)
                    }
                }
            }
        }
    }

    private fun checkLoginStatus() {
        val token = viewModel.getToken()
        if (!token.isNullOrEmpty()) {
            startNewActivity(MenuActivity::class.java)
            finish()
        }
    }
}