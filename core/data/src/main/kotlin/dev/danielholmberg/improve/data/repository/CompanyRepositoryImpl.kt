package dev.danielholmberg.improve.data.repository

import dev.danielholmberg.improve.data.firebase.datasource.FirebaseCompanyDataSource
import dev.danielholmberg.improve.data.mapper.CompanyMapper
import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.repository.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CompanyRepositoryImpl @Inject constructor(
    private val companyDataSource: FirebaseCompanyDataSource
) : CompanyRepository {

    override fun getCompanies(): Flow<List<Company>> = flow {
        val companies = mutableMapOf<String, Company>()
        companyDataSource.observeCompanies().collect { event ->
            when (event) {
                is FirebaseCompanyDataSource.CompanyEvent.Added -> {
                    val company = CompanyMapper.toDomain(event.dto)
                    companies[company.id] = company
                }
                is FirebaseCompanyDataSource.CompanyEvent.Changed -> {
                    val company = CompanyMapper.toDomain(
                        event.dto,
                        contacts = companies[event.dto.id.orEmpty()]?.contacts ?: emptyMap()
                    )
                    companies[company.id] = company
                }
                is FirebaseCompanyDataSource.CompanyEvent.Removed -> {
                    companies.remove(event.id)
                }
            }
            emit(companies.values.toList())
        }
    }

    override suspend fun addCompany(company: Company): Result<Unit> {
        val result = companyDataSource.addCompany(CompanyMapper.toDto(company))
        return result.map { }
    }

    override suspend fun deleteCompany(companyId: String): Result<Unit> =
        companyDataSource.deleteCompany(companyId)
}
