# SaferPath 🛡️🚗
### Intelligent Safe Route Navigation & Community Safety Web Platform

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/HTML)
[![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)](https://developer.mozilla.org/en-US/docs/Web/CSS)
[![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![Leaflet](https://img.shields.io/badge/Leaflet-199900?style=for-the-badge&logo=leaflet&logoColor=white)](https://leafletjs.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

---

## 📖 Table of Contents
- [Project Overview](#-project-overview)
- [Problem Statement](#-problem-statement)
- [Proposed Solution](#-proposed-solution)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Technologies Used](#-technologies-used)
- [Project Folder Structure](#-project-folder-structure)
- [Installation Instructions](#-installation-instructions)
- [Execution Instructions](#-execution-instructions)
- [API Endpoints](#-api-endpoints)
- [Screenshots & UI Preview](#-screenshots--ui-preview)
- [Future Enhancements](#-future-enhancements)
- [Author & Acknowledgments](#-author--acknowledgments)

---

## 🌟 Project Overview

**SaferPath** is an intelligent, safety-centric navigation web platform designed to prioritize personal safety over simply finding the shortest or fastest route. Built using a lightweight, zero-dependency Java HTTP backend and an interactive web interface powered by Leaflet.js, SaferPath evaluates routes based on real-world safety factors—such as street lighting, crowd presence, police surveillance, CCTV coverage, and user-reported hazards—to ensure commuters and pedestrians reach their destinations safely.

---

## ❗ Problem Statement

Standard navigation applications (such as Google Maps or Apple Maps) optimize exclusively for:
1. **Shortest Distance:** Direct routes that frequently guide pedestrians through dark, deserted alleys or narrow shortcuts.
2. **Fastest Travel Time:** Congestion-avoidance algorithms that push travelers onto isolated backroads.

These traditional algorithms ignore crucial personal safety parameters. For night-shift workers, women, college students, and solitary commuters, traveling along the fastest route can expose them to poorly lit stretches, high-crime intersections, or areas lacking emergency access. There is a lack of accessible, community-driven navigation platforms that place traveler security and well-being first.

---

## 💡 Proposed Solution

**SaferPath** redefines commuter navigation by introducing multi-factor **Safety Scoring**:
- **Holistic Safety Scoring:** Calculates an aggregate score (0–100) factoring in infrastructure lighting, patrol presence, emergency facility proximity, and road condition.
- **Safety Tiers:** Color-coded categorization (`Very Safe 🟢`, `Safe 🟡`, `Moderate 🟠`, `Unsafe 🔴`) allowing users to make informed travel choices.
- **Real-Time Turn-by-Turn Navigation:** Dynamic waypoint navigation with simulated GPS tracking, speed, bearing, and distance estimation.
- **Crowdsourced Safety Reporting:** Empowers users to report safety incidents (e.g., poor lighting, suspicious activities, road hazards) in real time to benefit the entire community.

---

## 🚀 Key Features

- **Dynamic Route Planning:**
  - Choose origin and destination across key regional hubs.
  - Compares routes with detailed distance, duration, and safety breakdown.
- **Interactive Map Visualizer:**
  - High-performance mapping via Leaflet.js and OpenStreetMap.
  - Visualized path polylines, start/end pins, and waypoint markers.
- **Simulated GPS & Live Navigation:**
  - Built-in GPS device simulator with real-time movement calculations.
  - Step-by-step turn-by-turn maneuvers and dynamic recalculation.
- **Community Safety Incident Reporting:**
  - Submit incident reports categorized by type, description, and location.
  - Immediate reflection on the regional safety analysis score.
- **Emergency SOS & Quick Help:**
  - One-tap quick emergency guidance and safety tips for night-time commuters.
- **Zero-Dependency Architecture:**
  - Built entirely on native standard Java (`com.sun.net.httpserver.HttpServer`).
  - No external Maven/Gradle heavy dependencies required; compiles and runs natively anywhere.
- **Multi-Device & LAN Support:**
  - Automatically identifies local IP and serves both `localhost` and local Wi-Fi networks for smartphone testing.
- **Mobile-First Responsive Design:**
  - Smooth fluid UI optimized for smartphones, tablets, and desktop browsers.

---

## 🏗️ System Architecture

```
+------------------------------------------------------------------+
|                     Client Web Interface                         |
|   (HTML5, Modern CSS3, Vanilla JavaScript, Leaflet.js Maps)     |
+---------------------------------+--------------------------------+
                                  | HTTP / JSON REST APIs
                                  v
+------------------------------------------------------------------+
|               SaferPath Java Embedded HTTP Server                |
|                    (com.sun.net.httpserver)                      |
|                                                                  |
|  +---------------------+  +--------------------+  +-----------+  |
|  |    StaticHandler    |  |   RoutesHandler    |  |Navigation |  |
|  | (Serves Frontend UI)|  |  (Route Evaluator) |  |  Handler  |  |
|  +---------------------+  +--------------------+  +-----------+  |
|  +---------------------+  +--------------------+  +-----------+  |
|  |    SafetyHandler    |  |     GPSHandler     |  |Locations  |  |
|  |  (Safety Scoring)   |  |   (GPS Simulator)  |  |  Handler  |  |
|  +---------------------+  +--------------------+  +-----------+  |
+---------------------------------+--------------------------------+
                                  |
                                  v
+------------------------------------------------------------------+
|                 Core Business Models & Services                  |
|  • Location.java          • Route.java          • RouteSegment   |
|  • SafetyLevel.java       • SafetyReport.java   • GPSService     |
|  • SafetyAnalysisService  • NavigationSession   • GPSDevice      |
+------------------------------------------------------------------+
```

---

## 💻 Technologies Used

| Domain | Technology / Library | Description |
|---|---|---|
| **Backend Runtime** | Java SE (JDK 17+) | Core server implementation and multi-threading |
| **HTTP Server** | `com.sun.net.httpserver.HttpServer` | High-performance lightweight embedded web server |
| **Concurrency** | `java.util.concurrent.*` | Thread-safe concurrent data maps & cached thread pool |
| **Frontend UI** | HTML5, CSS3, JavaScript (ES6+) | Single-page responsive interface |
| **Mapping Engine** | Leaflet.js | Open-source mobile-friendly interactive map engine |
| **Map Tiles** | OpenStreetMap (OSM) | Free geographic tile layer |
| **Automation** | Windows Batch Script (`run.bat`) | Automated compilation and execution pipeline |

---

## 📂 Project Folder Structure

```
HACK-IT-ON/
├── src/
│   ├── Location.java                   # Geographic location coordinates and Haversine distance calculations
│   ├── Route.java                      # Route model with duration, distance, waypoints, and safety scoring
│   ├── RouteSegment.java               # Turn-by-turn navigation maneuver segment generator
│   ├── SafetyLevel.java                # Enum defining safety tiers (VERY_SAFE, SAFE, MODERATE, UNSAFE)
│   ├── SafetyReport.java               # Data model for user-reported community incidents
│   ├── SaferPathWebApp.java            # Main application entry point, embedded server, handlers & frontend UI
│   ├── SaferPathWebApp_Final.java      # Regional route configuration reference implementation
│   ├── SaferPathWebApp_fixed.java      # Modular handler implementation
│   ├── SaferPathWebApp_handlers.java   # HTTP REST API handler definitions
│   └── SaferPathWebApp_models.java     # Auxiliary data model specifications
├── .gitignore                          # Git exclusion rules for bytecode, logs, and temp files
├── run.bat                             # One-click Windows build and execution script
└── README.md                           # Comprehensive project documentation
```

---

## ⚙️ Installation Instructions

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or higher installed.
  - Verify installation: `javac -version` and `java -version`
- **Git:** Installed on your local machine.
- **Web Browser:** Modern browser (Chrome, Edge, Firefox, Safari).

### Clone the Repository
```bash
git clone https://github.com/pradeepkrishnak07-stack/SaferPath.git
cd SaferPath
```

---

## ▶️ Execution Instructions

### Method 1: Using the Batch Script (Recommended for Windows)
Simply double-click `run.bat` or run it from the command line:
```cmd
run.bat
```
The script will:
1. Clean previous `.class` files.
2. Compile `src/SaferPathWebApp.java` using `javac`.
3. Launch the server at `http://localhost:8080`.

### Method 2: Manual Compilation & Run (Cross-Platform)

#### Windows (PowerShell / Command Prompt):
```bash
# Compile the application into the current directory
javac -d . src\SaferPathWebApp.java

# Run the compiled application
java SaferPathWebApp
```

#### Linux / macOS:
```bash
# Compile the application
javac -d . src/SaferPathWebApp.java

# Run the compiled application
java SaferPathWebApp
```

### Accessing the Web Application
- **Local machine:** Open your browser and navigate to:
  ```
  http://localhost:8080
  ```
- **Mobile devices on the same Wi-Fi:** Open the network URL displayed in the console (e.g. `http://192.168.x.x:8080`).

---

## 🔌 API Endpoints

The embedded Java HTTP server exposes the following RESTful endpoints:

| Endpoint | Method | Description |
|---|---|---|
| `/` | `GET` | Serves the single-page responsive web application |
| `/api/locations` | `GET` | Returns list of configured regional landmarks and coordinates |
| `/api/routes` | `GET` | Returns available routes between start and destination with safety scores |
| `/api/navigation` | `GET` / `POST` | Manages live navigation sessions and turn-by-turn updates |
| `/api/safety` | `GET` / `POST` | Fetches safety analytics and accepts community safety reports |
| `/api/gps` | `GET` | Provides real-time GPS coordinate simulation data |
| `/api/location/current` | `GET` | Fetches current location coordinates of the active device |
| `/api/network-info` | `GET` | Returns local network IP information for mobile connection |

---

## 📱 Screenshots & UI Preview

> *Visual layout of the SaferPath web application interface:*

```
+---------------------------------------------------------------+
|  🛡️ SafePath - Intelligent Navigation                         |
|  Prioritizing your safety on every journey                    |
+---------------------------------------------------------------+
| [ 🗺️ Plan Journey ] [ 📍 Live Navigation ] [ 📊 Safety Info ] |
+---------------------------------------------------------------+
|  +---------------------------------------------------------+  |
|  |                                                         |  |
|  |               Interactive Leaflet.js Map                |  |
|  |           Route Polylines & Safety Waypoints            |  |
|  |                                                         |  |
|  +---------------------------------------------------------+  |
|                                                               |
|  [Select Origin: Bus Stand]  --->  [Destination: College]     |
|                                                               |
|  Route A: Recommended (Score: 92/100) 🟢 Very Safe            |
|  • Distance: 3.8 km | Time: 12 mins                           |
|  • Street Lighting: High | CCTV Coverage: Active              |
|                                                               |
|  [ Start Navigation ]          [ Report Safety Concern ]      |
+---------------------------------------------------------------+
```

---

## 🔮 Future Enhancements

- [ ] **AI-Powered Danger Prediction:** Machine learning model to predict safety ratings dynamically based on real-time weather, events, and crime rate statistics.
- [ ] **Native Mobile Application:** React Native / Flutter companion app integrating real device GPS sensors and accelerometer (fall detection).
- [ ] **Automated SOS Trigger:** Automatic emergency alerts sent via SMS/WhatsApp with live GPS tracking if the user stops moving or deviates off-route unexpectedly.
- [ ] **Offline Map Caching:** PWA (Progressive Web App) service worker implementation for navigation without cellular connectivity.
- [ ] **Municipal Integration:** Direct dispatch pipeline for city authorities and police departments to address reported lighting failures and hazardous road spots.

---

## 👤 Author & Acknowledgments

- **Developer:** [PRADEEP KRISHNA K](https://github.com/pradeepkrishnak07-stack)
- **GitHub:** [@pradeepkrishnak07-stack](https://github.com/pradeepkrishnak07-stack)
- Developed for Hackathons & Smart City Safety Initiatives.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) - feel free to use and adapt it for research and educational purposes.
