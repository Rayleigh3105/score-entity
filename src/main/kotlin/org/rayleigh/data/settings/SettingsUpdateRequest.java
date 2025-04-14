package org.rayleigh.data.settings;

import java.time.LocalDateTime;

public class SettingsUpdateRequest {
    private Long id;
    private LocalDateTime endTime;

    public SettingsUpdateRequest() {
    }

    public SettingsUpdateRequest(Long id, LocalDateTime endTime) {
        this.id = id;
        this.endTime = endTime;
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
}
