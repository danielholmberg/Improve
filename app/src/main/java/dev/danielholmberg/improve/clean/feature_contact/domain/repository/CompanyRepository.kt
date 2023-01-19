package dev.danielholmberg.improve.clean.feature_contact.domain.repository

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company

interface CompanyRepository {
    fun generateNewCompanyId(): String?
    fun addCompany(company: Company)
    fun deleteCompany(company: Company)
    fun saveCompanies(companies: HashMap<String?, Company>)
    fun addChildEventListener(childEventListener: ChildEventListener)
    fun addChildEventListener(orderBy: String, childEventListener: ChildEventListener)
    fun getCompaniesRef(): DatabaseReference
}