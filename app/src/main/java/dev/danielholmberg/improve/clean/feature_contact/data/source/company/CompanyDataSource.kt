package dev.danielholmberg.improve.clean.feature_contact.data.source.company

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.CompanyEntity

interface CompanyDataSource {
    val companiesRef: DatabaseReference
    fun generateNewCompanyId(): String?
    fun addCompany(companyEntity: CompanyEntity)
    fun deleteCompany(companyEntity: CompanyEntity)
    fun saveCompanies(companyEntitiesMap: Map<String?, CompanyEntity>)
    fun addChildEventListener(childEventListener: ChildEventListener)
    fun addChildEventListener(orderBy: String, childEventListener: ChildEventListener)
}
