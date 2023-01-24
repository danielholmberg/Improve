package dev.danielholmberg.improve.legacy.Fragments

import dev.danielholmberg.improve.Improve.Companion.instance
import dev.danielholmberg.improve.legacy.Managers.DatabaseManager
import dev.danielholmberg.improve.legacy.Services.DriveServiceHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.ProgressDialog
import android.widget.ImageButton
import dev.danielholmberg.improve.legacy.Utilities.NoteInputValidator
import com.google.android.material.textfield.TextInputLayout
import android.widget.EditText
import com.google.android.flexbox.FlexboxLayout
import androidx.recyclerview.widget.RecyclerView
import dev.danielholmberg.improve.legacy.Adapters.VipImagesAdapter
import dev.danielholmberg.improve.legacy.Models.VipImage
import android.os.Parcelable
import android.widget.Toast
import dev.danielholmberg.improve.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.services.drive.DriveScopes
import android.widget.TextView
import android.content.Intent
import android.widget.RelativeLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.JustifyContent
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexWrap
import android.text.TextWatcher
import android.text.Editable
import dev.danielholmberg.improve.legacy.ViewHolders.TagViewHolder
import dev.danielholmberg.improve.legacy.Callbacks.StorageCallback
import android.app.Activity
import android.text.TextUtils
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.gms.common.api.Scope
import dev.danielholmberg.improve.legacy.Models.Note
import dev.danielholmberg.improve.legacy.Models.Tag
import java.text.DateFormat
import java.util.*

class NoteDetailsDialogFragment : DialogFragment() {

    private var databaseManager: DatabaseManager? = null
    private var mDriveServiceHelper: DriveServiceHelper? = null
    private var activity: AppCompatActivity? = null
    private var toolbar: Toolbar? = null
    private var noteBundle: Bundle? = null
    private var originalNote: Note? = null
    private var isOriginallyStared = false
    private var parentFragment = 0
    private var exportDialog: ProgressDialog? = null
    private var editMode = false
    private var updatedNote: Note? = null
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
    private var newTags: ArrayList<String?>? = null
    private var oldTags: HashMap<String?, Boolean?>? = null
    private var validator: NoteInputValidator? = null
    private var inputTitleLayout: TextInputLayout? = null
    private var inputInfoLayout: TextInputLayout? = null
    private var inputTitle: EditText? = null
    private var inputInfo: EditText? = null
    private var tagsList: FlexboxLayout? = null
    private var noteId: String? = null
    private var noteTitle: String? = null
    private var noteInfo: String? = null
    private var noteTimestampAdded: String? = null
    private var noteTimestampUpdated: String? = null

    // VIP
    private var vipImagesRecyclerView: RecyclerView? = null
    private var vipImagesAdapter: VipImagesAdapter? = null
    private var currentImages: ArrayList<VipImage>? = null
    private var originalImages: ArrayList<VipImage>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseManager = instance!!.databaseManager
        activity = getActivity() as AppCompatActivity?
        mDriveServiceHelper = instance!!.driveServiceHelper
        noteBundle = arguments
        if (noteBundle != null) {
            parentFragment = noteBundle!!.getInt(NOTE_PARENT_FRAGMENT_KEY)
            originalNote = noteBundle!!.getParcelable<Parcelable>(NOTE_KEY) as Note?
        } else {
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
        isCancelable = false
        newTagColor = "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColorNull))


        // Set transparent background and no title to enable corner radius.
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawableResource(R.drawable.background_note_details)
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            dialog!!.setOnKeyListener { _, keyCode, _ ->
                if ((keyCode == KeyEvent.KEYCODE_BACK
                            ) && (dialog != null
                            ) && (fragmentManager != null)
                ) {
                    dialog!!.dismiss()
                    fragmentManager!!.popBackStackImmediate()
                }
                true
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Objects.requireNonNull(this.dialog!!.window)
            ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        toolbar = view.findViewById<View>(R.id.toolbar_note_details_fragment) as Toolbar
        createOptionsMenu()
        toolbar!!.setNavigationIcon(R.drawable.ic_menu_close_primary)
        toolbar!!.setOnMenuItemClickListener { item ->
            if (editMode) {
                when (item.itemId) {
                    R.id.addTag -> {
                        showAddNewTagDialog()
                        true
                    }
                    R.id.starNote -> {
                        originalNote!!.stared = !originalNote!!.isStared()
                        createOptionsMenu()
                        true
                    }
                    R.id.noteDone -> {
                        if (validator!!.formIsValid()) {
                            updateNote()
                        }
                        true
                    }
                    R.id.addImage -> {
                        chooseImage()
                        true
                    }
                    else -> true
                }
            } else {
                when (item.itemId) {
                    R.id.noteInfo -> {
                        showInfoDialog()
                        true
                    }
                    R.id.noteUnarchive -> {
                        unarchiveNote()
                        true
                    }
                    R.id.noteArchive -> {
                        showArchiveDialog()
                        true
                    }
                    R.id.noteDelete -> {
                        showDeleteNoteDialog()
                        true
                    }
                    R.id.noteExport -> {
                        checkDrivePermission()
                        true
                    }
                    R.id.noteEdit -> {
                        editMode = true
                        newTags = ArrayList()
                        createOptionsMenu()
                        toggleMode(editMode)
                        true
                    }
                    else -> true
                }
            }
        }
        inputTitleLayout = view.findViewById<View>(R.id.input_title_layout) as TextInputLayout
        inputInfoLayout = view.findViewById<View>(R.id.input_info_layout) as TextInputLayout
        inputTitle = inputTitleLayout!!.findViewById<View>(R.id.input_title) as EditText
        inputInfo = inputInfoLayout!!.findViewById<View>(R.id.input_info) as EditText

        // VIP Views
        vipImagesRecyclerView = view.findViewById<View>(R.id.images_list) as RecyclerView
        tagsList = view.findViewById<View>(R.id.footer_note_tags_list) as FlexboxLayout
        validator = NoteInputValidator((context)!!, (inputTitleLayout)!!)
        toggleMode(editMode)
        if (originalNote != null) {
            if (instance!!.isVipUser) {
                vipImagesAdapter = VipImagesAdapter((originalNote!!.id)!!, false)
                if (originalNote!!.hasImage()) {
                    originalImages = ArrayList()
                    for (vipImageId: String? in originalNote!!.vipImages) {
                        originalImages!!.add(VipImage(vipImageId))
                    }
                    currentImages = originalImages
                    vipImagesAdapter!!.addImages(originalImages!!)
                    Log.d(TAG, "Initial image count: " + originalImages!!.size)
                }
                vipImagesRecyclerView!!.adapter = vipImagesAdapter
                val layoutManager = LinearLayoutManager(instance, LinearLayoutManager.HORIZONTAL, false)
                vipImagesRecyclerView!!.layoutManager = layoutManager
            }
            populateNoteDetails()
            oldTags = HashMap(originalNote!!.getTags())
        } else {
            Toast.makeText(context, "Unable to show Note details", Toast.LENGTH_SHORT).show()
            dismissDialog()
        }
    }

    private fun checkDrivePermission() {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(instance),
                Scope(DriveScopes.DRIVE_FILE)
            )
        ) {
            GoogleSignIn.requestPermissions(
                (instance!!.notesFragmentRef)!!,
                REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION,
                GoogleSignIn.getLastSignedInAccount(instance), Scope(DriveScopes.DRIVE_FILE)
            )
        } else {
            exportNoteToDrive()
        }
    }

    override fun onResume() {
        super.onResume()
        Objects.requireNonNull(dialog!!.window)
            ?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
    }

    private fun createOptionsMenu() {
        Log.d(TAG, "Menu is redrawn")
//        val menuTitle =
//            (toolbar!!.findViewById<View>(R.id.toolbar_note_dialog_title_tv) as TextView)
        val menu = toolbar!!.menu
        menu.clear()

        // Check (if)Edit or (else)Show.
        if (editMode) {
//            menuTitle.setText(R.string.title_edit_note)
            toolbar!!.inflateMenu(R.menu.menu_edit_note)
            toolbar!!.setNavigationOnClickListener { showDiscardChangesDialog() }
            if (originalNote!!.isStared()) {
                menu.findItem(R.id.starNote).setIcon(R.drawable.ic_star_enabled_accent)
                menu.findItem(R.id.starNote).setTitle(R.string.menu_note_star_disable)
            } else {
                menu.findItem(R.id.starNote).setIcon(R.drawable.ic_star_disabled_accent)
                menu.findItem(R.id.starNote).setTitle(R.string.menu_note_star_enable)
            }

            // Show Menu-group with VIP features.
            menu.setGroupEnabled(R.id.vipMenuGroup, instance!!.isVipUser)
            menu.setGroupVisible(R.id.vipMenuGroup, instance!!.isVipUser)
        } else {
//            menuTitle.setText(R.string.note_activity_details)
            toolbar!!.inflateMenu(R.menu.fragment_note_details_show)
            toolbar!!.setNavigationOnClickListener { dismissDialog() }

            // Depending on the parent fragment.
            // Change MenuItem "Archive" and "Unarchive".
            if (parentFragment == R.integer.NOTES_FRAGMENT) {
                menu.findItem(R.id.noteArchive).isVisible = true
                menu.findItem(R.id.noteUnarchive).isVisible = false
            } else if (parentFragment == R.integer.ARCHIVED_NOTES_FRAGMENT) {
                menu.findItem(R.id.noteUnarchive).isVisible = true
                menu.findItem(R.id.noteArchive).isVisible = false
            }
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
                editMode = false
                toggleMode(editMode)
                originalNote!!.stared = isOriginallyStared
                if (originalNote!!.hasImage()) {
                    currentImages = originalImages
                    vipImagesAdapter = VipImagesAdapter((originalNote!!.id)!!, false)
                    vipImagesAdapter!!.addImages((currentImages)!!)
                    vipImagesRecyclerView!!.adapter = vipImagesAdapter
                }
                originalNote!!.setTags((oldTags)!!)
                createOptionsMenu()
                populateNoteDetails()
                dialogInterface.dismiss()
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
        val addDialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_add_tag, null, false)
        val tagInputContainer =
            addDialogView.findViewById<View>(R.id.tag_input_container) as RelativeLayout
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
        val tagsAdapter = instance!!.tagsAdapter
        existingTagsListView.adapter = tagsAdapter
        labelCounterCurrent.text = "0"
        labelEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                labelCounterCurrent.text = charSequence.length.toString()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        tagsAdapter!!.setCurrentNote(originalNote)
        val addNewTagDialog = AlertDialog.Builder(
            (context)!!
        )
            .setTitle(R.string.menu_tag_add)
            .setView(addDialogView)
            .setCancelable(false)
            .setNeutralButton("Edit Tags", null)
            .setPositiveButton("Done") { dialogInterface, _ ->
                tagsAdapter.removeCurrentNote()
                tagsAdapter.setEditMode(false)
                tagInputContainer.visibility = View.VISIBLE
                renderTagList()
                dialogInterface.dismiss()
            }
            .create()
        addNewTagDialog.show()
        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    val listener: View.OnClickListener = this
                    tagsAdapter.setEditMode(true)
                    tagInputContainer.visibility = View.GONE
                    renderTagList()
                    addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).text = "Stop Edit"
                    addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        tagsAdapter.setEditMode(false)
                        tagInputContainer.visibility = View.VISIBLE
                        renderTagList()
                        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL).text = "Edit Tags"
                        addNewTagDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
                            .setOnClickListener(listener)
                    }
                }
            })
        Objects.requireNonNull(addNewTagDialog.window)?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        createTagButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if (TextUtils.isEmpty(labelEditText.text)) {
                    labelEditText.error = getString(R.string.err_msg_tag_label)
                    return
                }
                val newTagId = databaseManager!!.tagRef.push().key
                val newTag = Tag(newTagId)
                newTag.setLabel(labelEditText.text.toString())
                newTag.color = newTagColor
                if ((newTagColor == "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColorNull)))) {
                    newTag.textColor =
                        "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagTextColorNull))
                } else {
                    newTag.textColor =
                        "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagTextColor))
                }
                databaseManager!!.addTag(newTag)
                tagsAdapter.addTag(newTag)
                originalNote!!.addTag(newTag.id)
                newTags!!.add(newTag.id)

                // Only update the List of Tags to keep all other edited information in check.
                renderTagList()

                // Reset
                if (labelEditText.text != null) labelEditText.text!!.clear()
                newTagColor = "#" + Integer.toHexString(ContextCompat.getColor(instance!!, R.color.tagColorNull))
                uncheckAllOtherTags()

                // Display the latest added Tag
                layoutManager.scrollToPosition(tagsAdapter.itemCount - 1)
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

    /**
     * Changes the NoteDetails layout depending on the incoming editMode.
     * @param editMode - True; Edit, False; Show
     */
    private fun toggleMode(editMode: Boolean) {
        inputTitleLayout!!.isCounterEnabled = editMode
        inputTitle!!.isEnabled = editMode
        inputInfo!!.isEnabled = editMode
        if (editMode) {
            isOriginallyStared = originalNote!!.isStared()
            if (vipImagesAdapter != null) {
                vipImagesAdapter!!.setEditMode(true)
            }
            inputInfo!!.visibility = View.VISIBLE
            inputTitle!!.requestFocus()
            dialog!!.setCancelable(false)
            activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        } else {
            if (vipImagesAdapter != null) {
                vipImagesAdapter!!.setEditMode(false)
            }
            dialog!!.setCancelable(true)
            activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            if (TextUtils.isEmpty(noteInfo)) {
                inputInfo!!.visibility = View.GONE
            }
        }
    }

    /**
     * Sets all the necessary detailed information about the Note.
     */
    private fun populateNoteDetails() {
        noteId = originalNote!!.id
        noteTitle = originalNote!!.title
        noteInfo = originalNote!!.info
        if (originalNote!!.added != null) {
            noteTimestampAdded = transformMillisToDateString(originalNote!!.added!!.toLong())
        }
        if (originalNote!!.updated != null) {
            noteTimestampUpdated = transformMillisToDateString(originalNote!!.updated!!.toLong())
        }
        if (originalNote!!.title != null) {
            inputTitle!!.setText(noteTitle)
        }
        if (originalNote!!.info != null) {
            inputInfo!!.setText(noteInfo)
        }
        if (TextUtils.isEmpty(noteInfo)) {
            inputInfo!!.visibility = View.GONE
        } else {
            inputInfo!!.visibility = View.VISIBLE
        }
        if (originalNote!!.hasImage()) {
            vipImagesRecyclerView!!.visibility = View.VISIBLE
        } else {
            // Hide VIP Images List
            vipImagesRecyclerView!!.visibility = View.GONE
        }
        renderTagList()
    }

    private fun renderTagList() {
        tagsList!!.removeAllViews()
        for (tagId: String? in originalNote!!.getTags().keys) {
            val tagView = LayoutInflater.from(context).inflate(R.layout.item_tag, tagsList, false)
            val tagViewHolder = TagViewHolder(tagView)
            tagViewHolder.bindModelToView(instance!!.tagsAdapter!!.getTag(tagId))
            tagsList!!.addView(tagView)
        }
    }

    private fun transformMillisToDateString(timeInMillis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return DateFormat.getDateTimeInstance().format(calendar.time)
    }

    private fun showInfoDialog() {
        val noteInfoLayout =
            LayoutInflater.from(context).inflate(R.layout.dialog_note_info, null) as RelativeLayout
        val noteAddedTimestamp =
            noteInfoLayout.findViewById<TextView>(R.id.note_info_added_timestamp_tv)
        val noteUpdatedTimestamp =
            noteInfoLayout.findViewById<TextView>(R.id.note_info_updated_timestamp_tv)
        noteAddedTimestamp.text = noteTimestampAdded
        noteUpdatedTimestamp.text = noteTimestampUpdated
        val alertDialogBuilder = AlertDialog.Builder(
            (context)!!
        ).setTitle(R.string.dialog_info_note_title)
            .setIcon(R.drawable.ic_menu_info_primary)
            .setView(noteInfoLayout)
            .setPositiveButton("OK"
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    /**
     * Creates a new file via the Drive REST API.
     */
    private fun exportNoteToDrive() {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Creating a file.")
            exportDialog = ProgressDialog.show(
                context, "Exporting Note to Google Drive",
                "In progress...", true
            )
            exportDialog!!.show()
            Log.d(TAG, "Note exported info: " + originalNote.toString())
            mDriveServiceHelper!!.createFile(
                DriveServiceHelper.TYPE_NOTE,
                originalNote!!.title,
                originalNote.toString()
            )
                .addOnSuccessListener {
                    Log.d(TAG, "Created file")
                    exportDialog!!.cancel()
                    dismissDialog()
                    Toast.makeText(instance, "Note exported", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Couldn't create file.", e)
                    exportDialog!!.cancel()
                    Toast.makeText(instance, "Failed to export Note", Toast.LENGTH_LONG).show()
                }
        } else {
            Log.e(TAG, "DriveServiceHelper wasn't initialized.")
            Toast.makeText(instance, "Failed to export Note", Toast.LENGTH_LONG).show()
        }
    }

    private fun showArchiveDialog() {
        val alertDialogBuilder = AlertDialog.Builder(
            (context)!!
        ).setTitle(R.string.dialog_archive_note_title)
            .setMessage(R.string.dialog_archive_note_msg)
            .setPositiveButton("Yes"
            ) { _, _ -> archiveNote() }.setNegativeButton("No"
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    private fun showDeleteNoteDialog() {
        val alertDialogBuilder = AlertDialog.Builder(
            (context)!!
        ).setTitle(R.string.dialog_delete_note_title)
            .setMessage(R.string.dialog_delete_note_msg)
            .setPositiveButton("Yes"
            ) { _, _ ->
                if (originalNote!!.isArchived()) {
                    deleteNoteFromArchive(originalNote)
                } else {
                    deleteNote(originalNote)
                }
            }.setNegativeButton("No"
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
        val dialog = alertDialogBuilder.create()
        dialog.show()
    }

    private fun deleteNoteFromArchive(note: Note?) {
        databaseManager!!.deleteNoteFromArchive((note)!!)
        dismissDialog()
    }

    private fun deleteNote(note: Note?) {
        databaseManager!!.deleteNote((note)!!)
        dismissDialog()
    }

    private fun unarchiveNote() {
        databaseManager!!.unarchiveNote((originalNote)!!)
        dismissDialog()
    }

    private fun archiveNote() {
        databaseManager!!.archiveNote((originalNote)!!)
        dismissDialog()
    }

    private fun updateNote() {
        val oldId = originalNote!!.id
        val archived = originalNote!!.isArchived()
        val newTitle = inputTitle!!.text.toString()
        var newInfo = inputInfo!!.text.toString()
        val stared = originalNote!!.isStared()
        val timestampAdded = originalNote!!.added
        val timestampUpdated = System.currentTimeMillis().toString()
        if (TextUtils.isEmpty(newInfo.trim { it <= ' ' })) {
            newInfo = ""
        }
        updatedNote = Note(oldId)
        updatedNote!!.title = newTitle
        updatedNote!!.info = newInfo
        updatedNote!!.stared = stared
        updatedNote!!.setTags(originalNote!!.getTags())
        updatedNote!!.added = timestampAdded
        updatedNote!!.archived = archived
        updatedNote!!.updated = timestampUpdated
        if (instance!!.isVipUser && vipImagesAdapter != null) {
            // User is a VIP user and the VIP image adapter has been initialized.
            currentImages = vipImagesAdapter!!.imageList
            if (currentImages!!.size > 0) {
                // The VIP user has added a image to the current Note.
                Log.d(TAG, "Updated image count: " + currentImages!!.size)
                var vipImageDiff = false
                for (vipImage: VipImage in currentImages!!) {
                    if (originalImages == null) {
                        vipImageDiff = true
                        break
                    } else {
                        if (!originalImages!!.contains(vipImage)) {
                            vipImageDiff = true
                            break
                        }
                    }
                }
                Log.d(TAG, "vipImageDiff: $vipImageDiff")
                if (vipImageDiff) {
                    uploadImages()
                } else {
                    val images = ArrayList<String?>()
                    for (vipImage: VipImage in currentImages!!) {
                        images.add(vipImage.id)
                    }
                    updatedNote!!.vipImages = images
                    originalNote = updatedNote
                    oldTags = HashMap(originalNote!!.getTags())
                    if (originalNote!!.isArchived()) {
                        databaseManager!!.updateArchivedNote(updatedNote!!)
                    } else {
                        databaseManager!!.updateNote(updatedNote!!)
                    }
                    editMode = false
                    populateNoteDetails()
                    createOptionsMenu()
                    toggleMode(editMode)
                }
            } else {
                originalNote = updatedNote
                oldTags = HashMap(originalNote!!.getTags())
                if (originalNote!!.isArchived()) {
                    databaseManager!!.updateArchivedNote(updatedNote!!)
                } else {
                    databaseManager!!.updateNote(updatedNote!!)
                }
                editMode = false
                populateNoteDetails()
                createOptionsMenu()
                toggleMode(editMode)
            }
        } else {
            originalNote = updatedNote
            oldTags = HashMap(originalNote!!.getTags())
            if (originalNote!!.isArchived()) {
                databaseManager!!.updateArchivedNote(updatedNote!!)
            } else {
                databaseManager!!.updateNote(updatedNote!!)
            }
            editMode = false
            populateNoteDetails()
            createOptionsMenu()
            toggleMode(editMode)
        }
    }

    private fun uploadImages() {
        Log.d(TAG, "Uploading " + vipImagesAdapter!!.itemCount + " image(s)")
        var imagesToUpload = ArrayList<VipImage>()
        if (originalImages != null) {
            for (vipImage: VipImage in vipImagesAdapter!!.imageList) {
                if (originalImages!!.contains(vipImage)) {
                    updatedNote!!.addVipImage(vipImage.id)
                } else {
                    imagesToUpload.add(vipImage)
                }
            }
        } else {
            imagesToUpload = vipImagesAdapter!!.imageList
        }
        val progressDialogText = "Saving attached image(s)"
        val progressDialog = ProgressDialog(instance!!.mainActivityRef)
        progressDialog.setTitle(progressDialogText)
        progressDialog.show()
        progressDialog.setMessage("Uploading " + imagesToUpload.size + " image(s)...")
        instance!!.storageManager!!.uploadMultipleImages(imagesToUpload, object : StorageCallback {
            override fun onSuccess(`object`: Any) {
                Log.d(TAG, "Last image uploaded successfully!")
                val uploadedImages = `object` as ArrayList<VipImage>
                for (vipImage: VipImage in uploadedImages) {
                    updatedNote!!.addVipImage(vipImage.id)
                }
                originalImages = currentImages
                originalNote = updatedNote
                oldTags = HashMap(originalNote!!.getTags())
                if (originalNote!!.isArchived()) {
                    databaseManager!!.updateArchivedNote((updatedNote)!!)
                } else {
                    databaseManager!!.updateNote((updatedNote)!!)
                }
                progressDialog.dismiss()
                editMode = false
                populateNoteDetails()
                createOptionsMenu()
                toggleMode(editMode)
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
                    // User selected multiple images.
                    val numberOfImagesSelected = data.clipData!!.itemCount
                    Log.d(TAG, "Multiple ($numberOfImagesSelected) images selected.")
                    for (i in 0 until numberOfImagesSelected) {
                        val originalFilePath = data.clipData!!.getItemAt(i).uri.toString()
                        val imageId = UUID.randomUUID().toString()
                        Log.d(TAG, "ImageId: $imageId")
                        Log.d(TAG, "ImagePath: $originalFilePath")
                        val vipImage = VipImage(imageId)
                        vipImage.originalFilePath = originalFilePath
                        vipImagesAdapter!!.add(vipImage)
                        vipImagesRecyclerView!!.visibility = View.VISIBLE
                    }
                } else if (data.data != null) {
                    // User selected a single image.
                    Log.d(TAG, "1 image selected.")
                    val originalFilePath = data.data.toString()
                    val imageId = UUID.randomUUID().toString()
                    val vipImage = VipImage(imageId)
                    vipImage.originalFilePath = originalFilePath
                    vipImagesAdapter!!.add(vipImage)
                    vipImagesRecyclerView!!.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (editMode) {
            if (newTags != null) {
                // Re-add newly created (and therefore added) Tags from Note.
                for (tagId: String? in newTags!!) {
                    originalNote!!.addTag(tagId)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (editMode) {
            // Remove newly created (and therefore added) Tags from Note.
            for (tagId: String? in newTags!!) {
                originalNote!!.removeTag(tagId)
            }
        }
    }

    companion object {
        val TAG: String = NoteDetailsDialogFragment::class.java.simpleName
        const val NOTE_PARENT_FRAGMENT_KEY = "parentFragment"
        const val NOTE_ADAPTER_POS_KEY = "adapterItemPos"
        const val NOTE_KEY = "originalNote"
        private const val PICK_IMAGE_REQUEST = 1
        private const val REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION = 999
        fun newInstance(): NoteDetailsDialogFragment {
            return NoteDetailsDialogFragment()
        }
    }
}