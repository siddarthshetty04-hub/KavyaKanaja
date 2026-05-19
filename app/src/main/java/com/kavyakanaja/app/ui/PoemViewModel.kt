package com.kavyakanaja.app.ui

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kavyakanaja.app.data.Poem
import com.kavyakanaja.app.data.PoemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PoemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PoemRepository(application)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _poemOfTheDay = MutableStateFlow<Poem?>(null)
    val poemOfTheDay: StateFlow<Poem?> = _poemOfTheDay.asStateFlow()

    private val _allPoems = MutableStateFlow<List<Poem>>(emptyList())
    val allPoems: StateFlow<List<Poem>> = _allPoems.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentLineIndex = MutableStateFlow<Int?>(null)
    val currentLineIndex: StateFlow<Int?> = _currentLineIndex.asStateFlow()

    private val _ttsReady = MutableStateFlow(false)
    val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    private var tts: TextToSpeech? = null

    init {
        // ✅ CRITICAL FIX: Load JSON on IO thread to prevent ANR
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val poems = repository.getPoems()
                val potd = repository.getPoemOfTheDay()
                withContext(Dispatchers.Main) {
                    _allPoems.value = poems
                    _poemOfTheDay.value = potd
                }
            } catch (e: Exception) {
                Log.e("PoemViewModel", "Error loading poems: ${e.message}", e)
            }
        }
        // TTS init is async via callback so safe to call here
        initTTS(application)
    }

    private fun initTTS(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                var result = tts?.setLanguage(Locale("kn", "IN"))
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w("PoemViewModel", "Kannada TTS not available – falling back to Indian English")
                    tts?.setLanguage(Locale("en", "IN"))
                    isKannadaSupported = false
                } else {
                    isKannadaSupported = true
                }
                tts?.setSpeechRate(0.85f)
                tts?.setPitch(1.0f)
                mainHandler.post { _ttsReady.value = true }
                Log.d("PoemViewModel", "TTS ready")
            } else {
                Log.e("PoemViewModel", "TTS init FAILED status=$status")
            }
        }
    }

    private var speakJob: kotlinx.coroutines.Job? = null
    private var isKannadaSupported = true

    // Add this to initTTS success block:
    // isKannadaSupported = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)

    fun speakLines(lines: List<String>) {
        if (!_ttsReady.value) return
        stopSpeaking()
        
        _isPlaying.value = true
        _currentLineIndex.value = null

        speakJob = viewModelScope.launch {
            for (index in lines.indices) {
                if (!_isPlaying.value) break
                val line = lines[index]
                
                _currentLineIndex.value = index
                
                // If Kannada is not supported, the English engine will skip Kannada text.
                // We provide a basic phonetic transliteration so it produces audio.
                val textToSpeak = if (isKannadaSupported) line else transliterateKannadaToEnglish(line)
                
                // Speak the line
                tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "line_$index")
                
                // Calculate dynamic delay for highlighting
                val delayMs = maxOf(2500L, line.length * 130L)
                kotlinx.coroutines.delay(delayMs)
            }
            // Finished
            _isPlaying.value = false
            _currentLineIndex.value = null
        }
    }

    private fun transliterateKannadaToEnglish(text: String): String {
        val consonants = mapOf(
            'ಕ' to "k", 'ಖ' to "kh", 'ಗ' to "g", 'ಘ' to "gh", 'ಙ' to "ng",
            'ಚ' to "ch", 'ಛ' to "chh", 'ಜ' to "j", 'ಝ' to "jh", 'ಞ' to "ny",
            'ಟ' to "t", 'ಠ' to "th", 'ಡ' to "d", 'ಢ' to "dh", 'ಣ' to "n",
            'ತ' to "th", 'ಥ' to "thh", 'ದ' to "dh", 'ಧ' to "dhh", 'ನ' to "n",
            'ಪ' to "p", 'ಫ' to "f", 'ಬ' to "b", 'ಭ' to "bh", 'ಮ' to "m",
            'ಯ' to "y", 'ರ' to "r", 'ಲ' to "l", 'ವ' to "v", 'ಶ' to "sh",
            'ಷ' to "sh", 'ಸ' to "s", 'ಹ' to "h", 'ಳ' to "l"
        )
        // Improved mappings to force English TTS to read correctly:
        // 'a' -> 'uh', 'i' -> 'ih', 'u' -> 'oo', 'e' -> 'eh'
        val independentVowels = mapOf(
            'ಅ' to "uh", 'ಆ' to "ah", 'ಇ' to "ih", 'ಈ' to "ee", 'ಉ' to "oo", 'ಊ' to "ooh",
            'ಋ' to "ru", 'ಎ' to "eh", 'ಏ' to "ay", 'ಐ' to "eye", 'ಒ' to "oh", 'ಓ' to "ohh", 'ಔ' to "ow"
        )
        val dependentVowels = mapOf(
            '\u0CBE' to "ah", '\u0CBF' to "ih", '\u0CC0' to "ee", '\u0CC1' to "oo", '\u0CC2' to "ooh",
            '\u0CC3' to "ru", '\u0CC6' to "eh", '\u0CC7' to "ay", '\u0CC8' to "eye", '\u0CCA' to "oh",
            '\u0CCB' to "ohh", '\u0CCC' to "ow"
        )
        val modifiers = mapOf('ಂ' to "m", 'ಃ' to "h")
        val virama = '\u0CCD'

        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (independentVowels.containsKey(c)) {
                sb.append(independentVowels[c])
            } else if (consonants.containsKey(c)) {
                sb.append(consonants[c])
                // Look ahead to see if there's a dependent vowel or virama
                if (i + 1 < text.length) {
                    val nextC = text[i + 1]
                    if (dependentVowels.containsKey(nextC)) {
                        sb.append(dependentVowels[nextC])
                        i++ // Skip the dependent vowel since we processed it
                    } else if (nextC == virama) {
                        // It's a half consonant, don't append the inherent 'a'
                        i++ // Skip the virama
                    } else {
                        // It's a full consonant with no modifier, append inherent 'a' (now 'uh')
                        sb.append("uh")
                    }
                } else {
                    sb.append("uh")
                }
            } else if (modifiers.containsKey(c)) {
                sb.append(modifiers[c])
            } else {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    fun stopSpeaking() {
        speakJob?.cancel()
        tts?.stop()
        _isPlaying.value = false
        _currentLineIndex.value = null
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

class PoemViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PoemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PoemViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
