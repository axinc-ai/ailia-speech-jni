package axip.ailia_speech

interface IntermediateCallback {
    /**
     * Called when intermediate speech recognition results are available.
     *
     * @param text The intermediate recognition text
     * @return 0 to continue recognition, non-zero to abort
     */
    fun onIntermediateResult(text: String): Int
}

data class AiliaSpeechText(
    var text: String? = null,
    var timeStampBegin: Float = 0f,
    var timeStampEnd: Float = 0f,
    var speakerId: Int = 0,
    var language: String? = null,
    var confidence: Float = 0f
)

class AiliaSpeech(
    envId: Int = -1, // Ailia.ENVIRONMENT_ID_AUTO
    numThread: Int = 0, // Ailia.MULTITHREAD_AUTO
    memoryMode: Int = 11, // Ailia.MEMORY_REDUCE_CONSTANT or MEMORY_REDUCE_CONSTANT_WITH_INPUT_INITIALIZER or MEMORY_REUSE_INTERSTAGE,
    task: Int = AILIA_SPEECH_TASK_TRANSCRIBE,
    flags: Int = AILIA_SPEECH_FLAG_NONE,
) {
    companion object {
        /****************************************************************
         * モデルタイプ定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_TINY
         * @brief Whisper Tiny model
         *
         * \~english
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_TINY
         * @brief Whisper Tiny model
         */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_TINY = (0)

        /**
         * \~japanese
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_BASE
         * @brief Whisper Base model
         *
         * \~english
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_BASE
         * @brief Whisper Base model
         */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_BASE = (1)

        /**
         * \~japanese
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_SMALL
         * @brief Whisper Small model
         *
         * \~english
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_SMALL
         * @brief Whisper Small model
         */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_SMALL = (2)

        /**
         * \~japanese
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_MEDIUM
         * @brief Whisper Medium model
         *
         * \~english
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_MEDIUM
         * @brief Whisper Medium model
         */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_MEDIUM = (3)

        /**
         * \~japanese
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE
         * @brief Whisper Large model
         *
         * \~english
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE
         * @brief Whisper Large model
         */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE = (4)

        /**
         * \~japanese
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE_V3
         * @brief Whisper Large V3 model
         *
         * \~english
         * @def AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE_V3
         * @brief Whisper Large V3 model
         */
        const val AILIA_SPEECH_MODEL_TYPE_WHISPER_MULTILINGUAL_LARGE_V3 = (5)

        /****************************************************************
         * タスク定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_TASK_TRANSCRIBE
         * @brief Transcribe mode
         *
         * \~english
         * @def AILIA_SPEECH_TASK_TRANSCRIBE
         * @brief Transcribe mode
         */
        const val AILIA_SPEECH_TASK_TRANSCRIBE = (0)

        /**
         * \~japanese
         * @def AILIA_SPEECH_TASK_TRANSLATE
         * @brief Translate mode
         *
         * \~english
         * @def AILIA_SPEECH_TASK_TRANSLATE
         * @brief Translate mode
         */
        const val AILIA_SPEECH_TASK_TRANSLATE = (1)

        /****************************************************************
         * 制約定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_CONSTRAINT_CHARACTERS
         * @brief 文字の制約を行います。
         *
         * \~english
         * @def AILIA_SPEECH_CONSTRAINT_CHARACTERS
         * @brief Constraint by characters
         */
        const val AILIA_SPEECH_CONSTRAINT_CHARACTERS = (0)

        /**
         * \~japanese
         * @def AILIA_SPEECH_CONSTRAINT_WORDS
         * @brief 単語の制約を行います。単語はカンマで区切ります。
         *
         * \~english
         * @def AILIA_SPEECH_CONSTRAINT_WORDS
         * @brief Constraint by words. Separate words with commas.
         */
        const val AILIA_SPEECH_CONSTRAINT_WORDS = (1)

        /****************************************************************
         * フラグ定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_FLAG_NONE
         * @brief Default flag
         *
         * \~english
         * @def AILIA_SPEECH_FLAG_NONE
         * @brief Default flag
         */
        const val AILIA_SPEECH_FLAG_NONE = (0)

        /**
         * \~japanese
         * @def AILIA_SPEECH_FLAG_LIVE
         * @brief Live mode
         *
         * \~english
         * @def AILIA_SPEECH_FLAG_LIVE
         * @brief Live mode
         */
        const val AILIA_SPEECH_FLAG_LIVE = (1)

        /****************************************************************
         * VAD定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_VAD_TYPE_SILERO
         * @brief SileroVAD
         *
         * \~english
         * @def AILIA_SPEECH_VAD_TYPE_SILERO
         * @brief SileroVAD
         */
        const val AILIA_SPEECH_VAD_TYPE_SILERO = (0)

        /****************************************************************
         * DIARIZATION定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_DIARIZATION_TYPE_PYANNOTE_AUDIO
         * @brief PyannoteAudio
         *
         * \~english
         * @def AILIA_SPEECH_DIARIZATION_TYPE_PYANNOTE_AUDIO
         * @brief PyannoteAudio
         */
        const val AILIA_SPEECH_DIARIZATION_TYPE_PYANNOTE_AUDIO = (0)

        /****************************************************************
         * 辞書定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_DICTIONARY_TYPE_REPLACE
         * @brief 置換辞書
         *
         * \~english
         * @def AILIA_SPEECH_DICTIONARY_TYPE_REPLACE
         * @brief Dictionary for replace
         */
        const val AILIA_SPEECH_DICTIONARY_TYPE_REPLACE = (0)

        /****************************************************************
         * 後処理定義
         **/

        /**
         * \~japanese
         * @def AILIA_SPEECH_POST_PROCESS_TYPE_T5
         * @brief T5
         *
         * \~english
         * @def AILIA_SPEECH_POST_PROCESS_TYPE_T5
         * @brief T5
         */
        const val AILIA_SPEECH_POST_PROCESS_TYPE_T5 = (0)

        /**
         * \~japanese
         * @def AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_EN_JA
         * @brief FuguMT EN JA
         *
         * \~english
         * @def AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_EN_JA
         * @brief FuguMT EN JA
         */
        const val AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_EN_JA = (1)

        /**
         * \~japanese
         * @def AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_JA_EN
         * @brief FuguMT JA EN
         *
         * \~english
         * @def AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_JA_EN
         * @brief FuguMT JA EN
         */
        const val AILIA_SPEECH_POST_PROCESS_TYPE_FUGUMT_JA_EN = (2)

        /**
         * \~japanese
         * @def AILIA_SPEECH_SPEAKER_ID_UNKNOWN
         * @brief speaker_id が無効であることを示す値 (話者分離無効時などに設定される)
         *
         * \~english
         * @def AILIA_SPEECH_SPEAKER_ID_UNKNOWN
         * @brief indicate that speaker_id is invalid (set when speaker separation is disabled, etc.)
         */
        const val AILIA_SPEECH_SPEAKER_ID_UNKNOWN = (0xFFFFFFFF)


        init {
            System.loadLibrary("ailia_speech")
        }
    }

    private var ailiaSpeech: Long = 0

    init {
        ailiaSpeech = create(envId, numThread, memoryMode, task, flags)
    }

    fun close() {
        destroy(ailiaSpeech)
    }

    fun openModel(encoderPath: String, decoderPath: String, modelType: Int): Int {
        return openModelFile(ailiaSpeech, encoderPath, decoderPath, modelType)
    }

    fun openVad(vadPath: String, vadType: Int): Int {
        return openVadFile(ailiaSpeech, vadPath, vadType)
    }

    fun openDictionary(dictionaryPath: String, dictionaryType: Int): Int {
        return openDictionaryFile(ailiaSpeech, dictionaryPath, dictionaryType)
    }

    fun openPostProcess(encoderPath: String, decoderPath: String, sourcePath: String, targetPath: String, prefix: String, postProcessType: Int): Int {
        return openPostProcessFile(ailiaSpeech, encoderPath, decoderPath, sourcePath, targetPath, prefix, postProcessType)
    }

    fun openDiarization(segmentationPath: String, embeddingPath: String, diarizationType: Int): Int {
        return openDiarizationFile(ailiaSpeech, segmentationPath, embeddingPath, diarizationType)
    }

    fun pushInputData(src: FloatArray, channels: Int, samples: Int, samplingRate: Int): Int {
        return pushInputData(ailiaSpeech, src, channels, samples, samplingRate)
    }

    fun resetTranscribeState(): Int {
        return resetTranscribeState(ailiaSpeech)
    }

    fun finalizeInputData(): Int {
        return finalizeInputData(ailiaSpeech)
    }

    fun getBuffered(): Int {
        return getBuffered(ailiaSpeech)
    }

    fun getComplete(): Int {
        return getComplete(ailiaSpeech)
    }

    fun setPrompt(prompt: String): Int {
        return setPrompt(ailiaSpeech, prompt)
    }

    fun setConstraint(constraint: String, type: Int): Int {
        return setConstraint(ailiaSpeech, constraint, type)
    }

    fun setLanguage(language: String): Int {
        return setLanguage(ailiaSpeech, language)
    }

    fun setSilentThreshold(silentThreshold: Float, speechSec: Float, noSpeechSec: Float): Int {
        return setSilentThreshold(ailiaSpeech, silentThreshold, speechSec, noSpeechSec)
    }

    fun setIntermediateCallback(callback: IntermediateCallback): Int {
        return setIntermediateCallback(ailiaSpeech, callback)
    }

    fun transcribe(): Int {
        return transcribe(ailiaSpeech)
    }

    fun postProcess(): Int {
        return postProcess(ailiaSpeech)
    }

    fun getTextCount(): Int {
        return getTextCount(ailiaSpeech)
    }

    fun getText(idx: Int): AiliaSpeechText {
        return getText(ailiaSpeech, idx)
    }

    fun transcribeState(): Int {
        return transcribeState(ailiaSpeech)
    }

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

    private external fun transcribeState(handle: Long): Int

    private external fun getErrorDetail(handle: Long): String
}