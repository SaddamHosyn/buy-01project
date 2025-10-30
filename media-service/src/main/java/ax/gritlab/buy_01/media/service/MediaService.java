package ax.gritlab.buy_01.media.service;

import ax.gritlab.buy_01.media.config.StorageProperties;
import ax.gritlab.buy_01.media.model.Media;
import ax.gritlab.buy_01.media.model.User;
import ax.gritlab.buy_01.media.repository.MediaRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    private final MediaRepository mediaRepository;
    private final StorageProperties storageProperties;
    private Path rootLocation;

    @Getter
    @RequiredArgsConstructor
    public static class MediaResource {
        private final Resource resource;
        private final String contentType;
    }

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storageProperties.getLocation());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public Media save(MultipartFile file, User user) {
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File exceeds maximum size of 2MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Invalid file type. Only images are allowed.");
        }

        try {
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID() + extension;

            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename)).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            Media media = Media.builder()
                    .originalFilename(originalFilename)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .filePath(uniqueFilename)
                    .userId(user.getId())
                    .build();

            return mediaRepository.save(media);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    public MediaResource getResourceById(String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        Resource resource = loadAsResource(media.getFilePath());
        return new MediaResource(resource, media.getContentType());
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    public void delete(String id, User user) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        if (!media.getUserId().equals(user.getId())) {
            throw new RuntimeException("User does not have permission to delete this media");
        }

        try {
            Path file = rootLocation.resolve(media.getFilePath());
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Log this, but don't prevent DB record deletion
            System.err.println("Failed to delete file: " + media.getFilePath());
        }

        mediaRepository.delete(media);
    }
}
