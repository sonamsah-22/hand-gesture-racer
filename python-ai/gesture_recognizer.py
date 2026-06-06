class GestureRecognizer:
    def __init__(self, frame_width=640):
        self.frame_width = frame_width
        self.center_zone = (0.40, 0.50)

    def recognize(self, landmarks) -> str:
        if landmarks is None:
            return "NONE"

        wrist_x = landmarks[0].x
        if wrist_x < self.center_zone[0]:
            return "LEFT"
        elif wrist_x > self.center_zone[1]:
            return "RIGHT"

        if self._is_fist(landmarks):
            return "BRAKE"
        return "ACCELERATE"

    def _is_fist(self, landmarks) -> bool:
        fingers = [(8,6),(12,10),(16,14),(20,18)]
        curled = sum(1 for tip, pip in fingers if landmarks[tip].y > landmarks[pip].y)
        return curled >= 3