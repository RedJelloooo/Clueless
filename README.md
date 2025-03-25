# Clueless - Multiplayer Scoreboard Game
Clueless is a Java-based scoreboard game featuring both a command-line and GUI-based version. It supports multiplayer gameplay through a client-server architecture.

## Features
- Command-line version for quick gameplay.
- GUI version with visuals and multiplayer support.
- Score tracking and leaderboard system.
- Multiplayer mode with server-client communication.

## Prerequisites
Before running the game, ensure you have:

1. Java Development Kit (JDK 8+) installed
Check your installation with:
```
java --version
```

If Java is not installed, download and install it from [Oracle](https://www.oracle.com/java/technologies/downloads) or use [OpenJDK](https://openjdk.org/).

2. Git (for cloning the repository)

## Setup & Compilation
1. Clone the repo
```
git clone https://github.com/RedJelloooo/Clueless.git
cd Clueless/project
```

2. Compile the Java Source Code
```
javac -d bin $(find src -name "*.java")
```

## Running the Game
1. Run the Server
```
java -cp bin ServerDriver &
```
- The server must be running before clients can connect
- The server runs on `localhost` by default

2. Run the Client(s)
```
java -cp bin ClientDriver &
```
- All Clients run on `localhost` by default