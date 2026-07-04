package com.kelelep.controller;

import com.kelelep.model.DrainageInfrastructure;
import com.kelelep.model.ConcretePipe;
import com.kelelep.model.AbsorptionArea;
import com.kelelep.exception.KelelepOverflowException;
import com.kelelep.service.SimulationEngine;
import com.kelelep.service.GeminiService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardController {

    @FXML private TableView<DrainageInfrastructure> assetTable;
    @FXML private TableColumn<DrainageInfrastructure, String> colId;
    @FXML private TableColumn<DrainageInfrastructure, String> colType;
    @FXML private TableColumn<DrainageInfrastructure, Double> colCapacity;
    @FXML private TableColumn<DrainageInfrastructure, Double> colVolume;

    @FXML private Label statusIndicator;
    @FXML private Slider rainSlider;
    @FXML private Label rainValueLabel;
    
    // Panel Tengah (Simulasi)
    @FXML private TextArea simulationLogArea;
    @FXML private BarChart<String, Number> hydraulicChart;
    private XYChart.Series<String, Number> seriesVolume;
    private XYChart.Series<String, Number> seriesCapacity;

    // Panel Kanan (AI Advisor - Chat)
    @FXML private Label aiStatusLabel;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessageContainer;
    @FXML private TextField chatInputField;
    @FXML private Button btnSendChat;

    // CRUD input fields
    @FXML private TextField inputId;
    @FXML private ComboBox<String> inputType;
    @FXML private TextField inputCapacity;

    // Start/Stop buttons
    @FXML private Button btnStart;
    @FXML private Button btnStop;

    private SimulationEngine engine;
    private GeminiService geminiService;
    private ObservableList<DrainageInfrastructure> tableData;

    // Timeline untuk simulasi kontinu
    private Timeline simulationTimeline;
    private boolean isSimulationRunning = false;

    // Tracking aset yang meluap
    private final Set<String> overflowedAssets = new HashSet<>();
    private List<KelelepOverflowException> lastOverflows = null;
    private double lastRainIntensity = 0.0;

    @FXML
    public void initialize() {
        engine = new SimulationEngine();
        geminiService = new GeminiService();

        // Setup kolom tabel
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("maxCapacity"));
        colVolume.setCellValueFactory(new PropertyValueFactory<>("currentVolume"));

        inputType.setItems(FXCollections.observableArrayList("Concrete Pipe", "Absorption Area"));

        rainSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                rainValueLabel.setText(String.format("%.1f mm/h", newValue.doubleValue())));

        setupTableRowFactory();
        setupSimulationTimeline();
        setupChart();

        // Tampilkan pesan sambutan di chat
        addChatBubble("AI", "Halo! Saya KELELEP Advisor. Ada yang bisa saya bantu terkait infrastruktur drainase, manajemen banjir, atau teknik sipil perkotaan?");

        if (geminiService.isConfigured()) {
            aiStatusLabel.setText("● AI Gemini Terhubung");
            aiStatusLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 11px;");
        } else {
            aiStatusLabel.setText("● AI Belum Dikonfigurasi (isi config.properties)");
            aiStatusLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 11px;");
        }
    }

    /**
     * Dipanggil dari LandingController untuk menerima data parsing CSV atau load dummy.
     */
    public void setInitialData(List<DrainageInfrastructure> initialData) {
        if (initialData != null && !initialData.isEmpty()) {
            engine.getUrbanNetwork().clear();
            engine.getUrbanNetwork().addAll(initialData);
            logSystemEvent("Data eksternal dari CSV berhasil dimuat (" + initialData.size() + " aset).");
        } else {
            engine.loadDummyData();
            logSystemEvent("Data dummy bawaan berhasil dimuat.");
        }
        tableData = FXCollections.observableArrayList(engine.getUrbanNetwork());
        assetTable.setItems(tableData);
        updateChartData();
    }

    // =========================================================================
    // LOG & CHAT UI
    // =========================================================================

    private void logSystemEvent(String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            simulationLogArea.appendText("[" + time + "] " + message + "\n");
        });
    }

    private void addChatBubble(String sender, String message) {
        Platform.runLater(() -> {
            boolean isUser = sender.equals("User");

            Text nameLabel = new Text((isUser ? "👤 [ANDA]" : "🤖 [KELELEP AI]") + "\n");
            nameLabel.getStyleClass().add("chat-name");

            Text messageText = new Text(message);
            messageText.getStyleClass().add("chat-text");

            TextFlow textFlow = new TextFlow(nameLabel, messageText);
            
            HBox bubble = new HBox(textFlow);
            bubble.setMaxWidth(300);
            bubble.getStyleClass().add(isUser ? "chat-bubble-user" : "chat-bubble-ai");

            HBox row = new HBox(bubble);
            row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

            chatMessageContainer.getChildren().add(row);
            
            // Auto scroll ke bawah
            chatScrollPane.layout();
            chatScrollPane.setVvalue(1.0);
        });
    }

    // =========================================================================
    // FITUR 3: CRUD
    // =========================================================================

    @FXML
    public void onAddAsset() {
        String id = inputId.getText().trim();
        String type = inputType.getValue();
        String capacityText = inputCapacity.getText().trim();

        if (id.isEmpty()) { showAlert("Input Error", "ID saluran tidak boleh kosong!"); return; }
        if (type == null || type.isEmpty()) { showAlert("Input Error", "Pilih tipe infrastruktur!"); return; }
        if (capacityText.isEmpty()) { showAlert("Input Error", "Kapasitas maksimal tidak boleh kosong!"); return; }

        double capacity;
        try {
            capacity = Double.parseDouble(capacityText);
            if (capacity <= 0) { showAlert("Input Error", "Kapasitas harus lebih besar dari 0!"); return; }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Kapasitas harus berupa angka yang valid!"); return;
        }

        for (DrainageInfrastructure existing : engine.getUrbanNetwork()) {
            if (existing.getId().equalsIgnoreCase(id)) {
                showAlert("Duplicate ID", "Aset dengan ID '" + id + "' sudah ada!"); return;
            }
        }

        DrainageInfrastructure newAsset = "Concrete Pipe".equals(type)
                ? new ConcretePipe(id, capacity, 12.0)
                : new AbsorptionArea(id, capacity, 50.0);

        engine.getUrbanNetwork().add(newAsset);
        tableData.add(newAsset);
        logSystemEvent("Aset baru ditambahkan: " + id);
        updateChartData();

        inputId.clear();
        inputType.setValue(null);
        inputCapacity.clear();
    }

    @FXML
    public void onDeleteAsset() {
        DrainageInfrastructure selected = assetTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Pilihan Kosong", "Pilih baris aset di tabel yang ingin dihapus!"); return; }

        engine.getUrbanNetwork().remove(selected);
        tableData.remove(selected);
        overflowedAssets.remove(selected.getId());
        logSystemEvent("Aset dihapus: " + selected.getId());
        updateChartData();
    }

    // =========================================================================
    // FITUR BARCHART
    // =========================================================================

    private void setupChart() {
        seriesVolume = new XYChart.Series<>();
        seriesVolume.setName("Current Volume");
        
        seriesCapacity = new XYChart.Series<>();
        seriesCapacity.setName("Max Capacity");

        hydraulicChart.getData().addAll(seriesVolume, seriesCapacity);
    }

    private void updateChartData() {
        Platform.runLater(() -> {
            seriesVolume.getData().clear();
            seriesCapacity.getData().clear();
            
            for (DrainageInfrastructure asset : engine.getUrbanNetwork()) {
                XYChart.Data<String, Number> volData = new XYChart.Data<>(asset.getId(), asset.getCurrentVolume());
                XYChart.Data<String, Number> capData = new XYChart.Data<>(asset.getId(), asset.getMaxCapacity());
                
                seriesVolume.getData().add(volData);
                seriesCapacity.getData().add(capData);
                
                // Ubah warna bar menjadi merah jika overflow
                if (overflowedAssets.contains(asset.getId())) {
                    volData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                        if (newNode != null) {
                            newNode.setStyle("-fx-bar-fill: #EF4444;");
                        }
                    });
                }
            }
        });
    }

    // =========================================================================
    // FITUR 4: Simulator Timeline Kontinu
    // =========================================================================

    private void setupSimulationTimeline() {
        simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> runOneTick()));
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    @FXML
    public void onStartSimulation() {
        if (isSimulationRunning) return;

        isSimulationRunning = true;
        btnStart.setText("▶ RUNNING...");
        btnStart.setDisable(true);
        logSystemEvent("Simulasi DIMULAI. (Intensitas: " + String.format("%.1f", rainSlider.getValue()) + " mm/h)");
        simulationTimeline.play();
    }

    @FXML
    public void onStopSimulation() {
        if (simulationTimeline != null) simulationTimeline.stop();

        isSimulationRunning = false;
        btnStart.setText("▶ START SIMULATION");
        btnStart.setDisable(false);

        rainSlider.setValue(0);
        statusIndicator.setText("SAFE");
        setStatusStyle("status-safe");
        logSystemEvent("Simulasi DIHENTIKAN. Semua level air di-reset normal.");

        // Tanya user jika sebelumnya ada banjir sebelum di-reset
        if (lastOverflows != null && !lastOverflows.isEmpty()) {
            List<KelelepOverflowException> tempOverflows = lastOverflows;
            double tempRain = lastRainIntensity;
            
            Platform.runLater(() -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Konsultasi Mitigasi AI");
                confirm.setHeaderText("Simulasi Mendeteksi Banjir/Meluap");
                confirm.setContentText("Simulasi mendeteksi adanya aset yang meluap. Apakah Anda ingin mengirimkan laporan kejadian ke Gemini AI untuk mendapatkan panduan mitigasi?");
                
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        triggerAiMitigationReport(tempOverflows, tempRain);
                    }
                });
            });
        }

        for (DrainageInfrastructure asset : engine.getUrbanNetwork()) asset.resetVolume();
        overflowedAssets.clear();
        lastOverflows = null;
        assetTable.refresh();
        updateChartData();
        geminiService.resetChatHistory();
    }

    private void runOneTick() {
        double currentRain = rainSlider.getValue();
        List<KelelepOverflowException> overflows = engine.runSimulationCycle(currentRain);
        assetTable.refresh();
        updateChartData();

        if (overflows.isEmpty()) {
            if (overflowedAssets.isEmpty()) {
                statusIndicator.setText("SAFE");
                setStatusStyle("status-safe");
            }
        } else {
            // Tandai aset yang meluap
            for (KelelepOverflowException e : overflows) {
                if (overflowedAssets.add(e.getInfrastructureId())) {
                    logSystemEvent("⚠️ ALERT: Aset " + e.getInfrastructureId() + " MELUAP!");
                }
            }
            statusIndicator.setText("⚠ KELELEP DETECTED!");
            setStatusStyle("status-danger");
            assetTable.refresh();
            updateChartData();

            // Simpan state banjir terbaru untuk ditanyakan nanti saat simulasi di-stop
            this.lastOverflows = overflows;
            this.lastRainIntensity = currentRain;
        }
    }

    // =========================================================================
    // FITUR 5: Row Factory — Pewarnaan Baris Overflow
    // =========================================================================

    private void setupTableRowFactory() {
        assetTable.setRowFactory(tv -> new TableRow<DrainageInfrastructure>() {
            @Override
            protected void updateItem(DrainageInfrastructure item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    getStyleClass().removeAll("row-overflow");
                } else if (overflowedAssets.contains(item.getId())) {
                    if (!getStyleClass().contains("row-overflow")) getStyleClass().add("row-overflow");
                } else {
                    getStyleClass().removeAll("row-overflow");
                }
            }
        });
    }

    // =========================================================================
    // FITUR 6: Gemini AI — Laporan Mitigasi (Ke Log Terminal)
    // =========================================================================

    private void triggerAiMitigationReport(List<KelelepOverflowException> overflows, double rainfall) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        double totalExcess = overflows.stream().mapToDouble(KelelepOverflowException::getExcessVolume).sum();

        StringBuilder overflowData = new StringBuilder();
        overflowData.append("Waktu Deteksi: ").append(timestamp).append("\n");
        overflowData.append("Intensitas Hujan: ").append(String.format("%.1f", rainfall)).append(" mm/jam\n");
        overflowData.append("Total Volume Meluap: ").append(String.format("%.2f", totalExcess)).append(" m³\n");
        overflowData.append("Aset Terdampak:\n");

        for (KelelepOverflowException e : overflows) {
            engine.getUrbanNetwork().stream()
                    .filter(a -> a.getId().equals(e.getInfrastructureId()))
                    .findFirst()
                    .ifPresent(asset -> {
                        double pct = (e.getExcessVolume() / asset.getMaxCapacity()) * 100;
                        overflowData.append("  - ").append(asset.getId())
                                .append(" (").append(asset.getType()).append(")")
                                .append(": meluap ").append(String.format("%.2f", e.getExcessVolume()))
                                .append(" m³ (+").append(String.format("%.1f", pct)).append("%)\n");
                    });
        }

        logSystemEvent("Menganalisis kondisi meluap. Mengirim data ke AI Advisor...");

        geminiService.requestMitigationReport(overflowData.toString())
                .thenAccept(response -> Platform.runLater(() -> {
                    addChatBubble("AI", "🚨 LAPORAN MITIGASI DARURAT 🚨\n\n" + response);
                    logSystemEvent("Laporan mitigasi AI diterima di panel chat.");
                }));
    }

    // =========================================================================
    // FITUR 6: Gemini AI — Chat Interaktif (Ke Panel Kanan)
    // =========================================================================

    @FXML
    public void onSendChat() {
        String message = chatInputField.getText().trim();
        if (message.isEmpty()) return;

        // Tampilkan pesan user sebagai bubble chat
        addChatBubble("User", message);

        chatInputField.clear();
        btnSendChat.setDisable(true);

        aiStatusLabel.setText("● AI Sedang Menjawab...");
        aiStatusLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 11px;");

        // HBox sementara (Tanda sedang mengetik)
        Text typingText = new Text("🤖 [KELELEP AI]\nSedang memproses...");
        typingText.getStyleClass().add("chat-text");
        HBox typingBubble = new HBox(new TextFlow(typingText));
        typingBubble.setMaxWidth(300);
        typingBubble.getStyleClass().add("chat-bubble-ai");
        HBox typingRow = new HBox(typingBubble);
        typingRow.setAlignment(Pos.CENTER_LEFT);
        
        chatMessageContainer.getChildren().add(typingRow);
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);

        geminiService.sendChatMessage(message)
                .thenAccept(response -> Platform.runLater(() -> {
                    // Hapus indikator typing dan masukkan respons asli
                    chatMessageContainer.getChildren().remove(typingRow);
                    addChatBubble("AI", response);

                    btnSendChat.setDisable(false);
                    chatInputField.requestFocus();
                    aiStatusLabel.setText("● AI Terhubung");
                    aiStatusLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-size: 11px;");
                }));
    }

    // =========================================================================
    // FITUR: EXPORT REPORT TO PDF
    // =========================================================================

    @FXML
    public void onGeneratePdfReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save KELELEP Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("KELELEP_Report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
        
        File file = fileChooser.showSaveDialog(btnStart.getScene().getWindow());
        if (file == null) return;

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            document.add(new Paragraph("Laporan Sistem Drainase Kota (KELELEP)", titleFont));
            document.add(new Paragraph("Tanggal: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n\n", normalFont));

            document.add(new Paragraph("1. Status Sistem Saat Ini", headerFont));
            document.add(new Paragraph("Intensitas Hujan Terakhir: " + String.format("%.1f", rainSlider.getValue()) + " mm/h", normalFont));
            document.add(new Paragraph("Status Overflow: " + (overflowedAssets.isEmpty() ? "SAFE (Aman)" : "BAHAYA (Meluap)"), normalFont));
            document.add(new Paragraph("Aset Meluap: " + overflowedAssets.size() + " titik.\n\n", normalFont));

            document.add(new Paragraph("2. Daftar Aset Infrastruktur", headerFont));
            for (DrainageInfrastructure asset : engine.getUrbanNetwork()) {
                double pct = (asset.getCurrentVolume() / asset.getMaxCapacity()) * 100;
                String status = overflowedAssets.contains(asset.getId()) ? "[MELUAP]" : "[NORMAL]";
                document.add(new Paragraph(
                    String.format("- %s (%s) | Vol: %.2f / %.2f m3 (%.1f%%) %s", 
                        asset.getId(), asset.getType(), asset.getCurrentVolume(), asset.getMaxCapacity(), pct, status), 
                    normalFont
                ));
            }

            document.add(new Paragraph("\n3. Log Sistem", headerFont));
            String logs = simulationLogArea.getText();
            if (logs.length() > 500) logs = logs.substring(logs.length() - 500); // Ambil 500 karakter terakhir
            document.add(new Paragraph("Menampilkan log terbaru:\n" + logs, normalFont));

            document.close();

            logSystemEvent("Laporan PDF berhasil disimpan ke: " + file.getAbsolutePath());
            showAlert("Berhasil", "Laporan PDF berhasil disimpan!");

        } catch (Exception e) {
            logSystemEvent("GAGAL menyimpan laporan PDF: " + e.getMessage());
            showAlert("Error", "Gagal menyimpan PDF: " + e.getMessage());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void setStatusStyle(String styleClass) {
        statusIndicator.getStyleClass().removeAll("status-safe", "status-danger");
        statusIndicator.getStyleClass().add(styleClass);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}