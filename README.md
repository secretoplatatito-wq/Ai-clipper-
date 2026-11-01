# ClipForge AI 🎬⚡

**ClipForge AI** (`com.clipforge.ai`) is an AI-powered Video Creation, Clipping, and Multi-Track Editing Platform for Android, combining the intelligent short-form clipping of **OpusClip** with the creative manual editing precision of **CapCut**.

---

## 🌟 Key Features

### 1. 🤖 AI Clipper (OpusClip-Style Automated Clipping)
- **Zero-loss Native Trimming**: Uses Android `MediaExtractor` and `MediaMuxer` to generate crisp MP4 clips without external heavy server dependencies.
- **Transcript & Timestamp Engine**: Extracts audio and segments speech with word-level precision timestamps.
- **Viral Moment Detection**: Scores candidates from 0 to 100 on 10 viral metrics (Hook Strength, Curiosity Gap, Information Density, Retention Potential, Emotional Arc, Pacing, Sound Clarity, Visual Action, Shareability, and Trend Relevance).
- **Multi-Clip Generation**: Select 3, 5, 10, 15, or custom clip targets with durations ranging from 15s to 90s.
- **Smart 9:16 Reframing**: Real-time face tracking (`android.media.FaceDetector`) dynamically centers the active speaker from horizontal 16:9 into vertical 9:16 frames with Exponential Moving Average (EMA) temporal smoothing.

### 2. ✂️ Professional Multi-Track Video Editor (CapCut-Style)
- **Multi-Track Interactive Timeline**: Main video, audio tracks, text layers, sound effects, and animated subtitle streams.
- **Non-Destructive Editing Operations**:
  - Precision trimming with start/end trim handles.
  - Split at playhead.
  - Delete & reorder segments.
  - Dynamic playback speed (0.25x, 0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x).
  - Canvas aspect ratio switching (9:16 vertical, 16:9 horizontal, 1:1 square, 4:5 social).
  - Full Undo & Redo state history stack.
- **Real-Time Video Player**: Custom Android player with zoom, pan, speed scaling, looping, and millisecond scrubbing.

### 3. 💬 Animated Karaoke Captions
- **8 Caption Presets**: Viral Bold, Podcast Style, Minimal, Cinematic, Gaming, Educational, Highlighted Words, and Classic Subtitles.
- **Word-Level Karaoke Animation**: Highlights active words in vibrant electric amber and neon cyan in sync with the speaker.
- **Direct Editor Customization**: Edit transcript text, change colors, adjust font sizes, and adjust position on screen.

### 4. 🧠 Autonomous AI Brain & Conversational Copilot
- **Autonomous Director Pipeline**: Executes multi-step video intelligence using structured planning (`StructuredEditingPlan`), internal tool orchestration (`VideoEditingTools`), and self-evaluating quality audits (`QualityController`).
- **Conversational AI Copilot**: Type natural language commands such as *"Make this video vertical 9:16"*, *"Speed up to 1.25x"*, or *"Apply viral bold captions"*, and the Copilot directly executes timeline modifications with live feedback.

### 5. 🌐 Universal BYOK AI Provider System
- **Cloud Providers**: Google Gemini, OpenAI (GPT-4o/Whisper), Anthropic Claude 3.5 Sonnet, DeepSeek, and OpenRouter.
- **Local AI / Offline Servers**: Connect to Ollama or LM Studio over local Wi-Fi / LAN IP (`http://192.168.x.x:11434`) with automated model discovery and health ping checks.
- **Task Specialization**: Assign distinct models for transcript reasoning, clip scoring, title generation, or video creation.

### 6. 🎨 AI Video Generator Studio
- Text-to-Video and Image-to-Video generation interface with aspect ratio controls, negative prompts, duration settings, and direct import to project timelines.

### 7. 📤 Production MP4 Export & Gallery Sync
- Real-time progress tracking, bitrate optimization, and direct saving to the Android device photo gallery via `MediaStore` (Android 10+ scoped storage compliant) with native Android Share Intent integration.

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Local Persistence**: Room Database (SQLite) with KSP (`ProjectEntity`, `ClipEntity`, `AIProviderEntity`)
- **Video Processing**: Android `MediaMetadataRetriever`, `MediaExtractor`, `MediaMuxer`, `FaceDetector`
- **Networking**: OkHttp 4.x / Retrofit 2.x with Coroutines
- **Image Loading**: Coil Compose
- **Architecture**: Clean Architecture / MVVM with unidirectional data flow (`StateFlow`)
- **Testing**: Robolectric Local JVM test suite (`ExampleRobolectricTest.kt`)

---

## 🚀 Building the APK

### Prerequisites
- JDK 17 or higher
- Android SDK 34/35/36 installed

### Build Commands

```bash
# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble the installable Debug APK
gradle :app:assembleDebug

# The compiled APK is generated at:
# app/build/outputs/apk/debug/app-debug.apk
# and copied to:
# build-outputs/ClipForge-AI-debug.apk
```

---

## 📦 CI/CD Automation

A GitHub Actions workflow is provided at `.github/workflows/main.yml`. On every push to `main` or pull request, the workflow:
1. Sets up JDK 17.
2. Validates Gradle dependencies and caches artifacts.
3. Executes unit and Robolectric tests (`:app:testDebugUnitTest`).
4. Compiles the debug APK (`:app:assembleDebug`).
5. Uploads `ClipForge-AI-debug.apk` as a downloadable GitHub release artifact.

---

## 📄 License
Licensed under the Apache License, Version 2.0.
