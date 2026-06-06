package com.racer;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        GameController game = new GameController(stage);
        game.start();
    }
    public static void main(String[] args) { launch(args); }
}