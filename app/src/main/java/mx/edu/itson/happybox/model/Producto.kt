package mx.edu.itson.happybox.model

import com.google.firebase.firestore.Exclude

data class Producto(
    val id: Int = 0,
    val nombre: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val descripcion: String = "",
    val disponible: Boolean = true,
    val stock: Int = 0,
    val imagenUrl: String = "",
    // Recurso local: Firestore lo ignora, se asigna en memoria
    @get:Exclude val imagenResId: Int = 0
)
