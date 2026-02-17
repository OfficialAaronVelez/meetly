package com.example.meetly

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    private const val SENDER_EMAIL = "themeetlyappl@gmail.com"
    private const val SENDER_PASSWORD = "kpivnenvpoljbxmn"

    fun sendCredentialsEmail(recipient: String, userPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("EmailSender", "Attempting to send email to $recipient...")
            
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.socketFactory.port", "465")
                put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
                put("mail.smtp.auth", "true")
                put("mail.smtp.port", "465")
                put("mail.smtp.ssl.enable", "true")
                put("mail.debug", "true") // This will print the full SMTP log to Logcat
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })

            try {
                val mm = MimeMessage(session)
                mm.setFrom(InternetAddress(SENDER_EMAIL, "Meetly"))
                mm.addRecipient(Message.RecipientType.TO, InternetAddress(recipient))
                mm.subject = "Bienvenido a Meetly!"
                mm.setText("""
                    ¡Hola!
                    
                    Gracias por registrarte en Meetly. Aquí están tus credenciales para iniciar sesión:
                    
                    Email: $recipient
                    Contraseña: $userPassword
                    
                    ¡Disfruta de la aplicación!
                    
                    - El equipo de Meetly
                """.trimIndent())

                Transport.send(mm)
                Log.d("EmailSender", "SUCCESS: Credentials email sent to $recipient")
            } catch (e: Exception) {
                // This will print the actual reason for the failure
                Log.e("EmailSender", "FAILURE: ${e.message}", e)
            }
        }
    }
}
