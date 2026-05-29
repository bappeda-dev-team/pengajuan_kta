package cc.kertaskerja.pengajuan_kta.service.storage;

import cc.kertaskerja.pengajuan_kta.config.StorageProperties;
import cc.kertaskerja.pengajuan_kta.exception.InternalServerException;
import cc.kertaskerja.pengajuan_kta.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalStorageService implements FileStorageService {

    private final Path rootLocation;

    public LocalStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new InternalServerException("Could not initialize storage location", e);
        }
    }

    // Helper to prevent path concatenation errors if a raw URL bypasses R2StorageService
    private String extractFilename(String filename) {
        if (filename == null) return null;
        if (filename.startsWith("http://") || filename.startsWith("https://")) {
            return filename.substring(filename.lastIndexOf('/') + 1);
        }
        return filename;
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new InternalServerException("Failed to store empty file.");
            }

            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            if (originalFilename.contains("..")) {
                throw new InternalServerException("Cannot store file with relative path outside current directory.");
            }

            String uniqueFilename = UUID.randomUUID() + "-" + originalFilename;
            Path destinationFile = this.rootLocation.resolve(uniqueFilename).normalize();

            if (!destinationFile.startsWith(this.rootLocation)) {
                throw new InternalServerException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueFilename;
        } catch (IOException | NullPointerException e) {
            throw new InternalServerException("Failed to store file.", e);
        }
    }

    @Override
    public byte[] download(String filename) {
        try {
            // Apply defensive extraction
            String safeFilename = extractFilename(filename);
            Path file = rootLocation.resolve(safeFilename).normalize();

            if (!file.startsWith(this.rootLocation)) {
                throw new ResourceNotFoundException("Cannot access file outside storage directory.");
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new ResourceNotFoundException("File not found " + filename, e);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            // Apply defensive extraction
            String safeFilename = extractFilename(filename);
            Path file = rootLocation.resolve(safeFilename).normalize();

            if (file.startsWith(this.rootLocation)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            throw new InternalServerException("Failed to delete file.", e);
        }
    }

    @Override
    public Path getRootLocation() {
        return rootLocation;
    }
}