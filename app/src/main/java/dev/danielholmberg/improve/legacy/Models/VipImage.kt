package dev.danielholmberg.improve.legacy.Models

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.Exclude

@IgnoreExtraProperties
class VipImage(var id: String?) {

    @get:Exclude
    @set:Exclude
    var originalFilePath: String? = null

}