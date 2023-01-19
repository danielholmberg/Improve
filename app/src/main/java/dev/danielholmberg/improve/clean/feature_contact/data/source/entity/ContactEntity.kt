package dev.danielholmberg.improve.clean.feature_contact.data.source.entity

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Contact

@IgnoreExtraProperties
data class ContactEntity(
    var id: String? = null,
    var name: String? = null,
    var companyId: String? = null,
    var email: String? = "",
    var phone: String? = "",
    var comment: String? = "",
    var timestampAdded: String? = null,
    var timestampUpdated: String? = null
) {
    @Exclude
    fun fromContact(contact: Contact): ContactEntity {
        return ContactEntity(
            id = contact.id,
            name = contact.name,
            companyId = contact.companyId,
            email = contact.email,
            phone = contact.phone,
            comment = contact.comment,
            timestampAdded = contact.timestampAdded,
            timestampUpdated = contact.timestampUpdated,
        )
    }

    @Exclude
    fun toContact(): Contact {
        return Contact(
            id = id,
            name = name,
            companyId = companyId,
            email = email,
            phone = phone,
            comment = comment,
            timestampAdded = timestampAdded,
            timestampUpdated = timestampUpdated,
        )
    }
}
