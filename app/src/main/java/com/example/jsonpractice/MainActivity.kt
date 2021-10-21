package com.example.jsonpractice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    var contactsList = arrayListOf<Contact>()

    lateinit var nameTextView: TextView
    lateinit var locationTextView: TextView
    lateinit var mobileTextView: TextView
    lateinit var emailTextView: TextView
    lateinit var notFoundTextView: TextView
    lateinit var getContactButton: Button
    lateinit var numberEditText: EditText
    lateinit var contactLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameTextView = findViewById(R.id.tv_contactName)
        locationTextView = findViewById(R.id.tv_contactLocation)
        mobileTextView = findViewById(R.id.tv_contactMobile)
        emailTextView = findViewById(R.id.tv_contactEmail)
        notFoundTextView = findViewById(R.id.tv_notFound)
        getContactButton = findViewById(R.id.btn_getContact)
        numberEditText = findViewById(R.id.edt_number)
        contactLinearLayout = findViewById(R.id.ll_contact)

        contactLinearLayout.isVisible = false
        notFoundTextView.isVisible = false

        CoroutineScope(IO).launch {
            contactsList = async { requestData() }.await()
        }


        getContactButton.setOnClickListener {
            if (numberEditText.text.isNotEmpty()) {
                val userInput = numberEditText.text.toString().toInt()
                try{
                    showContact(contactsList[userInput])
                }
                catch (e: IndexOutOfBoundsException){
                    showNotFound()
                }
            } else {
                Toast.makeText(this, "please enter a number", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showContact(contact: Contact) {
        notFoundTextView.isVisible = false
        contactLinearLayout.isVisible = true
        nameTextView.text = contact.name
        locationTextView.text = contact.location
        mobileTextView.text = contact.mobile
        emailTextView.text = contact.email
        numberEditText.text.clear()
        numberEditText.clearFocus()
    }

    private fun showNotFound() {
        contactLinearLayout.isVisible = false
        notFoundTextView.isVisible = true
    }

    private fun requestData(): ArrayList<Contact> {
        val list = arrayListOf<Contact>()
        try {
            val okHttpClient = OkHttpClient()
            val request = Request.Builder()
                .url("https://dojo-recipes.herokuapp.com/contacts/")
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response != null) {
                if (response.code == 200) {
                    val jsonArray = JSONArray(response.body!!.string())
                    Log.d("HELP", jsonArray.toString())
                    for (index in 0 until jsonArray.length()) {
                        val nameObj = jsonArray.getJSONObject(index)
                        val name = nameObj.getString("name")
                        val location = nameObj.getString("location")
                        val mobile = nameObj.getString("mobile")
                        val email = nameObj.getString("email")
                        list.add(Contact(name, location, mobile, email))
                    }

                }
            }
        } catch (e: Exception) {
            Log.d("TextViewActivity", e.message.toString())
        }
        return list
    }
}

data class Contact(val name: String, val location: String, val mobile: String, val email: String)
