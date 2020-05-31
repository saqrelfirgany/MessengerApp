package com.example.messenger.views

import com.example.messenger.R
import com.example.messenger.models.ChatMessage
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_messege_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
    var chatPartnerUser : User? = null

    override fun getLayout(): Int {
        return R.layout.latest_messege_row
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.messege_textview_latestmessage.text = chatMessage.text
        val chatParetner : String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatParetner = chatMessage.toId
        }else{
            chatParetner = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatParetner")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)
                viewHolder.itemView.username_textview_latestmessage.text = chatPartnerUser?.username
                val targetImageView = viewHolder.itemView.imageview_latestmessage
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(targetImageView)
            }

        })


    }

}