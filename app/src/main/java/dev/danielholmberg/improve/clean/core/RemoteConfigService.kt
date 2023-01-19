package dev.danielholmberg.improve.clean.core

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class RemoteConfigService(
    private val remoteConfig: FirebaseRemoteConfig
) {

    fun setConfigSettingsAsync(configSettings: FirebaseRemoteConfigSettings) {
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    fun setDefaultsAsync(remoteConfigDefaults: Int) {
        remoteConfig.setDefaultsAsync(remoteConfigDefaults)
    }

    fun setDefaultsAsync(defaults: Map<String, Any>) {
        remoteConfig.setDefaultsAsync(defaults)
    }

    fun fetchAndActivate(): Task<Boolean> {
        return remoteConfig.fetchAndActivate()
    }

    fun getPrivatePolicyText(): String {
        return remoteConfig.getString(PRIVACY_POLICY_TEXT_KEY)
    }

    fun getVipUsers(): String {
        return remoteConfig.getString(VIP_USERS_KEY)
    }

    companion object {
        const val VIP_USERS_KEY = "vip_users"
        const val PRIVACY_POLICY_TEXT_KEY = "privacy_policy_text"
    }
}