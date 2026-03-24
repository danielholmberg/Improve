package dev.danielholmberg.improve.data.mapper

import dev.danielholmberg.improve.data.firebase.dto.TagDto
import dev.danielholmberg.improve.domain.model.Tag

object TagMapper {

    fun toDomain(dto: TagDto): Tag = Tag(
        id = dto.id.orEmpty(),
        label = dto.label.orEmpty(),
        color = dto.color.orEmpty(),
        textColor = dto.textColor.orEmpty()
    )

    fun toDto(domain: Tag): TagDto = TagDto(
        id = domain.id.ifEmpty { null },
        label = domain.label.ifEmpty { null },
        color = domain.color.ifEmpty { null },
        textColor = domain.textColor.ifEmpty { null }
    )
}
