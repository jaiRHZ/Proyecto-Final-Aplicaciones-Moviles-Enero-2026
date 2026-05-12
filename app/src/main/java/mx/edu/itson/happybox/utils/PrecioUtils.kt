package mx.edu.itson.happybox.utils

import mx.edu.itson.happybox.model.ItemCarrito

data class ResumenPrecios(
    val costoItems: Double, 
    val subtotal: Double,   
    val iva: Double,        
    val envio: Double,      
    val total: Double,      
    val aplicaPromoEnvio: Boolean
)

object PrecioUtils {
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

    fun formatearPrecio(precio: Double): String {
        return String.format("$%.2f MXN", precio)
    }
}
