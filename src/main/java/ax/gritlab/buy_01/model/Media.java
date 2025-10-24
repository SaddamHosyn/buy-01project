package ax.gritlab.buy_01.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotNull;

@Document(collection = "media")
public class Media {

    @Id
    private String id;

    // TODO: add validator for path?
    @NotNull
    @Field("imagePath")
    private String imagePath;

    @NotNull
    @Field("productId")
    private String productId;

    public Media(String id, String imagePath, String productId) {
        super();
        this.id = id;
        this.imagePath = imagePath;
        this.productId = productId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}