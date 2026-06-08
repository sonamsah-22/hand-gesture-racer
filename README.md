# Hand Gesture Racer

A real-time hand gesture controlled car racing game built with Python (OpenCV + MediaPipe) and Java (JavaFX), connected via TCP sockets.

## Tech Stack
- Python — OpenCV, MediaPipe (hand tracking)
- Java 26 — JavaFX (game engine)
- TCP Sockets — real-time communication
- Maven — build system

## How to Run

### 1. Install dependencies
- JDK 26
- Maven
- Python 3.10+

pip install opencv-python mediapipe


### 2. Start Java game first

cd java-game
mvn javafx:run


### 3. Start Python AI second

cd python-ai
python main.py


## Gesture Controls


 Hand left -> Steer left
 
 Hand right -> Steer right
 
 Open palm -> Accelerate 
 
 Closed fist -> Brake 

## Project Structure

    hand-gesture-racer/
    ├── java-game/              # JavaFX game engine
    │   ├── src/
    │   │   └── main/java/com/racer/
    │   │       ├── Main.java
    │   │       ├── GameController.java
    │   │       ├── SocketServer.java
    │   │       ├── PlayerCar.java
    │   │       ├── EnemyCar.java
    │   │       └── Road.java
    │   └── pom.xml
    └── python-ai/              # Hand gesture detection
        ├── main.py
        ├── hand_detector.py
        ├── gesture_recognizer.py
        └── socket_client.py
