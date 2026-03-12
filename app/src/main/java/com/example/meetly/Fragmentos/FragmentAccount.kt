package com.example.meetly.Fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.meetly.OpcionesLogin
import com.example.meetly.R
import com.example.meetly.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth

class FragmentAccount : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        
        cargarImagenRandom()

        binding.BtnCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, OpcionesLogin::class.java))
            activity?.finishAffinity()
        }
    }

    private fun cargarImagenRandom() {
        val randomSeed = (1..1000).random()
        val imageUrl = "https://picsum.photos/seed/$randomSeed/200/200"

        Glide.with(mContext)
            .load(imageUrl)
            .placeholder(R.drawable.img_perfil)
            .circleCrop()
            .into(binding.TvPerfil)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
