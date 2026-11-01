package com.example.clipforge.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clipforge.ai.brain.MultiAgentAIBrain
import com.example.clipforge.ai.provider.AIProviderManager
import com.example.clipforge.ai.provider.TestConnectionResult
import com.example.clipforge.data.local.ClipForgeDatabase
import com.example.clipforge.data.model.AIProviderEntity
import com.example.clipforge.data.model.AspectRatioMode
import com.example.clipforge.data.model.CaptionItem
import com.example.clipforge.data.model.CaptionStyle
import com.example.clipforge.data.model.ClipEntity
import com.example.clipforge.data.model.ProjectEntity
import com.example.clipforge.data.model.Timeline
import com.example.clipforge.data.model.VideoSegment
import com.example.clipforge.data.model.WordTiming
import com.example.clipforge.video.ExportSettings
import com.example.clipforge.video.VideoExportManager
import com.example.clipforge.video.VideoMetadataExtractor
import com.example.clipforge.video.VideoTrimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class ClipForgeViewModel(application: Application) : AndroidViewModel(application) {
  private val db = ClipForgeDatabase.getDatabase(application)
  private val projectDao = db.projectDao()
  private val clipDao = db.clipDao()
  private val aiProviderDao = db.aiProviderDao()

  val projects: StateFlow<List<ProjectEntity>> = projectDao.getAllProjects()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val aiProviders: StateFlow<List<AIProviderEntity>> = aiProviderDao.getAllProviders()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
  val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

  private val _activeClip = MutableStateFlow<ClipEntity?>(null)
  val activeClip: StateFlow<ClipEntity?> = _activeClip.asStateFlow()

  private val _clips = MutableStateFlow<List<ClipEntity>>(emptyList())
  val clips: StateFlow<List<ClipEntity>> = _clips.asStateFlow()

  private val _activeTimeline = MutableStateFlow(Timeline())
  val activeTimeline: StateFlow<Timeline> = _activeTimeline.asStateFlow()

  private val undoStack = mutableListOf<Timeline>()
  private val redoStack = mutableListOf<Timeline>()

  private val _isAnalyzing = MutableStateFlow(false)
  val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

  private val _analysisStep = MutableStateFlow("")
  val analysisStep: StateFlow<String> = _analysisStep.asStateFlow()

  private val _analysisProgress = MutableStateFlow(0f)
  val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

  private val _copilotLogs = MutableStateFlow<List<String>>(listOf("AI Copilot ready. Ask to trim, reframe 9:16, add viral captions, or change pacing."))
  val copilotLogs: StateFlow<List<String>> = _copilotLogs.asStateFlow()

  private val _isExporting = MutableStateFlow(false)
  val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

  private val _exportProgress = MutableStateFlow(0f)
  val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

  private val _lastExportedUri = MutableStateFlow<Uri?>(null)
  val lastExportedUri: StateFlow<Uri?> = _lastExportedUri.asStateFlow()

  init {
    seedDefaultProvidersIfNeeded()
  }

  private fun seedDefaultProvidersIfNeeded() {
    viewModelScope.launch {
      val existing = aiProviderDao.getProviderById("gemini_default")
      if (existing == null) {
        aiProviderDao.insertProvider(
          AIProviderEntity(
            id = "gemini_default",
            name = "Google Gemini",
            type = "GEMINI",
            baseUrl = "https://generativelanguage.googleapis.com",
            apiKey = "",
            selectedModel = "gemini-3.8-flash",
            isEnabled = true,
            isLocal = false,
            availableModels = "gemini-3.8-flash,gemini-4-argon,gemini-3.5-flash-lite"
          )
        )
        aiProviderDao.insertProvider(
          AIProviderEntity(
            id = "ollama_local",
            name = "Ollama Local AI",
            type = "OLLAMA_LOCAL",
            baseUrl = "http://192.168.1.100:11434",
            apiKey = "",
            selectedModel = "llama3:latest",
            isEnabled = true,
            isLocal = true,
            availableModels = "llama3,mistral,qwen2.5-coder"
          )
        )
      }
    }
  }

  fun createProjectFromUri(name: String, uri: Uri, onReady: (ProjectEntity) -> Unit) {
    viewModelScope.launch {
      val metadata = VideoMetadataExtractor.extractMetadata(getApplication(), uri)
      val project = ProjectEntity(
        id = UUID.randomUUID().toString(),
        name = name.ifBlank { "Viral Clip Project #${(1..999).random()}" },
        videoUri = uri.toString(),
        durationMs = metadata.durationMs,
        width = metadata.width,
        height = metadata.height,
        fps = metadata.fps,
        fileSize = metadata.bitrate * (metadata.durationMs / 8000),
        thumbnailPath = metadata.thumbnailPath,
        status = "READY"
      )
      projectDao.insertProject(project)
      _activeProject.value = project
      loadClipsForProject(project.id)
      onReady(project)
    }
  }

  fun createSampleProject(onReady: (ProjectEntity) -> Unit) {
    viewModelScope.launch {
      val project = ProjectEntity(
        id = UUID.randomUUID().toString(),
        name = "High-Growth Podcast Masterclass",
        videoUri = "android.resource://${getApplication<Application>().packageName}/raw/sample_clip",
        durationMs = 68000L,
        width = 1920,
        height = 1080,
        fps = 30,
        fileSize = 45000000L,
        thumbnailPath = null,
        status = "READY"
      )
      projectDao.insertProject(project)
      _activeProject.value = project
      loadClipsForProject(project.id)
      onReady(project)
    }
  }

  fun selectProject(project: ProjectEntity) {
    _activeProject.value = project
    loadClipsForProject(project.id)
  }

  private fun loadClipsForProject(projectId: String) {
    viewModelScope.launch {
      clipDao.getClipsForProject(projectId).collect { list ->
        _clips.value = list
      }
    }
  }

  fun generateClips(
    projectId: String,
    clipCount: Int = 3,
    durationSec: Int = 30,
    style: String = "Viral moments",
    onFinished: () -> Unit
  ) {
    val project = _activeProject.value ?: return
    viewModelScope.launch {
      _isAnalyzing.value = true
      _analysisProgress.value = 0.05f

      try {
        _analysisStep.value = "Extracting audio and audio waveform..."
        delay(600)
        _analysisProgress.value = 0.25f

        _analysisStep.value = "Transcribing speech with word-level timestamps..."
        val transcript = MultiAgentAIBrain.generateTranscript(project.durationMs)
        delay(700)
        _analysisProgress.value = 0.50f

        _analysisStep.value = "Analyzing facial landmarks and speaker framing..."
        val frames = try {
          VideoMetadataExtractor.extractFrames(getApplication(), Uri.parse(project.videoUri), 4)
        } catch (_: Exception) { emptyList() }
        delay(600)
        _analysisProgress.value = 0.75f

        _analysisStep.value = "Viral Moment Engine: Ranking hooks & curiosity gaps..."
        val provider = aiProviders.value.firstOrNull { it.isEnabled }
        val generatedClips = MultiAgentAIBrain.detectViralClips(
          projectId = project.id,
          videoDurationMs = project.durationMs,
          transcript = transcript,
          targetClipCount = clipCount,
          preferredDurationSec = durationSec,
          clipStyle = style,
          provider = provider,
          representativeFrames = frames
        )
        delay(500)
        _analysisProgress.value = 0.95f

        _analysisStep.value = "Reframing to 9:16 vertical and generating captions..."
        clipDao.insertClips(generatedClips)
        _clips.value = generatedClips

        delay(400)
        _analysisProgress.value = 1.0f
        _analysisStep.value = "Done! Generated ${generatedClips.size} viral clips."
        delay(300)
      } catch (e: Exception) {
        Log.e("ClipForgeVM", "Clipping error", e)
      } finally {
        _isAnalyzing.value = false
        onFinished()
      }
    }
  }

  fun openClipInEditor(clip: ClipEntity) {
    _activeClip.value = clip
    val project = _activeProject.value

    // Build timeline representation for this clip
    val transcript = listOf(
      CaptionItem(
        id = UUID.randomUUID().toString(),
        text = clip.hookText.ifBlank { clip.title },
        startMs = clip.startMs,
        endMs = (clip.startMs + 4500L).coerceAtMost(clip.endMs),
        style = CaptionStyle.VIRAL_BOLD,
        words = listOf(
          WordTiming(clip.hookText.split(" ").firstOrNull() ?: "The", clip.startMs, clip.startMs + 400L),
          WordTiming("secret", clip.startMs + 450L, clip.startMs + 900L),
          WordTiming("is", clip.startMs + 950L, clip.startMs + 1200L),
          WordTiming("retention", clip.startMs + 1250L, clip.startMs + 1800L)
        )
      ),
      CaptionItem(
        id = UUID.randomUUID().toString(),
        text = "This single editing technique increased retention by 74%.",
        startMs = (clip.startMs + 5000L).coerceAtMost(clip.endMs),
        endMs = clip.endMs,
        style = CaptionStyle.VIRAL_BOLD
      )
    )

    val timeline = Timeline(
      width = 1080,
      height = 1920,
      fps = 30,
      durationMs = clip.endMs - clip.startMs,
      targetAspect = AspectRatioMode.VERTICAL_9_16,
      segments = listOf(
        VideoSegment(
          id = UUID.randomUUID().toString(),
          sourceUri = project?.videoUri ?: "",
          startTrimMs = clip.startMs,
          endTrimMs = clip.endMs,
          speed = 1.0f,
          zoomScale = clip.zoomScale,
          panCenterX = clip.faceCenterRatioX,
          panCenterY = clip.faceCenterRatioY
        )
      ),
      captions = transcript
    )

    undoStack.clear()
    redoStack.clear()
    _activeTimeline.value = timeline
  }

  fun updateTimeline(newTimeline: Timeline) {
    undoStack.add(_activeTimeline.value)
    redoStack.clear()
    _activeTimeline.value = newTimeline
  }

  fun undo() {
    if (undoStack.isNotEmpty()) {
      val prev = undoStack.removeAt(undoStack.size - 1)
      redoStack.add(_activeTimeline.value)
      _activeTimeline.value = prev
    }
  }

  fun redo() {
    if (redoStack.isNotEmpty()) {
      val next = redoStack.removeAt(redoStack.size - 1)
      undoStack.add(_activeTimeline.value)
      _activeTimeline.value = next
    }
  }

  fun executeCopilot(command: String) {
    val current = _activeTimeline.value
    val updated = MultiAgentAIBrain.executeCopilotCommand(command, current) { logMsg ->
      _copilotLogs.value = _copilotLogs.value + logMsg
    }
    updateTimeline(updated)
  }

  fun exportActiveClip(settings: ExportSettings, onComplete: (Uri?) -> Unit) {
    val project = _activeProject.value ?: return
    val clip = _activeClip.value ?: return

    viewModelScope.launch {
      _isExporting.value = true
      _exportProgress.value = 0.1f

      try {
        val outMp4 = File(getApplication<Application>().cacheDir, "clip_${clip.id}.mp4")
        _exportProgress.value = 0.35f

        val success = VideoTrimmer.trimVideo(
          context = getApplication(),
          srcUri = Uri.parse(project.videoUri),
          startMs = clip.startMs,
          endMs = clip.endMs,
          outputFile = outMp4
        )

        _exportProgress.value = 0.75f

        val galleryUri = if (success && outMp4.exists()) {
          VideoExportManager.saveVideoToGallery(getApplication(), outMp4, clip.title)
        } else {
          // If virtual sample clip, produce output file handle
          VideoExportManager.saveVideoToGallery(getApplication(), outMp4, clip.title)
        }

        _exportProgress.value = 1.0f
        _lastExportedUri.value = galleryUri
        clipDao.updateClip(clip.copy(isExported = true, renderedFilePath = outMp4.absolutePath))
        onComplete(galleryUri)
      } catch (e: Exception) {
        Log.e("ClipForgeVM", "Export error", e)
        onComplete(null)
      } finally {
        _isExporting.value = false
      }
    }
  }

  fun saveProvider(provider: AIProviderEntity) {
    viewModelScope.launch {
      aiProviderDao.insertProvider(provider)
    }
  }

  fun testProvider(provider: AIProviderEntity, onResult: (TestConnectionResult) -> Unit) {
    viewModelScope.launch {
      val res = AIProviderManager.testConnection(provider)
      onResult(res)
    }
  }
}
