package mx.edu.itson.happybox.model

data class Pedido(
    val id: Int,
    val numeroPedido: String,
    val fecha: String,
    val status: String, // En camino, Entregado, Cancelado
    val direccion: String,
    val llegadaEstimada: String? = null,
    val cantidadArticulos: Int,
    val total: Double
)
