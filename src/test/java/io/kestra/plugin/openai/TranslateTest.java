package io.kestra.plugin.openai;

import com.google.common.collect.ImmutableMap;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * This test will only test the main task, this allow you to send any input
 * parameters to your task and test the returning behaviour easily.
 */
@MicronautTest
class TranslateTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run() throws Exception {
        RunContext runContext = runContextFactory.of(ImmutableMap.of("variable", "John Doe"));

        Translate task = Translate.builder()
            .apiKey("")
            .to("english")
            .text("Ceci est un tutorial Kestra")
            .build();

        Translate.Output runOutput = task.run(runContext);

        assertThat(runOutput.getTranslation(), is("\"This is a Kestra tutorial.\""));
    }
}
