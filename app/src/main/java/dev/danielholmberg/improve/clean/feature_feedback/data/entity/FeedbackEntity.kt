package dev.danielholmberg.improve.clean.feature_feedback.data.entity

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.feature_feedback.domain.model.Feedback

@IgnoreExtraProperties
class FeedbackEntity(
    var uid: String? = null,
    var feedback_id: String? = null,
    var title: String? = null,
    var feedback: String? = null,
    var timestamp: String? = null
) {
    @Exclude
    fun fromFeedback(feedback: Feedback): FeedbackEntity {
        return FeedbackEntity(
            uid = feedback.uid,
            feedback_id = feedback.feedbackId,
            title = feedback.title,
            feedback = feedback.feedback,
            timestamp = feedback.timestamp
        )
    }

    @Exclude
    fun toFeedback(): Feedback {
        return Feedback(
            uid = uid,
            feedbackId = feedback_id,
            title = title,
            feedback = feedback,
            timestamp = timestamp
        )
    }
}