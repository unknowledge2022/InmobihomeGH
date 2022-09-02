package com.inmobihome

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.inmobihome.databinding.ActivitySplashBinding
import com.squareup.picasso.Picasso

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var binding :  ActivitySplashBinding
    private var uriImage: Uri? = null
    private val REQ_ONE_TAP = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        //val cred = oneTapClient.getSignInCredentialFromIntent(intent).profilePictureUri
        //Log.i("signin","${cred.profilePictureUri}")

        binding.access.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,
                            null, 0, 0, 0, null
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                }
                .addOnFailureListener(this) { e ->
                    e.printStackTrace()
                }
        }
        setContentView(binding.root)
       // startActivity(Intent(this,MainActivity::class.java))

        /**
         * Analytics
         */
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "SplashActivity")
        }


        /**
         * Cloud Messaging
         */
        if (intent?.extras != null) {
            val bundle = intent.extras
            if (bundle != null) {
                for (key in bundle.keySet()) {
                    Log.d(Utils.tag, "Key: $key - Value: ${bundle.get(key)}")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //val currentuser = FirebaseAuth.getInstance().currentUser
        if (FirebaseAuth.getInstance().currentUser != null) {
            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
            if (intent.extras != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    for (key in bundle.keySet()) {
                        mainIntent.putExtra(key, bundle.get(key).toString())
                    }
                }
            }
           //Log.i("currentuser","$currentuser")
            //if(uriImage != null)
              //  mainIntent.putExtra("uriprofile",uriImage)
            startActivity(mainIntent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("actual","Previoous")
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                   // uriImage = credential.profilePictureUri
                    //Picasso.get().load(credential.profilePictureUri).into(binding.imagelogo)
                   // binding.tvnameapp.text = credential.displayName

                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        Firebase.auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    val currentUser = FirebaseAuth.getInstance().currentUser

                                    /**
                                     * Firestore
                                     */
                                    var collectionReference = FirebaseFirestore.getInstance().collection("users")
                                    collectionReference.document(currentUser?.uid!!).get()
                                        .addOnSuccessListener {
                                            var user = it.toObject(User::class.java)
                                            if (user == null) {
                                                user = User(
                                                    id = currentUser.uid,
                                                    name = currentUser.displayName!!,
                                                    email = currentUser.email!!,
                                                )
                                                it.reference.set(user)
                                            }
                                            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
                                            //startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                            //if (intent?.extras != null){
                                            mainIntent.putExtra("uriprofile",credential.profilePictureUri)
                                            mainIntent.putExtra("nameprofile",credential.displayName)
                                           // }
                                            startActivity(mainIntent)
                                            finish()
                                        }.addOnFailureListener {
                                            it.printStackTrace()
                                        }
                                }
                            }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }
    }
    override fun onBackPressed() {
        //evitamos la accion del boton atras
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (checkPlayServices()) {
            if (checkPermissions()) {
                Thread {
                    try {
                        //Thread.sleep(1500)
                    } catch (ex: Exception) {
                        Log.e(Configuration.tag, ex.message, ex)
                    }
                   // startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    //finish()
                }.start()
            }
        }
    }


    private fun checkPlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this@SplashActivity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404)!!.show()
            }
            return false
        }
        return true
    }

    private fun checkPermissions(): Boolean {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ), 101
            )
        } else {
            return true
        }
        return false
    }
}


