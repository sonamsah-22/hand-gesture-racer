import socket

class SocketClient:
    def __init__(self, host='localhost', port=9999):
        self.host = host
        self.port = port
        self.sock = None

    def connect(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((self.host, self.port))
        print(f"Connected to Java game at {self.host}:{self.port}")

    def send(self, command: str):
        if self.sock:
            self.sock.sendall((command + '\n').encode('utf-8'))

    def close(self):
        if self.sock:
            self.sock.close()