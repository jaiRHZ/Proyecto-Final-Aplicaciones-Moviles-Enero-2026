package mx.edu.itson.happybox.model

data class Resena(
    val id: String = "",           // ID del documento en Firestore (auto-generado)
    val productoId: Int = 0,       // ID del producto al que pertenece
    val titulo: String = "",
    val comentario: String = "",
    val rating: Int = 0,           // 1 – 5 estrellas
    val autorUid: String = "",     // UID de Firebase Auth del autor
    val autorNombre: String = "",  // Nombre del autor (para mostrar en UI)
    val fecha: Long = 0L           // System.currentTimeMillis() al crear
)
