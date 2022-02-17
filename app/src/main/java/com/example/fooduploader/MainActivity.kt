package com.example.fooduploader

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.fooduploader.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        initLogger()
        initViews()
        viewModel.readCredentials(applicationContext.assets)
    }

    private fun initViews() {
        binding.apply {
            selectMenuBtn.setOnClickListener {
                getMenuFile.launch("image/*")
            }
            selectTableBtn.setOnClickListener {
                getTableFile.launch("application/vnd.ms-excel")
            }
            uploadFilesBtn.setOnClickListener {
                if (viewModel.isUploadPermitted())
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel.upload(contentResolver)
                    }
                else
                    showDialog()
            }
        }
    }

    private fun showDialog() {
        AlertDialog.Builder(this@MainActivity).apply {
            setMessage(R.string.alert_file_not_entered)
            setPositiveButton(android.R.string.ok) { dialog, which ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun initLogger() {
        viewModel.initLogger(this, binding.debugLog)
        Timber.d("Logger initialized")
    }

    private val getMenuFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            viewModel.menuUri = uri
            viewModel.setMenuBtnName(uri?.extractFileName())
        }

    private val getTableFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            viewModel.tableUri = uri
            viewModel.setTableBtnName(uri?.extractFileName())
        }

    private fun Uri.extractFileName(): String? {
        contentResolver.query(this, null, null, null, null)?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            return it.getString(nameIndex)
        }
        return null
    }
}