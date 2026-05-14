package com.example.meetly.Adaptadores

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.media.MediaPlayer
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

class AdaptadorChat(private val context: Context, private val chatArray: ArrayList<Chat>) : 
    RecyclerView.Adapter<AdaptadorChat.HolderChat>() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        private const val MENSAJE_IZQUIERDO = 0
        private const val MENSAJE_DERECHO = 1
    }

    inner class HolderChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Tv_mensaje: TextView = itemView.findViewById(R.id.Tv_mensaje)
        var Iv_mensaje: ShapeableImageView = itemView.findViewById(R.id.Iv_mensaje)
        var Tv_tiempo_mensaje: TextView = itemView.findViewById(R.id.Tv_tiempo_mensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        val layout = if (viewType == MENSAJE_DERECHO) R.layout.item_chat_derecho else R.layout.item_chat_izquierdo
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return HolderChat(view)
    }

    override fun getItemCount(): Int = chatArray.size

    override fun getItemViewType(position: Int): Int {
        return if (chatArray[position].emisorUid == firebaseAuth.uid) MENSAJE_DERECHO else MENSAJE_IZQUIERDO
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modeloChat = chatArray[position]
        holder.Tv_tiempo_mensaje.text = Constantes.obtenerFechaHora(modeloChat.tiempo)

        when (modeloChat.tipoMensaje) {
            Constantes.MENSAJE_TIPO_TEXTO -> {
                holder.Tv_mensaje.visibility = View.VISIBLE
                holder.Iv_mensaje.visibility = View.GONE
                holder.Tv_mensaje.text = modeloChat.mensaje
            }
            Constantes.MENSAJE_TIPO_IMAGEN -> {
                holder.Tv_mensaje.visibility = View.GONE
                holder.Iv_mensaje.visibility = View.VISIBLE
                Glide.with(context).load(modeloChat.mensaje)
                    .placeholder(R.drawable.ic_meetly_background)
                    .into(holder.Iv_mensaje)
                
                holder.Iv_mensaje.setOnClickListener { visualizadorImagen(modeloChat.mensaje) }
            }
            Constantes.MENSAJE_TIPO_AUDIO -> {
                holder.Tv_mensaje.visibility = View.VISIBLE
                holder.Iv_mensaje.visibility = View.GONE
                holder.Tv_mensaje.text = "▶ Mensaje de voz"
                holder.Tv_mensaje.setOnClickListener { reproducirAudio(modeloChat.mensaje) }
            }
        }

        if (modeloChat.emisorUid == firebaseAuth.uid) {
            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Eliminar mensaje")
                    .setMessage("¿Deseas eliminar este mensaje?")
                    .setPositiveButton("Eliminar") { _, _ -> eliminarMensaje(position, modeloChat) }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }
        }
    }

    private fun reproducirAudio(url: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepare()
                start()
            }
            Toast.makeText(context, "Reproduciendo audio...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al reproducir audio", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarMensaje(position: Int, modeloChat: Chat) {
        val chatRuta = Constantes.rutaChat(modeloChat.receptorUid, modeloChat.emisorUid)
        FirebaseDatabase.getInstance().reference
            .child("Chats").child(chatRuta).child(modeloChat.idMensaje)
            .removeValue()
    }

    private fun visualizadorImagen(imagen: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.visualizador_img)
        val pv = dialog.findViewById<PhotoView>(R.id.PV_img)
        val btnCerrar = dialog.findViewById<MaterialButton>(R.id.BtnCerrarVisualizador)
        Glide.with(context).load(imagen).into(pv)
        btnCerrar.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
