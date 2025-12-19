# 🥬 Caducity

**Track groceries. Reduce waste. Save money.**

Caducity is a Kotlin Multiplatform grocery expiration tracker that helps you monitor your food inventory and never waste groceries again.

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.2.21-purple?logo=kotlin" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Compose%20Multiplatform-1.10.0--rc01-blue?logo=jetpackcompose" alt="Compose"/>
  <img src="https://img.shields.io/badge/Material%203-Expressive-green" alt="Material 3"/>
  <img src="https://img.shields.io/badge/platforms-Android%20%7C%20Desktop%20%7C%20Web-lightgrey" alt="Platforms"/>
</p>

---

## ✨ Features

### Current
- 📊 **Dashboard** - Visual overview of all products with status indicators
- 🔍 **Product Details** - Track individual instances with expiration dates
- 🎨 **Adaptive UI** - Responsive design for phones, tablets, and desktops
- 🌓 **Theme Customization** - Light/Dark mode with dynamic colors
- 🌐 **Multiplatform** - Android, Desktop, Web, and WebAssembly

### Coming Soon
- ➕ Create and edit products
- 📝 Manage product instances
- 🔔 Expiration notifications
- 📈 Waste statistics & insights
- 🔎 Search and filter
- 📱 Barcode scanning (mobile)

See [**DEVELOPMENT_PLAN.md**](./DEVELOPMENT_PLAN.md) for the complete roadmap.

---

## 🚀 Quick Start

### Prerequisites
- JDK 11 or higher
- Android SDK (for Android builds)

### Run on Desktop
```bash
./gradlew :composeApp:run
```

### Run on Web
```bash
./gradlew jsBrowserDevelopmentRun
# Open http://localhost:8080
```

### Install on Android
```bash
./gradlew installDebug
```

For more commands and options, see [**QUICK_START_GUIDE.md**](./QUICK_START_GUIDE.md).

---

## 🏗️ Architecture

Built with **Clean Architecture** principles and **Kotlin Multiplatform**:

```
┌─────────────────────────────────────┐
│  UI Layer (Compose Multiplatform)   │  ← Screens, ViewModels
├─────────────────────────────────────┤
│  Domain Layer                        │  ← Use Cases, Business Logic
├─────────────────────────────────────┤
│  Data Layer                          │  ← Repositories, Data Sources
└─────────────────────────────────────┘
         ↓           ↓           ↓
    Android      Desktop     Web/WASM
```

**Key Technologies:**
- **Compose Multiplatform** - Shared UI across platforms
- **Material 3 Expressive** - Modern, adaptive design system
- **Koin** - Dependency injection
- **Navigation 3** - Type-safe multiplatform navigation
- **kotlinx-datetime** - Cross-platform date/time handling

---

## 📂 Project Structure

```
composeApp/src/
├── commonMain/         # Shared code (UI, domain, data)
│   ├── ui/            # Compose screens and components
│   ├── domain/        # Business logic and use cases
│   ├── data/          # Data sources and repositories
│   └── di/            # Dependency injection modules
├── androidMain/       # Android-specific implementations
├── desktopMain/       # Desktop-specific implementations
└── webMain/           # Web-specific implementations
```

---

## 🛠️ Development

### Build
```bash
./gradlew clean build
```

### Test
```bash
./gradlew allTests
```

### Create Distribution
```bash
# Desktop
./gradlew :composeApp:packageDistributionForCurrentOS

# Web
./gradlew jsBrowserDistribution
```

See [**CLAUDE.md**](./CLAUDE.md) for detailed development guidelines.

---

## 📋 Development Roadmap

The app is being developed in **10 phases** over approximately **5-8 months**:

1. ✅ **Core Product Management** (Partially Complete)
2. 📋 **Instance Management**
3. 📋 **Data Persistence**
4. 📋 **Notifications & Reminders**
5. 📋 **Enhanced User Experience**
6. 📋 **Analytics & Insights**
7. 📋 **Sharing & Collaboration**
8. 📋 **Advanced Features**
9. 📋 **Platform Optimization**
10. 📋 **Polish & Release**

View the full plan in [**DEVELOPMENT_PLAN.md**](./DEVELOPMENT_PLAN.md).

---

## 🤝 Contributing

Contributions are welcome! This project is in active development. Check the [Development Plan](./DEVELOPMENT_PLAN.md) to see what needs to be built.

### Development Setup
1. Clone the repository
2. Open in IntelliJ IDEA or Android Studio
3. Sync Gradle
4. Run on your preferred platform

---

## 📄 License

[Add your license here]

---

## 🙏 Acknowledgments

Built with:
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Material 3](https://m3.material.io/)
- [Koin](https://insert-koin.io/)

---

## 📞 Contact

[Add your contact information or links here]

---

<p align="center">Made with ❤️ to reduce food waste</p>
