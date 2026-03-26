package mx.edu.itson.happybox.model

object CarritoManager {

    // Lista interna — solo accesible a través de las funciones de abajo
    private val items: MutableList<ItemCarrito> = mutableListOf()

    // ── Lectura ───────────────────────────────────────────────
    /** Devuelve una copia de la lista (para no exponer la referencia). */
    fun obtenerItems(): List<ItemCarrito> = items.toList()

    /** Número total de artículos (suma de cantidades). */
    fun contarArticulos(): Int = items.sumOf { it.cantidad }

    /** Total en pesos de todo el carrito. */
    fun calcularTotal(): Double = items.sumOf { it.subtotal }

    /**
     * Agrega un producto al carrito.
     * Si el producto ya existe, incrementa su cantidad.
     */
    fun agregar(producto: Producto) {
        val existente = items.find { it.producto.id == producto.id }
        if (existente != null) {
            existente.cantidad++
        } else {
            items.add(ItemCarrito(producto))
        }
    }

    /**
     * Incrementa en 1 la cantidad de un item.
     * No hay límite definido — en producción validarías stock.
     */
    fun incrementar(item: ItemCarrito) {
        val target = items.find { it.producto.id == item.producto.id }
        target?.cantidad++
    }

    /**
     * Decrementa en 1 la cantidad.
     * Si la cantidad llega a 0, elimina el item del carrito.
     */
    fun decrementar(item: ItemCarrito): Boolean {
        val target = items.find { it.producto.id == item.producto.id }
        if (target != null) {
            target.cantidad--
            if (target.cantidad <= 0) {
                items.remove(target)
                return true // indica que fue eliminado
            }
        }
        return false // solo decrementó
    }

    /** Elimina un item completamente sin importar su cantidad. */
    fun eliminar(item: ItemCarrito) {
        items.removeAll { it.producto.id == item.producto.id }
    }

    /** Vacía todo el carrito (se llama al confirmar el pedido). */
    fun vaciar() {
        items.clear()
    }

    /** ¿El carrito no tiene ningún producto? */
    fun estaVacio(): Boolean = items.isEmpty()

}