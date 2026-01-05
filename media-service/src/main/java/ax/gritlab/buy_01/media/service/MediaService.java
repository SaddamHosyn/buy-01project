package ax.gritlab.buy_01.media.service;

import ax.gritlab.buy_01.media.config.StorageProperties;
import ax.gritlab.buy_01.media.exception.InvalidFileTypeException;
import ax.gritlab.buy_01.media.exception.ResourceNotFoundException;
import ax.gritlab.buy_01.media.exception.UnauthorizedException;
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

/**
 * Service class for managing media operations.
 */
@Service
@RequiredArgsConstructor
public class MediaService {
    /**
     * Maximum file size allowed (2MB).
     */
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private final MediaRepository mediaRepository;
    private final StorageProperties storageProperties;
    private final RestTemplate restTemplate;
    private Path rootLocation;

    @Value("${api.gateway.url:http://localhost:8080/api/media}")
    private String apiGatewayUrl;

    @Value("${product.service.url:http://localhost:8082}")
    private String productServiceUrl;

    /**
     * Wrapper class for media resource with content type.
     */
    @Getter
    @RequiredArgsConstructor
    public static final class MediaResource {
        private final Resource resource;
        private final String contentType;
    }

    /**
     * Initializes the storage location.
     */
    @PostConstruct
    public void init() {
        this.rootLocation =
                Paths.get(storageProperties.getLocation());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Could not initialize storage", e);
        }
    }

    /**
     * Helper method to delete physical file.
     *
     * @param filePath the path of the file to delete
     */
    private void deletePhysicalFile(final String filePath) {
        if (filePath != null
            && !filePath.startsWith("http://")
            && !filePath.startsWith("https://")) {
            try {
                Path file = rootLocation.resolve(filePath);
                Files.deleteIfExists(file);
            } catch (IOException e) {
                System.err.println("Failed to delete file: "
                                   + filePath);
            }
        }
    }

    /**
     * Find media by user ID.
     *
     * @param userId the user ID
     * @return list of media
     */
    public List<Media> findByUserId(final String userId) {
        return mediaRepository.findByUserId(userId);
    }

    /**
     * Associate media with a product.
     *
     * @param mediaId the media ID
     * @param productId the product ID
     * @param userId the user ID
     * @return updated media
     */
    public Media associateWithProduct(final String mediaId,
                                      final String productId,
                                      final String userId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Media not found with id: " + mediaId));
        if (!media.getUserId().equals(userId)) {
            throw new UnauthorizedException(
                    "You do not have permission to "
                    + "associate this media");
        }
        media.setProductId(productId);
        media.setUpdatedAt(LocalDateTime.now());
        Media updatedMedia = mediaRepository.save(media);
        return updatedMedia;
    }

    /**
     * Delete all media associated with a product.
     *
     * @param productId the product ID
     */
    public void deleteMediaByProductId(final String productId) {
        List<Media> medias =
                mediaRepository.findByProductId(productId);
        for (Media media : medias) {
            deletePhysicalFile(media.getFilePath());
        }

        // Remove records from DB
        if (!medias.isEmpty()) {
            mediaRepository.deleteAll(medias);
        }
    }

    /**
     * Delete media by explicit list of media IDs.
     *
     * @param ids list of media IDs
     */
    public void deleteMediaByIds(final List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Media> medias = mediaRepository.findAllById(ids);
        for (Media media : medias) {
            deletePhysicalFile(media.getFilePath());
        }

        if (!medias.isEmpty()) {
            mediaRepository.deleteAll(medias);
        }
    }

    /**
     * Delete all media owned by a user.
     *
     * @param userId the user ID
     */
    public void deleteMediaByUserId(final String userId) {
        if (userId == null) {
            return;
        }

        List<Media> medias = mediaRepository.findByUserId(userId);
        for (Media media : medias) {
            deletePhysicalFile(media.getFilePath());
        }

        if (!medias.isEmpty()) {
            mediaRepository.deleteAll(medias);
        }
    }

    /**
     * Save uploaded media file.
     *
     * @param file the uploaded file
     * @param user the user uploading the file
     * @return saved media entity
     */
    public Media save(final MultipartFile file, final User user) {
        if (file.isEmpty()) {
            throw new InvalidFileTypeException(
                    "Failed to store empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileTypeException(
                    "File exceeds maximum size of 2MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null
            || !contentType.startsWith("image/")) {
            throw new InvalidFileTypeException(
                    "Invalid file type. Only images are allowed.");
        }

        try {
            String originalFilename =
                    Objects.requireNonNull(file.getOriginalFilename());
            String extension = originalFilename.substring(
                    originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID() + extension;

            Path destinationFile = this.rootLocation
                    .resolve(Paths.get(uniqueFilename))
                    .normalize()
                    .toAbsolutePath();
            if (!destinationFile.getParent()
                 .equals(this.rootLocation.toAbsolutePath())) {
                throw new InvalidFileTypeException(
                        "Cannot store file outside "
                        + "current directory.");
            }

            try (InputStream inputStream =
                         file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                           StandardCopyOption.REPLACE_EXISTING);
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
            savedMedia.setUrl(apiGatewayUrl + "/images/"
                              + savedMedia.getId());
            return mediaRepository.save(savedMedia);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    /**
     * Get resource by ID.
     *
     * @param id the media ID
     * @return media resource
     */
    public MediaResource getResourceById(final String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Media not found with id: " + id));

        Resource resource = loadAsResource(media.getFilePath());
        return new MediaResource(resource, media.getContentType());
    }

    /**
     * Load file as resource.
     *
     * @param filename the filename
     * @return the resource
     */
    public Resource loadAsResource(final String filename) {
        try {
            // Check if it's an external URL
            if (filename.startsWith("http://")
                || filename.startsWith("https://")) {
                // Return external URL as resource
                return new UrlResource(filename);
            }

            // Otherwise, load from local filesystem
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException(
                        "Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException(
                    "Could not read file: " + filename);
        }
    }

    /**
     * Delete media by ID.
     *
     * @param id the media ID
     * @param user the user deleting the media
     */
    public void delete(final String id, final User user) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Media not found with id: " + id));

        if (!media.getUserId().equals(user.getId())) {
            throw new UnauthorizedException(
                    "You do not have permission to "
                    + "delete this media");
        }

        // If media is associated with a product, notify
        // product service to remove it
        if (media.getProductId() != null) {
            try {
                String url = productServiceUrl + "/products/"
                        + media.getProductId()
                        + "/remove-media/" + media.getId();

                System.out.println(
                        "Calling Product Service to "
                        + "remove media: " + url);

                restTemplate.delete(url);

                System.out.println(
                        "Successfully removed media from product");
            } catch (Exception e) {
                // Log the full error
                System.err.println(
                        "Failed to update product after "
                        + "media deletion: " + e.getMessage());
                e.printStackTrace();

                // DON'T throw exception - still proceed with
                // media deletion
            }
        }

        // Delete physical file
        deletePhysicalFile(media.getFilePath());

        // Delete database record
        mediaRepository.delete(media);
    }

}
