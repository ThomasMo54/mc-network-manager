package com.motompro.instancemanager.plugin.config.general

import com.fasterxml.jackson.annotation.JsonProperty

data class PterodactylConfig(
    @field:JsonProperty("panel-url")
    val panelUrl: String,
    @field:JsonProperty("app-key")
    val appKey: String,
    @field:JsonProperty("client-key")
    val clientKey: String,
    @field:JsonProperty("user-id")
    val userId: String,
)