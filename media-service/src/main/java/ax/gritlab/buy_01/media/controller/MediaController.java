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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/images")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Media> uploadImage(@RequestParam("file") MultipartFile file, Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        Media savedMedia = mediaService.save(file, (User) authentication.getPrincipal());
        return ResponseEntity.ok(savedMedia);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> serveImage(@PathVariable String id) {
        MediaService.MediaResource mediaResource = mediaService.getResourceById(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mediaResource.getContentType())
                .body(mediaResource.getResource());
    }

    @DeleteMapping("/images/{id}")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Void> deleteImage(@PathVariable String id, Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        mediaService.delete(id, (User) authentication.getPrincipal());
        return ResponseEntity.noContent().build();
    }
}
