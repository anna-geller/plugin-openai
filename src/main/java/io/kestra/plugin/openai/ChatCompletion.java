package io.kestra.plugin.openai;

import com.theokanning.openai.Usage;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.executions.metrics.Counter;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Given a list of messages comprising a conversation, the model will return a response.",
    description = "For more informations, refer to: https://platform.openai.com/docs/api-reference/chat/create"
)
public class ChatCompletion extends AbstractTask implements RunnableTask<ChatCompletion.Output> {
    @Schema(
        title = "A list of messages comprising the conversation so far.",
        description = "Required if prompt not set."
    )
    private List<ChatMessage> messages;
    @Schema(
        title = "Message to send to the API as prompt. Will be send as a user.",
        description = "If not set, messages are required and the last will be take as input by the API."
    )
    private String prompt;
    @Schema(
        title = "What sampling temperature to use, between 0 and 2."
    )
    private Double temperature;
    @Schema(
        title = "An alternative to sampling with temperature, where the model considers the results of the tokens with top_p probability mass."
    )
    private Double topP;

    @Schema(
        title = "How many chat completion choices to generate for each input message."
    )
    private Integer n;
    @Schema(
        title = "Up to 4 sequences where the API will stop generating further tokens."
    )
    private List<String> stop;
    @Schema(
        title = "The maximum number of tokens to generate in the chat completion."
    )
    private Integer maxTokens;
    @Schema(
        title = "Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far."
    )
    private Double presencePenalty;

    @Schema(
        title = "Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far."
    )
    private Double frequencyPenalty;
    @Schema(
        title = "Modify the likelihood of specified tokens appearing in the completion."
    )
    private Map<String, Integer> logitBias;
    @Schema(
        title = "Id of the model tu use",
        description = "See the model endpoint compatibility table for details: https://platform.openai.com/docs/models/model-endpoint-compatibility."
    )
    @PluginProperty(dynamic = true)
    @NotNull
    private String model;

    @Override
    public ChatCompletion.Output run(RunContext runContext) throws Exception {
        OpenAiService client = this.client(runContext);

        if (this.messages == null && this.prompt == null) {
            throw new Exception("At least messages or prompt must be set");
        }

        List<String> stop = this.stop != null ? runContext.render(this.stop) : null;
        String user = runContext.render(this.user);
        String model = runContext.render(this.model);

        List<ChatMessage> messages = new ArrayList<>();
        // Render all messages content
        if (this.messages != null) {
            for (ChatMessage message : this.messages) {
                try {
                    message.setContent(runContext.render(message.getContent()));
                } catch (IllegalVariableEvaluationException e) {
                    throw new RuntimeException(e);
                }
                messages.add(message);
            }
        }
        if (this.prompt != null) {
            messages.add(buildMessage("user", runContext.render(this.prompt)));
        }

        ChatCompletionResult chatCompletionResult = client.createChatCompletion(ChatCompletionRequest.builder()
            .messages(messages)
            .model(model)
            .temperature(this.temperature)
            .topP(this.topP)
            .n(this.n)
            .stop(stop)
            .maxTokens(this.maxTokens)
            .presencePenalty(this.presencePenalty)
            .frequencyPenalty(this.frequencyPenalty)
            .logitBias(this.logitBias)
            .user(user)
            .build()
        );

        runContext.metric(Counter.of("usage.prompt_tokens", chatCompletionResult.getUsage().getPromptTokens()));
        runContext.metric(Counter.of("usage.completion_tokens", chatCompletionResult.getUsage().getCompletionTokens()));
        runContext.metric(Counter.of("usage.total_tokens", chatCompletionResult.getUsage().getTotalTokens()));

        return ChatCompletion.Output.builder()
            .id(chatCompletionResult.getId())
            .object(chatCompletionResult.getObject())
            .created(chatCompletionResult.getCreated())
            .model(chatCompletionResult.getModel())
            .choices(chatCompletionResult.getChoices())
            .usage(chatCompletionResult.getUsage())
            .build();
    }

    @Builder
    @Getter
    public static class Output implements io.kestra.core.models.tasks.Output {
        @Schema(
            title = "Unique id assigned to this chat completion."
        )
        String id;
        @Schema(
            title = "The type of object returned, should be \"chat.completion\"."
        )
        String object;
        @Schema(
            title = "The creation time in epoch seconds."
        )
        long created;
        @Schema(
            title="The GPT model used."
        )
        String model;
        @Schema(
            title = "A list of all generated completions."
        )
        List<ChatCompletionChoice> choices;
        @Schema(
            title = "The API usage for this request."
        )
        Usage usage;
    }

    private ChatMessage buildMessage(String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setRole(role);
        message.setContent(content);

        return message;
    }
}
