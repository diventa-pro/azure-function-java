package com.contoso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    public static final String EVENT_HUB_NAME = "functions-gco-20250218";
    public static final String EVENT_HUB_CONNECTION = "EventHubConnection";

   /* @FunctionName("blobFunction")
    public void blobFunction(
            @BlobTrigger(name = "file",
                    dataType = "binary",
                    path = "uploads/{fileName}",
                    connection = "BlobStorageConnection") byte[] content,
            @BindingName("fileName") String fileName,
            final ExecutionContext context
    ) {
        context.getLogger().info("Executing function blobFunction for file " + fileName + " with size " + content.length);
    }

    @FunctionName("periodicSqlQuery")
    public void periodicSqlQuery(
            @TimerTrigger(name = "periodicSqlQuery", schedule = "0 *\/5 * * * *") String when,
            @CosmosDBInput(name = "database",
                    databaseName = "WMS",
                    containerName = "WMSContainer"
                    , connection = "CosmosDBCnnection", sqlQuery = "SELECT * FROM Products where processed = false")List<Map<String, Object>> recprds,

            ) {

    }*/

    @FunctionName("periodicTask")
    @EventHubOutput(name = "event", eventHubName = EVENT_HUB_NAME, connection = EVENT_HUB_CONNECTION)
    public Map<String, String> periodicTask(
            @TimerTrigger(name = "periodicTaskTrigger", schedule = "0 */1 * * * *") String when,
            final ExecutionContext context
    ) {
        context.getLogger().info("Periodic task triggered. " + when);
        return Map.of("message", "Executed at " + LocalDateTime.now() + " when was " + when);
    }

    @FunctionName("processEventHubMessage")
    public void processEventHubMessage(
            @EventHubTrigger(
                    name = "event",
                    eventHubName = EVENT_HUB_NAME,
                    connection = EVENT_HUB_CONNECTION
            ) String message,
            final ExecutionContext context
    ) {
        context.getLogger().info("Arrived message via eventhub: " + message);
    }

    /**
     * This function listens at endpoint "/api/httpget". Invoke it using "curl" command in bash:
     * curl "http://localhost:7071/api/httpget?name=Awesome%20Developer"
     */
    @FunctionName("httpget")
    @EventHubOutput(name = "event", eventHubName = EVENT_HUB_NAME, connection = EVENT_HUB_CONNECTION)
    public Map<String, String> run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String name = Optional.ofNullable(request.getQueryParameters().get("name")).orElse("World");

        return Map.of("message", "Hello, " + name);
    }

    /**
     * This function listens at endpoint "/api/httppost". Invoke it using "curl" command in bash:
     * curl -i -X POST http://localhost:7071/api/httppost -H "Content-Type: text/json" -d "{\"name\": \"Awesome Developer\", \"age\": \"25\"}"
     */
    @FunctionName("httppost")
    //@StorageAccount("BlobStorageConnection")
    public HttpResponseMessage runPost(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Optional<String>> request,
            /*@BlobOutput(
                    name = "target",
                    path = "uploads/myfile"
            ) OutputBinding<byte[]> output,*/
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a POST request.");

        // Parse request body
        String name;
        Integer age;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(request.getBody().orElse("{}"));
            name = Optional.ofNullable(jsonNode.get("name")).map(JsonNode::asText).orElse(null);
            age = Optional.ofNullable(jsonNode.get("age")).map(JsonNode::asInt).orElse(null);
            if (name == null || age == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Please provide both name and age in the request body.").build();
            }
        } catch (Exception e) {
            context.getLogger().severe("Error parsing request body: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Error parsing request body").build();
        }

        String message = "Hello, " + name + "! You are " + age + " years old.";
        //output.setValue(message.getBytes(StandardCharsets.UTF_8));
        return request.createResponseBuilder(HttpStatus.OK).body(message).build();
    }
}
