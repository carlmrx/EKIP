package com.example.ekip

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_auth.logoutButton
import kotlinx.android.synthetic.main.activity_home.*

class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN=100
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        actionBar?.hide()
        supportActionBar?.hide()
        var analytics= FirebaseAnalytics.getInstance(this)
        val bundle=Bundle()
        bundle.putString("message","integracion de firebase correcta")
        analytics.logEvent("InitScreen",bundle)
        //setup
        setup()
        session()
    }

    override fun onStart() {
        super.onStart()
        authLayout.visibility=View.VISIBLE

    }
            private fun session(){
                val prefs=getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
                val email=prefs.getString("email",null)
                val provider=prefs.getString("provider",null)
                if(email!=null && provider!=null ){
                    authLayout.visibility=View.INVISIBLE
                    showhome(email,ProviderType.valueOf(provider))
                }
            }

            private fun setup(){
                title = "Authentication"
                logoutButton.setOnClickListener {
                    if (Email_edittext.text.isNotEmpty()&& password_edittext.text.isNotEmpty()){
                        FirebaseAuth.getInstance()
                            .createUserWithEmailAndPassword(Email_edittext.text.toString(),
                                password_edittext.text.toString()).addOnCompleteListener{
                            if(it.isSuccessful){
                                showhome(it.result?.user?.email ?:"",ProviderType.BASIC)
                            }else{
                                showaert()
                            }

                        }
                    }

                }
                googleButton.setOnClickListener {
                    //cinfiguracion de autenticacion
                    val googleconf= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleClient=GoogleSignIn.getClient(this,googleconf)
                    googleClient.signOut()
                    startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)
                }
                loguinButton.setOnClickListener {
                    if (Email_edittext.text.isNotEmpty()&& password_edittext.text.isNotEmpty()){
                        FirebaseAuth.getInstance().
                        signInWithEmailAndPassword(Email_edittext.text.toString(),
                            password_edittext.text.toString()).addOnCompleteListener{
                            if(it.isSuccessful){
                                showhome(it.result?.user?.email ?:"",ProviderType.BASIC)
                            }else{
                                showaert()
                            }

                        }
                    }
                }
            }
            private fun showaert(){
                val builder= AlertDialog.Builder(this)
                builder.setTitle("error")
                builder.setMessage("se produjo un error de autenticacion de el usuario")
                builder.setPositiveButton("Aceptar",null)
                val dialog:AlertDialog=builder.create()
                dialog.show()

            }
            private fun showhome(email:String,provider:ProviderType){
            val homeintent=Intent(this,HomeActivity::class.java).apply {
                putExtra("email",email)
                putExtra("provider",provider.name)

            }
            startActivity(homeintent)
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val credentian = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credentian)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showhome(account.email ?: "", ProviderType.GOOGLE)
                            } else {
                                showaert()
                            }
                        }

                }
            }
            catch (e: ApiException){
                showaert()
            }

        }
    }
}