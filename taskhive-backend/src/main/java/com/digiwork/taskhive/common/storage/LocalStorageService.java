package com.digiwork.taskhive.common.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
public class LocalStorageService implements StorageService {

    @Value("${storage.local.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${storage.local.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    public String store(MultipartFile file, String directory, String filename) throws IOException {
        String extension = getExtension(file.getOriginalFilename());
        String fullFilename = filename + "." + extension;

        Path dirPath = Paths.get(uploadDir, directory);
        Files.createDirectories(dirPath);

        Path filePath = dirPath.resolve(fullFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = directory + "/" + fullFilename;
        log.info("File stored at: {}", filePath.toAbsolutePath());
        return relativePath;
    }

    @Override
    public void delete(String filePath) throws IOException {
        if (filePath == null)
            return;
        Path path = Paths.get(uploadDir, filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("File deleted: {}", path.toAbsolutePath());
        }
    }

    @Override
    public String getUrl(String filePath) {
        if (filePath == null)
            return null;
        return baseUrl + "/" + filePath;
    }

    private String getExtension(String filename) {
        if (filename == null)
            return "jpg";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "jpg";
    }
}
