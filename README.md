# Vault - Biometric Companion for Lawnchair & Custom Launchers

**Vault** is a lightweight security companion designed to work alongside launchers like **Lawnchair**. 

### ⚠️ Important Clarification
**Vault does not hide applications from the Android system.** Instead, it provides a biometric-protected "safe" where you can place shortcuts to any app installed on your device. It is specifically designed for users who hide apps in their main launcher (like Lawnchair's "Hide Apps" feature) and need a secure, authenticated way to access them.

## Key Features

- 🔐 **Biometric Access**: Protected by the official Android Biometric API (Fingerprint, Face, or Device Credential).
- 🎨 **Lawnchair Companion**: Perfect for accessing apps you've hidden from your main app drawer.
- 📱 **Customizable Vault**: Select any installed app to appear inside your Vault, whether they are hidden in your launcher or not.
- 🌓 **Full Dark Mode**: Seamlessly respects your system's dark/light theme.
- ⚡ **Modern Stack**: Built with Jetpack Compose and Material 3 for a native look and feel.
- 💾 **Safe Storage**: Your selection is persisted using Jetpack DataStore.

## Why use Vault?

Many launchers (like Lawnchair) allow you to hide apps from the drawer, but they often lack a quick, secure way to launch them without unhiding them first. **Vault** fills this gap by acting as a secondary, secure launcher:

1. **Hide** your sensitive apps in Lawnchair (or your preferred launcher).
2. **Add** those same apps to **Vault**.
3. **Access** them instantly by opening Vault and authenticating with your fingerprint.

## Technical Details

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3
- **Storage**: Preferences DataStore
- **Permissions**:
    - `USE_BIOMETRIC`: For secure authentication.
    - `QUERY_ALL_PACKAGES`: To list and launch your installed applications.

## Installation

Clone the repository and build using Android Studio:

```bash
git clone https://github.com/Stunext/vault.git
```

Or you can use a build from the releases page

https://github.com/Stunext/vault/releases

## Contributing

Contributions, bug reports, and feature requests are welcome! 

## License

This project is licensed under the terms you selected in the repository. See the `LICENSE` file for more details.

---
*Created with ❤️ to enhance your privacy and launcher experience.*
