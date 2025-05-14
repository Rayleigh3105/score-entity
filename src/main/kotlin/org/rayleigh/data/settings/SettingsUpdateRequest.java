package org.rayleigh.data.settings;

import java.time.LocalDateTime;

public class SettingsUpdateRequest {
    private Long id;
    private String price;
    private LocalDateTime endTime;

    public SettingsUpdateRequest() {
    }

    public SettingsUpdateRequest(Long id, LocalDateTime endTime, String price) {
        this.id = id;
        this.endTime = endTime;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
