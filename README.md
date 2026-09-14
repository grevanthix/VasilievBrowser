# Vasiliev Browser

A lightweight Android browser built without extra libraries or bloat, so it stays small and runs fine on old or weak devices.

## Features

- Tabs with a grid view
- Incognito mode per tab
- Browsing history
- Find in page
- View page source
- File downloads
- Copy text/link, open link in new tab or incognito
- Custom homepage
- Toggle JavaScript and image loading
- User-Agent switcher
- Restore tabs on startup
- Appearance settings: toolbar position, corner radius, black background and more
- English and Russian localization
- Optional site blocklist/warning list (empty by default, see below)
- Clear cache and cookies

## Site blocklist

The browser has no built-in list of dangerous sites. By default the list is empty.

If you want to add one, edit `res/raw/blocklist` before building. The format example is in `res/raw/example`.

## Build (Linux)

Requires JDK and Android SDK.

```bash
./gradlew assembleRelease
```
APK will be in `app/build/outputs/apk/release/`.
# License
Apache License 2.0. See [LICENSE](./LICENSE) file.
