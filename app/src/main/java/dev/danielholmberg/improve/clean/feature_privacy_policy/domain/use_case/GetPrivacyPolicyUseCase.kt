package dev.danielholmberg.improve.clean.feature_privacy_policy.domain.use_case

import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.model.PrivacyPolicy
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.repository.PrivacyPolicyRepository

class GetPrivacyPolicyUseCase(
    private val privacyPolicyRepository: PrivacyPolicyRepository
){
    operator fun invoke(): PrivacyPolicy {
        return privacyPolicyRepository.getPrivacyPolicy()
    }
}