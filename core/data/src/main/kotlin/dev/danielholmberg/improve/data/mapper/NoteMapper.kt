package dev.danielholmberg.improve.data.mapper

import dev.danielholmberg.improve.data.firebase.dto.NoteDto
import dev.danielholmberg.improve.domain.model.Note

object NoteMapper {

    fun toDomain(dto: NoteDto): Note = Note(
        id = dto.id.orEmpty(),
        title = dto.title.orEmpty(),
        info = dto.info.orEmpty(),
        isStarred = dto.stared,
        isArchived = dto.archived,
        added = dto.added.orEmpty(),
        updated = dto.updated.orEmpty(),
        tags = dto.tags ?: emptyMap(),
        vipImages = dto.vipImages?.toList() ?: emptyList()
    )

    fun toDto(domain: Note): NoteDto = NoteDto(
        id = domain.id.ifEmpty { null },
        title = domain.title.ifEmpty { null },
        info = domain.info.ifEmpty { null },
        stared = domain.isStarred,
        archived = domain.isArchived,
        added = domain.added.ifEmpty { null },
        updated = domain.updated.ifEmpty { null },
        tags = domain.tags.ifEmpty { null },
        vipImages = if (domain.vipImages.isEmpty()) null else ArrayList(domain.vipImages)
    )
}
