package cen.xiaoyuan.epub.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.ui.setupWithNavController
import cen.xiaoyuan.epub.EpubConfig
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.databinding.ActivityMainBinding
import cen.xiaoyuan.epub.ui.library.AddEpubBook
import cen.xiaoyuan.epub.ui.preference.Settings
import cen.xiaoyuan.epub.ui.viewmodel.EventViewModel
import cen.xiaoyuan.epub.ui.library.LibraryViewModel
import cen.xiaoyuan.epub.utils.fadeToVisibilityUnsafe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
open class MainActivity : AppCompatActivity(), CoroutineScope {

    private val observer = Observer<String> { it.setupDarkModePreference() }
    override val coroutineContext: CoroutineContext get() = Dispatchers.IO

    private lateinit var binding: ActivityMainBinding

    private val navController: NavController
        get() = findNavController(R.id.nav_host_fragment_content_main)

    private val events: EventViewModel by viewModels()
    private val library: LibraryViewModel by viewModels()

    private val document =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            launch { library.processEpubFiles(uris, isFolder = false, isPick = true){ it.processResult() } }
        }

    private val documentTree =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            launch { uri?.takePersistableUriPermission()?.also {takeUri ->
                library.processEpubFiles(listOf(takeUri),true){ it.processResult() }
            }
        }
    }

    private fun String.processResult(){
        launch(Dispatchers.Main.immediate) {  MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle(R.string.add_epub_status)
            .setCancelable(false)
            .setMessage(this@processResult)
            .setPositiveButton(R.string.close,null)
            .show()
        }
    }

    private val rail get() = binding.navigationRail

    private val destinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
        when(destination.id){
            R.id.to_settings -> rail.selectedItemId = R.id.to_settings
            R.id.to_library -> rail.selectedItemId = R.id.to_library
            R.id.to_excerpt -> rail.selectedItemId = R.id.to_excerpt
        }
        val showNail = resources.getBoolean(R.bool.show_navigation_rail)
        val visible = destination.id != R.id.to_reader
        if(!showNail) binding.navView.fadeToVisibilityUnsafe(
                visible = visible,
                gone = true
            )
        else binding.navigationRail.fadeToVisibilityUnsafe(
            visible = visible,
            gone = true
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Settings.DARK_MODE.observeForever(observer)

        intent.data?.open()

        rail.setOnItemSelectedListener { onOptionsItemSelected(it) }

        binding.navView.setupWithNavController(findNavController(R.id.nav_host_fragment_content_main))

        navController.addOnDestinationChangedListener(destinationChangedListener)

        repeatWithViewLifecycle {
            launch {
                events.addEpub.collect { showAddEpubDialog() }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Settings.DARK_MODE.removeObserver(observer)
    }

    private fun String.setupDarkModePreference() {
        val values = resources.getStringArray(R.array.set_dark_mode_values)
        when (this) {
            values[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            values[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun Uri.open(){
        val str = toString()
        if(str.contains("internal") || str.contains("external")) {
            takePersistableUriPermission(isFolder = true)
            launch {
                /*listOf(this@open).process(
                    isFolder = false,
                    isPick = true
                ).result()*/
            }
        } else MaterialAlertDialogBuilder(this@MainActivity).apply {
            setMessage(getString(R.string.epub_uri_error_message,this@open.authority))
            setTitle(R.string.epub_uri_error_title)
            setPositiveButton(R.string.close,null)
            show()
        }
    }

    private fun showAddEpubDialog() {
        AddEpubBook(
            files = { document.launch(arrayOf(EpubConfig.EPUB_FILE_MIME,EpubConfig.EPUB_FILE_MIME2)) },
            folder = { documentTree.launch(null) }
        ).show(supportFragmentManager,javaClass.simpleName)
    }

    private fun Uri.takePersistableUriPermission(isFolder:Boolean = false): Uri {
        if(!isFolder) contentResolver.takePersistableUriPermission(
            this, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        else this@MainActivity.grantUriPermission(
            this@MainActivity.packageName,this, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        return this
    }

    private inline fun AppCompatActivity.repeatWithViewLifecycle(
        minState: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline block: suspend CoroutineScope.() -> Unit
    ) {
        if (minState == Lifecycle.State.INITIALIZED || minState == Lifecycle.State.DESTROYED) {
            throw IllegalArgumentException("minState must be between INITIALIZED and DESTROYED")
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(minState) {
                block()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    private fun Int.toTarget(){
        if(navController.currentDestination?.id!=this) {
            if(navController.currentDestination?.id!=R.id.to_library
                && navController.currentDestination?.id!=R.id.to_reader)
                navController.navigateUp()
            navController.navigate(this)
        }
    }

    private fun toHome(){
        if(navController.currentDestination?.id!=R.id.to_library) {
            navController.navigateUp()
            toHome()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.to_settings -> R.id.to_settings.toTarget()
            R.id.to_excerpt -> R.id.to_excerpt.toTarget()
            R.id.to_library -> toHome()
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}