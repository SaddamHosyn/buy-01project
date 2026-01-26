package ax.gritlab.buy_01.media.service;

import ax.gritlab.buy_01.media.config.StorageProperties;
import ax.gritlab.buy_01.media.exception.InvalidFileTypeException;
import ax.gritlab.buy_01.media.exception.ResourceNotFoundException;
import ax.gritlab.buy_01.media.exception.UnauthorizedException;
import ax.gritlab.buy_01.media.model.Media;
import ax.gritlab.buy_01.media.model.Role;
import ax.gritlab.buy_01.media.model.User;
import ax.gritlab.buy_01.media.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MediaService.
 */
@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MediaService mediaService;

    @TempDir
    Path tempDir;

    private Media testMedia;
    private User testUser;
    private final String mediaId = "media-123";
    private final String userId = "user-456";
    private final String productId = "product-789";

    @BeforeEach
    void setUp() {
        testMedia = Media.builder()
                .id(mediaId)
                .originalFilename("test-image.jpg")
                .contentType("image/jpeg")
                .size(1024L)
                .filePath("unique-filename.jpg")
                .userId(userId)
                .productId(productId)
                .url("http://localhost:8080/api/media/images/media-123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUser = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .role(Role.SELLER)
                .build();

        when(storageProperties.getLocation()).thenReturn(tempDir.toString());
        ReflectionTestUtils.setField(mediaService, "apiGatewayUrl", "http://localhost:8080/api/media");
        ReflectionTestUtils.setField(mediaService, "productServiceUrl", "http://localhost:8082");

        mediaService.init();
    }

    @Nested
    @DisplayName("findByUserId Tests")
    class FindByUserIdTests {

        @Test
        @DisplayName("Should return media list for user")
        void findByUserId_ReturnsMediaList() {
            Media media2 = Media.builder()
                    .id("media-456")
                    .userId(userId)
                    .build();

            when(mediaRepository.findByUserId(userId)).thenReturn(Arrays.asList(testMedia, media2));

            List<Media> result = mediaService.findByUserId(userId);

            assertEquals(2, result.size());
            verify(mediaRepository, times(1)).findByUserId(userId);
        }

        @Test
        @DisplayName("Should return empty list when user has no media")
        void findByUserId_ReturnsEmptyList() {
            when(mediaRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

            List<Media> result = mediaService.findByUserId(userId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("associateWithProduct Tests")
    class AssociateWithProductTests {

        @Test
        @DisplayName("Should associate media with product successfully")
        void associateWithProduct_Success() {
            Media unassociatedMedia = Media.builder()
                    .id(mediaId)
                    .userId(userId)
                    .build();

            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(unassociatedMedia));
            when(mediaRepository.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));

            Media result = mediaService.associateWithProduct(mediaId, productId, userId);

            assertNotNull(result);
            assertEquals(productId, result.getProductId());
            verify(mediaRepository, times(1)).save(any(Media.class));
        }

        @Test
        @DisplayName("Should throw exception when media not found")
        void associateWithProduct_MediaNotFound_ThrowsException() {
            when(mediaRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> mediaService.associateWithProduct("nonexistent", productId, userId));
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void associateWithProduct_NotOwner_ThrowsException() {
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

            assertThrows(UnauthorizedException.class,
                    () -> mediaService.associateWithProduct(mediaId, productId, "other-user"));
        }

        @Test
        @DisplayName("Should update timestamp on association")
        void associateWithProduct_UpdatesTimestamp() {
            LocalDateTime originalUpdatedAt = testMedia.getUpdatedAt();
            testMedia.setUserId(userId);
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
            when(mediaRepository.save(any(Media.class))).thenAnswer(i -> i.getArgument(0));

            mediaService.associateWithProduct(mediaId, productId, userId);

            assertNotEquals(originalUpdatedAt, testMedia.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("deleteMediaByProductId Tests")
    class DeleteMediaByProductIdTests {

        @Test
        @DisplayName("Should delete all media for product")
        void deleteMediaByProductId_Success() {
            Media media2 = Media.builder()
                    .id("media-456")
                    .productId(productId)
                    .filePath("http://example.com/image.jpg")
                    .build();

            when(mediaRepository.findByProductId(productId)).thenReturn(Arrays.asList(testMedia, media2));
            doNothing().when(mediaRepository).deleteAll(anyList());

            mediaService.deleteMediaByProductId(productId);

            verify(mediaRepository, times(1)).deleteAll(anyList());
        }

        @Test
        @DisplayName("Should handle product with no media")
        void deleteMediaByProductId_NoMedia() {
            when(mediaRepository.findByProductId(productId)).thenReturn(new ArrayList<>());

            mediaService.deleteMediaByProductId(productId);

            verify(mediaRepository, never()).deleteAll(anyList());
        }
    }

    @Nested
    @DisplayName("deleteMediaByIds Tests")
    class DeleteMediaByIdsTests {

        @Test
        @DisplayName("Should delete media by IDs")
        void deleteMediaByIds_Success() {
            List<String> ids = Arrays.asList("media-1", "media-2");
            Media media1 = Media.builder().id("media-1").filePath("http://example.com/1.jpg").build();
            Media media2 = Media.builder().id("media-2").filePath("http://example.com/2.jpg").build();

            when(mediaRepository.findAllById(ids)).thenReturn(Arrays.asList(media1, media2));
            doNothing().when(mediaRepository).deleteAll(anyList());

            mediaService.deleteMediaByIds(ids);

            verify(mediaRepository, times(1)).deleteAll(anyList());
        }

        @Test
        @DisplayName("Should handle null IDs list")
        void deleteMediaByIds_NullList() {
            mediaService.deleteMediaByIds(null);

            verify(mediaRepository, never()).findAllById(anyList());
        }

        @Test
        @DisplayName("Should handle empty IDs list")
        void deleteMediaByIds_EmptyList() {
            mediaService.deleteMediaByIds(new ArrayList<>());

            verify(mediaRepository, never()).findAllById(anyList());
        }
    }

    @Nested
    @DisplayName("deleteMediaByUserId Tests")
    class DeleteMediaByUserIdTests {

        @Test
        @DisplayName("Should delete all media for user")
        void deleteMediaByUserId_Success() {
            Media media1 = Media.builder().id("m1").userId(userId).filePath("http://example.com/1.jpg").build();
            Media media2 = Media.builder().id("m2").userId(userId).filePath("http://example.com/2.jpg").build();

            when(mediaRepository.findByUserId(userId)).thenReturn(Arrays.asList(media1, media2));
            doNothing().when(mediaRepository).deleteAll(anyList());

            mediaService.deleteMediaByUserId(userId);

            verify(mediaRepository, times(1)).deleteAll(anyList());
        }

        @Test
        @DisplayName("Should handle null user ID")
        void deleteMediaByUserId_NullUserId() {
            mediaService.deleteMediaByUserId(null);

            verify(mediaRepository, never()).findByUserId(anyString());
        }

        @Test
        @DisplayName("Should handle user with no media")
        void deleteMediaByUserId_NoMedia() {
            when(mediaRepository.findByUserId(userId)).thenReturn(new ArrayList<>());

            mediaService.deleteMediaByUserId(userId);

            verify(mediaRepository, never()).deleteAll(anyList());
        }
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should throw exception for empty file")
        void save_EmptyFile_ThrowsException() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", new byte[0]);

            assertThrows(InvalidFileTypeException.class,
                    () -> mediaService.save(emptyFile, testUser));
        }

        @Test
        @DisplayName("Should throw exception for file exceeding max size")
        void save_FileTooLarge_ThrowsException() {
            byte[] largeContent = new byte[3 * 1024 * 1024]; // 3MB
            MockMultipartFile largeFile = new MockMultipartFile(
                    "file", "large.jpg", "image/jpeg", largeContent);

            assertThrows(InvalidFileTypeException.class,
                    () -> mediaService.save(largeFile, testUser));
        }

        @Test
        @DisplayName("Should throw exception for non-image file")
        void save_NonImageFile_ThrowsException() {
            MockMultipartFile textFile = new MockMultipartFile(
                    "file", "test.txt", "text/plain", "hello".getBytes());

            assertThrows(InvalidFileTypeException.class,
                    () -> mediaService.save(textFile, testUser));
        }

        @Test
        @DisplayName("Should throw exception for null content type")
        void save_NullContentType_ThrowsException() {
            MockMultipartFile fileWithNullType = new MockMultipartFile(
                    "file", "test.jpg", null, "content".getBytes());

            assertThrows(InvalidFileTypeException.class,
                    () -> mediaService.save(fileWithNullType, testUser));
        }

        @Test
        @DisplayName("Should save valid image file")
        void save_ValidImage_Success() {
            MockMultipartFile validImage = new MockMultipartFile(
                    "file", "test.jpg", "image/jpeg", "fake image content".getBytes());

            when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
                Media m = invocation.getArgument(0);
                m.setId("new-media-id");
                return m;
            });

            Media result = mediaService.save(validImage, testUser);

            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals("image/jpeg", result.getContentType());
            verify(mediaRepository, times(2)).save(any(Media.class));
        }
    }

    @Nested
    @DisplayName("getResourceById Tests")
    class GetResourceByIdTests {

        @Test
        @DisplayName("Should throw exception when media not found")
        void getResourceById_NotFound_ThrowsException() {
            when(mediaRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> mediaService.getResourceById("nonexistent"));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should throw exception when media not found")
        void delete_NotFound_ThrowsException() {
            when(mediaRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> mediaService.delete("nonexistent", testUser));
        }

        @Test
        @DisplayName("Should throw exception when user is not owner")
        void delete_NotOwner_ThrowsException() {
            User otherUser = User.builder().id("other-user").build();
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));

            assertThrows(UnauthorizedException.class,
                    () -> mediaService.delete(mediaId, otherUser));
        }

        @Test
        @DisplayName("Should delete media and notify product service")
        void delete_Success_NotifiesProductService() {
            testMedia.setFilePath("http://example.com/image.jpg");
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
            doNothing().when(restTemplate).delete(anyString());
            doNothing().when(mediaRepository).delete(testMedia);

            mediaService.delete(mediaId, testUser);

            verify(restTemplate, times(1)).delete(anyString());
            verify(mediaRepository, times(1)).delete(testMedia);
        }

        @Test
        @DisplayName("Should continue with deletion even if product service call fails")
        void delete_ProductServiceFails_StillDeletes() {
            testMedia.setFilePath("http://example.com/image.jpg");
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
            doThrow(new RuntimeException("Service unavailable")).when(restTemplate).delete(anyString());
            doNothing().when(mediaRepository).delete(testMedia);

            mediaService.delete(mediaId, testUser);

            verify(mediaRepository, times(1)).delete(testMedia);
        }

        @Test
        @DisplayName("Should not call product service when media has no productId")
        void delete_NoProductId_SkipsProductService() {
            testMedia.setProductId(null);
            testMedia.setFilePath("http://example.com/image.jpg");
            when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(testMedia));
            doNothing().when(mediaRepository).delete(testMedia);

            mediaService.delete(mediaId, testUser);

            verify(restTemplate, never()).delete(anyString());
            verify(mediaRepository, times(1)).delete(testMedia);
        }
    }
}
