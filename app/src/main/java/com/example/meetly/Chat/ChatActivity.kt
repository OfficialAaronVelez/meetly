package com.example.meetly.Chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.meetly.Adaptadores.AdaptadorChat
import com.example.meetly.Constantes
import com.example.meetly.Modelos.Chat
import com.example.meetly.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var uid = ""
    private var miUid = ""
    private var chatRuta = ""
    private var imagenUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        uid = intent.getStringExtra("uid")!!
        miUid = firebaseAuth.uid!!
        chatRuta = Constantes.rutaChat(uid, miUid)

        binding.IbRegresar.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.enviarFAB.setOnClickListener { validarMensaje() }
        binding.adjuntarFAB.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                imagenGaleria()
            } else {
                solicitarPermisoAlmacenamiento.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        cargarMensajes()
    }

    private fun cargarMensajes() {
        val mensajesArrayList = ArrayList<Chat>()
        FirebaseDatabase.getInstance().getReference("Chats").child(chatRuta)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mensajesArrayList.clear()
                    for (ds in snapshot.children) {
                        try {
                            mensajesArrayList.add(ds.getValue(Chat::class.java)!!)
                        } catch (e: Exception) {
                            Log.e("ChatActivity", "Error al cargar mensaje: ${e.message}")
                        }
                    }
                    binding.chatsRV.adapter = AdaptadorChat(this@ChatActivity, mensajesArrayList)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "Error: ${error.message}")
                }
            })
    }

    private fun validarMensaje() {
        val mensaje = binding.EtMensajeChat.text.toString().trim()
        if (mensaje.isEmpty()) {
            Toast.makeText(this, "Ingrese un mensaje", Toast.LENGTH_SHORT).show()
        } else {
            enviarMensaje(Constantes.MENSAJE_TIPO_TEXTO, mensaje, Constantes.obtenerTiempoDis())
        }
    }

    private fun enviarMensaje(tipoMensaje: String, mensaje: String, tiempo: Long) {
        progressDialog.setMessage("Enviando mensaje")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = refChat.push().key!!
        val hashMap = hashMapOf<String, Any>(
            "idMensaje" to keyId,
            "tipoMensaje" to tipoMensaje,
            "mensaje" to mensaje,
            "emisorUid" to miUid,
            "receptorUid" to uid,
            "tiempo" to tiempo
        )

        refChat.child(chatRuta).child(keyId).setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.EtMensajeChat.setText("")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun imagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        resultadoGaleriaARL.launch(intent)
    }

    private val resultadoGaleriaARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultado ->
            if (resultado.resultCode == Activity.RESULT_OK) {
                imagenUri = resultado.data?.data
                subirImgStorage()
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    private val solicitarPermisoAlmacenamiento =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) imagenGaleria()
            else Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
        }

    private fun subirImgStorage() {
        progressDialog.setMessage("Subiendo imagen")
        progressDialog.show()
        val tiempo = Constantes.obtenerTiempoDis()
        FirebaseStorage.getInstance().getReference("ImagenesChat/$tiempo")
            .putFile(imagenUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    enviarMensaje(Constantes.MENSAJE_TIPO_IMAGEN, uri.toString(), tiempo)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
