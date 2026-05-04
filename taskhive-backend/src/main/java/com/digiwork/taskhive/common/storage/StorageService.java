package com.digiwork.taskhive.common.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Storage service abstraction for file uploads.
 */
public interface StorageService {

    /**
     * Store a file and return the URL/path to access it.
     *
     * @param file      the uploaded file
     * @param directory subdirectory (e.g., "employees/photos")
     * @param filename  desired filename (without extension)
     * @param safeExtension validated secure extension to append
     * @return the accessible URL or relative path
     */
    String store(MultipartFile file, String directory, String filename, String safeExtension) throws IOException;

    /**
     * Delete a file by its stored path.
     */
    void delete(String filePath) throws IOException;

    /**
     * Get the full URL for a stored file path.
     */
    String getUrl(String filePath);

    /**
     * Load a file as a Resource.
     * @param filePath the relative path to the stored file
     * @return the resource
     */
    Resource loadAsResource(String filePath) throws IOException;
}
