# 📝 SP Notes — com.spmods.notes

Simple notes app for Google Play Protect appeal.

## Features
- Notes create / edit / delete
- SQLite local storage
- Clean minimal UI
- No unnecessary permissions

## GitHub Secrets Setup

| Secret | Value |
|--------|-------|
| `KEYSTORE_BASE64` | `base64 -w 0 your.jks` |
| `STORE_PASSWORD` | Store password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

## Build
Push to `main` → GitHub Actions automatically builds signed APK.
