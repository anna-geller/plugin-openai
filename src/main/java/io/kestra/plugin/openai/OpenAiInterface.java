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
        title = "Id of the model tu use",
        description = "See the model endpoint compatibility table for details: https://platform.openai.com/docs/models/how-we-use-your-data."
    )
    @PluginProperty(dynamic = true)
    @NotNull
    String getModel();
}
