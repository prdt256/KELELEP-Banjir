package com.kelelep.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Service untuk berkomunikasi dengan Google Gemini API secara asynchronous.
 * API Key dibaca dari src/main/resources/config.properties.
 */
public class GeminiService {

    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;

    // Menyimpan riwayat chat agar AI memiliki konteks percakapan sebelumnya
    private final StringBuilder chatHistory = new StringBuilder();

    public GeminiService() {
        this.apiKey = loadApiKey();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.gson = new Gson();
    }

    /**
     * Membaca API Key dari file config.properties di dalam resources.
     */
    private String loadApiKey() {
        try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
            if (is == null) {
                System.err.println("[GeminiService] PERINGATAN: config.properties tidak ditemukan!");
                return "";
            }
            Properties props = new Properties();
            props.load(is);
            String key = props.getProperty("GEMINI_API_KEY", "").trim();
            if (key.equals("PASTE_API_KEY_ANDA_DI_SINI") || key.isBlank()) {
                System.err.println("[GeminiService] PERINGATAN: API Key belum diisi di config.properties!");
                return "";
            }
            System.out.println("[GeminiService] API Key berhasil dimuat.");
            return key;
        } catch (IOException e) {
            System.err.println("[GeminiService] ERROR membaca config: " + e.getMessage());
            return "";
        }
    }

    /**
     * Mengecek apakah API Key sudah dikonfigurasi.
     */
    public boolean isConfigured() {
        return !apiKey.isBlank();
    }

    /**
     * Mengirim prompt LAPORAN MITIGASI BANJIR ke Gemini secara asynchronous.
     * Dipanggil otomatis saat KelelepOverflowException terdeteksi.
     *
     * @param overflowData ringkasan data banjir (aset, volume, dll.)
     * @return CompletableFuture berisi teks respons AI
     */
    public CompletableFuture<String> requestMitigationReport(String overflowData) {
        String systemContext = "Kamu adalah AI advisor sistem manajemen banjir kota bernama KELELEP. " +
                "Berikan laporan mitigasi taktis dalam Bahasa Indonesia yang singkat dan terstruktur. " +
                "Gunakan format laporan dengan poin-poin rekomendasi tindakan darurat. " +
                "Tambahkan estimasi waktu penanganan bila memungkinkan.";

        String prompt = systemContext + "\n\n" +
                "DATA KONDISI DARURAT SAAT INI:\n" + overflowData + "\n\n" +
                "Berikan laporan mitigasi taktis segera!";

        return sendRequest(prompt);
    }

    /**
     * Mengirim pesan CHAT interaktif dari pengguna ke Gemini secara asynchronous.
     * Menyimpan riwayat percakapan agar AI memiliki konteks.
     *
     * @param userMessage pesan dari pengguna
     * @return CompletableFuture berisi teks respons AI
     */
    public CompletableFuture<String> sendChatMessage(String userMessage) {
        String systemContext = "Kamu adalah AI advisor sistem manajemen banjir dan drainase kota bernama KELELEP Advisor. " +
                "Jawab pertanyaan dalam Bahasa Indonesia dengan ringkas dan informatif. " +
                "Fokus pada topik: infrastruktur drainase, manajemen banjir, gorong-gorong, area resapan, dan teknik sipil perkotaan.";

        // Tambahkan pesan user ke riwayat
        chatHistory.append("User: ").append(userMessage).append("\n");

        String prompt = systemContext + "\n\nRIWAYAT PERCAKAPAN:\n" + chatHistory.toString() +
                "\nJawablah sebagai KELELEP Advisor:";

        return sendRequest(prompt).thenApply(response -> {
            // Simpan respons AI ke riwayat
            chatHistory.append("Advisor: ").append(response).append("\n");
            return response;
        });
    }

    /**
     * Mereset riwayat chat (dipanggil saat simulasi di-stop).
     */
    public void resetChatHistory() {
        chatHistory.setLength(0);
        System.out.println("[GeminiService] Riwayat chat direset.");
    }

    /**
     * Inti dari pengiriman request ke Gemini REST API.
     */
    private CompletableFuture<String> sendRequest(String prompt) {
        if (!isConfigured()) {
            return CompletableFuture.completedFuture(
                    "⚠️ [KELELEP AI] API Key belum dikonfigurasi!\n\n" +
                    "Langkah pengaktifan:\n" +
                    "1. Buka file: src/main/resources/config.properties\n" +
                    "2. Ganti nilai GEMINI_API_KEY dengan API Key Anda\n" +
                    "3. Dapatkan API Key gratis di: https://aistudio.google.com/app/apikey\n" +
                    "4. Restart aplikasi"
            );
        }

        // Susun body JSON request sesuai format Gemini API
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        // Konfigurasi safety agar AI lebih bebas menjawab konteks bencana
        JsonArray safetySettings = new JsonArray();
        String[] harmCategories = {
                "HARM_CATEGORY_HARASSMENT",
                "HARM_CATEGORY_HATE_SPEECH",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                "HARM_CATEGORY_DANGEROUS_CONTENT"
        };
        for (String category : harmCategories) {
            JsonObject safety = new JsonObject();
            safety.addProperty("category", category);
            safety.addProperty("threshold", "BLOCK_NONE");
            safetySettings.add(safety);
        }
        requestBody.add("safetySettings", safetySettings);

        String jsonBody = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_ENDPOINT + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(30))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseGeminiResponse(response.body());
                    } else if (response.statusCode() == 400) {
                        return "⚠️ [KELELEP AI] API Key tidak valid atau request bermasalah.\n" +
                               "Status: " + response.statusCode() + "\n" +
                               "Pastikan API Key Anda benar di config.properties.";
                    } else if (response.statusCode() == 429) {
                        return "⚠️ [KELELEP AI] Rate limit API tercapai. Coba lagi sebentar.";
                    } else {
                        return "⚠️ [KELELEP AI] Error dari server Gemini.\n" +
                               "Status HTTP: " + response.statusCode();
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("[GeminiService] ERROR koneksi: " + ex.getMessage());
                    return "⚠️ [KELELEP AI] Gagal terhubung ke Gemini API.\n" +
                           "Pastikan koneksi internet Anda aktif.\nError: " + ex.getMessage();
                });
    }

    /**
     * Mengekstrak teks dari respons JSON Gemini API.
     */
    private String parseGeminiResponse(String responseBody) {
        try {
            JsonObject json = gson.fromJson(responseBody, JsonObject.class);
            return json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
        } catch (Exception e) {
            System.err.println("[GeminiService] ERROR parsing respons: " + e.getMessage());
            return "⚠️ [KELELEP AI] Gagal membaca respons dari AI. Respons tidak terduga.";
        }
    }
}
