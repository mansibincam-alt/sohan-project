package com.sohanbhai.app

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val imagePath: String? = null
)

class Conversation(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "নতুন চ্যাট",
    var updated: Long = System.currentTimeMillis(),
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf()
)

class Attachment(val mime: String, val bytes: ByteArray, val name: String)

/** Chat history lives only inside the app's private storage. */
object Store {
    val conversations: SnapshotStateList<Conversation> = mutableStateListOf()
    private var file: File? = null
    private var imageDir: File? = null

    fun init(ctx: Context) {
        file = File(ctx.filesDir, "history.json")
        imageDir = File(ctx.filesDir, "images").apply { mkdirs() }
        val f = file ?: return
        if (!f.exists()) return
        runCatching {
            val arr = JSONArray(f.readText())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val msgs = o.getJSONArray("messages")
                val list = mutableStateListOf<ChatMessage>()
                for (j in 0 until msgs.length()) {
                    val m = msgs.getJSONObject(j)
                    list.add(
                        ChatMessage(
                            id = m.getString("id"),
                            isUser = m.getBoolean("isUser"),
                            text = m.getString("text"),
                            imagePath = m.optString("imagePath").ifEmpty { null }
                        )
                    )
                }
                conversations.add(
                    Conversation(o.getString("id"), o.getString("title"), o.getLong("updated"), list)
                )
            }
        }
    }

    fun saveImage(bytes: ByteArray): String {
        val f = File(imageDir, "${UUID.randomUUID()}.png")
        f.writeBytes(bytes)
        return f.absolutePath
    }

    fun persist() {
        val arr = JSONArray()
        conversations.forEach { c ->
            val msgs = JSONArray()
            c.messages.forEach { m ->
                msgs.put(
                    JSONObject()
                        .put("id", m.id)
                        .put("isUser", m.isUser)
                        .put("text", m.text)
                        .put("imagePath", m.imagePath ?: "")
                )
            }
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("title", c.title)
                    .put("updated", c.updated)
                    .put("messages", msgs)
            )
        }
        file?.writeText(arr.toString())
    }

    /** Moves the conversation to the top of history and saves it. */
    fun touch(c: Conversation) {
        conversations.remove(c)
        conversations.add(0, c)
        c.updated = System.currentTimeMillis()
        persist()
    }
}
