package ax.gritlab.buy_01.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for file storage.
 */
@ConfigurationProperties("storage")
public final class StorageProperties {

    /**
     * The location where files are stored.
     */
    private String location = "uploads";

    /**
     * Gets the storage location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the storage location.
     *
     * @param newLocation the location to set
     */
    public void setLocation(final String newLocation) {
        this.location = newLocation;
    }

}
