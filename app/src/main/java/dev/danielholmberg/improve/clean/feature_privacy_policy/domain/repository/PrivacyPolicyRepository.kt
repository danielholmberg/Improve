package dev.danielholmberg.improve.clean.feature_privacy_policy.domain.repository

import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.model.PrivacyPolicy

interface PrivacyPolicyRepository {
    fun getPrivacyPolicy(): PrivacyPolicy
}