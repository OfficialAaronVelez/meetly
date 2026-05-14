package com.example.meetly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.meetly.Fragmentos.FragmentAccount
import com.example.meetly.Fragmentos.FragmentChats
import com.example.meetly.Fragmentos.FragmentHome
import com.example.meetly.Fragmentos.FragmentMyEvents
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
        actualizarToken()
        pedirPermisoNotificaciones()

        // Fragment Inicio por defecto
        verFragmentHome()

        // Botón FAB Central para abrir Chats
        binding.FAB.setOnClickListener {
            Log.d("MainActivity", "FAB Chat clicked")
            verFragmentChats()
            binding.BottomNV.selectedItemId = R.id.Item_Chats
        }

        binding.BottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.Item_Home -> { verFragmentHome(); true }
                R.id.Item_My_Events -> { verFragmentMyEvents(); true }
                R.id.Item_Chats -> { verFragmentChats(); true }
                R.id.Item_Account -> { verFragmentCuenta(); true }
                else -> false
            }
        }
    }

    private fun actualizarToken() {
        val uid = firebaseAuth.uid ?: return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            FirebaseDatabase.getInstance().getReference("Usuarios").child(uid)
                .child("fcmToken").setValue(token)
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) Log.w("MainActivity", "Notification permission denied")
    }

    private fun verFragmentHome(){
        binding.TituloRL.text = "Inicio"
        supportFragmentManager.beginTransaction()
            .replace(binding.FragmentL1.id, FragmentHome(), "FragmentInicio")
            .commit()
    }

    private fun verFragmentMyEvents(){
        binding.TituloRL.text = "Mis Eventos"
        supportFragmentManager.beginTransaction()
            .replace(binding.FragmentL1.id, FragmentMyEvents(), "FragmentMisEventos")
            .commit()
    }

    private fun verFragmentChats(){
        Log.d("MainActivity", "Showing FragmentChats")
        binding.TituloRL.text = "Chats"
        supportFragmentManager.beginTransaction()
            .replace(binding.FragmentL1.id, FragmentChats(), "FragmentChats")
            .commit()
    }

    private fun verFragmentCuenta(){
        binding.TituloRL.text = "Cuenta"
        supportFragmentManager.beginTransaction()
            .replace(binding.FragmentL1.id, FragmentAccount(), "FragmentCuenta")
            .commit()
    }

    private fun comprobarSesion(){
        if(firebaseAuth.currentUser == null){
            startActivity(Intent(this, OpcionesLogin::class.java))
            finishAffinity()
        }
    }
}
