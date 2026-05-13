package com.example.meetly.Adaptadores

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meetly.Chat.ChatActivity
import com.example.meetly.Modelos.Usuario
import com.example.meetly.R

class AdaptadorUsuario(
    private val contexto: Context,
    private val listaUsuarios: List<Usuario>
) : RecyclerView.Adapter<AdaptadorUsuario.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(contexto).inflate(R.layout.item_usuario, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listaUsuarios.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]
        holder.uid.text = usuario.uid
        holder.nombre.text = usuario.nombres
        holder.email.text = usuario.email
        Glide.with(contexto).load(usuario.imagen)
            .placeholder(R.drawable.ic_imagen_perfil)
            .into(holder.imagen)

        holder.itemView.setOnClickListener {
            val intent = Intent(contexto, ChatActivity::class.java)
            intent.putExtra("uid", usuario.uid)
            contexto.startActivity(intent)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uid: TextView = itemView.findViewById(R.id.item_uid)
        val nombre: TextView = itemView.findViewById(R.id.item_nombre)
        val email: TextView = itemView.findViewById(R.id.item_email)
        val imagen: ImageView = itemView.findViewById(R.id.item_imagen)
    }
}
