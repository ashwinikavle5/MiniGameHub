# 🎮 MiniGameHub

**MiniGameHub** is a Java-based Android application that brings multiple mini-games together in one app. It includes user registration and login, a game menu, five mini-games, and a scoreboard backed by a local SQLite database.

> **Project type:** Mobile App Development (MAD)  
> **Platform:** Android  
> **Language:** Java  
> **UI:** XML  
> **Database:** SQLite  
> **IDE:** Android Studio

---

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Games Included](#-games-included)
- [Technologies Used](#-technologies-used)
- [Application Flow](#-application-flow)
- [Project Structure](#-project-structure)
- [Important Files](#-important-files)
- [Database](#-database)
- [How to Run](#-how-to-run)
- [Concepts Demonstrated](#-concepts-demonstrated)
- [Future Improvements](#-future-improvements)

---

## 📖 Overview

MiniGameHub is designed to provide a simple, convenient place to play different mini-games. Users can create an account, log in, choose a game from the menu, play, and view scores saved by the application.

The project demonstrates Android Activities, Intents, event handling, custom game drawing with Canvas/View where applicable, and local database operations with SQLite.

## ✨ Features

- User signup and login
- Game menu with five mini-games
- Username passed between screens using Android `Intent`
- Individual gameplay logic for each game
- Score storage and retrieval using SQLite
- Scoreboard screen
- Restart and game-over flows where implemented
- Local data storage without requiring a remote database

## 🕹️ Games Included

### 1. Snake
Control the snake's direction, collect food, and grow. The game checks for collisions with the wall and the snake's own body. `SnakeView.java` handles the custom game drawing and gameplay logic.

### 2. Tic Tac Toe
Play on a 3 × 3 grid. The game checks rows, columns, and diagonals to determine whether a player has won or the game is a draw.

### 3. Subway Runner
A simple three-lane runner inspired by endless-runner games. The player changes lanes to avoid obstacles. Collision detection determines when the run ends.

### 4. Bubble Shooter
Aim and shoot colored bubbles toward a cluster. When the fired bubble attaches, the game checks connected bubbles of the same color. Groups of three or more are removed according to the game rules.

### 5. Sorting Balls
Sort colored balls into tubes. The game allows selection of the top ball and checks whether a move is valid, based on the destination tube's color and capacity. A level is completed when the sorting condition is met.

### 6. Score Board
Displays saved game scores retrieved from the local database.

> **Note:** The current Java filenames `TileEscapeActivity.java`, `FruitBlastActivity.java`, and `FruitBlastView.java` are retained for the Subway Runner and Sorting Balls implementations.

## 🧰 Technologies Used

| Technology | Purpose |
|---|---|
| Java | App, event-handling, and game logic |
| XML | Android screen layouts |
| Android Studio | Development, testing, and building |
| Android SDK | Android APIs and build tools |
| SQLite | Local storage for user and score data |
| Android `Intent` | Navigation between Activities and passing values |
| Android `Canvas` / `View` | Custom game drawing where used |

## 🔄 Application Flow

```text
Launch App
    |
    v
MainActivity.java (Login)
    |
    v
DatabaseHelper.java ---> SQLite (verify login)
    |
    v
MainActivity2.java (Game Menu)
    |
    +----> Snake
    +----> Tic Tac Toe
    +----> Subway Runner
    +----> Bubble Shooter
    +----> Sorting Balls
    +----> Score Board
                 |
                 v
       DatabaseHelper.java
                 |
                 v
          SQLite Database
                 |
                 v
       ScoreBoardActivity.java
```

The exact score-saving behavior depends on the implementation of each game and the methods provided by `DatabaseHelper.java`.

## 📂 Project Structure

The following is a guide to the main files. Your Android Studio project may contain additional Gradle, resource, and theme files.

```text
MiniGameHub/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/minigamehub/
│           │   ├── MainActivity.java
│           │   ├── SignUpActivity.java
│           │   ├── MainActivity2.java
│           │   ├── DatabaseHelper.java
│           │   ├── SnakeGameActivity.java
│           │   ├── SnakeView.java
│           │   ├── TicTacToeActivity.java
│           │   ├── TileEscapeActivity.java
│           │   ├── BubbleShooterActivity.java
│           │   ├── BubbleShooterView.java
│           │   ├── FruitBlastActivity.java
│           │   ├── FruitBlastView.java
│           │   └── ScoreBoardActivity.java
│           ├── res/
│           │   └── layout/
│           │       ├── activity_main.xml
│           │       ├── activity_signup.xml
│           │       ├── activity_game_menu.xml
│           │       ├── activity_snake_game.xml
│           │       ├── activity_tic_tac_toe.xml
│           │       ├── activity_bubble_shooter.xml
│           │       └── activity_score_board.xml
│           └── AndroidManifest.xml
└── README.md
```

*This is a representative structure based on the project's described files. Check Android Studio for the exact layouts and resources present in your current project.*

## 🧩 Important Files

| File | Responsibility |
|---|---|
| `AndroidManifest.xml` | Declares app components and the launcher Activity |
| `MainActivity.java` | Displays the login screen and handles login |
| `SignUpActivity.java` | Handles user registration |
| `DatabaseHelper.java` | Creates/manages SQLite tables and handles database operations |
| `MainActivity2.java` | Displays the game menu and opens selected games |
| `SnakeGameActivity.java` | Manages the Snake screen and controls |
| `SnakeView.java` | Snake movement, food, collision, and drawing |
| `TicTacToeActivity.java` | Tic Tac Toe moves and winner checking |
| `TileEscapeActivity.java` | Subway Runner screen and gameplay |
| `BubbleShooterActivity.java` | Manages the Bubble Shooter screen |
| `BubbleShooterView.java` | Bubble movement, collisions, color matching, and drawing |
| `FruitBlastActivity.java` | Manages the Sorting Balls screen and controls |
| `FruitBlastView.java` | Sorting rules, valid moves, and level completion |
| `ScoreBoardActivity.java` | Retrieves and displays saved scores |

### What does `AndroidManifest.xml` do?
It informs Android about application components and identifies the launcher Activity through the `MAIN` action and `LAUNCHER` category.

### What does `DatabaseHelper.java` do?
It centralizes database operations, such as creating tables, inserting or checking user information, and saving or retrieving scores, depending on the methods implemented in the file. It does not run the game itself.

## 🗄️ Database

MiniGameHub uses **SQLite**, a local database available on Android devices. The app accesses it through `DatabaseHelper.java`.

Typical data flow:

- **Signup:** registration screen → database helper → user record stored.
- **Login:** login screen → database helper → credentials checked.
- **Score saving:** game result → database helper → score record stored.
- **Scoreboard:** scoreboard screen → database helper → saved scores retrieved and displayed.

The exact table names, column names, and database schema are defined in `DatabaseHelper.java`.

## ▶️ How to Run

1. Install and open **Android Studio**.
2. Select **Open** and choose the MiniGameHub project folder.
3. Wait for Gradle sync to finish.
4. Connect an Android device with USB debugging enabled, or create an Android emulator.
5. Press **Run ▶** to build and launch the app.
6. Register a user if needed, log in, and select a game from the menu.

### Build an APK

1. In Android Studio, open **Build**.
2. Choose **Build Bundle(s) / APK(s)** → **Build APK(s)**.
3. Wait for the build to finish and use the notification's APK link to locate the generated file.

Menu wording can vary slightly between Android Studio versions.

## 🧠 Concepts Demonstrated

- Android Activities and lifecycle basics
- XML-based UI design
- Intents for navigation and passing data
- Button clicks and touch-event handling
- Object-oriented programming
- Game loops, movement, and collision detection where applicable
- Custom drawing using Canvas/View where applicable
- SQLite CRUD-style operations, depending on the implemented methods
- Score tracking and display

## 🚀 Future Improvements

- Add difficulty settings and more levels
- Improve animations and sound effects
- Add filters or sorting to the scoreboard
- Add unit tests for game rules and database operations
- Add accessibility improvements and additional screen-size support

## 👩‍💻 Author

**Project:** MiniGameHub  
**Developed with:** Java, XML, Android Studio, and SQLite

---
