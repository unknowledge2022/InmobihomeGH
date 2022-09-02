package com.inmobihome

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.inmobihome.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {


    private var currentUser: User? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var analytics: FirebaseAnalytics

    private lateinit var configuration: Configuration
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var usersReference: DatabaseReference

    var arrayList: ArrayList<Inmueble> = arrayListOf()
    var arrayListResult: ArrayList<Inmueble> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        val tvversion = findViewById<TextView>(R.id.tvversion)
        setSupportActionBar(toolBar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
       // val extras =intent.extras

        analytics = FirebaseAnalytics.getInstance(this)

        /**
         * Remote Config
         */
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //supportActionBar?.title = "${remoteConfig["appTitle"].asString()} [${remoteConfig["version"].asLong()}]"
                Log.d("remote","${remoteConfig["appTitle"].asString()} [${remoteConfig["version"].asDouble()}]")
                    tvversion.text = "V. [${remoteConfig["version"].asDouble()}]"
                }
            }


        /**
         * Firestore
         */
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!).get()
            .addOnSuccessListener {
                currentUser = it.toObject(User::class.java)

            /**
            * Cloud Messaging
            */
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                val token = task.result
                currentUser?.token = token
                it.reference.set(currentUser!!)
                Log.d(Utils.tag, "Push Notification token $token")
            })
            }.addOnFailureListener {
                it.printStackTrace()
            }

        /**
         * Cloud Messaging
         */
        FirebaseMessaging.getInstance().subscribeToTopic("inmobihome")
        FirebaseMessaging.getInstance().subscribeToTopic("android")

        /**
         * Analytics
         */
        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "MainActivity")
        }

        val currentUserImage = FirebaseAuth.getInstance().currentUser
        val image = findViewById<ShapeableImageView>(R.id.imgphotouser)
        Picasso.get().load(currentUserImage?.photoUrl).into(image)

        val adaptertipo = ArrayAdapter.createFromResource(this, R.array.tipo, android.R.layout.simple_spinner_item)
        binding.spintipo.adapter = adaptertipo
        binding.spintipo.setSelection(0)

        binding.tietbuscar.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                Log.i("buscar","buscar ${p0.toString()}")
                if (p0 != null) {
                    if (p0.length>=4)
                        buscar(p0.toString())
                }
                binding.tilbuscar.helperText = ""
            }

        })
        obtenerDatos()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.extras != null) {
            val bundle = intent.extras
            if (bundle != null) {
                for (key in bundle.keySet()) {
                    Log.d(Utils.tag, "Main Activity - Key: $key - Value: ${bundle.get(key)}")
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(messageReceiver, IntentFilter(SMessaging.broadcastReceiver))
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(messageReceiver)
        super.onPause()
    }

    private val messageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.extras != null) {
                val bundle = intent.extras
                if (bundle != null) {
                    for (key in bundle.keySet()) {
                        Log.d(Utils.tag, "Key: $key - Value: ${bundle.get(key)}")
                    }

                    Toast.makeText(applicationContext, "${bundle.get("body")}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_salir->{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this@MainActivity, SplashActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

fun contiene (aguja : String , pajar : String):Boolean{

    val pattern = aguja.lowercase().toRegex()
    return(pattern.containsMatchIn(pajar.lowercase()))
}
  private fun obtenerDatos(){
      configuration = Configuration.create(this@MainActivity)

      /**
       * Firebase
       */
      firebaseDatabase = Firebase.database
      usersReference = firebaseDatabase.getReference("inmuebles")

      usersReference.addValueEventListener(object : ValueEventListener {
          override fun onDataChange(dataSnapshot: DataSnapshot) {
              arrayList.clear()
              for (i in dataSnapshot.children){
                  arrayList.add(i.getValue<Inmueble>() as Inmueble)
                  //val data = i.getValue<Inmueble>() as Inmueble
              }
              Log.d("Arraylist on DataChange:","$arrayList")
          }

          override fun onCancelled(databaseError: DatabaseError) {
              // Getting Post failed, log a message
              Log.w("Inmobicasas", "loadPost:onCancelled", databaseError.toException())
              // ...
          }
      })

  }

    fun buscar(valor: String){
        if(arrayList.isEmpty())
            obtenerDatos()
        arrayListResult.clear()

        for (i in arrayList.indices) {
            if (contiene(valor,arrayList[i].departamento!!)||contiene (valor,arrayList[i].ciudad!!)) {
                arrayListResult.add(arrayList[i])
               // Log.d("Msg","${arrayListResult}")
                Log.d("Msg","${arrayListResult.size}")
            }
        }
        if(arrayListResult.size >0){
            binding.btnMotrarRV.setText("Mostrar ${arrayListResult.size} resultados")
            binding.btnMotrarRV.isEnabled = true
        }
        else{
            binding.btnMotrarRV.text = "Sin resultados"
            binding.btnMotrarRV.isEnabled = false
        }

           /** for (i in arrayList.indices){
                if(arrayList.get(i).ciudad==valor){
                    arrayListResult.add(arrayList.get(i))
                }
            }*/

        //Log.d("${arrayList.get(0)}","Msg")
    }

    fun onClickMostrarRV(view: View) {
        if(binding.tietbuscar.text.isNullOrEmpty()) {
            binding.tilbuscar.helperText = "Introduce un lugar"
        }
        else{
            var intent: Intent? = null
            intent = Intent(this@MainActivity, RecyclerViewActivity::class.java)
            intent?.putExtra("Ubicaciones", arrayListResult)
            startActivity(intent)
        }
    }
    fun onClickMostrarMapa(view: View) {
        var intent: Intent?
        intent = Intent(this@MainActivity, MapActivity::class.java)
        intent?.putExtra("Ubicaciones", arrayListResult)
        startActivity(intent)
    }
}