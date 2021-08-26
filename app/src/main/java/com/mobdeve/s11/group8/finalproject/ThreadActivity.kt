package com.mobdeve.s11.group8.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class ThreadActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var threadAdapter: ThreadAdapter
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var threadIds: ArrayList<String>

    private lateinit var profileId: String
    private lateinit var etEmail: EditText

    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thread)

        initRecyclerView()
        initFirebase()
        initData()
    }

    private fun searchEmails(email : String){

        this.reference.orderByChild(Collections.email.name).equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.hasChildren()) {
                        for (data in snapshot.children){
                            profileId = data.key.toString()
                        }

                        if (userId != profileId) {
                            val thread = Thread(
                                arrayOf(userId, profileId).toCollection(ArrayList<String>()),
                                arrayOf(Chat(userId, profileId, "", Calendar.getInstance()))
                                .toCollection(ArrayList<Chat>()),
                            )

                            val newThreadKey = database.getReference(Collections.threads.name).push().key
                            if (newThreadKey != null) {
                                database.getReference(Collections.threads.name).child(newThreadKey)
                                .setValue(thread).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val intent = Intent(this@ThreadActivity, ChatActivity::class.java)
                                        intent.putExtra(Keys.THREAD_ADD_NEW_KEY.name, newThreadKey.toString())
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(this@ThreadActivity,"Oh no! Something went wrong :(", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@ThreadActivity, "Hmm, you can't add yourself :/", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ThreadActivity, "User does not exist :/", Toast.LENGTH_SHORT).show()
                    }

                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ThreadActivity, "Oh no! Something went wrong :(", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun initFirebase() {
        database = FirebaseDatabase.getInstance()
        reference = database.reference.child(Keys.USERS.name)
        user = FirebaseAuth.getInstance().currentUser!!
        userId = user.uid
    }

    private fun initData(){
        this.threadIds = arrayOf("-Mhln9-lImhb5B0SiAI-", "-MhluSWS_6b8wKOGCsta", "-MhnGb6a7SP7en7oCjV2").toCollection(ArrayList<String>());
        threadAdapter.submitList(threadIds)
    }

    private fun initRecyclerView(){
        val rvThreads : RecyclerView = findViewById(R.id.rv_thread_list)
        rvThreads.layoutManager = LinearLayoutManager(this@ThreadActivity)
        threadAdapter = ThreadAdapter(this)
        rvThreads.adapter = threadAdapter

        etEmail = findViewById(R.id.et_thread_email)
        btnAdd = findViewById(R.id.btn_thread_add)
        btnAdd.setOnClickListener { v ->
            val email = etEmail.text.toString().trim()
            if (email.isNotEmpty() && userId.isNotEmpty()){
                searchEmails(email)
            } else {
                Toast.makeText(this,"Email cannot be blank :P", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(Keys.THREAD_ID_KEY.name, threadIds[position])
        Toast.makeText(this, threadIds[position], Toast.LENGTH_SHORT).show()
        startActivity(intent);
    }

    override fun onResume() {
        super.onResume()
        initData()
    }
}