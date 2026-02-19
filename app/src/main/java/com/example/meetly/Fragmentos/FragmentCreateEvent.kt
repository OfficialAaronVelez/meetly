package com.example.meetly.Fragmentos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.meetly.databinding.FragmentCreateEventBinding
import java.util.*

class FragmentCreateEvent : Fragment() {

    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private val calendar = Calendar.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivEventCover.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Select Cover Photo
        binding.cardCoverPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Date Picker
        binding.etEventDate.setOnClickListener {
            showDatePicker()
        }

        // Time Picker
        binding.etEventTime.setOnClickListener {
            showTimePicker()
        }

        // Create Event Button
        binding.btnCreateEvent.setOnClickListener {
            validarDatos()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                binding.etEventDate.setText("$dayOfMonth/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                binding.etEventTime.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun validarDatos() {
        val title = binding.etEventTitle.text.toString().trim()
        val description = binding.etEventDescription.text.toString().trim()
        val date = binding.etEventDate.text.toString().trim()
        val time = binding.etEventTime.text.toString().trim()
        val location = binding.etEventLocation.text.toString().trim()

        if (title.isEmpty()) {
            binding.etEventTitle.error = "Enter title"
        } else if (description.isEmpty()) {
            binding.etEventDescription.error = "Enter description"
        } else if (date.isEmpty()) {
            binding.etEventDate.error = "Select date"
        } else if (time.isEmpty()) {
            binding.etEventTime.error = "Select time"
        } else if (location.isEmpty()) {
            binding.etEventLocation.error = "Enter location"
        } else {
            // TODO: Upload image to Firebase Storage and event data to Realtime Database
            Toast.makeText(requireContext(), "Creating event...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
