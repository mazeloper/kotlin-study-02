package com.jschoi.develop.aop_part03_chapter05

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    private val emailEditText: EditText by lazy {
        findViewById(R.id.emailEditText)
    }
    private val passwordEditText: EditText by lazy {
        findViewById(R.id.passwordEditText)
    }
    private val loginButton: Button by lazy {
        findViewById(R.id.logoutButton)
    }
    private val signUpButton: Button by lazy {
        findViewById(R.id.signUpButton)
    }
    private val facebookLoginButton: LoginButton by lazy {
        findViewById(R.id.facebookLoginButton)
    }

    private val onClickedListener = View.OnClickListener { view ->
        when (view) {
            loginButton -> {    // 로그인버튼
                val email = getInputEmail()
                val password = getInputPassword()

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        handleSuccessLogin()
                    } else {
                        Toast.makeText(
                            this, "로그인에 실패하였습니다.\n${it.exception}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            signUpButton -> {   // 회원가입 버튼
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
            facebookLoginButton -> {    // 페이스북 로그인버튼
                // email과 public_profile 가져오겠다.
                facebookLoginButton.setPermissions("email", "public_profile")
                facebookLoginButton.registerCallback(
                    callbackManager,
                    object : FacebookCallback<LoginResult> {
                        override fun onSuccess(result: LoginResult) {
                            // 로그인이 성공적
                            val credential =
                                FacebookAuthProvider.getCredential(result.accessToken.token)
                            auth.signInWithCredential(credential)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        handleSuccessLogin()
                                    } else {
                                        Toast.makeText(
                                            this@LoginActivity,
                                            "페이스북 로그인에  실패하였습니다.\n${task.exception}",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                        }

                        override fun onCancel() {
                        }

                        override fun onError(error: FacebookException?) {
                            Toast.makeText(
                                this@LoginActivity,
                                "로그인에 실패하였습니다 ${error.toString()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()

        initViews()

    }

    private fun initViews() {
        // Click Event
        loginButton.setOnClickListener(onClickedListener)
        signUpButton.setOnClickListener(onClickedListener)
        facebookLoginButton.setOnClickListener(onClickedListener)

        // Text Change Event
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

    private fun getInputEmail(): String {
        return emailEditText.text.toString()
    }

    private fun getInputPassword(): String {
        return passwordEditText.text.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 로그인 정상 처리 로직
     */
    private fun handleSuccessLogin() {
        if (auth.currentUser == null) {
            return  // Do nothing..
        }
        val userId = auth.currentUser?.uid.orEmpty()
        val currentUserDB = Firebase.database.reference.child("Users").child(userId)
        val user = mutableMapOf<String, Any>()

        user["userId"] = userId
        currentUserDB.updateChildren(user)

        finish()
    }
}