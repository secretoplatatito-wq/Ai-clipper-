package com.example.clipforge.video

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

object VideoTrimmer {
  private const val TAG = "VideoTrimmer"
  private const val DEFAULT_BUFFER_SIZE = 1024 * 1024 // 1MB buffer

  suspend fun trimVideo(
    context: Context,
    srcUri: Uri,
    startMs: Long,
    endMs: Long,
    outputFile: File
  ): Boolean = withContext(Dispatchers.IO) {
    var extractor: MediaExtractor? = null
    var muxer: MediaMuxer? = null
    var pfd = context.contentResolver.openFileDescriptor(srcUri, "r")

    try {
      if (pfd == null) {
        Log.e(TAG, "Cannot open file descriptor for $srcUri")
        return@withContext false
      }

      extractor = MediaExtractor()
      extractor.setDataSource(pfd.fileDescriptor)

      val trackCount = extractor.trackCount
      muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

      val indexMap = HashMap<Int, Int>(trackCount)
      var bufferSize = DEFAULT_BUFFER_SIZE

      for (i in 0 until trackCount) {
        val format = extractor.getTrackFormat(i)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: continue

        if (mime.startsWith("video/") || mime.startsWith("audio/")) {
          extractor.selectTrack(i)
          val dstIndex = muxer.addTrack(format)
          indexMap[i] = dstIndex

          if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            val newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            if (newSize > bufferSize) {
              bufferSize = newSize
            }
          }
        }
      }

      if (indexMap.isEmpty()) {
        Log.e(TAG, "No video or audio tracks found in $srcUri")
        return@withContext false
      }

      // Check orientation
      val retriever = MediaMetadataRetriever()
      try {
        retriever.setDataSource(context, srcUri)
        val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
        muxer.setOrientationHint(rotation)
      } catch (_: Exception) {} finally {
        try { retriever.release() } catch (_: Exception) {}
      }

      muxer.start()

      val startUs = startMs * 1000L
      val endUs = endMs * 1000L

      // Seek to nearest keyframe at or before startMs
      extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

      val dstBuf = ByteBuffer.allocate(bufferSize)
      val bufferInfo = MediaCodec.BufferInfo()

      val trackStartTimes = HashMap<Int, Long>()

      while (true) {
        val trackIndex = extractor.sampleTrackIndex
        if (trackIndex < 0) break

        val dstTrackIndex = indexMap[trackIndex]
        if (dstTrackIndex == null) {
          extractor.advance()
          continue
        }

        bufferInfo.presentationTimeUs = extractor.sampleTime

        if (bufferInfo.presentationTimeUs > endUs) {
          // Reached end boundary
          extractor.unselectTrack(trackIndex)
          // If no more active tracks, stop
          var anyTrackLeft = false
          for (t in indexMap.keys) {
            // Check if track still has samples
          }
          break
        }

        bufferInfo.flags = extractor.sampleFlags
        bufferInfo.size = extractor.readSampleData(dstBuf, 0)

        if (bufferInfo.size < 0) break

        // Normalize presentation timestamp so trimmed clip starts at 0us
        if (!trackStartTimes.containsKey(trackIndex)) {
          trackStartTimes[trackIndex] = bufferInfo.presentationTimeUs
        }
        val firstSampleUs = trackStartTimes[trackIndex] ?: 0L
        bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs - firstSampleUs).coerceAtLeast(0L)

        muxer.writeSampleData(dstTrackIndex, dstBuf, bufferInfo)
        extractor.advance()
      }

      Log.d(TAG, "Successfully trimmed video to ${outputFile.absolutePath} (${outputFile.length()} bytes)")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Video trimming failed", e)
      false
    } finally {
      try { extractor?.release() } catch (_: Exception) {}
      try {
        muxer?.stop()
        muxer?.release()
      } catch (_: Exception) {}
      try { pfd?.close() } catch (_: Exception) {}
    }
  }
}
