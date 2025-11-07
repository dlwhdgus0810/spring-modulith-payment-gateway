package me.hyunlee.laundry.order.adapter.out.persistence.order

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import me.hyunlee.laundry.order.domain.model.catalog.AddOnType

@Converter(autoApply = false)
class AddOnsJsonConverter(
    private val mapper: ObjectMapper = ObjectMapper()
) : AttributeConverter<Set<AddOnType>, String> {

    override fun convertToDatabaseColumn(attribute: Set<AddOnType>?): String =
        mapper.writeValueAsString(attribute ?: emptySet<AddOnType>())

    override fun convertToEntityAttribute(dbData: String?): Set<AddOnType> {
        if (dbData.isNullOrBlank()) return emptySet()
        val typeRef = object : TypeReference<Set<AddOnType>>() {}
        return mapper.readValue(dbData, typeRef)
    }
}