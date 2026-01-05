package ax.gritlab.buy_01.media.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a media entity stored in the database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "media")
public final class Media {

    /**
     * The unique identifier for the media.
     */
    @Id
    private String id;

    /**
     * The original filename of the uploaded media.
     */
    private String originalFilename;

    /**
     * The content type of the media (e.g., image/jpeg).
     */
    private String contentType;

    /**
     * The size of the media file in bytes.
     */
    private long size;

    /**
     * Path to the file on disk or key in object storage.
     */
    private String filePath;

    /**
     * The user (seller) who owns this media.
     */
    private String userId;

    /**
     * Optional: The product this media is associated with.
     */
    private String productId;

    /**
     * The public URL to access this media.
     */
    private String url;

    /**
     * The timestamp when the media was created.
     */
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * The timestamp when the media was last updated.
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
