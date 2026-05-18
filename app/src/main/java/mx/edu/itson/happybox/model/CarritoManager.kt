package mx.edu.itson.happybox.model

/**
 * Gestor centralizado del carrito de compras en memoria.
 *
 * Implementado como un objeto singleton para garantizar una única fuente
 * de verdad del estado del carrito durante la sesión activa del usuario.
 * La persistencia en Firestore es responsabilidad de [CarritoFragment]
 * y [CheckoutActivity]; este objeto solo mantiene el estado local.
 *
 * Al confirmar un pedido exitoso, se debe llamar a [vaciar] para limpiar
 * el estado de la sesión.
 */
object CarritoManager {

    // Lista interna — solo accesible a través de las funciones de abajo
    private val items: MutableList<ItemCarrito> = mutableListOf()

    // ── Lectura ───────────────────────────────────────────────
    /** Devuelve una copia inmutable de la lista para no exponer la referencia interna. */
    fun obtenerItems(): List<ItemCarrito> = items.toList()

    /** Retorna el número total de artículos sumando las cantidades de cada línea. */
    fun contarArticulos(): Int = items.sumOf { it.cantidad }

    /** Retorna el costo total en pesos de todos los artículos en el carrito. */
    fun calcularTotal(): Double = items.sumOf { it.subtotal }

    /**
     * Agrega un producto al carrito.
     *
     * Si el producto ya existe en el carrito, incrementa su cantidad en 1
     * en lugar de agregar una línea duplicada.
     *
     * @param producto El producto que se desea agregar.
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
     * Incrementa en 1 la cantidad de un artículo existente en el carrito.
     *
     * @param item El artículo cuya cantidad se desea incrementar.
     */
    fun incrementar(item: ItemCarrito) {
        val target = items.find { it.producto.id == item.producto.id }
        target?.cantidad++
    }

    /**
     * Decrementa en 1 la cantidad de un artículo.
     *
     * Si la cantidad resultante es 0 o menor, el artículo se elimina
     * completamente del carrito.
     *
     * @param item El artículo cuya cantidad se desea decrementar.
     * @return `true` si el artículo fue eliminado del carrito, `false` si solo se decrementó.
     */
    fun decrementar(item: ItemCarrito): Boolean {
        val target = items.find { it.producto.id == item.producto.id }
        if (target != null) {
            target.cantidad--
            if (target.cantidad <= 0) {
                items.remove(target)
                return true
            }
        }
        return false
    }

    /**
     * Elimina un artículo del carrito de forma completa, independientemente de su cantidad.
     *
     * @param item El artículo que se desea eliminar.
     */
    fun eliminar(item: ItemCarrito) {
        items.removeAll { it.producto.id == item.producto.id }
    }

    /**
     * Elimina todos los artículos del carrito.
     *
     * Debe invocarse desde [CheckoutActivity] tras confirmar un pedido exitoso.
     */
    fun vaciar() {
        items.clear()
    }

    /**
     * Indica si el carrito no contiene ningun artículo.
     *
     * @return `true` si la lista de artículos esta vacía, `false` en caso contrario.
     */
    fun estaVacio(): Boolean = items.isEmpty()
}