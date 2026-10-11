package com.example.avatarbatchsetter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.avatarbatchsetter.databinding.ActivityMainBinding
import com.example.avatarbatchsetter.ui.AvatarLibraryFragment
import com.example.avatarbatchsetter.ui.ContactsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val readOk = result[Manifest.permission.READ_CONTACTS] == true
        val writeOk = result[Manifest.permission.WRITE_CONTACTS] == true
        if (readOk && writeOk) {
            viewModel.loadContacts()
        } else {
            Toast.makeText(this, "需要联系人读写权限才能使用头像设置功能", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ContactsFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment? = when (item.itemId) {
                R.id.nav_contacts -> ContactsFragment()
                R.id.nav_library -> AvatarLibraryFragment()
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }

        requestContactPermissionIfNeeded()
    }

    fun requestContactPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
            )
        )
    }

    private fun requestContactPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestContactPermission()
        } else {
            viewModel.loadContacts()
        }
    }
}
