package com.chatgptlite.wanted.data.llm

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.chatgptlite.wanted.models.TextCompletionsParam
import kotlinx.coroutines.flow.Flow
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class LlamaRepositoryImpl: OpenAIRepository {
    val TFLITE_MODEL_NAME = "lite_model_file.tflite" // TODO: replace this

    private var context: Context? = null
    private var tfLiteModel: MappedByteBuffer? = null
    private var tfLite: Interpreter? = null
    private var TAG: String = "LLMDataLoader"

    var model_loaded: Boolean = false
    fun getBuildStatus(): Boolean {
        return model_loaded
    }

    fun close() {

        tfLite?.close()

        tfLiteModel?.clear()

    }
    @Throws(IOException::class)
    fun initializeModel(context: Context, tfliteFile: String?): Boolean {
        this.context = context

        try {
            tfLiteModel = loadModelFile(
                context.applicationContext.assets,
                tfliteFile
            )
            Log.i(TAG, "model loaded")
            val tfLiteOptions = Interpreter.Options()
            tfLiteOptions.setNumThreads(4)
            tfLiteOptions.setUseXNNPACK(true)

            tfLite = Interpreter(tfLiteModel!!, tfLiteOptions)

            model_loaded = true
            Log.d(
                TAG,
                "Label list Loaded Successfully"
            )
            return true
        } catch (e: IOException) {
            Log.e(
                TAG,
                "TFLite Model Loading Unsuccessfull"
            )
            e.printStackTrace()
            return false
        }
    }
    @Throws(IOException::class)
    fun loadModelFile(assets: AssetManager, modelFilename: String?): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename!!)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun textCompletionsWithStream(params: TextCompletionsParam): Flow<String> {
        TODO("Not yet implemented")
    }


}