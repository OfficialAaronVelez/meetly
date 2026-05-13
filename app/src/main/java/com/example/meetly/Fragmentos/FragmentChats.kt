package com.example.meetly.Fragmentos

import android.content.Context
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FragmentChats : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var mContext: Context
    private var usuarioLista: ArrayList<Usuario> = ArrayList()

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        binding.RVUsuarios.setHasFixedSize(true)
        binding.RVUsuarios.layoutManager = LinearLayoutManager(mContext)

        binding.EtBuscarUsuario.doOnTextChanged { texto, _, _, _ ->
            buscarUsuario(texto.toString())
        }

        listarUsuarios()
        return binding.root
    }

    private fun listarUsuarios() {
        val miUid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().reference
            .child("Usuarios").orderByChild("nombres")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
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
                    Toast.makeText(mContext, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun buscarUsuario(query: String) {
        val miUid = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseDatabase.getInstance().reference
            .child("Usuarios").orderByChild("nombres")
            .startAt(query).endAt(query + "")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
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
        if (usuarioLista.isEmpty()) {
            binding.tvSinUsuarios.visibility = View.VISIBLE
            binding.RVUsuarios.visibility = View.GONE
        } else {
            binding.tvSinUsuarios.visibility = View.GONE
            binding.RVUsuarios.visibility = View.VISIBLE
            binding.RVUsuarios.adapter = AdaptadorUsuario(mContext, usuarioLista)
        }
    }
}
