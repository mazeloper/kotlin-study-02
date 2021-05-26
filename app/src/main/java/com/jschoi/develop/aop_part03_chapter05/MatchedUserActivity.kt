package com.jschoi.develop.aop_part03_chapter05

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jschoi.develop.aop_part03_chapter05.adapter.MatchedUserAdapter
import com.jschoi.develop.aop_part03_chapter05.data.CardItem

class MatchedUserActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var userDB: DatabaseReference
    private val matchedUserAdapter = MatchedUserAdapter()
    private val cardItems = mutableListOf<CardItem>()
    private val matchedUserRecyclerView by lazy {
        findViewById<RecyclerView>(R.id.matchedUserRecyclerView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        userDB = Firebase.database.reference.child(DBKey.USERS)

        getMatchUsers()
        initMatchedUserRecyclerView()

    }

    private fun getMatchUsers() {
        val matchedDB = userDB.child(getCurrentUserID()).child(DBKey.LIKED_BY).child("match")
        matchedDB.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (snapshot.key?.isNotEmpty() == true) {
                    getMatchUser(snapshot.key.orEmpty())
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getMatchUser(userId: String) {
        userDB.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cardItems.add(CardItem(userId, snapshot.child(DBKey.NAME).value.toString()))
                matchedUserAdapter.submitList(cardItems)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun initMatchedUserRecyclerView() {
        matchedUserRecyclerView.apply {
            Log.d("TAG","############################")
            layoutManager = LinearLayoutManager(this@MatchedUserActivity)
            this.adapter = matchedUserAdapter
        }
    }

    private fun getCurrentUserID(): String {
        if (auth.currentUser == null) {
            // 로그인이 되어있지 않음.
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }
}