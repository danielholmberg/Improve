package dev.danielholmberg.improve.clean.feature_feedback.data.source

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.clean.feature_feedback.data.entity.FeedbackEntity
import dev.danielholmberg.improve.clean.feature_feedback.domain.util.FeedbackCallback

class FeedbackDataSourceImpl(
    private val databaseService: FirebaseDatabase
) : FeedbackDataSource {
    private val feedbackRef: DatabaseReference
        get() = databaseService.reference.child(FEEDBACK_REF)

    override fun generateNewFeedbackId(): String? {
        return feedbackRef.push().key
    }

    override fun submit(feedbackEntity: FeedbackEntity, callback: FeedbackCallback) {
        feedbackRef.child(feedbackEntity.feedback_id!!)
            .setValue(feedbackEntity) { databaseError, _ ->
                if (databaseError != null) {
                    Log.e(TAG, "Failed to submit feedback to Firebase: $databaseError")
                    callback.onFailure(databaseError.toString())
                } else {
                    callback.onSuccess()
                }
            }
    }

    companion object {
        private val TAG = FeedbackDataSourceImpl::class.java.simpleName
        private const val FEEDBACK_REF = "feedback"
    }
}