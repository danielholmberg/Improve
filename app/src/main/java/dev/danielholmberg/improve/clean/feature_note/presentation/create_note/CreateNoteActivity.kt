package dev.danielholmberg.improve.clean.feature_note.presentation.create_note

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
import dev.danielholmberg.improve.clean.MainActivity
import dev.danielholmberg.improve.clean.feature_note.domain.model.Image
import dev.danielholmberg.improve.clean.feature_note.domain.model.Tag
import dev.danielholmberg.improve.clean.feature_note.domain.repository.ImageRepository
import dev.danielholmberg.improve.clean.feature_note.domain.repository.NoteRepository
import dev.danielholmberg.improve.clean.feature_note.domain.repository.TagRepository
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.*
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.image.DownloadImageToLocalFileUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.image.UploadImagesUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.note.*
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.tag.AddTagUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.use_case.tag.GenerateNewTagUseCase
import dev.danielholmberg.improve.clean.feature_note.domain.util.NoteInputValidator
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.view_holder.TagViewHolder
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.ImagesAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.notes.adapter.TagsAdapter
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import java.util.*

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var viewModel: CreateNoteViewModel

    private lateinit var validator: NoteInputValidator

    private lateinit var inputTitle: TextInputEditText
    private lateinit var inputInfo: TextInputEditText
    private lateinit var tagsFooterListLayout: FlexboxLayout

    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var imagesAdapter: ImagesAdapter

    private var tagsAdapter: TagsAdapter? = null

    private var newTagColor: String = Tag.DEFAULT_COLOR.toHex()
    private lateinit var noTagColorBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_note)

        val toolbar = findViewById<View>(R.id.toolbar_add_note) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // 1. Create ViewModel with UseCases and inject necessary repositories
        val noteRepository: NoteRepository = instance!!.noteRepository
        val tagRepository: TagRepository = instance!!.tagRepository
        val imageRepository: ImageRepository = instance!!.imageRepository
        viewModel = CreateNoteViewModel(
            noteUseCases = NoteUseCases(
                generateNewNoteUseCase = GenerateNewNoteUseCase(
                    noteRepository = noteRepository
                ),
                addNoteUseCase = AddNoteUseCase(
                    noteRepository = noteRepository
                ),
                archiveNoteUseCase = ArchiveNoteUseCase(
                    noteRepository = noteRepository,
                ),
                unarchiveNoteUseCase = UnarchiveNoteUseCase(
                    noteRepository = noteRepository
                ),
                addArchivedNoteUseCase = AddArchivedNoteUseCase(
                    noteRepository = noteRepository
                ),
                addChildEventListenerUseCase = AddChildEventListenerUseCase(
                    noteRepository = noteRepository
                ),
                addChildEventListenerForArchiveUseCase = AddChildEventListenerForArchiveUseCase(
                    noteRepository = noteRepository
                ),
                updateArchivedNoteUseCase = UpdateArchivedNoteUseCase(
                    noteRepository = noteRepository
                )
            ),
            tagUseCases = TagUseCases(
                generateNewTagUseCase = GenerateNewTagUseCase(
                    tagRepository = tagRepository
                ),
                addTagUseCase = AddTagUseCase(
                    tagRepository = tagRepository
                )
            ),
            imageUseCases = ImageUseCases(
                uploadImagesUseCase = UploadImagesUseCase(
                    imageRepository = imageRepository
                ),
                downloadImageToLocalFileUseCase = DownloadImageToLocalFileUseCase(
                    imageRepository = imageRepository
                )
            )
        )

        initActivity()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(0) {
                handleOnBackNavigation()
            }
        }
    }

    private fun initActivity() {
        val inputTitleLayout = findViewById<View>(R.id.input_title_layout) as TextInputLayout
        tagsFooterListLayout = findViewById<View>(R.id.footer_note_tags_list) as FlexboxLayout
        inputTitle = findViewById<View>(R.id.input_title) as TextInputEditText
        inputInfo = findViewById<View>(R.id.input_info) as TextInputEditText
        inputTitle.requestFocus()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        validator = NoteInputValidator(this, inputTitleLayout)

        tagsAdapter = instance?.tagsAdapter

        viewModel.generateNewNote()

        if (instance!!.isVipUser) {
            imagesRecyclerView = findViewById<View>(R.id.images_list) as RecyclerView
            imagesAdapter = ImagesAdapter(false)
            imagesRecyclerView.adapter = imagesAdapter
            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            imagesRecyclerView.layoutManager = layoutManager
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
                if (validator.formIsValid()) {
                    addNote()
                }
                return true
            }
            R.id.starNote -> {
                viewModel.note.toggleIsStared()
                if (viewModel.note.isStared) {
                    item.setIcon(R.drawable.ic_star_enabled_accent)
                    item.setTitle(R.string.menu_note_star_disable)
                } else {
                    item.setIcon(R.drawable.ic_star_disabled_accent)
                    item.setTitle(R.string.menu_note_star_enable)
                }
                return true
            }
            R.id.addImage -> {
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
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setMessage(R.string.dialog_discard_changes_msg)
            .setPositiveButton("Discard") { dialogInterface, _ ->
                dialogInterface.dismiss()
                handleOnBackNavigation()
            }
            .setNegativeButton("Keep editing") { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    /**
     * Displays a dialog window to the user in order to choose a Tag color and enter a new Tag label.
     */
    private fun showAddNewTagDialog() {
        val addDialogView = layoutInflater.inflate(R.layout.dialog_add_tag, null, false)
        val labelEditText = addDialogView.findViewById<TextInputEditText>(R.id.tag_label_et)
        val createTagButton = addDialogView.findViewById<ImageButton>(R.id.create_tag_btn)
        val existingTagsListView = addDialogView.findViewById<RecyclerView>(R.id.existing_tags_list)
        val labelCounterCurrent = addDialogView.findViewById<TextView>(R.id.tag_label_counter_current)

        noTagColorBtn = addDialogView.findViewById(R.id.tag_no_color)

        val tagListLayout = addDialogView.findViewById<FlexboxLayout>(R.id.tag_color_list)
        val tagViews = tagListLayout.children
        tagViews.forEachIndexed { index, view ->
            if (index == 0) {
                noTagColorBtn.setOnClickListener {
                    newTagColor = Tag.DEFAULT_COLOR.toHex()
                    view.background = ContextCompat.getDrawable(instance!!, Tag.CLEAR_BACKGROUND)
                    uncheckAllOtherTags(tagViews)
                }
            } else {
                view.setOnClickListener {
                    newTagColor = Tag.tagColors[index-1].toHex()
                    view.background = ContextCompat.getDrawable(instance!!, Tag.tagCheckedBackgrounds[index-1])
                    uncheckAllOtherTags(tagViews)
                }
            }
        }

        existingTagsListView.setHasFixedSize(false)
        val layoutManager = FlexboxLayoutManager(instance)
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

        tagsAdapter!!.setCurrentNote(viewModel.note)

        val addNewTagDialog = AlertDialog.Builder(this)
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
                if (labelEditText.text.toString().isBlank()) {
                    labelEditText.error = getString(R.string.err_msg_tag_label)
                    return
                }

                val label = labelEditText.text.toString()
                val color = newTagColor

                val newTag = viewModel.generateNewTag(label, color)

                if (newTagColor == Tag.DEFAULT_COLOR.toHex()) {
                    newTag.textColor = Tag.DEFAULT_TEXT_COLOR.toHex()
                } else {
                    newTag.textColor = Tag.tagTextColor.toHex()
                }

                viewModel.addTag(newTag)
                tagsAdapter?.addTag(newTag)

                renderTagList()

                // Reset
                if (labelEditText.text != null) labelEditText.text!!.clear()
                newTagColor = Tag.DEFAULT_COLOR.toHex()
                uncheckAllOtherTags(tagViews)

                instance?.tagsAdapter?.let { layoutManager.scrollToPosition(it.itemCount - 1) }
            }
        })
    }

    private fun uncheckAllOtherTags(tagViews: Sequence<View>) {
        tagViews.forEachIndexed { index, view ->
            if (index != 0 && newTagColor != Tag.tagColors[index-1].toHex()) {
                view.background = ContextCompat.getDrawable(instance!!, Tag.tagUncheckedBackgrounds[index-1])
            }
        }
    }

    private fun renderTagList() {
        tagsFooterListLayout.removeAllViews()
        for (tagId: String? in viewModel.note.tags.keys) {
            val tagView = LayoutInflater.from(instance).inflate(R.layout.item_tag, tagsFooterListLayout, false)
            val tagViewHolder = TagViewHolder(tagView)
            tagViewHolder.bindModelToView(tagsAdapter?.getTag(tagId))
            tagsFooterListLayout.addView(tagView)
        }
    }

    private fun addNote() {
        val title = inputTitle.text.toString()
        val info = inputInfo.text.toString().ifBlank { "" }

        if (instance!!.isVipUser && imagesAdapter.itemCount > 0) {
            // Wait on adding new note and returning to parent activity
            // until all image uploads has been successfully completed.
            uploadImagesFirstThenAdd(title, info)
        } else {
            viewModel.addNote(title, info)
            showParentActivity()
        }
    }

    private fun uploadImagesFirstThenAdd(title: String, info: String) {
        Log.d(TAG, "Uploading ${imagesAdapter.itemCount} image(s)")
        val progressDialogText = "Saving attached image(s)"
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle(progressDialogText)
        progressDialog.show()
        progressDialog.setMessage("Uploading ${imagesAdapter.itemCount} image(s)...")

        viewModel.uploadMultipleImages(imagesAdapter.imageList, object : ImageCallback {
                override fun onSuccess(`object`: Any) {
                    Log.d(TAG, "Last image uploaded successfully!")
                    val uploadedImages = `object` as ArrayList<Image>
                    for (image: Image in uploadedImages) {
                        viewModel.note.addImage(image.id!!)
                    }
                    progressDialog.dismiss()

                    viewModel.addNote(title, info)
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
        resetUI()
        startActivity(Intent(this, MainActivity::class.java))
        finishAfterTransition()
    }

    private fun resetUI() {
        if (inputTitle.text != null) inputTitle.text!!.clear()
        if (inputInfo.text != null) inputInfo.text!!.clear()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data!!.clipData != null) {
                handleSelectedMultipleImages(data)
            } else if (data.data != null) {
                handleSelectedSingleImage(data)
            }
        }
    }

    private fun handleSelectedSingleImage(data: Intent) {
        Log.d(TAG, "1 image selected.")
        val originalFilePath = data.data.toString()
        val imageId = UUID.randomUUID().toString()
        val image = Image(imageId)
        image.originalFilePath = originalFilePath
        imagesAdapter.add(image)
        imagesAdapter.setEditMode(true)
        imagesRecyclerView.visibility = View.VISIBLE
    }

    private fun handleSelectedMultipleImages(data: Intent) {
        val numberOfImagesSelected = data.clipData!!.itemCount
        Log.d(TAG, "Multiple ($numberOfImagesSelected) images selected.")
        for (i in 0 until numberOfImagesSelected) {
            val originalFilePath = data.clipData!!.getItemAt(i).uri.toString()
            val imageId = UUID.randomUUID().toString()
            val image = Image(imageId)
            image.originalFilePath = originalFilePath
            imagesAdapter.add(image)
            imagesAdapter.setEditMode(true)
            imagesRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        handleOnBackNavigation()
    }

    private fun handleOnBackNavigation() {
        showParentActivity()
    }

    public override fun onStart() {
        super.onStart()
        // Re-add newly created (and therefore added) Tags from Note.
        for (tagId: String? in viewModel.tagIdList) {
            viewModel.note.addTag(tagId!!)
        }
    }

    public override fun onPause() {
        super.onPause()
        // Remove newly created (and therefore added) Tags from Note.
        for (tagId: String? in viewModel.tagIdList) {
            viewModel.note.removeTag(tagId!!)
        }
    }

    companion object {
        private val TAG = BuildConfig.TAG + CreateNoteActivity::class.java.simpleName
        private const val PICK_IMAGE_REQUEST = 1
    }
}