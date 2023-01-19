package dev.danielholmberg.improve.clean.feature_note.domain.model

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.Exclude

@IgnoreExtraProperties
class Image(var id: String?) {

    @get:Exclude
    @set:Exclude
    var originalFilePath: String? = null

}