package com.kelelep;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Memuat file desain FXML
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/views/Login.fxml"));

        // Memasang FXML ke dalam Scene berukuran 1280x720
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);

        stage.setTitle("KELELEP SIMULATOR v1.0");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}