package com.rkcomputer.rkaiassistant

import android.Manifest
import android.content.Intent
import android.provider.ContactsContract
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

private val Bg = Color(0xFF07110D)
private val Panel = Color(0xFF102019)
private val Neon = Color(0xFF53F59A)
private val Muted = Color(0xFF9CB7A7)

data class ChatMessage(val text: String, val fromUser: Boolean)

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var onSpeechResult: ((String) -> Unit)? = null
    private var onSpeechStatus: ((String) -> Unit)? = null
    private var pendingCallRequest = false

    private val contactPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val contactUri = result.data?.data
            if (contactUri != null) {
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val number = cursor.getString(0)
                        // Opens the dialer; the user must review and tap Call.
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                    }
                }
            }
        }
        pendingCallRequest = false
    }

    private val micPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startListening()
        else onSpeechStatus?.invoke("মাইক্রোফোন ব্যবহারের অনুমতি দেওয়া হয়নি")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        setContent {
            RKAssistantScreen(
                onMic = { result, status ->
                    onSpeechResult = result
                    onSpeechStatus = status
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) startListening()
                    else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                },
                onSpeak = { speak(it) },
                onCallContact = { openContactPicker() }
            )
        }
    }

    private fun openContactPicker() {
        pendingCallRequest = true
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        contactPicker.launch(intent)
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            onSpeechStatus?.invoke("এই ফোনে Speech Recognition সেবা পাওয়া যায়নি")
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).also { recognizer ->
            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { onSpeechStatus?.invoke("শুনছি…") }
                override fun onBeginningOfSpeech() { onSpeechStatus?.invoke("কথা বলুন…") }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { onSpeechStatus?.invoke("প্রক্রিয়া চলছে…") }
                override fun onError(error: Int) { onSpeechStatus?.invoke("ভয়েস শনাক্ত করা যায়নি। আবার চেষ্টা করুন।") }
                override fun onResults(results: Bundle?) {
                    val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    words?.firstOrNull()?.let { onSpeechResult?.invoke(it) }
                    onSpeechStatus?.invoke("প্রস্তুত")
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "RK AI Assistant-কে বলুন")
            }
            recognizer.startListening(intent)
        }
    }

    private fun speak(text: String) {
        tts?.language = Locale("bn", "BD")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "rk_reply")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale("bn", "BD")
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun RKAssistantScreen(
    onMic: ((String) -> Unit, (String) -> Unit) -> Unit,
    onSpeak: (String) -> Unit,
    onCallContact: () -> Unit
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("আসসালামু আলাইকুম রুমান! আমি জিম (JIM), তোমার AI সহকারী। কীভাবে সাহায্য করতে পারি?", false)
        )
    }
    var input by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("প্রস্তুত") }

    fun sendMessage(raw: String) {
        val text = raw.trim()
        if (text.isEmpty()) return
        messages.add(ChatMessage(text, true))
        input = ""
        val isCallCommand = listOf("ফোন কর", "কল কর", "call ").any { text.lowercase().contains(it) }
        if (isCallCommand) {
            val reply = "কাকে ফোন করতে চাও, রুমান? Contacts থেকে নম্বর বেছে নাও। এরপর ডায়ালারে নম্বর দেখে নিজে Call চাপতে পারবে।"
            messages.add(ChatMessage(reply, false))
            onSpeak(reply)
            onCallContact()
        } else {
            val reply = demoReply(text)
            messages.add(ChatMessage(reply, false))
            onSpeak(reply)
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Neon, background = Bg, surface = Panel,
            onPrimary = Bg, onBackground = Color.White, onSurface = Color.White
        )
    ) {
        Column(
            Modifier.fillMaxSize().background(Bg).padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("RK AI ASSISTANT", color = Neon, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("আপনার স্মার্ট সহকারী", color = Muted, fontSize = 12.sp)
                }
                Surface(color = Color(0xFF173A29), shape = RoundedCornerShape(50)) {
                    Text("● ONLINE DEMO", color = Neon, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier.fillMaxWidth().height(150.dp)
                    .background(Panel, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(76.dp).background(Color(0xFF17452D), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("RK", color = Neon, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("আমি শুনছি, আমি সাহায্য করছি", color = Color.White, fontSize = 14.sp)
                    Text(status, color = Neon, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("কথোপকথন", color = Neon, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (msg.fromUser) Color(0xFF1D5538) else Panel,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Text(msg.text, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input, onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("এখানে লিখুন…", color = Muted) },
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = Neon, unfocusedBorderColor = Muted,
                        cursorColor = Neon
                    ),
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        onMic({ spoken -> sendMessage(spoken) }, { newStatus -> status = newStatus })
                    },
                    modifier = Modifier.size(48.dp).background(Color(0xFF17452D), CircleShape)
                ) { Icon(Icons.Default.Mic, contentDescription = "ভয়েস", tint = Neon) }
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = { sendMessage(input) },
                    modifier = Modifier.size(48.dp).background(Neon, CircleShape)
                ) { Icon(Icons.Default.Send, contentDescription = "পাঠান", tint = Bg) }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "প্রাথমিক সংস্করণ • ভয়েস ইনপুট ও বাংলা ভয়েস রিপ্লাই",
                color = Muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private fun demoReply(text: String): String {
    val q = text.lowercase()
    return when {
        "হ্যালো" in q || "hello" in q || "আসসালামু" in q ->
            "ওয়ালাইকুম আসসালাম! বলুন, কী কাজে সাহায্য করব?"
        "তোমার নাম" in q || "নাম কি" in q ->
            "আমার নাম জিম (JIM)। তোমার নাম রুমান।"
        "সময়" in q || "time" in q ->
            "আমি এখনো লাইভ সময় দেখার সুবিধা যুক্ত করিনি।"
        "কী করতে পার" in q || "কি করতে পার" in q ->
            "আমি চ্যাট করতে, ভয়েস শুনতে, উত্তর পড়ে শোনাতে এবং তোমার অনুমতিতে ফোনের Contacts খুলে কল করার প্রক্রিয়া শুরু করতে পারি।"
        else ->
            "আপনার কথাটি বুঝেছি: $text। এটি এখন ডেমো সংস্করণ। প্রকৃত AI উত্তর পেতে নিরাপদ অনলাইন AI সার্ভার সংযোগ পরের ধাপে যুক্ত করতে হবে।"
    }
}
