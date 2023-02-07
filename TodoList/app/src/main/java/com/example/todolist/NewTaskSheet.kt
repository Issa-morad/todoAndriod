package com.example.todolist

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.databinding.FragmentNewTaskSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalTime


class NewTaskSheet(var taskItem: TaskItem?) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNewTaskSheetBinding
    private lateinit var taskViewModel: TaskViewModel
    private var dueTime: LocalTime? = null


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()

        if (taskItem != null){
            binding.taskTitle.text = "Edit Task"
            val editable = Editable.Factory.getInstance()
            binding.name.text = editable.newEditable(taskItem!!.name)
            binding.desc.text = editable.newEditable(taskItem!!.desc)
            if (taskItem!!.dueTime() != null){
                dueTime = taskItem!!.dueTime()!!
                updateTimeButtonText()

            }
        }
        else{
            binding.taskTitle.text = "New Task"
        }
        taskViewModel = ViewModelProvider(activity).get(TaskViewModel::class.java)
        binding.saveButton.setOnClickListener{
            saveAction()

        }
        binding.timePickerButton.setOnClickListener {
            openTimePicker()
        }
        nameFocusListener()
        descFocusListener()
    }

    private fun nameFocusListener(){
        binding.name.setOnFocusChangeListener { _, focused ->
            if (!focused){
                binding.nameContainer.helperText = validName()
            }
        }
    }

    private fun validName(): String?
    {
        val nameText = binding.name.text.toString()
        if (!Patterns.DOMAIN_NAME.matcher(nameText).matches()) {
            return "Invalid Name"
        }
        if (nameText.length < 3) {
            return "Title must at least 3 characters"
        }
        if (nameText.length > 15) {
            return "Title max 15 characters"
        }
        return null
    }

    private fun descFocusListener(){
        binding.desc.setOnFocusChangeListener { _, focused ->
            if (!focused){
                binding.descContainer.helperText = validDesc()
            }
        }
    }

    private fun validDesc(): String?
    {
        val descText = binding.desc.text.toString()
        if (descText.length < 3) {
            return "description must at least 3 characters"
        }
        if (descText.length > 15) {
            return "description max 15 characters"
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openTimePicker() {
        if (dueTime == null)
            dueTime = LocalTime.now()
        val listener = TimePickerDialog.OnTimeSetListener{ _, selectedHour, selectedMinute ->
            dueTime = LocalTime.of(selectedHour,selectedMinute)
            updateTimeButtonText()
        }
        val dialog = TimePickerDialog(activity, listener, dueTime!!.hour, dueTime!!.minute, true)
        dialog.setTitle("Task Due")
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTimeButtonText() {
        binding.timePickerButton.text = String.format("%02d:%02d", dueTime!!.hour, dueTime!!.minute)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewTaskSheetBinding.inflate(inflater,container,false)
        return binding.root
    }


    private fun saveAction(){
        binding.nameContainer.helperText = validName()
        binding.descContainer.helperText = validDesc()
        val validName = binding.nameContainer.helperText == null
        val validDesc = binding.descContainer.helperText == null
        if (validName && validDesc)
            resetForm()

        else
            invalidForm()

        val name = binding.name.text.toString()
        val desc = binding.desc.text.toString()
        val dueTimeString = if (dueTime == null) null else TaskItem.timeFormatter.format(dueTime)
        if (taskItem == null){
            val newTask = TaskItem(name,desc,dueTimeString, null)
            taskViewModel.addTaskItem(newTask)
        }
        else{
            taskItem!!.name = name
            taskItem!!.desc = desc
            taskItem!!.dueTimeString = dueTimeString
            taskViewModel.updateTaskItem(taskItem!!)
        }
        binding.name.setText("")
        binding.desc.setText("")
        dismiss()
    }

    private fun invalidForm() {
        var message = ""
        if ( binding.descContainer.helperText != null)
            message += "\n\nDescription:" +  binding.descContainer.helperText
        if ( binding.nameContainer.helperText != null)
            message += "\n\nName:" +  binding.nameContainer.helperText

        AlertDialog.Builder(context)
            .setTitle("Invalid Form")
            .setMessage(message)
            .setPositiveButton("OK"){ _,_ ->
                // do noting
            }
            .show()
    }

    private fun resetForm() {
        var message = "Name:" +  binding.name.text
            message += "\nDescription:" +  binding.desc.text

        AlertDialog.Builder(context)
            .setTitle("Form Saved")
            .setMessage(message)
            .setPositiveButton("OK"){ _,_ ->
                binding.name.text = null
                binding.desc.text = null

                binding.nameContainer.helperText = getString(R.string.required)
                binding.descContainer.helperText = getString(R.string.required)
            }
            .show()
    }

}