package com.example.avatarbatchsetter.data

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.provider.ContactsContract

object ContactsHelper {

    fun queryContacts(resolver: ContentResolver): List<Contact> {
        val rawByContact = HashMap<Long, MutableList<Long>>()
        resolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.CONTACT_ID
            ),
            null, null, null
        )?.use { c ->
            while (c.moveToNext()) {
                val rawId = c.getLong(0)
                val contactId = c.getLong(1)
                rawByContact.getOrPut(contactId) { mutableListOf() }.add(rawId)
            }
        }

        val nameMap = HashMap<Long, String>()
        val phoneMap = HashMap<Long, Boolean>()
        resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER
            ),
            null, null,
            "${ContactsContract.Contacts.DISPLAY_NAME} COLLATE LOCALIZED ASC"
        )?.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                nameMap[id] = c.getString(1) ?: ""
                phoneMap[id] = c.getInt(2) > 0
            }
        }

        return rawByContact.mapNotNull { (contactId, rawIds) ->
            val name = nameMap[contactId] ?: return@mapNotNull null
            Contact(
                id = contactId,
                name = name,
                hasPhone = phoneMap[contactId] ?: false,
                rawContactIds = rawIds
            )
        }
    }

    fun setContactPhoto(
        resolver: ContentResolver,
        rawContactIds: List<Long>,
        photoBytes: ByteArray
    ): Boolean {
        val ops = ArrayList<ContentProviderOperation>()
        val photoMime = ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE

        for (rawId in rawContactIds) {
            val selection =
                "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
            val args = arrayOf(rawId.toString(), photoMime)

            ops.add(
                ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(selection, args)
                    .build()
            )
            ops.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawId)
                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                    .withValue(ContactsContract.Data.MIMETYPE, photoMime)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                    .build()
            )
        }

        return try {
            resolver.applyBatch(ContactsContract.AUTHORITY, ops)
            true
        } catch (e: Exception) {
            false
        }
    }
}
