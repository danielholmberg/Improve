package dev.danielholmberg.improve.clean.feature_feedback.data.source.entity

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.feature_feedback.domain.model.Feedback

@IgnoreExtraProperties
class FeedbackEntity(
    var uid: String? = null,
    var feedback_id: String? = null,
    var title: String? = null,
    var feedback: String? = null,
    private var timestamp: String? = null
) {
    @Exclude
    fun fromFeedback(feedback: Feedback): FeedbackEntity {
        return FeedbackEntity(
            uid = feedback.uid,
            feedback_id = feedback.feedbackId,
            title = feedback.subject,
            feedback = feedback.message,
            timestamp = feedback.timestamp
        )
    }

    @Exclude
    fun toFeedback(): Feedback {
        return Feedback(
            uid = uid,
            feedbackId = feedback_id,
            subject = title,
            message = feedback,
            timestamp = timestamp
        )
    }
}