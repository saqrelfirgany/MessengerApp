package com.example.messenger.register_login

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.messenger.R
import com.example.messenger.massages.LatestMessagesActivity
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        reguster_button_regestration.setOnClickListener {
            performRegister()
        }
        selectphoto_button_regester.setOnClickListener {
            Log.d("RegisterActivity","Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }

        already_have_account_text_view.setOnClickListener{
            Log.d("RegisterActivity" , "Try to show login activity")
            //launch login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    var selectedPhotoUri : Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity","Photo was Selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver ,selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_regester.alpha = 0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //selectphoto_button_regester.setBackgroundDrawable(bitmapDrawable)
        }
    }


    private fun performRegister(){

        val email = email_edittext_registration.text.toString()
        val password = password_edittext_registraion.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Please Enter ur Email or Password",Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity" , "Email is $email")
        Log.d("RegisterActivity" , "Password is $password ")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email , password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegisterActivity","Successfully created user with uid: ${it.result?.user?.uid}")
                uploadImageToFirebasStorage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity" , "Faild To create user :${it.message}")
                Toast.makeText(this,"${it.message}",Toast.LENGTH_LONG).show()
            }

    }

    private fun uploadImageToFirebasStorage(){

        if(selectedPhotoUri == null ) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity","successfully upload image ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity","File Location :$it")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("RegisterActivity" , "Faild To upload the image${it.message}")
                Toast.makeText(this,"${it.message}",Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserToFirebaseDatabase(profilImageUrl:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid,username_editext_rergistraion.text.toString(),profilImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Saved user to firebase database")

                //launch LatestMessegesActivity
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            }
            .addOnFailureListener{
                Log.d("RegisterActivity" , "Faild Saved user to firebase database${it.message}")
                Toast.makeText(this,"${it.message}",Toast.LENGTH_LONG).show()
            }
    }

}

