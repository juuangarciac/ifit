package com.uca.juangarcia.ifit.modules.training.client.dto;

public class IFitAIMaxMemoryIdResponseDto {
    private Integer maxMemoryId;

    public IFitAIMaxMemoryIdResponseDto() {
    }

     public IFitAIMaxMemoryIdResponseDto(Integer maxMemoryId) {
        this.maxMemoryId = maxMemoryId;
    }

    public Integer getMaxMemoryId() {
        return maxMemoryId;
    }

    public void setMaxMemoryId(Integer maxMemoryId) {
        this.maxMemoryId = maxMemoryId;
    }
}
