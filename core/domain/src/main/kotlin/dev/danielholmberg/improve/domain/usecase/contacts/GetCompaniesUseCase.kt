package dev.danielholmberg.improve.domain.usecase.contacts

import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.repository.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCompaniesUseCase @Inject constructor(private val companyRepository: CompanyRepository) {
    operator fun invoke(): Flow<List<Company>> =
        companyRepository.getCompanies().map { companies ->
            companies.sortedBy { it.name }
        }
}
