package com.example.meetly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.meetly.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    true
                }
                else -> {
                    false

                }
            }
        }
    }
}