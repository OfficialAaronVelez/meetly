package com.example.meetly.Fragmentos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meetly.Adaptadores.AdaptadorUsuario
import com.example.meetly.Modelos.Usuario
import com.example.meetly.databinding.FragmentChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FragmentChats : Fragment() {

    private var _binding: FragmentChatsBinding? = null
    private val binding get() = _binding!!
    
    private var usuarioLista: ArrayList<Usuario> = ArrayList()
    private var adaptadorUsuario: AdaptadorUsuario? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.RVUsuarios.setHasFixedSize(true)
        binding.RVUsuarios.layoutManager = LinearLayoutManager(requireContext())

        binding.EtBuscarUsuario.doOnTextChanged { texto, _, _, _ ->
            buscarUsuario(texto.toString())
        }

        listarUsuarios()
    }

    private fun listarUsuarios() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) return
        
        val miUid = firebaseUser.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios")
        
        ref.orderByChild("nombres").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                usuarioLista.clear()
                for (sn in snapshot.children) {
                    val usuario = sn.getValue(Usuario::class.java)
                    if (usuario != null && usuario.uid != miUid) {
                        usuarioLista.add(usuario)
                    }
                }
                actualizarUI()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FragmentChats", "Error: ${error.message}")
            }
        })
    }

    private fun buscarUsuario(query: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val miUid = firebaseUser.uid
        
        val ref = FirebaseDatabase.getInstance().reference.child("Usuarios")
        val dbQuery = if (query.isEmpty()) {
            ref.orderByChild("nombres")
        } else {
            ref.orderByChild("nombres").startAt(query).endAt(query + "\uf8ff")
        }

        dbQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null) return
                usuarioLista.clear()
                for (sn in snapshot.children) {
                    val usuario = sn.getValue(Usuario::class.java)
                    if (usuario != null && usuario.uid != miUid) {
                        usuarioLista.add(usuario)
                    }
                }
                actualizarUI()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("FragmentChats", "Error búsqueda: ${error.message}")
            }
        })
    }

    private fun actualizarUI() {
        if (_binding == null) return
        if (usuarioLista.isEmpty()) {
            binding.tvSinUsuarios.visibility = View.VISIBLE
            binding.RVUsuarios.visibility = View.GONE
        } else {
            binding.tvSinUsuarios.visibility = View.GONE
            binding.RVUsuarios.visibility = View.VISIBLE
            adaptadorUsuario = AdaptadorUsuario(requireContext(), usuarioLista)
            binding.RVUsuarios.adapter = adaptadorUsuario
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
