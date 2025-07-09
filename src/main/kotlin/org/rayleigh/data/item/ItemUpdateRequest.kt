package org.rayleigh.data.item

import java.math.BigDecimal

class ItemUpdateRequest(
    var name: String? = null,
    var price: BigDecimal = BigDecimal.ZERO,
    var quantity: Int = 0,
    var color: String? = null,
    var areaIds: List<Long> = emptyList(),
    var depositId: Long = 0
) {
}