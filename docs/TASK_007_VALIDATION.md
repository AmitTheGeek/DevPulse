# Task 007 Device Validation

Date: 2026-09-23

Target used:
- Temporary Android Emulator AVD
- Android 16 / API 36
- x86_64 Google Play system image already installed locally

Device discovery:
- No physical device was connected.
- No configured AVD existed before validation.
- A temporary AVD was created from the already-installed system image. No system image was downloaded.

Validation summary:
- Online smoke test passed for `AmitTheGeek`: search, developer dashboard, repository list, repository detail, save, Saved tab, Saved to detail, unsave, back navigation, Explore/Saved navigation, and manual refresh.
- Failure state passed for a nonexistent username: the app showed `Developer not found`.
- Offline smoke test passed after disabling emulator Wi-Fi/data: cached developer content, repository detail, and saved repository remained readable; manual refresh reported failure without clearing cached content.

Notes:
- The API 36 emulator showed intermittent System UI / Pixel Launcher ANR dialogs during early boot and first-run interaction. DevPulse remained usable after waiting/dismissing those system dialogs.
- Network was restored after offline validation.
