package com.joyreaim.beatstape.profilemanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProfileInfo {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long createdTimestamp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long lastUpdateTimestamp;
    public String sns;
    public String bio;
    public String userName;
    private String artistName;
}
