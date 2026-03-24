package dev.danielholmberg.improve.data.mapper

import dev.danielholmberg.improve.data.firebase.dto.ContactDto
import dev.danielholmberg.improve.domain.model.Contact

object ContactMapper {

    fun toDomain(dto: ContactDto): Contact = Contact(
        id = dto.id.orEmpty(),
        name = dto.name.orEmpty(),
        companyId = dto.companyId.orEmpty(),
        email = dto.email.orEmpty(),
        phone = dto.phone.orEmpty(),
        comment = dto.comment.orEmpty(),
        timestampAdded = dto.timestampAdded.orEmpty(),
        timestampUpdated = dto.timestampUpdated.orEmpty()
    )

    fun toDto(domain: Contact): ContactDto = ContactDto(
        id = domain.id.ifEmpty { null },
        name = domain.name.ifEmpty { null },
        companyId = domain.companyId.ifEmpty { null },
        email = domain.email.ifEmpty { null },
        phone = domain.phone.ifEmpty { null },
        comment = domain.comment.ifEmpty { null },
        timestampAdded = domain.timestampAdded.ifEmpty { null },
        timestampUpdated = domain.timestampUpdated.ifEmpty { null }
    )
}
