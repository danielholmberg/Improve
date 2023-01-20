package dev.danielholmberg.improve.clean.feature_privacy_policy.data.repository

import dev.danielholmberg.improve.clean.feature_privacy_policy.data.source.PrivacyPolicyDataSource
import dev.danielholmberg.improve.clean.feature_privacy_policy.data.source.entity.PrivacyPolicyEntity
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.model.PrivacyPolicy
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.repository.PrivacyPolicyRepository

class PrivacyPolicyRepositoryImpl(
    private val privatePolicyDataSource: PrivacyPolicyDataSource
) : PrivacyPolicyRepository {
    override fun getPrivacyPolicy(): PrivacyPolicy {
        val privacyPolicyEntity: PrivacyPolicyEntity = privatePolicyDataSource.getPrivacyPolicy()
        return privacyPolicyEntity.toPrivacyPolicy()
    }
}