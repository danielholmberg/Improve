package dev.danielholmberg.improve.clean.feature_contact.data.source.entity

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Contact

@IgnoreExtraProperties
data class CompanyEntity(
    var id: String? = null,
    var name: String? = null,
    var contacts: HashMap<String?, Contact> = HashMap()
) {
    @Exclude
    fun fromCompany(company: Company): CompanyEntity {
        return CompanyEntity(
            id = company.id,
            name = company.name,
            contacts = company.contacts
        )
    }

    @Exclude
    fun toCompany(): Company {
        return Company(
            id = id,
            name = name,
            contacts = contacts
        )
    }
}
