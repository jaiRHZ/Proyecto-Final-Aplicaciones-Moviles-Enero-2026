package mx.edu.itson.happybox.model

data class Producto(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val categoria: String,
    val descripcion: String,
    val disponible: Boolean = true,
    // En un proyecto real aquí iría la URL de la imagen.
    // Por ahora usamos un resource ID de drawable.
    val imagenResId: Int = 0
)
