package mx.edu.itson.happybox.model

/**
 * Representa una línea de artículo dentro del carrito de compras.
 *
 * Agrupa un [Producto] con la cantidad seleccionada por el usuario.
 * Es la unidad de dato principal que maneja [CarritoManager] en memoria
 * y que se persiste en la sub-colección `usuarios/{uid}/carrito` de Firestore.
 *
 * @property producto El producto asociado a esta línea del carrito.
 * @property cantidad Número de unidades seleccionadas por el usuario. Valor mínimo: 1.
 */
data class ItemCarrito(
    val producto: Producto,
    var cantidad: Int = 1
) {
    /**
     * Calcula el costo total de esta línea del carrito.
     *
     * Se recalcula dinámicamente cada vez que [cantidad] cambia.
     *
     * @return El resultado de multiplicar [Producto.precio] por [cantidad].
     */
    val subtotal: Double
        get() = producto.precio * cantidad
}