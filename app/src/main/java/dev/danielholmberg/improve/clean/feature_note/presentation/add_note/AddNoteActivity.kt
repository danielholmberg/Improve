package dev.danielholmberg.improve.clean.feature_note.presentation.add_note

import androidx.appcompat.app.AppCompatActivity
import dev.danielholmberg.improve.clean.feature_note.domain.util.NoteInputValidator
import android.widget.ImageButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.flexbox.FlexboxLayout
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import dev.danielholmberg.improve.R
import com.google.android.material.textfield.TextInputLayout
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.JustifyContent
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import android.text.TextWatcher
import android.text.Editable
import android.app.ProgressDialog
import android.widget.Toast
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.MainActivity
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.domain.model.Note
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository
import dev.danielholmberg.improve.clean.feature_note.domain.repository.TagRepository
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.TagViewHolder
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ImagesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.TagsAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import java.util.*
import kotlin.collections.ArrayList

class AddNoteActivity : AppCompatActivity() {

    private lateinit var noteRepository: NoteRepository
    private lateinit var tagRepository: TagRepository

    private var context: Context? = null
    private var validator: NoteInputValidator? = null
    private var newNote: Note? = null
    private var newTagColor: String? = null
    private var noTagColor: ImageButton? = null
    private var tagColor1: ImageButton? = null
    private var tagColor2: ImageButton? = null
    private var tagColor3: ImageButton? = null
    private var tagColor4: ImageButton? = null
    private var tagColor5: ImageButton? = null
    private var tagColor6: ImageButton? = null
    private var tagColor7: ImageButton? = null
    private var tagColor8: ImageButton? = null
    private var newTags: ArrayList<String>? = null
    private var inputTitle: TextInputEditText? = null
    private var inputInfo: TextInputEditText? = null
    private var tagsList: FlexboxLayout? = null
    private var toolbar: Toolbar? = null
    private var tagsAdapter: TagsAdapter? = null

    // VIP
    private var imagesRecyclerView: RecyclerView? = null
    private var imagesAdapter: ImagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)
        noteRepository = instance!!.noteRepository
        tagRepository = instance!!.tagRepository
        context = this
        initActivity()
    }

    private fun initActivity() {
        toolbar = findViewById<View>(R.id.toolbar_add_note) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val inputTitleLayout = findViewById<View>(R.id.input_title_layout) as TextInputLayout
        tagsList = findViewById<View>(R.id.footer_note_tags_list) as FlexboxLayout
        inputTitle = findViewById<View>(R.id.input_title) as TextInputEditText
        inputInfo = findViewById<View>(R.id.input_info) as TextInputEditText
        inputTitle!!.requestFocus()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        validator = NoteInputValidator(this, inputTitleLayout)
        tagsAdapter = instance!!.tagsAdapter
        newTagColor = "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColorNull))
        val id = noteRepository.generateNewNoteId()
        newNote = Note(id)
        newTags = ArrayList()
        if (instance!!.isVipUser) {
            imagesRecyclerView = findViewById<View>(R.id.images_list) as RecyclerView
            imagesAdapter = ImagesAdapter(newNote!!.id!!, false)
            imagesRecyclerView!!.adapter = imagesAdapter
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            imagesRecyclerView!!.layoutManager = layoutManager
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_note, menu)

        // Show Menu-group with VIP features.
        menu.setGroupEnabled(R.id.vipMenuGroup, instance!!.isVipUser)
        menu.setGroupVisible(R.id.vipMenuGroup, instance!!.isVipUser)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showDiscardChangesDialog()
                return true
            }
            R.id.addTag -> {
                showAddNewTagDialog()
                return true
            }
            R.id.noteDone -> {
                if (validator!!.formIsValid()) {
                    addNote()
                }
                return true
            }
            R.id.starNote -> {
                newNote!!.toggleIsStared()
                if (newNote!!.isStared) {
                    item.setIcon(R.drawable.ic_star_enabled_accent)
                    item.setTitle(R.string.menu_note_star_disable)
                } else {
                    item.setIcon(R.drawable.ic_star_disabled_accent)
                    item.setTitle(R.string.menu_note_star_enable)
                }
                return true
            }
            R.id.vipAddImage -> {
                // *** VIP feature *** //
                chooseImage()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    private fun showDiscardChangesDialog() {
        val alertDialogBuilder = AlertDialog.Builder(
            (context)!!
        )
            .setMessage(R.string.dialog_discard_changes_msg)
            .setPositiveButton("Discard") { dialogInterface, _ ->
                dialogInterface.dismiss()
                onBackPressed()
            }
            .setNegativeButton("Keep editing") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    /**
     * Displays a dialog window to the user in order to choose a Tag color and enter a new Tag label.
     * Adds the new Tag to Firebase.
     */
    private fun showAddNewTagDialog() {
        val addDialogView = layoutInflater.inflate(R.layout.dialog_add_tag, null, false)
        val labelEditText = addDialogView.findViewById<View>(R.id.tag_label_et) as TextInputEditText
        val createTagButton = addDialogView.findViewById<View>(R.id.create_tag_btn) as ImageButton
        val existingTagsListView =
            addDialogView.findViewById<View>(R.id.existing_tags_list) as RecyclerView
        val labelCounterCurrent =
            addDialogView.findViewById<View>(R.id.tag_label_counter_current) as TextView
        noTagColor = addDialogView.findViewById<View>(R.id.tag_no_color) as ImageButton
        tagColor1 = addDialogView.findViewById<View>(R.id.tag_color_1) as ImageButton
        tagColor2 = addDialogView.findViewById<View>(R.id.tag_color_2) as ImageButton
        tagColor3 = addDialogView.findViewById<View>(R.id.tag_color_3) as ImageButton
        tagColor4 = addDialogView.findViewById<View>(R.id.tag_color_4) as ImageButton
        tagColor5 = addDialogView.findViewById<View>(R.id.tag_color_5) as ImageButton
        tagColor6 = addDialogView.findViewById<View>(R.id.tag_color_6) as ImageButton
        tagColor7 = addDialogView.findViewById<View>(R.id.tag_color_7) as ImageButton
        tagColor8 = addDialogView.findViewById<View>(R.id.tag_color_8) as ImageButton
        existingTagsListView.setHasFixedSize(false)
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.alignItems = AlignItems.STRETCH
        layoutManager.flexWrap = FlexWrap.WRAP
        existingTagsListView.layoutManager = layoutManager
        existingTagsListView.adapter = instance!!.tagsAdapter
        labelCounterCurrent.text = "0"
        labelEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                labelCounterCurrent.text = charSequence.length.toString()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        tagsAdapter!!.setCurrentNote(newNote)
        val addNewTagDialog = AlertDialog.Builder(
            (context)!!
        )
            .setTitle(R.string.menu_tag_add)
            .setView(addDialogView)
            .setCancelable(false)
            .setNeutralButton("Edit Tags", null)
            .setPositiveButton("Done") { dialogInterface, _ ->
                tagsAdapter!!.removeCurrentNote()
                tagsAdapter!!.setEditMode(false)
                renderTagList()
                dialogInterface.dismiss()
            }
            .create()
        addNewTagDialog.show()
        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val listener: View.OnClickListener = this
                    tagsAdapter!!.setEditMode(true)
                    renderTagList()
                    addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).text = "Stop Edit"
                    addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        tagsAdapter!!.setEditMode(false)
                        renderTagList()
                        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).text = "Edit Tags"
                        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                            .setOnClickListener(listener)
                    }
                }
            })
        createTagButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (TextUtils.isEmpty(labelEditText.text)) {
                    labelEditText.error = getString(R.string.err_msg_tag_label)
                    return
                }
                val newTagId = tagRepository.generateNewTagId()
                val newTag = Tag(newTagId)
                newTag.label = labelEditText.text.toString()
                newTag.color = newTagColor
                if ((newTagColor == "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColorNull)))) {
                    newTag.textColor =
                        "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagTextColorNull))
                } else {
                    newTag.textColor =
                        "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagTextColor))
                }
                tagRepository.addTag(newTag)
                tagsAdapter!!.addTag(newTag)
                newNote!!.addTag(newTag.id)
                renderTagList()

                // Reset
                if (labelEditText.text != null) labelEditText.text!!.clear()
                newTagColor = "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColorNull))
                uncheckAllOtherTags()
                layoutManager.scrollToPosition(tagsAdapter!!.itemCount - 1)
            }
        })
        noTagColor!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColorNull))
            uncheckAllOtherTags()
        }
        tagColor1!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor1))
            tagColor1!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_1_checked)
            uncheckAllOtherTags()
        }
        tagColor2!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor2))
            tagColor2!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_2_checked)
            uncheckAllOtherTags()
        }
        tagColor3!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor3))
            tagColor3!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_3_checked)
            uncheckAllOtherTags()
        }
        tagColor4!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor4))
            tagColor4!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_4_checked)
            uncheckAllOtherTags()
        }
        tagColor5!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor5))
            tagColor5!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_5_checked)
            uncheckAllOtherTags()
        }
        tagColor6!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor6))
            tagColor6!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_6_checked)
            uncheckAllOtherTags()
        }
        tagColor7!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor7))
            tagColor7!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_7_checked)
            uncheckAllOtherTags()
        }
        tagColor8!!.setOnClickListener {
            newTagColor =
                "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor8))
            tagColor8!!.background =
                ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_8_checked)
            uncheckAllOtherTags()
        }
    }

    private fun uncheckAllOtherTags() {
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor1))) {
            tagColor1!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_1)
        }
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor2))) {
            tagColor2!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_2)
        }
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor3))) {
            tagColor3!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_3)
        }
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor4))) {
            tagColor4!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_4)
        }
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor5))) {
            tagColor5!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_5)
        }
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor6))) {
            tagColor6!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_6)
        }
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor7))) {
            tagColor7!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_7)
        }
        if (newTagColor != "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColor8))) {
            tagColor8!!.background = ContextCompat.getDrawable(instance!!, R.drawable.ic_tag_8)
        }
    }

    private fun renderTagList() {
        tagsList!!.removeAllViews()
        for (tagId: String? in newNote!!.tags.keys) {
            val tagView = LayoutInflater.from(context).inflate(R.layout.item_tag, tagsList, false)
            val tagViewHolder = TagViewHolder(tagView)
            tagViewHolder.bindModelToView(instance!!.tagsAdapter!!.getTag(tagId))
            tagsList!!.addView(tagView)
        }
    }

    private fun addNote() {
        val title = if (inputTitle!!.text == null) "" else inputTitle!!.text.toString()
        var info: String = if (inputInfo!!.text == null) "" else inputInfo!!.text.toString()
        val timestampAdded = System.currentTimeMillis().toString()
        if (TextUtils.isEmpty(info)) {
            info = ""
        }
        newNote!!.title = title
        newNote!!.info = info
        newNote!!.isArchived = false
        newNote!!.addedAt = timestampAdded
        newNote!!.updatedAt = timestampAdded
        if (instance!!.isVipUser && imagesAdapter!!.itemCount > 0) {
            // Wait on adding new note and returning to parent activity
            // until all image uploads has been successfully completed.
            uploadImages()
        } else {
            noteRepository!!.addNote((newNote)!!)
            showParentActivity()
        }
    }

    private fun uploadImages() {
        Log.d(TAG, "Uploading ${imagesAdapter!!.itemCount} image(s)")
        val progressDialogText = "Saving attached image(s)"
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle(progressDialogText)
        progressDialog.show()
        progressDialog.setMessage("Uploading ${imagesAdapter!!.itemCount} image(s)...")

        // TODO: Should be moved to UseCase

        instance!!.imageRepository
            ?.uploadMultipleImages(imagesAdapter!!.imageList, object : ImageCallback {
                override fun onSuccess(`object`: Any) {
                    Log.d(TAG, "Last image uploaded successfully!")
                    val uploadedImages = `object` as ArrayList<Image>
                    for (image: Image in uploadedImages) {
                        newNote!!.addImage(image.id)
                    }
                    progressDialog.dismiss()
                    noteRepository.addNote((newNote)!!)
                    showParentActivity()
                }

                override fun onFailure(errorMessage: String?) {
                    progressDialog.dismiss()
                    Toast.makeText(instance, "Failed to upload images!", Toast.LENGTH_LONG).show()
                }

                override fun onProgress(progress: Int) {}
            })
    }

    private fun showParentActivity() {
        restUI()
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    private fun restUI() {
        if (inputTitle!!.text != null) inputTitle!!.text!!.clear()
        if (inputInfo!!.text != null) inputInfo!!.text!!.clear()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data!!.clipData != null) {
                // User selected multiple images.
                val numberOfImagesSelected = data.clipData!!.itemCount
                Log.d(TAG, "Multiple ($numberOfImagesSelected) images selected.")
                for (i in 0 until numberOfImagesSelected) {
                    val originalFilePath = data.clipData!!.getItemAt(i).uri.toString()
                    val imageId = UUID.randomUUID().toString()
                    val image = Image(imageId)
                    image.originalFilePath = originalFilePath
                    imagesAdapter!!.add(image)
                    imagesAdapter!!.setEditMode(true)
                    imagesRecyclerView!!.visibility = View.VISIBLE
                }
            } else if (data.data != null) {
                // User selected a single image.
                Log.d(TAG, "1 image selected.")
                val originalFilePath = data.data.toString()
                val imageId = UUID.randomUUID().toString()
                val image = Image(imageId)
                image.originalFilePath = originalFilePath
                imagesAdapter!!.add(image)
                imagesAdapter!!.setEditMode(true)
                imagesRecyclerView!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onBackPressed() {
        showParentActivity()
    }

    public override fun onStart() {
        super.onStart()
        // Re-add newly created (and therefore added) Tags from Note.
        for (tagId: String? in newTags!!) {
            newNote!!.addTag(tagId)
        }
    }

    public override fun onPause() {
        super.onPause()
        // Remove newly created (and therefore added) Tags from Note.
        for (tagId: String? in newTags!!) {
            newNote!!.removeTag(tagId)
        }
    }

    companion object {
        private val TAG = AddNoteActivity::class.java.simpleName
        private const val PICK_IMAGE_REQUEST = 1
    }
}