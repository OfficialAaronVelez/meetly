const { onValueCreated } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.notificarNuevoMensaje = onValueCreated(
  { ref: "/Chats/{chatRuta}/{mensajeId}", region: "us-central1" },
  async (event) => {
    const mensaje = event.data.val();
    if (!mensaje) return;

    const { emisorUid, receptorUid, tipoMensaje, mensaje: contenido } = mensaje;
    const db = getDatabase();

    // Don't notify if recipient has blocked the sender
    const bloqueadoSnap = await db.ref(`/Bloqueados/${receptorUid}/${emisorUid}`).get();
    if (bloqueadoSnap.exists()) return;

    // Get sender name
    const emisorSnap = await db.ref(`/Usuarios/${emisorUid}`).get();
    const emisor = emisorSnap.val();
    if (!emisor) return;

    // Get recipient FCM token
    const tokenSnap = await db.ref(`/Usuarios/${receptorUid}/fcmToken`).get();
    const fcmToken = tokenSnap.val();
    if (!fcmToken) return;

    const body = tipoMensaje === "IMAGEN" ? "📷 Imagen" : contenido;

    await getMessaging().send({
      token: fcmToken,
      notification: {
        title: emisor.nombres,
        body,
      },
      data: {
        senderUid: emisorUid,
        senderName: emisor.nombres,
      },
      android: {
        priority: "high",
        notification: { channelId: "mensajes_chat" },
      },
    });
  }
);
