package com.example.meetly.Fragmentos

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.meetly.OpcionesLogin
import com.example.meetly.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth

class FragmentAccount : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

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

        binding.BtnCerrarSesion.setOnClickListener {
            firebaseAuth.signOut()
            // Use requireContext() for safety
            startActivity(Intent(requireContext(), OpcionesLogin::class.java))
            activity?.finishAffinity() // Closes all activities in the task
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}
