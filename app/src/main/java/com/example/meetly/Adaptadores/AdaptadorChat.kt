package com.example.meetly.Adaptadores

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meetly.Constantes
import com.example.meetly.Modelos.Chat
import com.example.meetly.R
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AdaptadorChat : RecyclerView.Adapter<AdaptadorChat.HolderChat> {

    private val context: Context
    private val chatArray: ArrayList<Chat>
    private val firebaseAuth: FirebaseAuth
    private var chatRuta = ""

    companion object {
        private const val MENSAJE_IZQUIERDO = 0
        private const val MENSAJE_DERECHO = 1
    }

    constructor(context: Context, chatArray: ArrayList<Chat>) {
        this.context = context
        this.chatArray = chatArray
        firebaseAuth = FirebaseAuth.getInstance()
    }

    inner class HolderChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Tv_mensaje: TextView = itemView.findViewById(R.id.Tv_mensaje)
        var Iv_mensaje: ShapeableImageView = itemView.findViewById(R.id.Iv_mensaje)
        var Tv_tiempo_mensaje: TextView = itemView.findViewById(R.id.Tv_tiempo_mensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        return if (viewType == MENSAJE_DERECHO) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_derecho, parent, false)
            HolderChat(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_izquierdo, parent, false)
            HolderChat(view)
        }
    }

    override fun getItemCount(): Int = chatArray.size

    override fun getItemViewType(position: Int): Int {
        return if (chatArray[position].emisorUid == firebaseAuth.uid) MENSAJE_DERECHO else MENSAJE_IZQUIERDO
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modeloChat = chatArray[position]
        holder.Tv_tiempo_mensaje.text = Constantes.obtenerFechaHora(modeloChat.tiempo)

        if (modeloChat.tipoMensaje == Constantes.MENSAJE_TIPO_TEXTO) {
            holder.Tv_mensaje.visibility = View.VISIBLE
            holder.Iv_mensaje.visibility = View.GONE
            holder.Tv_mensaje.text = modeloChat.mensaje

            if (modeloChat.emisorUid == firebaseAuth.uid) {
                holder.itemView.setOnClickListener {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("¿Qué deseas realizar?")
                        .setItems(arrayOf<CharSequence>("Eliminar mensaje", "Cancelar")) { _, which ->
                            if (which == 0) eliminarMensaje(position, holder, modeloChat)
                        }.show()
                }
            }
        } else {
            holder.Tv_mensaje.visibility = View.GONE
            holder.Iv_mensaje.visibility = View.VISIBLE
            try {
                Glide.with(context).load(modeloChat.mensaje)
                    .placeholder(R.drawable.img_enviada)
                    .into(holder.Iv_mensaje)
            } catch (e: Exception) {
                Log.e("AdaptadorChat", "Error al cargar imagen: ${e.message}")
            }

            if (modeloChat.emisorUid == firebaseAuth.uid) {
                holder.itemView.setOnClickListener {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("¿Qué desea realizar?")
                        .setItems(arrayOf<CharSequence>("Eliminar imagen", "Ver imagen", "Cancelar")) { _, which ->
                            if (which == 0) eliminarMensaje(position, holder, modeloChat)
                            else if (which == 1) visualizadorImagen(modeloChat.mensaje)
                        }.show()
                }
            } else {
                holder.itemView.setOnClickListener {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("¿Qué desea realizar?")
                        .setItems(arrayOf<CharSequence>("Ver imagen", "Cancelar")) { _, which ->
                            if (which == 0) visualizadorImagen(modeloChat.mensaje)
                        }.show()
                }
            }
        }
    }

    private fun eliminarMensaje(position: Int, holder: HolderChat, modeloChat: Chat) {
        chatRuta = Constantes.rutaChat(modeloChat.receptorUid, modeloChat.emisorUid)
        FirebaseDatabase.getInstance().reference
            .child("Chats").child(chatRuta).child(chatArray[position].idMensaje)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Mensaje eliminado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun visualizadorImagen(imagen: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.visualizador_img)
        val pv = dialog.findViewById<PhotoView>(R.id.PV_img)
        val btnCerrar = dialog.findViewById<MaterialButton>(R.id.BtnCerrarVisualizador)
        try {
            Glide.with(context).load(imagen).placeholder(R.drawable.img_enviada).into(pv)
        } catch (e: Exception) { }
        btnCerrar.setOnClickListener { dialog.dismiss() }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}
