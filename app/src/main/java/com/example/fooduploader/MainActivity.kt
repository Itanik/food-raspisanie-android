package com.example.fooduploader

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.fooduploader.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityViewModel by viewModels()
    private val logger = ObservableTimberTree(lifecycleScope)

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
                getTableFile.launch("application/*")
            }
            uploadFilesBtn.setOnClickListener {
                if (viewModel!!.isUploadPermitted())
                    lifecycleScope.launch(Dispatchers.IO) {
                        viewModel!!.upload(contentResolver)
                    }
                else
                    showDialog(R.string.alert_file_not_entered, android.R.string.ok)
            }
            checkMenuBtn.setOnClickListener {
                viewModel!!.openInBrowser(
                    this@MainActivity,
                    ButtonMode.MENU
                )
            }
            checkTableBtn.setOnClickListener {
                viewModel!!.openInBrowser(
                    this@MainActivity,
                    ButtonMode.TABLE
                )
            }
        }
    }

    private fun showDialog(message: Int, button: Int) {
        AlertDialog.Builder(this@MainActivity).apply {
            setMessage(message)
            setPositiveButton(button) { dialog, which ->
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun initLogger() {
        Timber.plant(logger)
        val logListAdapter = LogListAdapter()
        binding.debugLog.adapter = logListAdapter
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                logger.logFlow.collect {
                    logListAdapter.submitList(logListAdapter.currentList.plus(it))
                }
            }
        }
        Timber.d("Logger initialized")
    }

    private val getMenuFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            viewModel.menuUri = uri
            viewModel.setMenuBtnName(contentResolver.extractFileName(uri))
        }

    private val getTableFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val fileName = contentResolver.extractFileName(uri)
            if (fileName?.endsWith("xlsx") == false) {
                showDialog(R.string.alert_file_must_be_xlsx, android.R.string.ok)
            } else {
                viewModel.tableUri = uri
                viewModel.setTableBtnName(fileName)
            }
        }
}