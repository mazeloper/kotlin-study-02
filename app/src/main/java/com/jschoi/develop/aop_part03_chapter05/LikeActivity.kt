package com.jschoi.develop.aop_part03_chapter05

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jschoi.develop.aop_part03_chapter05.adapter.CardStackAdapter
import com.jschoi.develop.aop_part03_chapter05.data.CardItem
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class LikeActivity : AppCompatActivity(), CardStackListener {

    private lateinit var userDB: DatabaseReference

    private val auth = FirebaseAuth.getInstance()
    private val cardAdapter = CardStackAdapter()
    private val cardManager by lazy {
        CardStackLayoutManager(this@LikeActivity, this)
    }

    private var cardItems = mutableListOf<CardItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        userDB = Firebase.database.reference.child("Users")

        val currentUserDB = userDB.child(getCurrentUserID())
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 이름이 변경되었을 때, 다른 사용자에게 좋아요를 했을때
                if (snapshot.child("name").value == null) {
                    showNameInputPopup()
                    return
                }
                getUnSelectedUsers()
                // TODO 유저정보를 갱신
            }

            override fun onCancelled(error: DatabaseError) {
                // Do nothing..
            }
        })

        initViews()
        initCardStackView()
    }

    private fun initViews() {
        findViewById<Button>(R.id.signOutButton).apply {
            this.setOnClickListener {
                auth.signOut()
                startActivity(Intent(this@LikeActivity, MainActivity::class.java))
                finish()
            }
        }

        findViewById<Button>(R.id.matchListButton).apply {
            this.setOnClickListener {
                startActivity(Intent(this@LikeActivity, MatchedUserActivity::class.java))
                finish()
            }
        }
    }


    private fun showNameInputPopup() {
        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("이름을 입력해주세요.")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                if (editText.text.isEmpty()) {
                    showNameInputPopup()
                } else {
                    saveUserName(editText.text.toString())
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun initCardStackView() {
        findViewById<CardStackView>(R.id.card_stack_view).apply {
            layoutManager = cardManager
            adapter = cardAdapter
        }
    }

    private fun getUnSelectedUsers() {
        userDB.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // 현재 보고있는 유저아이디가 내가 아니며(상대방) 좋아요,싫어요 버튼을 누른적이 없는 유저
                if (snapshot.child("userId").value != getCurrentUserID()
                    && snapshot.child("likedBy").child("like").hasChild(getCurrentUserID()).not()
                    && snapshot.child("likedBy").child("disLike").hasChild(getCurrentUserID()).not()
                ) {
                    val userId = snapshot.child("userId").value.toString()
                    var name = "undecided"
                    if (snapshot.child("name").value != null) {
                        name = snapshot.child("name").value.toString()
                    }
                    cardItems.add(CardItem(userId, name))
                    cardAdapter.submitList(cardItems)
                    // cardAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                cardItems.find { it.userId == snapshot.key }?.let {
                    it.name = snapshot.child("name").value.toString()
                }
                cardAdapter.submitList(cardItems)
                // cardAdapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun saveUserName(name: String) {
        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String, Any>()

        user["userId"] = userId
        user["name"] = name
        currentUserDB.updateChildren(user)

        // TODO 유저정보를 가져와라
    }

    private fun getCurrentUserID(): String {
        if (auth.currentUser == null) {
            // 로그인이 되어있지 않음.
            finish()
        }
        return auth.currentUser?.uid.orEmpty()
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {
    }

    override fun onCardSwiped(direction: Direction?) {
        when (direction) {
            Direction.Right -> {
                Log.d("TAG", "#########################")
                like()
            }
            Direction.Left -> {
                Log.w("TAG", "#########################")
                disLike()
            }
            else -> Unit
        }
    }

    private fun like() {
        // 유저 ID 가져와 저장
        val card = cardItems[cardManager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId) // 상대방의 유저 ID
            .child("likedBy")
            .child("like")
            .child(getCurrentUserID())
            .setValue(true)

        saveMatchIfOtherUserLikedMe(card.userId)

        // TODO 매칭이 된 시점을 봐야한다.
        Toast.makeText(this, " ${card.name}님을 Like 하였습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun saveMatchIfOtherUserLikedMe(otherUserId: String) {
        // 상대방에 유저 DB 가져온다.
        val otherUserDB =
            userDB.child(getCurrentUserID()).child("likedBy").child("like").child(otherUserId)
        otherUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == true) {
                    userDB.child(getCurrentUserID()).child("likedBy").child("match")
                        .child(otherUserId).setValue(true)

                    userDB.child(otherUserId).child("likedBy").child("match")
                        .child(getCurrentUserID()).setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun disLike() {
        // 유저 ID 가져와 저장
        val card = cardItems[cardManager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId) // 상대방의 유저 ID
            .child("likedBy")
            .child("like")
            .child(getCurrentUserID())
            .setValue(true)

        Toast.makeText(this, " ${card.name}님을 DisLike 하였습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onCardRewound() {
    }

    override fun onCardCanceled() {
    }

    override fun onCardAppeared(view: View?, position: Int) {
    }

    override fun onCardDisappeared(view: View?, position: Int) {
    }

}