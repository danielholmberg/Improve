package dev.danielholmberg.improve.domain.usecase.contacts

import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.repository.CompanyRepository
import javax.inject.Inject

class AddCompanyUseCase @Inject constructor(private val companyRepository: CompanyRepository) {
    suspend operator fun invoke(company: Company): Result<Unit> {
        if (company.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Company name must not be blank"))
        }
        return companyRepository.addCompany(company)
    }
}
