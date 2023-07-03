package io.kestra.plugin.openai;

import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.image.ImageResult;
import com.theokanning.openai.service.OpenAiService;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Given a prompt, create an image",
    description = "For more information, refer to the [OpenAI Image Generation API docs](https://platform.openai.com/docs/api-reference/images/create)"
)
public class CreateImage extends AbstractImageGeneration implements RunnableTask<CreateImage.Output> {
    @Schema(
        title = "Whether you want to automatically download generated images"
    )
    @Builder.Default
    private Boolean download = true;

    @Override
    public CreateImage.Output run(RunContext runContext) throws Exception {
        OpenAiService client = this.client(runContext);

        String user = runContext.render(this.user);
        String prompt = runContext.render(this.prompt);

        ImageResult imageResult = client.createImage(CreateImageRequest.builder()
            .prompt(prompt)
            .size(this.size.getSize())
            .n(this.n)
            .responseFormat(this.responseFormat.getFormat())
            .user(user)
            .build()
        );

        CreateImage.Output.OutputBuilder output = Output.builder()
            .created(imageResult.getCreated())
            .data(imageResult.getData());


        if (this.download) {
            List<URI> files = new ArrayList<>();
            imageResult.getData().forEach(image -> {
                try {
                    if (this.responseFormat == FORMAT.URL) {
                        files.add(runContext.putTempFile(this.downloadUrl(runContext, image.getUrl())));
                    } else {
                        files.add(runContext.putTempFile(this.downloadB64Json(runContext, image.getB64Json())));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            output.files(files);
        }

        return output.build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "The creation time in epoch seconds"
        )
        long created;
        @Schema(
            title = "Metadata of generated images"
        )
        List<Image> data;
        @Schema(
            title = "Downloaded images"
        )
        List<URI> files;
    }

    private File downloadUrl(RunContext runContext, String url) throws IOException {
        File image = File.createTempFile("openai-" + UUID.randomUUID(), ".png");
        FileUtils.copyURLToFile(
            new URL(url),
            image
        );

        return image;
    }

    private File downloadB64Json(RunContext runContext, String encodedImage) throws IOException {
        File image = File.createTempFile("openai-" + UUID.randomUUID(), ".png");
        byte[] decodedBytes = Base64.getDecoder().decode(encodedImage);
        FileUtils.writeByteArrayToFile(image, decodedBytes);

        return image;
    }

}
