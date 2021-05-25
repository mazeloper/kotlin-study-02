package com.jschoi.develop.aop_part03_chapter05

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private val emailEditText: EditText by lazy {
        findViewById(R.id.emailEditText)
    }
    private val passwordEditText: EditText by lazy {
        findViewById(R.id.passwordEditText)
    }
    private val loginButton: Button by lazy {
        findViewById(R.id.loginButton)
    }
    private val signUpButton: Button by lazy {
        findViewById(R.id.signUpButton)
    }

    private val onClickedListener = View.OnClickListener { view ->
        when (view) {
            loginButton -> {
                val email = getInputEmail()
                val password = getInputPassword()

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        finish()
                    } else {
                        Toast.makeText(
                            this, "로그인에 실패하였습니다.\n${it.exception}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            signUpButton -> {
                val email = getInputEmail()
                val password = getInputPassword()

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "회원가입에 실패하였습니다.\n${it.exception}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        initViews()
    }

    private fun initViews() {
        loginButton.setOnClickListener(onClickedListener)
        signUpButton.setOnClickListener(onClickedListener)

        emailEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
        passwordEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
    }


    private fun getInputEmail() = emailEditText.text.toString()
    private fun getInputPassword() = passwordEditText.text.toString()

}