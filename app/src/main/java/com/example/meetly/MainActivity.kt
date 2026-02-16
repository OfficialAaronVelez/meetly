package com.example.meetly

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meetly.Fragmentos.FragmentAccount
import com.example.meetly.Fragmentos.FragmentHome
import com.example.meetly.com.example.meetly.OpcionesLogin
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

        verFragmentHome()

        binding.BottomNV.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.Item_Home -> {
                    true
                }
                R.id.Item_My_Events -> {
                    true
                }
                R.id.Item_Search -> {
                    true
                }
                R.id.Item_Account -> {
                    verFragmentCuenta()
                    true
                }
                else -> {
                    false

                }
            }
        }
    }
    private fun verFragmentHome(){
        binding.TituloRL.text = "Inicio"
        val fragment = FragmentHome()
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmenteTransition.commit()
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
        val fragmenteTransition = supportFragmentManager.beginTransaction()
        fragmenteTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmenteTransition.commit()
    }
}