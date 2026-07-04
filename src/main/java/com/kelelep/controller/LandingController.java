package com.kelelep.controller;

import com.kelelep.model.AbsorptionArea;
import com.kelelep.model.ConcretePipe;
import com.kelelep.model.DrainageInfrastructure;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LandingController {

    @FXML
    public void onShowCsvGuide(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Petunjuk Format Database CSV");
        alert.setHeaderText("Panduan Struktur Kolom File CSV");
        alert.setContentText(
            "File CSV Anda harus memiliki format/struktur data seperti berikut:\n\n" +
            "Baris 1 (Header): id,type,maxCapacity,attributeValue\n" +
            "Baris 2 (Data): CH-01,Concrete Pipe,1500,15.5\n" +
            "Baris 3 (Data): RES-01,Absorption Area,10000,85.0\n\n" +
            "Keterangan Kolom:\n" +
            "1. id : Kode unik aset bebas (cth: CH-01, PIPA-A, AREA-02)\n" +
            "2. type : Jenis aset, wajib ditulis 'Concrete Pipe' ATAU 'Absorption Area' (case-insensitive)\n" +
            "3. maxCapacity : Kapasitas tampung maksimal air dalam satuan m³ (angka)\n" +
            "4. attributeValue : \n" +
            "   - Jika tipe 'Concrete Pipe', ini adalah diameter pipa (inci)\n" +
            "   - Jika tipe 'Absorption Area', ini adalah daya serap tanah (mm/jam)\n\n" +
            "File template contoh 'network_template.csv' dapat Anda temukan di folder resource proyek Anda."
        );
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }

    @FXML
    public void onOpenSpreadsheetTemplate(ActionEvent event) {
        String url = "https://docs.google.com/spreadsheets/d/1luL_PyscM4aXokFK8J9nyoIOY8wcgf_lqvYze1f3Uho/edit?usp=sharing"; // Tautan template Google Sheets resmi yang baru
        try {
            // Membuka browser bawaan sistem OS
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                // Alternatif jika OS tidak support desktop AWT langsung (Linux headless/sandbox)
                Runtime.getRuntime().exec("xdg-open " + url);
            }
        } catch (Exception e) {
            System.err.println("[SYS] Gagal membuka browser: " + e.getMessage());
            // Fallback menampilkan link agar user tahu
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Tautan Google Sheets");
            alert.setHeaderText("Gagal Membuka Browser Otomatis");
            alert.setContentText("Silakan salin tautan berikut ke browser Anda:\n\n" + url);
            alert.showAndWait();
        }
    }

    @FXML
    public void onUploadCsv(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih File Data Infrastruktur (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("[SYS] File CSV terpilih: " + selectedFile.getAbsolutePath());
            List<DrainageInfrastructure> importedData = parseCsv(selectedFile);
            loadDashboard(stage, importedData);
        } else {
            System.out.println("[SYS] Batal memilih file.");
        }
    }

    @FXML
    public void onGoToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        System.out.println("[SYS] Memuat data dummy bawaan...");
        loadDashboard(stage, null); // null berarti gunakan dummy data di DashboardController
    }

    private List<DrainageInfrastructure> parseCsv(File file) {
        List<DrainageInfrastructure> dataList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Mendukung pemisah koma (,) maupun titik koma (;)
                String[] values = line.split("[,;]");
                
                // Cek jika baris pertama adalah header, kita skip
                if (isFirstLine) {
                    isFirstLine = false;
                    // Jika kolom pertama berisi kata "id" (header), skip baris ini
                    if (values[0].trim().equalsIgnoreCase("id") || values[0].trim().contains("id")) {
                        continue;
                    }
                }
                
                if (values.length >= 4) {
                    try {
                        String id = values[0].trim().replace("\"", "");
                        String type = values[1].trim().replace("\"", "");
                        double capacity = Double.parseDouble(values[2].trim().replace("\"", ""));
                        double attr = Double.parseDouble(values[3].trim().replace("\"", ""));

                        if (type.equalsIgnoreCase("Concrete Pipe")) {
                            dataList.add(new ConcretePipe(id, capacity, attr));
                        } else if (type.equalsIgnoreCase("Absorption Area")) {
                            dataList.add(new AbsorptionArea(id, capacity, attr));
                        } else {
                            System.out.println("[SYS] Tipe tidak dikenal pada baris: " + line);
                        }
                    } catch (NumberFormatException e) {
                        // Skip baris yang gagal di-parse menjadi angka (misal ada baris header ganda)
                        System.out.println("[SYS] Melewati baris tidak valid / header ganda: " + line);
                    }
                }
            }
            System.out.println("[SYS] Berhasil mem-parsing " + dataList.size() + " baris data dari CSV.");
        } catch (IOException e) {
            System.err.println("[SYS] Gagal membaca file CSV: " + e.getMessage());
        }
        return dataList;
    }

    private void loadDashboard(Stage stage, List<DrainageInfrastructure> initialData) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/MainDashboard.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
            
            // Pass data ke DashboardController
            DashboardController controller = fxmlLoader.getController();
            controller.setInitialData(initialData);

            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Gagal memuat Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
