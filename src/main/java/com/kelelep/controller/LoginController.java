package com.kelelep.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void onSignIn() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validasi akun akses dasar sistem
        if ("admin".equals(username) && "admin123".equals(password)) {
            try {
                // Mendapatkan reference stage/window aktif saat ini
                Stage stage = (Stage) usernameField.getScene().getWindow();

                // Memuat dan berpindah ke halaman utama (Landing Page)
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/LandingPage.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 1280, 720);

                stage.setScene(scene);
                stage.centerOnScreen();

            } catch (IOException e) {
                errorLabel.setText("Kesalahan sistem: Gagal memuat Landing Page.");
                e.printStackTrace();
            }
        } else {
            errorLabel.setText("Akses ditolak: Username atau password tidak valid.");
        }
    }
}