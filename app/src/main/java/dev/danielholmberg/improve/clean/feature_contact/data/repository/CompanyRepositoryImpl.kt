package dev.danielholmberg.improve.clean.feature_contact.data.repository

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import dev.danielholmberg.improve.clean.feature_contact.data.source.company.CompanyDataSource
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.CompanyEntity
import dev.danielholmberg.improve.clean.feature_contact.domain.model.Company
import dev.danielholmberg.improve.clean.feature_contact.domain.repository.CompanyRepository

class CompanyRepositoryImpl(
    private val companyDataSource: CompanyDataSource
) : CompanyRepository {
    override fun generateNewCompanyId(): String? {
        return companyDataSource.generateNewCompanyId()
    }

    override fun addCompany(company: Company) {
        // Transform into Data Source model
        val companyEntity: CompanyEntity = CompanyEntity().fromCompany(company)
        companyDataSource.addCompany(companyEntity)
    }

    override fun deleteCompany(company: Company) {
        // Transform into Data Source model
        val companyEntity: CompanyEntity = CompanyEntity().fromCompany(company)
        companyDataSource.deleteCompany(companyEntity)
    }

    override fun saveCompanies(companies: HashMap<String?, Company>) {
        // Transform into Data Source model
        val companyEntitiesMap = companies.entries.associate {
            (id, company) -> Pair(id, CompanyEntity().fromCompany(company))
        }
        companyDataSource.saveCompanies(companyEntitiesMap)
    }

    override fun addChildEventListener(childEventListener: ChildEventListener) {
        companyDataSource.addChildEventListener(childEventListener)
    }

    override fun addChildEventListener(orderBy: String, childEventListener: ChildEventListener) {
        companyDataSource.addChildEventListener(orderBy, childEventListener)
    }

    override fun getCompaniesRef(): DatabaseReference {
        return companyDataSource.companiesRef
    }

    companion object {
        const val ORDER_BY_NAME = "name"
    }
}