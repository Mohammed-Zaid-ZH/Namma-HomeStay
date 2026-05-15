# Namma-HomeStay

Namma-HomeStay is a GenAI-powered Android application designed to help rural and coastal home-stay owners easily manage and promote their hospitality services. The app enables hosts to list rooms, showcase authentic local cuisine, manage enquiries, and connect with eco-tourists using a simple smartphone-friendly interface.

## Features

* Host profile management
* Room listing and availability management
* Local food menu management
* Local guide / nearby places section
* Traveller enquiry management
* AI-generated home-stay descriptions using Gemini AI
* AI-generated dish suggestions
* Firebase Authentication and Firestore integration
* Real-time data updates using Kotlin Flows

## Tech Stack

* Kotlin
* Android Studio
* Firebase Authentication
* Firebase Firestore
* Firebase Storage
* MVVM Architecture
* Kotlin Coroutines & StateFlow
* Gemini AI API

## Project Structure

```text
app/
 ├── data/           # Data models
 ├── repository/     # Firebase & API operations
 ├── ui/             # Screens and UI components
 ├── viewmodel/      # ViewModels and state management
 └── utils/          # Utility/helper classes
```

## Getting Started

### Prerequisites

Before running the project, ensure you have:

* Android Studio (latest version recommended)
* JDK 17 or above
* Firebase project setup
* Internet connection

## Installation Steps

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/NammaHomeStay.git
```

### 2. Open in Android Studio

* Open Android Studio
* Select **Open Project**
* Choose the cloned `NammaHomeStay` folder

### 3. Add Firebase Configuration

Since `google-services.json` is not included for security reasons:

1. Create your own Firebase project
2. Register your Android app package
3. Download the `google-services.json` file
4. Place it inside:

```text
app/google-services.json
```

### 4. Enable Firebase Services

Enable the following services in Firebase:

* Authentication
* Firestore Database
* Storage

### 5. Add Gemini API Key

Configure your Gemini API key inside the required API/repository configuration file.

## Running the App

Click the **Run ▶** button in Android Studio or use:

```bash
./gradlew assembleDebug
```

## Future Enhancements

* Online booking and payment integration
* Multilingual support
* AI-powered travel recommendations
* Maps and navigation integration
* Offline support for low-network regions

## Contributing

Contributions are welcome.

1. Fork the repository
2. Create a new branch
3. Commit your changes
4. Push to your branch
5. Create a Pull Request

## License

This project is developed for educational and internship purposes.

## Author

Mohammed Zaid Z H
