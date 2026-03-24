package dev.danielholmberg.improve.domain.repository

import dev.danielholmberg.improve.domain.model.Company
import kotlinx.coroutines.flow.Flow

interface CompanyRepository {
    fun getCompanies(): Flow<List<Company>>
    suspend fun addCompany(company: Company): Result<Unit>
    suspend fun deleteCompany(companyId: String): Result<Unit>
}
