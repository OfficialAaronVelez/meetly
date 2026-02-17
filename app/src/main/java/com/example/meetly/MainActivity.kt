package com.example.meetly

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meetly.Fragmentos.FragmentAccount
import com.example.meetly.Fragmentos.FragmentHome
import com.example.meetly.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

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
                R.id.Item_Account -> {
                    verFragmentCuenta()
                    true
                }
                else -> false
            }
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

    private fun verFragmentCuenta(){
        binding.TituloRL.text = "Cuenta"
        val fragment = FragmentAccount()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()
    }
}
