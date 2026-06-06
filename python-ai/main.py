import cv2
import time
from hand_detector import HandDetector
from gesture_recognizer import GestureRecognizer
from socket_client import SocketClient

def main():
    cap = cv2.VideoCapture(0)
    detector = HandDetector()
    recognizer = GestureRecognizer()
    client = SocketClient()

    print("Waiting for Java game to start...")
    while True:
        try:
            client.connect()
            break
        except ConnectionRefusedError:
            time.sleep(1)

    last_command = ""
    try:
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            frame = cv2.flip(frame, 1)  # mirror
            frame, landmarks = detector.detect(frame)
            command = recognizer.recognize(landmarks)

            if command != last_command:
                client.send(command)
                last_command = command

            cv2.putText(frame, f"CMD: {command}", (10, 40),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
            cv2.imshow("Hand Gesture Controller", frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
    finally:
        client.close()
        cap.release()
        cv2.destroyAllWindows()

if __name__ == "__main__":
    main()