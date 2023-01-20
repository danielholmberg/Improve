package dev.danielholmberg.improve.clean.feature_privacy_policy.data.source

import dev.danielholmberg.improve.clean.core.RemoteConfigService
import dev.danielholmberg.improve.clean.feature_privacy_policy.data.source.entity.PrivacyPolicyEntity

class PrivacyPolicyDataSourceImpl(
    private val remoteConfigService: RemoteConfigService
) : PrivacyPolicyDataSource {
    override fun getPrivacyPolicy(): PrivacyPolicyEntity {
        val text = remoteConfigService.getPrivatePolicyText()
        return PrivacyPolicyEntity(text = text)
    }
}