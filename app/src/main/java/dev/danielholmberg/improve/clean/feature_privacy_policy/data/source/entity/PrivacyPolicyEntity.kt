package dev.danielholmberg.improve.clean.feature_privacy_policy.data.source.entity

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.model.PrivacyPolicy

@IgnoreExtraProperties
class PrivacyPolicyEntity(
    val text: String = ""
) {
    @Exclude
    fun fromPrivacyPolicy(privacyPolicy: PrivacyPolicy): PrivacyPolicyEntity {
        return PrivacyPolicyEntity(
            text = privacyPolicy.html
        )
    }

    @Exclude
    fun toPrivacyPolicy(): PrivacyPolicy {
        return PrivacyPolicy(
            html = text
        )
    }
}