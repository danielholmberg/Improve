package dev.danielholmberg.improve.domain.usecase.contacts

import dev.danielholmberg.improve.domain.repository.CompanyRepository
import javax.inject.Inject

class DeleteCompanyUseCase @Inject constructor(private val companyRepository: CompanyRepository) {
    suspend operator fun invoke(companyId: String): Result<Unit> =
        companyRepository.deleteCompany(companyId)
}
