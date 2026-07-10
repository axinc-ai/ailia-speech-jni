package axip.ailia_speech

/**
 * Callback interface for receiving intermediate speech recognition results.
 */
interface IntermediateCallback {
    /**
     * Called when intermediate speech recognition results are available.
     *
     * @param text The intermediate recognition text
     * @return 0 to continue recognition, non-zero to abort
     */
    fun onIntermediateResult(text: String): Int
}

/**
 * Result of the speech recognition.
 *
 * @property text Recognized text (UTF-8).
 * @property timeStampBegin Start time of the text in seconds.
 * @property timeStampEnd End time of the text in seconds.
 * @property speakerId Speaker ID assigned by speaker diarization, or AILIA_SPEECH_SPEAKER_ID_UNKNOWN when diarization is disabled.
 * @property language Detected language code.
 * @property confidence Confidence of the recognition.
 */
data class AiliaSpeechText(
    var text: String? = null,
    var timeStampBegin: Float = 0f,
    var timeStampEnd: Float = 0f,
    var speakerId: Int = 0,
    var language: String? = null,
    var confidence: Float = 0f
)

/**
 * Speech recognition for audio waveforms.
 *
 * This class provides speech to text conversion (Whisper and SenseVoice),
 * voice activity detection, speaker diarization and post processing such as
 * translation via the ailia Speech native library.
 *
 * Feed the audio waveform with [pushInputData], notify the end of the input
 * with [finalizeInputData], then call [transcribe] while [getBuffered]
 * returns 1 and get the results with [getText]. [getComplete] returns 1 when
 * all of the input has been processed.
 *
 * @constructor Creates an ailia Speech instance.
 * @param envId Environment id of the ailia execution. -1 (ENVIRONMENT_ID_AUTO) selects the environment automatically.
 * @param numThread Number of threads. 0 (MULTITHREAD_AUTO) means the system's logical processor count.
 * @param memoryMode Memory management mode of the ailia execution. The default value 11 reduces the memory consumption.
 * @param task Task of the recognition, one of the AILIA_SPEECH_TASK_* constants.
 * @param flags Flags of the recognition, one of the AILIA_SPEECH_FLAG_* constants.
 */
class AiliaSpeech(
    envId: Int = -1, // Ailia.ENVIRONMENT_ID_AUTO
    numThread: Int = 0, // Ailia.MULTITHREAD_AUTO
    memoryMode: Int = 11, // Ailia.MEMORY_REDUCE_CONSTANT or MEMORY_REDUCE_CONSTANT_WITH_INPUT_INITIALIZER or MEMORY_REUSE_INTERSTAGE,
    task: Int = AILIA_SPEECH_TASK_TRANSCRIBE,
    flags: Int = AILIA_SPEECH_FLAG_NONE,
) {
    /**
     * Constants of ailia Speech.
     *
     * AILIA_SPEECH_MODEL_TYPE_* defines the model type passed to [openModel],
     * AILIA_SPEECH_TASK_* and AILIA_SPEECH_FLAG_* define the task and the flags
     * passed to the constructor.
     */
    companion object {
        // Model types

        /** Whisper Tiny model. */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_TINY = (0)

        /** Whisper Base model. */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_BASE = (1)

        /** Whisper Small model. */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_SMALL = (2)

        /** Whisper Medium model. */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_MEDIUM = (3)

        /** Whisper Large model. */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE = (4)

        /** Whisper Large V3 model. */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE_V3 = (5)

        /** SenseVoice Small model. */
        const val AILIA_SPEECH_MODEL_TYPE_SENSEVOICE_SMALL = (10)

        // Tasks

        /** Transcribe mode. */
        const val AILIA_SPEECH_TASK_TRANSCRIBE = (0)

        /** Translate mode (translate to English). */
        const val AILIA_SPEECH_TASK_TRANSLATE = (1)

        // Constraints

        /** Constraint by characters. */
        const val AILIA_SPEECH_CONSTRAINT_CHARACTERS = (0)

        /** Constraint by words. Separate words with commas. */
        const val AILIA_SPEECH_CONSTRAINT_WORDS = (1)

        // Flags

        /** Default flag (no option). */
        const val AILIA_SPEECH_FLAG_NONE = (0)

        /** Live mode. */
        const val AILIA_SPEECH_FLAG_LIVE = (1)

        // VAD types

        /** SileroVAD. */
        const val AILIA_SPEECH_VAD_TYPE_SILERO = (0)

        // Diarization types

        /** Pyannote Audio. */
        const val AILIA_SPEECH_DIARIZATION_TYPE_PYANNOTE_AUDIO = (0)

        // Dictionary types

        /** Dictionary for replace. */
        const val AILIA_SPEECH_DICTIONARY_TYPE_REPLACE = (0)

        // Post process types

        /** T5 (speech recognition error correction). */
        const val AILIA_SPEECH_POST_PROCESS_TYPE_T5 = (0)

        /** FuguMT translation from English to Japanese. */
        const val AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_EN_JA = (1)

        /** FuguMT translation from Japanese to English. */
        const val AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_JA_EN = (2)

        /** Indicates that speaker_id is invalid (set when speaker diarization is disabled, etc.). */
        const val AILIA_SPEECH_SPEAKER_ID_UNKNOWN = (0xFFFFFFFF)


        init {
            System.loadLibrary("ailia_speech")
        }
    }

    private var ailiaSpeech: Long = 0

    init {
        ailiaSpeech = create(envId, numThread, memoryMode, task, flags)
    }

    /**
     * Destroys the ailia Speech instance.
     */
    fun close() {
        destroy(ailiaSpeech)
    }

    /**
     * Opens the speech recognition model files.
     *
     * @param encoderPath Path to the encoder model file.
     * @param decoderPath Path to the decoder model file.
     * @param modelType Type of the model, one of the AILIA_SPEECH_MODEL_TYPE_* constants.
     * @return 0 if successful, otherwise an error code.
     */
    fun openModel(encoderPath: String, decoderPath: String, modelType: Int): Int {
        return openModelFile(ailiaSpeech, encoderPath, decoderPath, modelType)
    }

    /**
     * Opens the voice activity detection (VAD) model file.
     *
     * @param vadPath Path to the VAD model file.
     * @param vadType Type of the VAD, one of the AILIA_SPEECH_VAD_TYPE_* constants.
     * @return 0 if successful, otherwise an error code.
     */
    fun openVad(vadPath: String, vadType: Int): Int {
        return openVadFile(ailiaSpeech, vadPath, vadType)
    }

    /**
     * Opens a dictionary file for correcting the recognition results.
     *
     * @param dictionaryPath Path to the dictionary file.
     * @param dictionaryType Type of the dictionary, one of the AILIA_SPEECH_DICTIONARY_TYPE_* constants.
     * @return 0 if successful, otherwise an error code.
     */
    fun openDictionary(dictionaryPath: String, dictionaryType: Int): Int {
        return openDictionaryFile(ailiaSpeech, dictionaryPath, dictionaryType)
    }

    /**
     * Opens the post process model files for error correction or translation
     * of the recognition results.
     *
     * @param encoderPath Path to the encoder model file.
     * @param decoderPath Path to the decoder model file.
     * @param sourcePath Path to the source SentencePiece model file.
     * @param targetPath Path to the target SentencePiece model file.
     * @param prefix Prefix text passed to the model.
     * @param postProcessType Type of the post process, one of the AILIA_SPEECH_POST_PROCESS_TYPE_* constants.
     * @return 0 if successful, otherwise an error code.
     */
    fun openPostProcess(encoderPath: String, decoderPath: String, sourcePath: String, targetPath: String, prefix: String, postProcessType: Int): Int {
        return openPostProcessFile(ailiaSpeech, encoderPath, decoderPath, sourcePath, targetPath, prefix, postProcessType)
    }

    /**
     * Opens the speaker diarization model files.
     *
     * @param segmentationPath Path to the segmentation model file.
     * @param embeddingPath Path to the speaker embedding model file.
     * @param diarizationType Type of the diarization, one of the AILIA_SPEECH_DIARIZATION_TYPE_* constants.
     * @return 0 if successful, otherwise an error code.
     */
    fun openDiarization(segmentationPath: String, embeddingPath: String, diarizationType: Int): Int {
        return openDiarizationFile(ailiaSpeech, segmentationPath, embeddingPath, diarizationType)
    }

    /**
     * Pushes the audio waveform to the queue.
     *
     * It is not necessary to input the whole audio data at once, it is
     * possible to feed it little by little, so that it can be used in
     * real-time with the input from a microphone.
     *
     * @param src Audio waveform (PCM in the range -1.0 to 1.0, interleaved when multi channel).
     * @param channels Number of channels.
     * @param samples Number of samples per channel.
     * @param samplingRate Sampling rate in Hz.
     * @return 0 if successful, otherwise an error code.
     */
    fun pushInputData(src: FloatArray, channels: Int, samples: Int, samplingRate: Int): Int {
        return pushInputData(ailiaSpeech, src, channels, samples, samplingRate)
    }

    /**
     * Resets the transcribe state to process a new audio input.
     *
     * You must call this method after calling [finalizeInputData] and before
     * calling [pushInputData] again.
     *
     * @return 0 if successful, otherwise an error code.
     */
    fun resetTranscribeState(): Int {
        return resetTranscribeState(ailiaSpeech)
    }

    /**
     * Notifies the end of the audio input.
     *
     * By signaling the end of the audio file, [getBuffered] will return 1
     * even if 30 seconds worth of data does not exist.
     *
     * @return 0 if successful, otherwise an error code.
     */
    fun finalizeInputData(): Int {
        return finalizeInputData(ailiaSpeech)
    }

    /**
     * Determines if enough audio data has been fed to perform the transcription.
     *
     * @return 1 if [transcribe] can be called, 0 otherwise.
     */
    fun getBuffered(): Int {
        return getBuffered(ailiaSpeech)
    }

    /**
     * Determines if all of the audio data has been processed.
     *
     * @return 1 if all of the pushed audio data has been processed, 0 otherwise.
     */
    fun getComplete(): Int {
        return getComplete(ailiaSpeech)
    }

    /**
     * Sets the prompt text given to the model.
     *
     * @param prompt The text of the prompt (UTF-8).
     * @return 0 if successful, otherwise an error code.
     */
    fun setPrompt(prompt: String): Int {
        return setPrompt(ailiaSpeech, prompt)
    }

    /**
     * Sets the constraint of the recognition output.
     *
     * @param constraint The text of the constraint (UTF-8).
     * @param type Type of the constraint, one of the AILIA_SPEECH_CONSTRAINT_* constants.
     * @return 0 if successful, otherwise an error code.
     */
    fun setConstraint(constraint: String, type: Int): Int {
        return setConstraint(ailiaSpeech, constraint, type)
    }

    /**
     * Sets the language of the speech recognition.
     *
     * @param language Language code (e.g. "ja", "en" or "auto" for automatic detection).
     * @return 0 if successful, otherwise an error code.
     */
    fun setLanguage(language: String): Int {
        return setLanguage(ailiaSpeech, language)
    }

    /**
     * Sets the silent threshold of the voice activity detection.
     *
     * @param silentThreshold Threshold of the VAD output to detect a speech (0.0 to 1.0).
     * @param speechSec Duration in seconds to detect the start of a speech.
     * @param noSpeechSec Duration in seconds to detect the end of a speech.
     * @return 0 if successful, otherwise an error code.
     */
    fun setSilentThreshold(silentThreshold: Float, speechSec: Float, noSpeechSec: Float): Int {
        return setSilentThreshold(ailiaSpeech, silentThreshold, speechSec, noSpeechSec)
    }

    /**
     * Sets the callback for receiving intermediate recognition results.
     *
     * @param callback Callback called when intermediate results are available.
     * @return 0 if successful, otherwise an error code.
     */
    fun setIntermediateCallback(callback: IntermediateCallback): Int {
        return setIntermediateCallback(ailiaSpeech, callback)
    }

    /**
     * Performs the speech recognition of the buffered audio data.
     *
     * Call this method when [getBuffered] returns 1, then get the results
     * with [getTextCount] and [getText].
     *
     * @return 0 if successful, otherwise an error code.
     */
    fun transcribe(): Int {
        return transcribe(ailiaSpeech)
    }

    /**
     * Performs the post process (error correction or translation) of the
     * recognition results.
     *
     * @return 0 if successful, otherwise an error code.
     */
    fun postProcess(): Int {
        return postProcess(ailiaSpeech)
    }

    /**
     * Returns the number of the recognized text fragments.
     *
     * @return Number of the text fragments.
     */
    fun getTextCount(): Int {
        return getTextCount(ailiaSpeech)
    }

    /**
     * Returns the recognized text fragment.
     *
     * @param idx Index of the text fragment (0 to [getTextCount] - 1).
     * @return Recognized text fragment.
     */
    fun getText(idx: Int): AiliaSpeechText {
        return getText(ailiaSpeech, idx)
    }

    /**
     * Returns the details of the last error.
     *
     * @return Error message (UTF-8).
     */
    fun getErrorDetail(): String {
        return getErrorDetail(ailiaSpeech)
    }


    ///
    // JNI
    ///
    private external fun create(env_id: Int, num_thread: Int, memory_mode: Int, task: Int, flags: Int): Long

    private external fun destroy(handle: Long)

    private external fun openModelFile(handle: Long, encoder_path: String, decoder_path: String, model_type: Int): Int

    private external fun openVadFile(handle: Long, vad_pathL: String, vad_type: Int): Int

    private external fun openDictionaryFile(handle: Long, dictionary_path: String, dictionary_type: Int): Int

    private external fun openPostProcessFile(handle: Long, encoder_path: String, decoder_path: String, source_path: String, target_path: String, prefix: String, post_process_type: Int): Int

    private external fun openDiarizationFile(handle: Long, segmentation_path: String, embedding_path: String, type: Int): Int

    private external fun pushInputData(handle: Long, src: FloatArray, channels: Int, samples: Int, sampling_rate: Int): Int

    private external fun resetTranscribeState(handle: Long): Int

    private external fun finalizeInputData(handle: Long): Int

    private external fun getBuffered(handle: Long): Int

    private external fun getComplete(handle: Long): Int

    private external fun setPrompt(handle: Long, prompt: String): Int

    private external fun setConstraint(handle: Long, constraint: String, type: Int): Int

    private external fun setLanguage(handle: Long, language: String): Int

    private external fun setSilentThreshold(handle: Long, silent_threshold: Float, speech_sec: Float, no_speech_sec: Float): Int

    private external fun setIntermediateCallback(handle: Long, callback: IntermediateCallback): Int

    private external fun transcribe(handle: Long): Int

    private external fun postProcess(handle: Long): Int

    private external fun getTextCount(handle: Long): Int

    private external fun getText(handle: Long, idx: Int): AiliaSpeechText

    private external fun getErrorDetail(handle: Long): String
}
