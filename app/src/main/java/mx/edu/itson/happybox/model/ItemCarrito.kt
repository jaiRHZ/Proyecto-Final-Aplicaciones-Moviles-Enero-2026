package mx.edu.itson.happybox.model

data class ItemCarrito(
    val producto: Producto,
    var cantidad: Int = 1
) {
    // Precio total de esta línea (precio × cantidad)
    val subtotal: Double
        get() = producto.precio * cantidad
}