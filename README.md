# Ox Browser (Android)

A native Android web browser built with Kotlin and Android's built-in WebView. Tabs, address bar, back/forward, bookmarks, pull-to-refresh, browsing history, private/incognito tabs, downloads manager, a settings screen (homepage + default search engine), and a light/dark/system theme toggle.

## Features

- **Tabs** — open multiple tabs, switch and close them
- **Private tabs** — marked with a 🕵 icon and purple outline; skip history, disable cookies/cache
- **History** — every visited page (non-private) is logged; tap an entry to reopen it, or clear it all
- **Downloads** — files triggered via the browser download through Android's system DownloadManager and are logged in an in-app Downloads list
- **Bookmarks** — star any page, browse them from the menu
- **Settings** — set a custom homepage, pick a default search engine (Google/Bing/DuckDuckGo), and choose Light/Dark/Follow system theme
- Access all of the above from the ☰ menu button in the toolbar

## Open in Android Studio

1. Open Android Studio → **Open** → select this `ox-browser-android` folder.
2. Let Gradle sync (it will download dependencies automatically).
3. Run on an emulator or a connected device.

## Push to GitHub & auto-build with GitHub Actions

1. Create a new empty repo on GitHub (do NOT initialize it with a README).
2. From this project folder:

```bash
git init
git add .
git commit -m "Initial commit: Ox Browser Android"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

3. Go to the **Actions** tab of your repo. The `Build Ox Browser (Android)` workflow runs automatically, builds a debug `.apk`, and uploads it as a downloadable artifact on that workflow run.
4. Install the APK on your phone (enable "Install unknown apps" for your file manager/browser first), or connect a device and run from Android Studio.

## Notes

- Minimum Android version: Android 8.0 (API 26).
- This app only browses public websites — it does not include any video/media downloading functionality.
- To publish on the Google Play Store, you'll need a signed release build (a keystore) and a Play Console developer account — ask if you want help setting that up.
