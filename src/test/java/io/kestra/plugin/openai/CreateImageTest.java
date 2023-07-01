package io.kestra.plugin.openai;

import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@MicronautTest
public class CreateImageTest {
    @Inject
    private RunContextFactory runContextFactory;

    private String apiKey = "";


    @Test
    void runPromptUrl() throws Exception {
        RunContext runContext = runContextFactory.of();

        CreateImage task = CreateImage.builder()
            .apiKey(this.apiKey)
            .prompt("what is the capital of France?")
            .size(CreateImage.SIZE.SMALL)
            .build();

        CreateImage.Output runOutput = task.run(runContext);

        assertThat(runOutput.getData().size(), is(1));
        assertThat(runOutput.getFiles(), is(nullValue()));
    }

    @Test
    void runPromptB64Json() throws Exception {
        RunContext runContext = runContextFactory.of();

        CreateImage task = CreateImage.builder()
            .apiKey(this.apiKey)
            .prompt("what is the capital of France?")
            .size(CreateImage.SIZE.SMALL)
            .responseFormat(AbstractImageGeneration.FORMAT.B64_JSON)
            .build();

        CreateImage.Output runOutput = task.run(runContext);

        assertThat(runOutput.getData().size(), is(1));
        assertThat(runOutput.getData().get(0).getB64Json(), is(notNullValue()));
        assertThat(runOutput.getFiles(), is(nullValue()));
    }

    @Test
    void runPromptDownload() throws Exception {
        RunContext runContext = runContextFactory.of();

        CreateImage task = CreateImage.builder()
            .apiKey(this.apiKey)
            .prompt("and the capital of germany?")
            .size(CreateImage.SIZE.SMALL)
            .download(true)
            .build();

        CreateImage.Output runOutput = task.run(runContext);

        assertThat(runOutput.getData().size(), is(1));
        assertThat(runOutput.getData().get(0).getUrl(), is(notNullValue()));
        assertThat(runOutput.getFiles().size(), is(1));
    }
}
