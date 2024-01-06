// package dev.lueem.ai;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Properties;
// import java.io.FileInputStream;
// import java.io.IOException;

// import com.theokanning.openai.completion.chat.ChatCompletionRequest;
// import com.theokanning.openai.completion.chat.ChatMessage;
// import com.theokanning.openai.completion.chat.ChatMessageRole;
// import com.theokanning.openai.service.OpenAiService;

// public class OpenAiClient {

//     private OpenAiService service;
//     private Properties properties;

//     public OpenAiClient() {
//         properties = new Properties();
//         try {
//             properties.load(new FileInputStream("config.properties"));

//             String apiKey = properties.getProperty("openai.api.key");
//             service = new OpenAiService(apiKey);

//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//     public String askQuestion(String question) {
//         List<ChatMessage> messages = new ArrayList<>();
//         messages.add(new ChatMessage(ChatMessageRole.USER.value(), question));

//         ChatCompletionRequest request = ChatCompletionRequest.builder()
//                 .model("gpt-3.5-turbo-16k-0613")
//                 .messages(messages)
//                 .n(1)
//                 .maxTokens(10000)
//                 .build();

//         ChatMessage responseMessage = service.createChatCompletion(request).getChoices().get(0).getMessage();
//         return responseMessage.getContent();
//     }
// }
