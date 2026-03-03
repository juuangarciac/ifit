package com.uca.juangarcia.ifit.modules.training.client.dto;

public class IFitAIMaxMemoryIdResponseDTO {
    private Integer maxMemoryId;

    public IFitAIMaxMemoryIdResponseDTO() {
    }

     public IFitAIMaxMemoryIdResponseDTO(Integer maxMemoryId) {
        this.maxMemoryId = maxMemoryId;
    }

    public Integer getMaxMemoryId() {
        return maxMemoryId;
    }

    public void setMaxMemoryId(Integer maxMemoryId) {
        this.maxMemoryId = maxMemoryId;
    }
}
