# Android Traceroute App

A modern Android application for network diagnostics built with **Kotlin**, **Jetpack Compose**, and **Material 3**. Perform ping, traceroute, and DNS lookups with a beautiful, responsive UI.

## Features

### ✨ Core Functionality
- **Ping** - ICMP-based ping with packet loss and latency statistics
- **Traceroute** - UDP-based traceroute showing network hops with real-time progress
- **DNS Lookup** - Resolve hostnames to IP addresses with canonical name resolution

### 🎨 User Interface
- **Material 3 Design** - Modern, responsive UI with dynamic color support (Android 12+)
- **Tabbed Navigation** - Easy switching between Ping, Traceroute, and DNS operations
- **Live Progress** - Real-time hop visualization during traceroute execution
- **Dark Mode** - Full dark mode support with theme toggle
- **Error Handling** - Clear error messages with dismiss options

### 🏗️ Architecture
- **MVVM Pattern** - Clean separation of concerns with ViewModel and StateFlow
- **Coroutines** - Async network operations on background threads
- **Hilt DI** - Dependency injection for clean, testable code
- **Material 3 Theming** - Complete theme system with light/dark variants

## Requirements

- Android SDK 24+ (Android 7.0+)
- Kotlin 1.9.22+
- Gradle 8.2.0+

## Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Project Structure

```
src/main/java/com/brhanumehari/traceroute/
├── TracerouteApplication.kt          # Hilt entry point
├── di/
│   └── NetworkModule.kt              # Dependency injection
├── model/
│   └── NetworkModels.kt              # Data classes
├── network/
│   └── NetworkRepository.kt          # Network operations
├── ui/
│   ├── activity/
│   │   └── MainActivity.kt           # Compose entry point
│   ├── screen/
│   │   └── DiagnosticsScreen.kt      # Main UI
│   └── theme/
│       ├── Theme.kt                  # Material 3 theming
│       ├── Color.kt                  # Color palette
│       └── Type.kt                   # Typography
```

## Building

1. Clone the repository
2. Open in Android Studio (Giraffe or later recommended)
3. Sync Gradle files
4. Build and run on a device or emulator (API 24+)

```bash
./gradlew build
./gradlew installDebug
```

## Usage

### Ping
1. Enter a hostname or IP address
2. Tap the "Start Ping" button
3. View packet loss and latency statistics

### Traceroute
1. Enter a hostname or IP address
2. Tap the "Start Traceroute" button
3. Watch real-time hops appear on screen
4. Each hop shows IP, hostname, latency, and status
5. Export results using the "Export Results" button

### DNS Lookup
1. Enter a hostname
2. Tap the "Start DNS Lookup" button
3. View resolved IP addresses and canonical hostname

## Dependencies

- **Jetpack Compose** 1.6.2 - UI framework
- **Material 3** 1.2.0 - Design system
- **Hilt** 2.50 - Dependency injection
- **Coroutines** 1.7.3 - Async operations
- **Lifecycle** 2.7.0 - UI state management

## Configuration

All network timeouts are set to 5 seconds. Traceroute will attempt up to 30 hops or until the target is reached.

### Key Constants (`NetworkRepository.kt`)
- `DEFAULT_TIMEOUT = 5000` - Socket timeout in milliseconds
- `DEFAULT_MAX_HOPS = 30` - Maximum traceroute hops

## API Level Support

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## Performance

- All network operations run on background coroutines
- UI remains responsive during network calls
- Real-time updates for traceroute progress
- Efficient resource management with proper socket cleanup

## Theme System

The app supports:
- **Light Theme** - Bright blue primary color
- **Dark Theme** - Adaptive blue primary color
- **Dynamic Colors** - System theme on Android 12+ (API 31+)

Toggle theme with the sun/moon icon in the app bar.

## Error Handling

- **Network Timeouts** - Graceful 5-second timeout with user-friendly messages
- **DNS Resolution Failures** - Clear error messages
- **Invalid Hostnames** - Input validation with helpful feedback
- **Connection Errors** - Detailed error reporting

## Future Enhancements

- [ ] TCP traceroute option
- [ ] IPv6 support
- [ ] Save/load results history
- [ ] Detailed hop information modal
- [ ] Custom timeout configuration
- [ ] Share results functionality
- [ ] Route statistics and graphs

## License

This project is part of the android-traceroute-ap repository.

## Author

@brhanumehari

---

**Built with ❤️ using Kotlin and Jetpack Compose**
