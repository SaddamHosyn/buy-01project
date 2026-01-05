package ax.gritlab.buy_01.media.controller;

import ax.gritlab.buy_01.media.model.Media;
import ax.gritlab.buy_01.media.model.User;
import ax.gritlab.buy_01.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for media operations.
 */
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public final class MediaController {

    /**
     * Media service for business logic.
     */
    private final MediaService mediaService;

    /**
     * Gets all media for the authenticated user.
     *
     * @param authentication the authentication object
     * @return list of user's media
     */
    @GetMapping("/images")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<List<Media>> getAllUserMedia(
            final Authentication authentication) {
        List<Media> mediaList = mediaService.findByUserId(
                ((User) authentication.getPrincipal()).getId());
        return ResponseEntity.ok(mediaList);
    }

    /**
     * Uploads a new image.
     *
     * @param file           the image file
     * @param authentication the authentication object
     * @return the saved media
     */
    @PostMapping("/images")
    @PreAuthorize("hasAnyAuthority('SELLER', 'CLIENT')")
    public ResponseEntity<Media> uploadImage(
            @RequestParam("file") final MultipartFile file,
            final Authentication authentication) {
        Media savedMedia = mediaService.save(file,
                (User) authentication.getPrincipal());
        return ResponseEntity.ok(savedMedia);
    }

    /**
     * Serves an image by ID.
     *
     * @param id the media ID
     * @return the image resource
     */
    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> serveImage(
            @PathVariable final String id) {
        try {
            MediaService.MediaResource mediaResource = mediaService.getResourceById(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE,
                            mediaResource.getContentType())
                    .body(mediaResource.getResource());
        } catch (RuntimeException e) {
            // Return 404 for missing images instead of
            // throwing exception
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes an image by ID.
     *
     * @param id             the media ID
     * @param authentication the authentication object
     * @return no content response
     */
    @DeleteMapping("/images/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable final String id,
            final Authentication authentication) {
        mediaService.delete(id,
                (User) authentication.getPrincipal());
        return ResponseEntity.noContent().build();
    }

    /**
     * Associates media with a product.
     *
     * @param id             the media ID
     * @param productId      the product ID
     * @param authentication the authentication object
     * @return the updated media
     */
    @PutMapping("/images/{id}/product/{productId}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Media> associateWithProduct(
            @PathVariable final String id,
            @PathVariable final String productId,
            final Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        Media updatedMedia = mediaService.associateWithProduct(
                id, productId, userId);
        return ResponseEntity.ok(updatedMedia);
    }
}
