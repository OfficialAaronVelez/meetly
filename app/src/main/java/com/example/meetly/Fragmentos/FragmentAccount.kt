package com.example.meetly.Fragmentos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.meetly.R
import com.example.meetly.com.example.meetly.OpcionesLogin
import com.example.meetly.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private lateinit var binding: FragmentAccountBinding
private lateinit var firebaseAuth: FirebaseAuth
private lateinit var  mContext: Context

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentAccount.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentAccount : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        binding.BtnCerrarSesion.setOnClickListener {

            firebaseAuth.signOut()
            startActivity(Intent(mContext, OpcionesLogin::class.java))
            activity?.finishAffinity()

        }
    }

    override fun onAttach(context: Context){
        mContext = context
        super.onAttach(context)
    }
}