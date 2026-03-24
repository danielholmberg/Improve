package dev.danielholmberg.improve.data.mapper

import dev.danielholmberg.improve.data.firebase.dto.FeedbackDto
import dev.danielholmberg.improve.domain.model.Feedback

object FeedbackMapper {

    fun toDomain(dto: FeedbackDto): Feedback = Feedback(
        feedbackId = dto.feedback_id.orEmpty(),
        uid = dto.uid.orEmpty(),
        title = dto.title.orEmpty(),
        feedback = dto.feedback.orEmpty(),
        timestamp = dto.timestamp.orEmpty()
    )

    fun toDto(domain: Feedback): FeedbackDto = FeedbackDto(
        feedback_id = domain.feedbackId.ifEmpty { null },
        uid = domain.uid.ifEmpty { null },
        title = domain.title.ifEmpty { null },
        feedback = domain.feedback.ifEmpty { null },
        timestamp = domain.timestamp.ifEmpty { null }
    )
}
