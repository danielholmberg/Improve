package dev.danielholmberg.improve.clean.feature_privacy_policy.presentation

import androidx.lifecycle.ViewModel
import dev.danielholmberg.improve.clean.feature_privacy_policy.domain.use_case.PrivacyPolicyUseCases

class PrivacyPolicyViewModel(
    private val privacyPolicyUseCases: PrivacyPolicyUseCases
) : ViewModel() {

    fun getPrivacyPolicyHtml(): String {
        return privacyPolicyUseCases.getPrivacyPolicyUseCase().html
    }
}