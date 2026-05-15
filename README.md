# Server Dashboard Android Viewer v1.0.7

Compatibility bump for server-dashboard v6.4.8-b6. No WebView behavior changes were required; the compose rebuild fix is server-side.

# Server Dashboard Android Viewer v1.0.4

A small native Android WebView wrapper for the Server Dashboard.

## Default dashboard URL

```text
http://home.sidneyshelton.com:8029/
```

## Allowed in-app hosts

The app keeps these links inside the app:

- `home.sidneyshelton.com:8029`
- `sidneyshelton.com`
- `*.sidneyshelton.com`
- local LAN hosts on `192.168.0.*`
- `localhost` / `127.0.0.1`

The native **Open** button sends the current page to the external Android browser.

## Features

- Home button returns to the dashboard URL.
- Refresh button reloads the current dashboard page.
- Back button uses WebView history first.
- Tile View/Open links stay inside the app WebView.
- JavaScript, DOM storage, cookies, and mixed HTTP/HTTPS content are enabled.
- Cleartext HTTP is allowed for `home.sidneyshelton.com:8029` and LAN server access.
- Orientation changes keep the in-app browser open on the current page.
- Immersive full-screen mode is enabled.

## v1.0.4 changes

- Fixed portrait dashboard tile layout where cards became too tall and text stacked vertically.
- Added stronger mobile CSS injection for dashboard cards:
  - service cards use a compact mobile grid layout,
  - service names/descriptions stay single-line with ellipsis,
  - controls wrap cleanly under the service info,
  - ARK/MPD bottom summary rows stay on one line and truncate instead of stretching the card.
- Added the new high-quality SidscriServer-style app icon to Android launcher resources.
- Bumped Android `versionCode` to 4 and `versionName` to `1.0.4`.

## Build in Android Studio

1. Open Android Studio.
2. File > Open.
3. Select this project folder.
4. Let Gradle sync.
5. Build > Build Bundle(s) / APK(s) > Build APK(s).
6. Install the APK on your Android phone.

## Change default URL

Edit this line in:

```text
app/src/main/java/com/sidscri/serverdashboardviewer/MainActivity.java
```

```java
private static final String DASHBOARD_URL = "http://home.sidneyshelton.com:8029/";
```


## v1.0.5

- Updated launcher icon resources with the new high-quality SidscriServer icon in drawable and all mipmap densities.
- Kept dashboard View links inside the app while the native Open button launches the external browser.
- Added/kept mobile WebView CSS safeguards for compact portrait service tiles and resource colors.


## v1.0.6
- Kept the launcher icon resources in every Android density folder.
- No risky WebView changes; this build is paired with dashboard v6.4.7-b5, which fixes the dashboard data-loading regression.
