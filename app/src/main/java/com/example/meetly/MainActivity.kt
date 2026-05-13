package com.example.meetly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.meetly.Fragmentos.FragmentAccount
import com.example.meetly.Fragmentos.FragmentChats
import com.example.meetly.Fragmentos.FragmentHome
import com.example.meetly.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()
        solicitarPermisoNotificaciones()
        guardarFcmToken()

        // Ver Fragment Inicio por defecto
        verFragmentHome()

        binding.BottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.Item_Home -> {
                    verFragmentHome()
                    true
                }
                R.id.Item_My_Events -> {
                    // TODO: Implementar Fragment Mis Eventos
                    true
                }
                R.id.Item_Search -> {
                    // TODO: Implementar Fragment Buscar
                    true
                }
                R.id.Item_Chats -> {
                    verFragmentChats()
                    true
                }
                R.id.Item_Account -> {
                    verFragmentCuenta()
                    true
                }
                else -> false
            }
        }
    }

    private val solicitarNotifARL =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarNotifARL.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun guardarFcmToken() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseDatabase.getInstance().getReference("Usuarios/$uid/fcmToken").setValue(token)
        }
    }

    private fun verFragmentHome(){
        binding.TituloRL.text = "Inicio"
        val fragment = FragmentHome()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        // Asegúrate de que FragmentL1 existe en activity_main.xml
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmentTransition.commit()
    }

    private fun comprobarSesion(){
        if(firebaseAuth.currentUser == null){
            startActivity(Intent(this, OpcionesLogin::class.java))
            finishAffinity()
        }
    }

    private fun verFragmentChats() {
        binding.TituloRL.text = "Chats"
        val fragment = FragmentChats()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentChats")
        fragmentTransition.commit()
    }

    private fun verFragmentCuenta(){
        binding.TituloRL.text = "Cuenta"
        val fragment = FragmentAccount()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()
    }
}
