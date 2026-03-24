package dev.danielholmberg.improve.data.mapper

import dev.danielholmberg.improve.data.firebase.dto.CompanyDto
import dev.danielholmberg.improve.domain.model.Company

object CompanyMapper {

    fun toDomain(dto: CompanyDto, contacts: Map<String, Boolean> = emptyMap()): Company = Company(
        id = dto.id.orEmpty(),
        name = dto.name.orEmpty(),
        contacts = contacts
    )

    fun toDto(domain: Company): CompanyDto = CompanyDto(
        id = domain.id.ifEmpty { null },
        name = domain.name.ifEmpty { null }
    )
}
