package io.kestra.plugin.openai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Translate text with OpenAI API",
    description = "Use the OpenAI API to translate message from client"
)
public class Translate extends AbstractTask implements RunnableTask<Translate.Output> {
    @Schema(
        title = "Language to translate from"
    )
    @PluginProperty(dynamic = true)
    private String from;

    @Schema(
        title = "Language to translate to"
    )
    @PluginProperty(dynamic = true)
    @NotNull
    private String to;

    @Schema(
        title = "Text to translate"
    )
    @PluginProperty(dynamic = true)
    @NotNull
    private String text;

    @Override
    public Translate.Output run(RunContext runContext) throws Exception {
        // Easily create a client without any configuration in the Translate task
        OpenAiService client = this.client(runContext);

        String to = runContext.render(this.to);
        String text = runContext.render(this.text);
        String content;

        // Add language source if we know it
        if (this.from != null) {
            String from = runContext.render(this.from);
            content = String.format("Translate '%s' from %s in %s", text, from, to);
        } else {
            content = String.format("Translate '%s' in %s", text, to);
        }
        ChatMessage message = new ChatMessage("user", content);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
            .messages(List.of(message))
            .model("gpt-3.5-turbo")
            .n(1)
            .build();

        String translation = client.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();

        return Output.builder()
            .to(to)
            .text(text)
            .translation(translation)
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "The translated text"
        )
        private final String translation;

        @Schema(
            title = "The original text"
        )
        private final String text;

        @Schema(
            title = "Language to translate to"
        )
        private final String to;
    }
}
