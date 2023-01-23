package dev.danielholmberg.improve.clean.feature_contact.data.source.company

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.clean.feature_authentication.data.source.AuthDataSourceImpl.Companion.USERS_REF
import dev.danielholmberg.improve.clean.feature_authentication.domain.repository.AuthRepository
import dev.danielholmberg.improve.clean.feature_contact.data.source.entity.CompanyEntity

class CompanyDataSourceImpl(
    private val authRepository: AuthRepository,
    private val databaseService: FirebaseDatabase
) : CompanyDataSource {
    override val companiesRef: DatabaseReference
        get() {
            val currentUserId = authRepository.getCurrentUserId()
            return databaseService.getReference(USERS_REF)
                .child(currentUserId!!)
                .child(COMPANIES_REF)
        }

            override fun generateNewCompanyId(): String? {
                return companiesRef.push().key
            }

            override fun addCompany(companyEntity: CompanyEntity) {
                companiesRef.child(companyEntity.id!!).setValue(companyEntity) { databaseError, _ ->
                    if (databaseError != null) {
                        Log.e(
                            TAG,
                            "Failed to add Company (${companyEntity.id}) to Firebase: $databaseError"
                        )
                    }
                }
            }

            override fun deleteCompany(companyEntity: CompanyEntity) {
                companiesRef.child(companyEntity.id!!).removeValue { databaseError, _ ->
                    if (databaseError != null) {
                        Log.e(
                            TAG,
                            "Failed to delete Company (${companyEntity.id}) to Firebase: $databaseError"
                        )
                    }
                }
            }

            override fun saveCompanies(companyEntitiesMap: Map<String?, CompanyEntity>) {
                companiesRef.updateChildren(companyEntitiesMap) { databaseError, _ ->
                    if (databaseError != null) {
                        Log.e(TAG, "Failed to save Companies: $databaseError")
                    }
                }
            }

            override fun addChildEventListener(childEventListener: ChildEventListener) {
                companiesRef.addChildEventListener(childEventListener)
            }

            override fun addChildEventListener(orderBy: String, childEventListener: ChildEventListener) {
                companiesRef.orderByChild(orderBy).addChildEventListener(childEventListener)
            }

            companion object {
            private val TAG = BuildConfig.TAG + CompanyDataSourceImpl::class.java.simpleName
            private const val COMPANIES_REF = "companies"
        }
}