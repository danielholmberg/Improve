package dev.danielholmberg.improve.clean.feature_note.presentation.note_details

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.Scope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.api.services.drive.DriveScopes
import dev.danielholmberg.improve.BuildConfig
import dev.danielholmberg.improve.R
import dev.danielholmberg.improve.clean.Improve.Companion.instance
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
import dev.danielholmberg.improve.clean.feature_note.presentation.util.ImageCallback
import java.util.*

class NoteDetailsDialogFragment : DialogFragment() {

    private lateinit var viewModel: NoteDetailsViewModel

    private var validator: NoteInputValidator? = null

    private lateinit var toolbar: Toolbar
    private lateinit var inputTitleLayout: TextInputLayout
    private lateinit var inputInfoLayout: TextInputLayout
    private lateinit var inputTitle: EditText
    private lateinit var inputInfo: EditText
    private lateinit var tagsFooterListLayout: FlexboxLayout
    private lateinit var imagesRecyclerView: RecyclerView

    private var newTagColor: String = Tag.DEFAULT_COLOR.toHex()
    private lateinit var noTagColorBtn: ImageButton

    private var exportDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Create ViewModel with UseCases and inject necessary repositories
        val noteRepository: NoteRepository = instance!!.noteRepository
        val tagRepository: TagRepository = instance!!.tagRepository
        val imageRepository: ImageRepository = instance!!.imageRepository
        viewModel = NoteDetailsViewModel(
            noteDetailsUseCases = NoteDetailsUseCases(
                generateNewNoteUseCase = GenerateNewNoteUseCase(
                    noteRepository = noteRepository
                ),
                addNoteUseCase = AddNoteUseCase(
                    noteRepository = noteRepository
                ),
                updateNoteUseCase = UpdateNoteUseCase(
                    noteRepository = noteRepository
                ),
                deleteNoteUseCase = DeleteNoteUseCase(
                    noteRepository = noteRepository
                ),
                deleteNoteFromArchiveUseCase = DeleteNoteFromArchiveUseCase(
                    noteRepository = noteRepository
                ),
                updateArchivedNoteUseCase = UpdateArchivedNoteUseCase(
                    noteRepository = noteRepository
                ),
                archiveNoteUseCase = ArchiveNoteUseCase(
                    noteRepository = noteRepository
                ),
                unarchiveNoteUseCase = UnarchiveNoteUseCase(
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

        val success = viewModel.parseIncomingBundle(arguments)
        if (!success) {
            Toast.makeText(
                context, "Failed to show Note details, please try again",
                Toast.LENGTH_SHORT
            ).show()
            dismissDialog()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_details, container)
        toolbar = view.findViewById<View>(R.id.toolbar_note_details_fragment) as Toolbar

        // Set transparent background and no title to enable corner radius.
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_note_details)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setOnDismissListener {
            if (viewModel.editMode) showDiscardChangesDialog() else dismissDialog()
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "Menu is created")
        buildMenu()
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        buildMenu()
        toolbar.setNavigationIcon(R.drawable.ic_menu_close_primary)
        toolbar.setNavigationOnClickListener { if (viewModel.editMode) showDiscardChangesDialog() else dismissDialog() }
        toolbar.setOnMenuItemClickListener { item ->
            setOnMenuItemClickListener(item)
            true
        }

        inputTitleLayout = view.findViewById<View>(R.id.input_title_layout) as TextInputLayout
        inputInfoLayout = view.findViewById<View>(R.id.input_info_layout) as TextInputLayout
        inputTitle = inputTitleLayout.findViewById<View>(R.id.input_title) as EditText
        inputInfo = inputInfoLayout.findViewById<View>(R.id.input_info) as EditText

        // VIP Views
        imagesRecyclerView = view.findViewById<View>(R.id.images_list) as RecyclerView
        tagsFooterListLayout = view.findViewById<View>(R.id.footer_note_tags_list) as FlexboxLayout
        validator = NoteInputValidator(context!!, inputTitleLayout)

        val success = viewModel.initializeNoteDetails()
        if (success) {
            viewModel.populateNoteDetails(inputTitle, inputInfo)

            inputInfo.visibility = viewModel.getInputVisibility()
            imagesRecyclerView.adapter = viewModel.imagesAdapter
            imagesRecyclerView.visibility = viewModel.imagesViewVisibility
            imagesRecyclerView.layoutManager = LinearLayoutManager(
                instance,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            viewModel.renderExistingTagList(tagsFooterListLayout)
            toggleUI(viewModel.editMode)
        } else {
            Toast.makeText(context, "Unable to show Note details", Toast.LENGTH_SHORT).show()
            dismissDialog()
        }
    }

    private fun setOnMenuItemClickListener(item: MenuItem) {
        if (viewModel.editMode) {
            when (item.itemId) {
                R.id.addTag -> {
                    showAddNewTagDialog()
                }
                R.id.starNote -> {
                    viewModel.toggleStaredNote()
                    buildMenu()
                }
                R.id.noteDone -> {
                    if (validator!!.formIsValid()) {
                        updateNote()
                    }
                }
                R.id.addImage -> {
                    chooseImage()
                }
            }
        } else {
            when (item.itemId) {
                R.id.noteInfo -> {
                    showInfoDialog()
                }
                R.id.noteUnarchive -> {
                    viewModel.unarchiveNote()
                    dismissDialog()
                }
                R.id.noteArchive -> {
                    showArchiveDialog()
                }
                R.id.noteDelete -> {
                    showDeleteNoteDialog()
                }
                R.id.noteExport -> {
                    checkDrivePermission()
                }
                R.id.noteEdit -> {
                    viewModel.toggleEditMode()
                    buildMenu()
                    toggleUI(viewModel.editMode)
                }
            }
        }
    }

    private fun buildMenu() {
        toolbar.menu.clear()
        val menuTitle = toolbar.findViewById<TextView>(R.id.toolbar_note_dialog_title_tv)

        if (viewModel.editMode) {
            toolbar.inflateMenu(R.menu.menu_edit_note)
            menuTitle.setText(R.string.title_edit_note)

            if (viewModel.isNoteStared()) {
                toolbar.menu.findItem(R.id.starNote).setIcon(R.drawable.ic_star_enabled_accent)
                toolbar.menu.findItem(R.id.starNote).setTitle(R.string.menu_note_star_disable)
            } else {
                toolbar.menu.findItem(R.id.starNote).setIcon(R.drawable.ic_star_disabled_accent)
                toolbar.menu.findItem(R.id.starNote).setTitle(R.string.menu_note_star_enable)
            }

            // Show Menu-group with VIP features.
            toolbar.menu.setGroupEnabled(R.id.vipMenuGroup, instance!!.isVipUser)
            toolbar.menu.setGroupVisible(R.id.vipMenuGroup, instance!!.isVipUser)
        } else {
            toolbar.inflateMenu(R.menu.fragment_note_details_show)
            menuTitle.setText(R.string.note_activity_details)

            if (viewModel.isNoteArchived()) {
                toolbar.menu.findItem(R.id.noteUnarchive).isVisible = true
                toolbar.menu.findItem(R.id.noteArchive).isVisible = false
            } else {
                toolbar.menu.findItem(R.id.noteArchive).isVisible = true
                toolbar.menu.findItem(R.id.noteUnarchive).isVisible = false
            }
        }
    }

    private fun checkDrivePermission() {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(instance),
                Scope(DriveScopes.DRIVE_FILE)
            )
        ) {
            GoogleSignIn.requestPermissions(
                (instance!!.currentFragment)!!,
                REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION,
                GoogleSignIn.getLastSignedInAccount(instance), Scope(DriveScopes.DRIVE_FILE)
            )
        } else {
            exportNoteToDrive()
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(context!!)
            .setMessage(R.string.dialog_discard_changes_msg)
            .setPositiveButton("Discard") { dialogInterface, _ ->
                viewModel.toggleEditMode()

                viewModel.discardNoteChanges()
                viewModel.populateNoteDetails(inputTitle, inputInfo)

                buildMenu()
                toggleUI(viewModel.editMode)
                viewModel.renderOldTagList(tagsFooterListLayout)

                dialogInterface.dismiss()
            }
            .setNegativeButton("Keep editing") { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
            .show()
    }

    /**
     * Displays a dialog window to the user in order to choose a Tag color and enter a new Tag label.
     * Adds the new Tag to Firebase.
     */
    private fun showAddNewTagDialog() {
        val addDialogView = layoutInflater.inflate(R.layout.dialog_add_tag, null, false)
        val tagInputContainer = addDialogView.findViewById<View>(R.id.tag_input_container) as RelativeLayout
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

        viewModel.setCurrentNoteForTagsAdapter()

        val tagsDialog = AlertDialog.Builder(context!!)
            .setTitle(R.string.menu_tag_add)
            .setView(addDialogView)
            .setCancelable(false)
            .setNeutralButton("Edit Tags", null)
            .setPositiveButton("Done") { dialogInterface, _ ->
                viewModel.onEditTagsDone()
                viewModel.renderExistingTagList(tagsFooterListLayout)
                dialogInterface.dismiss()
            }
            .create()
            .also { it.show() }

        tagsDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val onEditListener: View.OnClickListener = this
                    viewModel.onEditTags()
                    viewModel.renderExistingTagList(tagsFooterListLayout)

                    tagInputContainer.visibility = View.GONE
                    tagsDialog.getButton(AlertDialog.BUTTON_NEUTRAL).text = "Stop Edit"
                    tagsDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        viewModel.onEditTagsDone()
                        viewModel.renderExistingTagList(tagsFooterListLayout)

                        tagInputContainer.visibility = View.VISIBLE
                        tagsDialog.getButton(AlertDialog.BUTTON_NEUTRAL).text = "Edit Tags"
                        tagsDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(onEditListener)
                    }
                }
            })

        tagsDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

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

                viewModel.createTag(newTag)
                viewModel.renderExistingTagList(tagsFooterListLayout)

                // Reset UI
                if (labelEditText.text != null) labelEditText.text!!.clear()
                newTagColor = Tag.DEFAULT_COLOR.toHex()
                uncheckAllOtherTags(tagViews)

                viewModel.scrollToPosition(layoutManager)
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

    /**
     * Changes the NoteDetails layout depending on the incoming editMode.
     * @param editMode - True; Edit, False; Show
     */
    private fun toggleUI(editMode: Boolean) {
        inputTitleLayout.isCounterEnabled = editMode
        inputTitle.isEnabled = editMode
        inputInfo.isEnabled = editMode

        inputInfo.visibility = viewModel.getInputVisibility()

        if (editMode) {
            viewModel.onEditImages()
            inputTitle.requestFocus()
            dialog?.setCancelable(false)
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        } else {
            viewModel.onEditImagesDone()
            dialog?.setCancelable(true)
            activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        }
    }

    private fun showInfoDialog() {
        val noteInfoLayout =LayoutInflater.from(context).inflate(R.layout.dialog_note_info, null) as RelativeLayout

        viewModel.populateNoteInfo(noteInfoLayout)

        AlertDialog.Builder(context!!)
            .setTitle(R.string.dialog_info_note_title)
            .setIcon(R.drawable.ic_menu_info_primary)
            .setView(noteInfoLayout)
            .setPositiveButton("OK"
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
            .show()
    }

    /**
     * Creates a new file via the Drive REST API.
     */
    private fun exportNoteToDrive() {
        Log.d(TAG, "Creating a file.")
        exportDialog = ProgressDialog.show(
            context, "Exporting Note to Google Drive",
            "In progress...", true
        ).also { it.show() }

        viewModel.exportNoteToGoogleDrive(onSuccess = {
            Log.d(TAG, "Created file")
            exportDialog?.cancel()
            dismissDialog()
            Toast.makeText(instance, "Note exported", Toast.LENGTH_LONG).show()
        }, onFailure = { error ->
            Log.e(TAG, "Couldn't create file: $error}")
            exportDialog?.cancel()
            Toast.makeText(instance, "Failed to export Note", Toast.LENGTH_LONG).show()
        })
    }

    private fun showArchiveDialog() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.dialog_archive_note_title)
            .setMessage(R.string.dialog_archive_note_msg)
            .setPositiveButton("Yes") { _, _ ->
                viewModel.archiveNote()
                dismissDialog()
            }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
            .show()
    }

    private fun showDeleteNoteDialog() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.dialog_delete_note_title)
            .setMessage(R.string.dialog_delete_note_msg)
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteNote()
                dismissDialog()
            }
            .setNegativeButton("No") { dialogInterface, _ -> dialogInterface.dismiss() }
            .create()
            .show()
    }

    private fun updateNote() {
        val newTitle = inputTitle.text.toString()
        val newInfo = inputInfo.text.ifBlank { "" }.toString()

        // The VIP user has added a image to the current Note.
        val imagesToUpload: ArrayList<Image> = viewModel.imagesToUpload()

        if (imagesToUpload.isNotEmpty()) {
            uploadImagesFirstThenAdd(title = newTitle, info = newInfo, imagesToUpload)
        } else {
            viewModel.updateNote(newTitle, newInfo)
        }

        viewModel.toggleEditMode()
        viewModel.populateNoteDetails(inputTitle, inputInfo)

        buildMenu()
        toggleUI(viewModel.editMode)
    }

    private fun uploadImagesFirstThenAdd(title: String, info: String, imagesToUpload: ArrayList<Image>) {
        val progressDialogText = "Saving attached image(s)"
        val progressDialog = ProgressDialog(instance!!.mainActivityRef)
        progressDialog.setTitle(progressDialogText)
        progressDialog.show()
        progressDialog.setMessage("Uploading " + imagesToUpload.size + " image(s)...")

        viewModel.uploadMultipleImages(imagesToUpload, object : ImageCallback {
            override fun onSuccess(`object`: Any) {
                Log.d(TAG, "Last image uploaded successfully!")
                val uploadedImages = `object` as ArrayList<Image>
                viewModel.onImagesUploaded(uploadedImages)
                viewModel.updateNote(title, info)
                progressDialog.dismiss()
            }

            override fun onFailure(errorMessage: String?) {
                progressDialog.dismiss()
                Toast.makeText(instance, "Failed to upload images!", Toast.LENGTH_LONG).show()
            }

            override fun onProgress(progress: Int) {}
        })
    }

    private fun dismissDialog() {
        dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION) {
                exportNoteToDrive()
            }
            if (requestCode == PICK_IMAGE_REQUEST) {
                if (data!!.clipData != null) {
                    handleSelectedMultipleImages(data)
                } else if (data.data != null) {
                    handleImageSelected(data.data.toString())
                }
            }
        }
    }

    private fun handleSelectedMultipleImages(data: Intent) {
        val numberOfImagesSelected = data.clipData!!.itemCount
        Log.d(TAG, "Multiple ($numberOfImagesSelected) images selected.")
        for (i in 0 until numberOfImagesSelected) {
            val filePath = data.clipData!!.getItemAt(i).toString()
            handleImageSelected(filePath)
        }
    }

    private fun handleImageSelected(filePath: String) {
        viewModel.addImageToAdapter(filePath)
        toggleUI(viewModel.editMode)
    }

    companion object {
        val TAG: String = BuildConfig.TAG + NoteDetailsDialogFragment::class.java.simpleName
        private const val PICK_IMAGE_REQUEST = 1
        private const val REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION = 999
        fun newInstance(): NoteDetailsDialogFragment {
            return NoteDetailsDialogFragment()
        }
    }
}