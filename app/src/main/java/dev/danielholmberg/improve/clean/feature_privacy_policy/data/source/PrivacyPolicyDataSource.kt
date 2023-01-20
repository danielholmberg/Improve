package dev.danielholmberg.improve.clean.feature_privacy_policy.data.source

import dev.danielholmberg.improve.clean.feature_privacy_policy.data.source.entity.PrivacyPolicyEntity

interface PrivacyPolicyDataSource {
    fun getPrivacyPolicy(): PrivacyPolicyEntity
}