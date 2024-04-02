package team.b2.bingojango.domain.purchase.model

import com.fasterxml.jackson.annotation.JsonCreator
import org.apache.commons.lang3.EnumUtils

enum class PurchaseStatus {
    ACTIVE, APPROVED, REJECTED, ON_VOTE;

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(name: String?): PurchaseStatus? =
            name?.let { EnumUtils.getEnumIgnoreCase(PurchaseStatus::class.java, it.trim()) }
    }
}