# Luxe AOD — Android Studio source project

A Kotlin prototype for a photo-backed luxury clock display for Android, targeted at the Motorola Edge 40 / Android 8+.

## Included
- Select a wallpaper from the system document/photo picker and remember it.
- Adjust wallpaper zoom/crop.
- Adjust clock size and choose Elegant Thin, Modern Bold, or Luxury Serif.
- Choose Gold, White, Ice Blue, or Rose accent colours.
- Full-screen dimmed clock with date and battery percentage.
- Periodic small position shifts to help reduce static-pixel exposure.

## Build
1. Open the `LuxeAOD` folder in Android Studio (current stable version).
2. Allow Gradle sync to download the Android Gradle Plugin/Kotlin dependencies.
3. Choose **Build > Build APK(s)**.
4. Install the generated debug APK on your Android phone. You may need to allow installation from that source.

## Important Android limitation
This is a full-screen AOD-style activity, not a replacement for Motorola's system-level screen-off/low-power AOD. Android does not provide ordinary third-party apps with a universal API to draw on the display after the device has powered the screen off. The activity keeps the display on while it is open, so it can use more battery; it does not guarantee that the display remains visible after pressing the phone's power button or after the system closes the activity. The brightness is set low while this activity is running. Burn-in protection is a basic periodic content shift, not a guarantee against burn-in.

## Permissions
No broad storage permission is requested. The photo is selected through Android's system picker. The app requests no network access.
