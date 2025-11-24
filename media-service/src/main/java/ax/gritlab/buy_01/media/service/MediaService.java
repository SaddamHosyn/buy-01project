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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    private final MediaRepository mediaRepository;
    private final StorageProperties storageProperties;
    private final RestTemplate restTemplate; // ADD THIS for inter-service communication
    private Path rootLocation;

    @Value("${api.gateway.url:http://localhost:8080/api/media}")
    private String apiGatewayUrl;

    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl; // ADD THIS

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

            LocalDateTime now = LocalDateTime.now();

            Media media = Media.builder()
                    .originalFilename(originalFilename)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .filePath(uniqueFilename)
                    .userId(user.getId())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            Media savedMedia = mediaRepository.save(media);

            // Set the URL after saving to get the ID
            savedMedia.setUrl(apiGatewayUrl + "/images/" + savedMedia.getId());
            return mediaRepository.save(savedMedia);

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
            // Check if it's an external URL (starts with http:// or https://)
            if (filename.startsWith("http://") || filename.startsWith("https://")) {
                // Return external URL as resource
                return new UrlResource(filename);
            }

            // Otherwise, load from local filesystem
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

    // If media is associated with a product, notify product service to remove it
    if (media.getProductId() != null) {
        try {
            String url = productServiceUrl + "/products/" + media.getProductId() + 
                        "/remove-media/" + media.getId();
            
            System.out.println("Calling Product Service to remove media: " + url); // DEBUG LOG
            
            restTemplate.delete(url);
            
            System.out.println("Successfully removed media from product"); // DEBUG LOG
        } catch (Exception e) {
            // Log the full error
            System.err.println("Failed to update product after media deletion: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace
            
            // DON'T throw exception - still proceed with media deletion
        }
    }

    // Delete physical file
    try {
        Path file = rootLocation.resolve(media.getFilePath());
        Files.deleteIfExists(file);
    } catch (IOException e) {
        System.err.println("Failed to delete file: " + media.getFilePath());
    }

    // Delete database record
    mediaRepository.delete(media);
}


    // Delete all media associated with a product
    public void deleteMediaByProductId(String productId) {
        mediaRepository.deleteByProductId(productId);
    }

    public List<Media> findAllByUserId(String userId) {
        return mediaRepository.findByUserId(userId);
    }

    public Media associateWithProduct(String mediaId, String productId, String userId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new RuntimeException("Media not found"));

        if (!media.getUserId().equals(userId)) {
            throw new RuntimeException("User does not have permission to modify this media");
        }

        media.setProductId(productId);
        media.setUpdatedAt(LocalDateTime.now());
        return mediaRepository.save(media);
    }
}
