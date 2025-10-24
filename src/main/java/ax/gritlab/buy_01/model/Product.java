package ax.gritlab.buy_01.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Document(collection = "Product")
public class Product {
    @Id
    private String id;

    @NotNull
    @Size(min = 2, max = 100)
    @Field("name")
    private String name;

    // can be null
    @Size(max = 500)
    @Field("description")
    private String description;

    @NotNull
    @Field("price")
    private Double price;

    @NotNull
    @Field("quantity")
    private Integer quantity;

    @NotNull
    @Field("userId")
    private String userId;

    public Product(String id, String name, String description, Double price, Integer quantity, String userId) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}