package cc.kertaskerja.pengajuan_kta.service.global;

import cc.kertaskerja.pengajuan_kta.dto.external.FileDownloadDTO;
import cc.kertaskerja.pengajuan_kta.service.storage.LocalStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

@Service
public class R2StorageService {

    private final LocalStorageService localStorageService;

    public R2StorageService(LocalStorageService localStorageService) {
        this.localStorageService = localStorageService;
    }

    public String upload(MultipartFile file) throws IOException {
        return localStorageService.upload(file);
    }

    public byte[] getObject(String key) {
        if (isRemoteUrl(key)) {
            return downloadFromUrl(key).getData();
        }
        return localStorageService.download(key);
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        if (isRemoteUrl(fileUrl)) {
            // Cannot delete remote legacy files from local backend. Skip gracefully.
            return;
        }
        localStorageService.delete(fileUrl);
    }

    public FileDownloadDTO.DownloadRes download(String fileUrl) {
        // Intercept remote URLs and download them via HTTP stream
        if (isRemoteUrl(fileUrl)) {
            return downloadFromUrl(fileUrl);
        }

        byte[] data = localStorageService.download(fileUrl);
        String contentType = detectContentType(fileUrl);
        return FileDownloadDTO.DownloadRes.builder()
              .data(data)
              .filename(fileUrl)
              .contentType(contentType)
              .build();
    }

    private boolean isRemoteUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private FileDownloadDTO.DownloadRes downloadFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();

            // FIX: Set timeout 10 detik agar tidak hang selamanya jika R2 down/diblokir
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            // Tambahkan User-Agent, beberapa bucket menolak request tanpa User-Agent
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            byte[] data;
            try (InputStream in = connection.getInputStream()) {
                data = in.readAllBytes();
            }

            // Ekstrak nama file dan Content-Type langsung dari HTTP Headers jika memungkinkan
            String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
            String contentType = connection.getContentType();

            if (contentType == null) {
                contentType = URLConnection.guessContentTypeFromName(filename);
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return FileDownloadDTO.DownloadRes.builder()
                  .data(data)
                  .filename(filename)
                  .contentType(contentType)
                  .build();
        } catch (Exception e) {
            // Jika terjadi timeout atau error koneksi, akan dilempar sebagai RuntimeException
            // sehingga Spring Boot mengembalikan HTTP 500, BUKAN loading selamanya.
            throw new RuntimeException("Gagal mengunduh file eksternal dari URL: " + fileUrl + " | Pesan: " + e.getMessage(), e);
        }
    }

    private String detectContentType(String filename) {
        try {
            String ct = Files.probeContentType(
                  localStorageService.getRootLocation().resolve(filename));
            return ct != null ? ct : "application/octet-stream";
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}