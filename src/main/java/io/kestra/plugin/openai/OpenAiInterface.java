package io.kestra.plugin.openai;

import io.kestra.core.models.annotations.PluginProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public interface OpenAiInterface {
    @Schema(
        title = "The OpenAI API key"
    )
    @PluginProperty(dynamic = true)
    @NotNull
    String getApiKey();
    @Schema(
        title = "A unique identifier representing your end-user."
    )
    @PluginProperty(dynamic = true)
    String getUser();
}
