package id.my.karyatra.audit.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileHelper {

    fun saveAndOpenPdf(context: Context, body: ResponseBody, fileName: String): ApiResult<Unit> {
        return try {
            val file = File(context.cacheDir, "$fileName.pdf")
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null

            try {
                val fileReader = ByteArray(4096)
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) break
                    outputStream.write(fileReader, 0, read)
                }
                outputStream.flush()
            } finally {
                inputStream?.close()
                outputStream?.close()
            }

            openPdf(context, file)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Gagal menyimpan atau membuka file: ${e.localizedMessage}")
        }
    }

    private fun openPdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // Read Exif orientation
            val exifInputStream = context.contentResolver.openInputStream(uri)
            val orientation = exifInputStream?.use {
                val exif = ExifInterface(it)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Rotate bitmap if needed
            val rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }

            val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.flush()
            outputStream.close()
            
            if (rotatedBitmap != bitmap) bitmap.recycle()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}
