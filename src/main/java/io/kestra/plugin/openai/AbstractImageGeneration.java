package io.kestra.plugin.openai;

import io.kestra.core.models.annotations.PluginProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class AbstractImageGeneration extends AbstractTask {
    @Schema(
        title = "Message to send to the API as prompt."
    )
    @NotNull
    @PluginProperty(dynamic = true)
    protected String prompt;
    @Schema(
        title = "The number of images to generate. Must be between 1 and 10."
    )
    protected Integer n;
    @Schema(
        title = "The size of the generated images."
    )
    @Builder.Default
    protected SIZE size = SIZE.LARGE;
    @Schema(
        title = "The format in which the generated images are returned.",
        description = "Must be one of `url` or `b64_json`."
    )
    @Builder.Default
    protected FORMAT responseFormat = FORMAT.URL;

    protected enum SIZE {
        SMALL("256x256"),
        MEDIUM("512x512"),
        LARGE("1024x1024");

        private final String value;

        SIZE(String value) {
            this.value = value;
        }

        public String getSize() {
            return value;
        }

    }

    protected enum FORMAT {
        URL("url"),
        B64_JSON("b64_json");

        private final String value;

        FORMAT(String value) {
            this.value = value;
        }

        public String getFormat() {
            return value;
        }

    }
}
