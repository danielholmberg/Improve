package dev.danielholmberg.improve.data.drive

import android.accounts.Account
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import dev.danielholmberg.improve.domain.model.Company
import dev.danielholmberg.improve.domain.model.Contact
import dev.danielholmberg.improve.domain.model.Note
import dev.danielholmberg.improve.domain.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DriveDataSource @Inject constructor(
    private val driveServiceFactory: DriveServiceFactory
) {

    suspend fun exportNotesAsCsv(notes: List<Note>, tags: Map<String, Tag>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val csv = buildString {
                    appendLine("Id,Title,Info,Starred,Archived,Added,Updated,Tags")
                    notes.forEach { note ->
                        val tagLabels = note.tags.keys
                            .mapNotNull { tagId -> tags[tagId]?.label }
                            .joinToString(";")
                        val escapedTitle = note.title.replace("\"", "\"\"")
                        val escapedInfo = note.info.replace("\"", "\"\"")
                        appendLine(
                            "${note.id},\"$escapedTitle\",\"$escapedInfo\"," +
                                "${note.isStarred},${note.isArchived}," +
                                "${note.added},${note.updated},\"$tagLabels\""
                        )
                    }
                }

                val driveService = driveServiceFactory.create()
                val metadata = File().apply {
                    name = "Improve_Notes_Export.csv"
                    mimeType = "text/plain"
                }
                val content = ByteArrayContent.fromString("text/plain", csv)
                val uploadedFile = driveService.files().create(metadata, content)
                    .setFields("id, name, webViewLink")
                    .execute()

                Result.success(uploadedFile.webViewLink ?: uploadedFile.id ?: "")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun exportContactAsVCard(contact: Contact, company: Company?): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val vcard = buildString {
                    appendLine("BEGIN:VCARD")
                    appendLine("VERSION:3.0")
                    appendLine("FN:${contact.name}")
                    appendLine("N:${contact.name};;;;")
                    if (contact.email.isNotEmpty()) {
                        appendLine("EMAIL;TYPE=INTERNET:${contact.email}")
                    }
                    if (contact.phone.isNotEmpty()) {
                        appendLine("TEL;TYPE=CELL:${contact.phone}")
                    }
                    if (company != null && company.name.isNotEmpty()) {
                        appendLine("ORG:${company.name}")
                    }
                    if (contact.comment.isNotEmpty()) {
                        appendLine("NOTE:${contact.comment}")
                    }
                    appendLine("END:VCARD")
                }

                val driveService = driveServiceFactory.create()
                val safeName = contact.name.replace(Regex("[^a-zA-Z0-9_\\- ]"), "_")
                val metadata = File().apply {
                    name = "$safeName.vcf"
                    mimeType = "text/vcard"
                }
                val content = ByteArrayContent.fromString("text/vcard", vcard)
                val uploadedFile = driveService.files().create(metadata, content)
                    .setFields("id, name, webViewLink")
                    .execute()

                Result.success(uploadedFile.webViewLink ?: uploadedFile.id ?: "")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
