package mx.edu.itson.happybox.utils

import mx.edu.itson.happybox.model.ItemCarrito

/**
 * Almacena el desglose completo de precios calculado para un pedido.
 *
 * Se obtiene como resultado de [PrecioUtils.calcularResumen] y se utiliza
 * para poblar los campos de la pantalla de resumen en [CheckoutActivity]
 * y en [CarritoFragment].
 *
 * @property costoItems Suma total del precio unitario multiplicado por la cantidad de cada artículo.
 * @property subtotal Costo de los artículos sin incluir el IVA.
 * @property iva 16% de impuesto calculado sobre [costoItems].
 * @property envio Costo de envío fijo. Es $0.00 cuando [aplicaPromoEnvio] es `true`.
 * @property total Monto final a cobrar al cliente, equivalente a [costoItems] + [envio].
 * @property aplicaPromoEnvio `true` si [costoItems] supera los $350.00 MXN, lo que activa el envío gratuito.
 */
data class ResumenPrecios(
    val costoItems: Double,
    val subtotal: Double,
    val iva: Double,
    val envio: Double,
    val total: Double,
    val aplicaPromoEnvio: Boolean
)

/**
 * Utilidad estatica para el calculo y formato de precios en la aplicacion.
 *
 * Centraliza la logica de negocio de precios para evitar duplicacion de calculos
 * entre [CarritoFragment] y [CheckoutActivity]. Todos sus metodos son estaticos
 * y no requieren instanciacion.
 *
 * @see ResumenPrecios
 */
object PrecioUtils {

    /**
     * Calcula el desglose completo de precios para una lista de articulos del carrito.
     *
     * Aplica un IVA del 16% y un costo de envio fijo de $50.00 MXN.
     * El envio es gratuito si el subtotal de los articulos supera los $350.00 MXN.
     *
     * @param listaItems Lista de [ItemCarrito] con los productos y sus cantidades.
     * @return Un objeto [ResumenPrecios] con todos los campos calculados y listos para mostrarse en la UI.
     */
    fun calcularResumen(listaItems: List<ItemCarrito>): ResumenPrecios {
        val costoItems = listaItems.sumOf { it.subtotal }
        val iva = costoItems * 0.16
        val subtotal = costoItems - iva

        val aplicaPromoEnvio = costoItems >= 350.0
        val envio = if (aplicaPromoEnvio) 0.0 else 50.0

        val total = costoItems + envio

        return ResumenPrecios(
            costoItems = costoItems,
            subtotal = subtotal,
            iva = iva,
            envio = envio,
            total = total,
            aplicaPromoEnvio = aplicaPromoEnvio
        )
    }

    /**
     * Formatea un valor numerico al estilo de moneda del proyecto.
     *
     * @param precio Valor numerico de tipo [Double] a formatear.
     * @return Cadena con el formato `$1234.56 MXN`.
     */
    fun formatearPrecio(precio: Double): String {
        return String.format("$%.2f MXN", precio)
    }
}
