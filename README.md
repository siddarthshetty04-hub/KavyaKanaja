# 🌾 Kavya Kanaja (Poetry Granary)

**Kavya Kanaja** is a premium, feature-rich Android application built with Jetpack Compose, designed to immerse users in the beauty of Kannada literature. It transforms poetry reading from a static experience into a highly interactive, gamified, and visually stunning journey. 

This repository represents the cutting edge of modern Android UI/UX design, featuring state-of-the-art animations, a comprehensive RPG-style leveling system, and innovative learning tools.

---

## ✨ Jaw-Dropping Features

### 🎨 Next-Generation UI & Animations
*   **Holographic 3D Cards:** Poem cards feature a physics-based 3D tilt effect on drag gestures (`graphicsLayer`), responding dynamically to user touch.
*   **Living Firefly Background:** A custom Compose `Canvas` physics engine that renders 35 glowing, drifting fireflies in the background, making the app feel alive.
*   **Fluid Micro-Animations:** Spring-physics bounce effects on buttons, animated content transitions, and smooth gradient overlays.

### 📚 Immersive Reading & Audio
*   **Zen Mode:** A full-screen, distraction-free reading overlay featuring auto-scrolling lyrics and a fully animated **Audio Visualizer** that pulses with the recitation.
*   **Custom Phonetic TTS Engine:** A sophisticated state-machine parser that transliterates complex Kannada script into highly accurate English phonetic representations (e.g., inherent 'a' as 'uh', 'u' as 'oo'). This forces standard Android English TTS engines to pronounce Kannada poetry perfectly, even on devices without native Kannada voice data.
*   **Interactive Glossary:** Tap amber-highlighted words directly inside the poem to reveal instant English translations.

### ⚡ Advanced Learning Tools
*   **Spritz-Style Speed Reader:** Read poems up to 500 WPM! This tool flashes words at the center of the screen with the **Optimal Recognition Point (ORP)** highlighted in red, training your eye to read at lightning speeds.
*   **Word Match Mini-Game:** Connect Kannada words to their English meanings in an interactive, animated matching puzzle.

### 🎮 Gamification & Duolingo-Style Engagement
*   **RPG Leveling & XP System:** Earn XP by reading, taking quizzes, and playing games. A dynamic progress bar tracks your journey to becoming a Kannada Scholar.
*   **Memorization Mode:** A fill-in-the-blank game that challenges you to reconstruct lines of poetry from a scrambled word bank. Earn grades (🏆 Master, 🥇 Expert) based on your performance.
*   **Daily Streaks & Reminders:** An automated background `AlarmManager` service reminds you to read daily to maintain your reading streak flame.

### ⚔️ Local Multiplayer (Pass & Play)
*   **Split-Screen Poem Battles:** Grab a friend! The screen splits in half (with the top half rotated 180 degrees), presenting both players with a scrambled line of poetry. Race to unscramble the words in the correct order. First to finish wins the round!

### 📊 "Spotify Wrapped" Analytics Dashboard
*   **Comprehensive Stats Screen:** Track your journey with a gorgeous analytics dashboard.
*   **Activity Heatmap:** A GitHub-style 30-day contribution graph showing your daily reading activity.
*   **Animated Charts:** See your most-read poets in beautifully animated bar charts and track your growing vocabulary.

---

## 🛠️ Technology Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **State Management:** Kotlin Coroutines & `StateFlow`
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Local Storage:** SharedPreferences (for XP, Streaks, and Favorites)
*   **Background Tasks:** `AlarmManager` & `BroadcastReceiver` (for daily notifications)
*   **Audio:** Android `TextToSpeech` API with custom phonetic mapping

---

## 🚀 Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   Minimum SDK: API 26 (Android 8.0)
*   Target SDK: API 34 (Android 14)

### Installation
1. Clone the repository:
   go to terminal/cmd/powershell and run the following command:

   git clone https://github.com/siddarthshetty04-hub/Kavya-Kanaja-Poetry-Granary

2. Open the project folder in Android Studio.
3. Sync the Gradle files.
4. Run the app on an emulator or physical Android device.

---

## 🚀 How to Run the Project

This is a standard Android project built with Kotlin and Jetpack Compose. You can run it using Android Studio or the command line.

### Method 1: Using Android Studio (Recommended)
1. **Download & Install [Android Studio](https://developer.android.com/studio)**.
2. Open Android Studio.
3. Click on **Open** (or File > Open).
4. Navigate to downloaded repository folder.
5. Wait for the **Gradle Sync** to finish (you can see the progress at the bottom right).
6. Connect an Android device via USB (with USB Debugging enabled) or start an Android Emulator from the Device Manager .
   (using your own device is recommended!!! try it with wireless debugging or usb debugging enabled from developer options in your phone settings).
7. Click the **Run** button (the green play icon ▶️) in the top toolbar, or press `Shift + F10`.

To change the voice of the app:
1. Open your phone's Settings.
2. Search for Text-to-Speech (or go to Accessibility -> Text-to-Speech output).
3. Make sure Speech Services by Google is selected as the preferred engine.
4. Tap the Settings Gear Icon ⚙️ next to it.
5. Tap Install voice data.
6. Scroll down, find Kannada (India), and tap the download button.

### Method 2: Using Command Line
If you have the Android SDK and Java installed on your system:
1. Open your terminal / command prompt.
2. Navigate to the project root directory:
   ```cmd
   cd "c:\Users\dell\Desktop\PROJECTS\Mind Matrix"
   ```
3. Build the debug APK:
   ```cmd
   gradlew.bat assembleDebug
   ```
4. Install it on a connected device/emulator:
   ```cmd
   gradlew.bat installDebug
   ```



## 📸 Screenshots & Experience

*The application relies heavily on `animateFloatAsState`, `rememberInfiniteTransition`, and advanced `PointerInput` gestures to deliver a 60FPS fluid experience. The color palette utilizes bespoke gradients (Deep Teal, Saffron, Ivory) rather than generic colors, resulting in a premium aesthetic.*

---


