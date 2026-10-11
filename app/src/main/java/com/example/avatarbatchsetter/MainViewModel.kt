package com.example.avatarbatchsetter

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.avatarbatchsetter.data.Avatar
import com.example.avatarbatchsetter.data.AvatarDb
import com.example.avatarbatchsetter.data.Contact
import com.example.avatarbatchsetter.data.ContactsHelper
import com.example.avatarbatchsetter.util.ImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val resolver = app.contentResolver
    private val avatarDao = AvatarDb.get(app).avatarDao()
    private val avatarDir = File(app.filesDir, "avatars").apply { mkdirs() }

    private val _contacts = MutableLiveData<List<Contact>>(emptyList())
    val contacts: LiveData<List<Contact>> = _contacts

    private val _selectedIds = MutableLiveData<Set<Long>>(emptySet())
    val selectedIds: LiveData<Set<Long>> = _selectedIds

    private val _avatars = MutableLiveData<List<Avatar>>(emptyList())
    val avatars: LiveData<List<Avatar>> = _avatars

    private val _processing = MutableLiveData(false)
    val processing: LiveData<Boolean> = _processing

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    fun loadContacts() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                ContactsHelper.queryContacts(resolver)
            }
            _contacts.value = list
            val valid = list.map { it.id }.toSet()
            _selectedIds.value = _selectedIds.value?.intersect(valid) ?: emptySet()
        }
    }

    fun toggleSelect(id: Long) {
        val cur = _selectedIds.value ?: emptySet()
        _selectedIds.value = if (id in cur) cur - id else cur + id
    }

    fun selectAll() {
        val all = _contacts.value?.map { it.id }?.toSet() ?: emptySet()
        _selectedIds.value = if (all.isNotEmpty() && all == _selectedIds.value) {
            emptySet()
        } else {
            all
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun loadAvatars() {
        viewModelScope.launch {
            _avatars.value = withContext(Dispatchers.IO) { avatarDao.getAll() }
        }
    }

    fun importAvatars(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _processing.value = true
            var imported = 0
            withContext(Dispatchers.IO) {
                for (uri in uris) {
                    try {
                        val input = resolver.openInputStream(uri) ?: continue
                        val file = File(
                            avatarDir,
                            "avatar_${System.currentTimeMillis()}_${imported}.jpg"
                        )
                        input.use { ins ->
                            FileOutputStream(file).use { outs -> ins.copyTo(outs) }
                        }
                        avatarDao.insert(Avatar(filePath = file.absolutePath))
                        imported++
                    } catch (_: Exception) {
                    }
                }
            }
            _processing.value = false
            _avatars.value = withContext(Dispatchers.IO) { avatarDao.getAll() }
            _message.value =
                if (imported > 0) "成功导入 $imported 张头像" else "导入失败，请重新选择图片"
        }
    }

    fun deleteAvatar(avatar: Avatar) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                avatarDao.delete(avatar)
                runCatching { File(avatar.filePath).delete() }
            }
            _avatars.value = withContext(Dispatchers.IO) { avatarDao.getAll() }
            _message.value = "已删除头像"
        }
    }

    private fun selectedContacts(): List<Contact> {
        val sel = _selectedIds.value ?: emptySet()
        return _contacts.value.orEmpty().filter { it.id in sel }
    }

    fun applyAvatar(avatar: Avatar) {
        val targets = selectedContacts()
        if (targets.isEmpty()) {
            _message.value = "请先在联系人页勾选要设置的联系人"
            return
        }
        viewModelScope.launch {
            _processing.value = true
            var ok = 0
            withContext(Dispatchers.IO) {
                val bytes = ImageUtil.toJpegBytes(ImageUtil.decodeFile(avatar.filePath, 512))
                for (c in targets) {
                    if (ContactsHelper.setContactPhoto(resolver, c.rawContactIds, bytes)) ok++
                }
            }
            _processing.value = false
            _message.value = "已为 $ok/${targets.size} 位联系人设置头像"
        }
    }

    fun randomMatch() {
        val targets = selectedContacts()
        if (targets.isEmpty()) {
            _message.value = "请先在联系人页勾选要设置的联系人"
            return
        }
        viewModelScope.launch {
            val avatars = withContext(Dispatchers.IO) { avatarDao.getAll() }
            if (avatars.isEmpty()) {
                _message.value = "头像库为空，请先到头像库导入头像"
                return@launch
            }
            _processing.value = true
            var ok = 0
            withContext(Dispatchers.IO) {
                val shuffled = avatars.shuffled()
                targets.forEachIndexed { i, c ->
                    val a = shuffled[i % shuffled.size]
                    val bytes = ImageUtil.toJpegBytes(ImageUtil.decodeFile(a.filePath, 512))
                    if (ContactsHelper.setContactPhoto(resolver, c.rawContactIds, bytes)) ok++
                }
            }
            _processing.value = false
            _message.value = "已为 $ok/${targets.size} 位联系人随机匹配头像"
        }
    }

    fun consumeMessage() {
        _message.value = null
    }
}
