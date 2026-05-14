package com.example.meetly.Chat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meetly.Adaptadores.AdaptadorChat
import com.example.meetly.Constantes
import com.example.meetly.Modelos.Chat
import com.example.meetly.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var progressDialog: android.app.ProgressDialog? = null

    private var uid = ""
    private var miUid = ""
    private var chatRuta = ""
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioPath: String = ""

    private var yoBloqueé = false
    private var meBloquearon = false

    private lateinit var adaptadorChat: AdaptadorChat
    private val mensajesArrayList = ArrayList<Chat>()

    // Modern Image Picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { 
            Log.d("ChatStorage", "Image selected: $it")
            subirImgStorage(it) 
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        uid = intent.getStringExtra("uid") ?: ""
        miUid = firebaseAuth.uid ?: ""
        chatRuta = Constantes.rutaChat(uid, miUid)

        // Setup RecyclerView
        binding.chatsRV.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        adaptadorChat = AdaptadorChat(this, mensajesArrayList)
        binding.chatsRV.adapter = adaptadorChat

        binding.TvNombreUsuario.text = intent.getStringExtra("nombre") ?: "Usuario"

        binding.IbRegresar.setOnClickListener { finish() }
        binding.enviarFAB.setOnClickListener { validarMensaje() }

        binding.IbMasOpciones.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(com.example.meetly.R.menu.menu_chat, popup.menu)
            popup.menu.findItem(com.example.meetly.R.id.opcion_bloquear)?.isVisible = !yoBloqueé
            popup.menu.findItem(com.example.meetly.R.id.opcion_desbloquear)?.isVisible = yoBloqueé
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    com.example.meetly.R.id.opcion_bloquear -> { bloquearUsuario(); true }
                    com.example.meetly.R.id.opcion_desbloquear -> { desbloquearUsuario(); true }
                    else -> false
                }
            }
            popup.show()
        }
        
        // Attach Image
        binding.adjuntarFAB.setOnClickListener { pickImage.launch("image/*") }

        // Voice Message: Hold to Record
        binding.micFAB.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (checkAudioPermission()) startRecording()
                    else pedirPermisoAudio()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (mediaRecorder != null) stopRecording()
                }
            }
            true
        }

        observarBloqueos()
        cargarMensajes()
    }

    private fun checkAudioPermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun pedirPermisoAudio() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) Toast.makeText(this, "Microphone permission required for voice messages", Toast.LENGTH_SHORT).show()
    }

    private fun startRecording() {
        try {
            audioPath = "${externalCacheDir?.absolutePath}/recording_${System.currentTimeMillis()}.mp3"
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioPath)
                prepare()
                start()
            }
            Toast.makeText(this, "Recording...", Toast.LENGTH_SHORT).show()
            Log.d("ChatStorage", "Recording started at: $audioPath")
        } catch (e: Exception) {
            Log.e("ChatStorage", "Recorder error: ${e.message}")
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            subirAudioStorage()
        } catch (e: Exception) {
            Log.e("ChatStorage", "Stop recording error: ${e.message}")
            mediaRecorder = null
        }
    }

    private fun subirAudioStorage() {
        val tiempo = Constantes.obtenerTiempoDis()
        val fileName = "AUDIO_${miUid}_${tiempo}.mp3"
        val ref = FirebaseStorage.getInstance().getReference("AudiosChat/$fileName")
        
        ref.putFile(Uri.fromFile(File(audioPath)))
            .addOnSuccessListener { task ->
                task.storage.downloadUrl.addOnSuccessListener { uri ->
                    enviarMensaje(Constantes.MENSAJE_TIPO_AUDIO, uri.toString(), tiempo)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error sending audio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImgStorage(uri: Uri) {
        mostrarProgress("Sending image...")
        val tiempo = Constantes.obtenerTiempoDis()
        val fileName = "IMG_${miUid}_${tiempo}.jpg"
        val ref = FirebaseStorage.getInstance().getReference("ImagenesChat/$fileName")
        
        ref.putFile(uri)
            .addOnSuccessListener { task ->
                task.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                    ocultarProgress()
                    enviarMensaje(Constantes.MENSAJE_TIPO_IMAGEN, downloadUri.toString(), tiempo)
                }.addOnFailureListener { e ->
                    ocultarProgress()
                    Log.e("ChatStorage", "URL error: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                ocultarProgress()
                Toast.makeText(this, "Error sending image", Toast.LENGTH_SHORT).show()
                Log.e("ChatStorage", "Upload error: ${e.message}")
            }
    }

    private fun cargarMensajes() {
        FirebaseDatabase.getInstance().getReference("Chats").child(chatRuta)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mensajesArrayList.clear()
                    for (ds in snapshot.children) {
                        ds.getValue(Chat::class.java)?.let { mensajesArrayList.add(it) }
                    }
                    adaptadorChat.notifyDataSetChanged()
                    if (mensajesArrayList.isNotEmpty()) binding.chatsRV.scrollToPosition(mensajesArrayList.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatActivity", "DB Error: ${error.message}")
                }
            })
    }

    private fun validarMensaje() {
        val mensaje = binding.EtMensajeChat.text.toString().trim()
        if (mensaje.isEmpty()) return
        enviarMensaje(Constantes.MENSAJE_TIPO_TEXTO, mensaje, Constantes.obtenerTiempoDis())
    }

    private fun enviarMensaje(tipoMensaje: String, mensaje: String, tiempo: Long) {
        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = refChat.child(chatRuta).push().key ?: return

        val hashMap = hashMapOf<String, Any>(
            "idMensaje" to keyId,
            "tipoMensaje" to tipoMensaje,
            "mensaje" to mensaje,
            "emisorUid" to miUid,
            "receptorUid" to uid,
            "tiempo" to tiempo
        )

        refChat.child(chatRuta).child(keyId).setValue(hashMap).addOnSuccessListener {
            binding.EtMensajeChat.setText("")
        }.addOnFailureListener { e ->
            Log.e("ChatActivity", "Error saving message: ${e.message}")
        }
    }

    private fun mostrarProgress(msg: String) {
        progressDialog = android.app.ProgressDialog(this).apply {
            setMessage(msg)
            setCancelable(false)
            show()
        }
    }

    private fun ocultarProgress() {
        progressDialog?.dismiss()
    }

    private fun observarBloqueos() {
        val db = FirebaseDatabase.getInstance().reference
        db.child("Bloqueados/$miUid/$uid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { yoBloqueé = snapshot.exists(); actualizarEstadoBloqueado() }
            override fun onCancelled(error: DatabaseError) {}
        })
        db.child("Bloqueados/$uid/$miUid").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) { meBloquearon = snapshot.exists(); actualizarEstadoBloqueado() }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun actualizarEstadoBloqueado() {
        val bloqueado = yoBloqueé || meBloquearon
        binding.enviarFAB.isEnabled = !bloqueado
        binding.adjuntarFAB.isEnabled = !bloqueado
        binding.micFAB.isEnabled = !bloqueado
        binding.EtMensajeChat.isEnabled = !bloqueado
        binding.TvBloqueado.visibility = if (bloqueado) View.VISIBLE else View.GONE
    }

    private fun bloquearUsuario() {
        FirebaseDatabase.getInstance().reference
            .child("Bloqueados/$miUid/$uid")
            .setValue(true)
            .addOnSuccessListener { Toast.makeText(this, "Usuario bloqueado", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this, "Error al bloquear", Toast.LENGTH_SHORT).show() }
    }

    private fun desbloquearUsuario() {
        FirebaseDatabase.getInstance().reference
            .child("Bloqueados/$miUid/$uid")
            .removeValue()
            .addOnSuccessListener { Toast.makeText(this, "Usuario desbloqueado", Toast.LENGTH_SHORT).show() }
            .addOnFailureListener { Toast.makeText(this, "Error al desbloquear", Toast.LENGTH_SHORT).show() }
    }
}
