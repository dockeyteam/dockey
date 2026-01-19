# Comment Notifications Azure Function

A serverless Azure Function that sends email notifications when comments are added to documents in the Dockey platform.

## Overview

This function processes comment events from the Dockey comments-service and sends email notifications to users who are watching the document. It can be triggered via:

1. **HTTP Webhook**: Comments service calls this function directly via HTTP POST
2. **Azure Event Grid**: Can be configured to listen to Kafka events via Event Grid integration
3. **Azure Service Bus**: Can be configured to consume messages from a Service Bus queue

## Features

- ✅ Email notifications for new comments
- ✅ Email notifications when comments are liked
- ✅ Configurable notification preferences (via document watchers)
- ✅ Integration with Azure Communication Services for email delivery
- ✅ Integration with Keycloak/user-service for user information
- ✅ Error handling and logging

## Prerequisites

1. **Azure Function App** (Python 3.9+)
2. **Azure Communication Services** resource with Email domain configured
3. **Application Insights** (optional, for monitoring)
4. **Access to Dockey services** (user-service, docs-service) for fetching user/document data

## Setup

### 1. Create Azure Communication Services Resource

```bash
# Create Communication Services resource
az communication create \
  --name dockey-notifications \
  --resource-group FRItest \
  --data-location UnitedStates

# Get connection string
az communication list-key \
  --name dockey-notifications \
  --resource-group FRItest
```

### 2. Configure Email Domain

1. Go to Azure Portal → Communication Services → Email
2. Add a custom domain or use Azure-managed domain
3. Configure DNS records as instructed
4. Verify domain ownership

### 3. Deploy Function App

```bash
# Install Azure Functions Core Tools
npm install -g azure-functions-core-tools@4

# Login to Azure
az login

# Create Function App
az functionapp create \
  --resource-group FRItest \
  --consumption-plan-location eastus \
  --runtime python \
  --runtime-version 3.9 \
  --functions-version 4 \
  --name dockey-notifications-function \
  --storage-account <storage-account-name>

# Deploy function
cd functions/comment-notifications
func azure functionapp publish dockey-notifications-function
```

### 4. Configure Application Settings

```bash
# Set connection string
az functionapp config appsettings set \
  --name dockey-notifications-function \
  --resource-group FRItest \
  --settings \
    COMMUNICATION_SERVICES_CONNECTION_STRING="endpoint=https://...;accesskey=..." \
    EMAIL_SENDER="DoNotReply@dockey.example.com" \
    APP_BASE_URL="https://dockey.example.com" \
    KEYCLOAK_URL="http://keycloak:8080" \
    KEYCLOAK_REALM="dockey"
```

### 5. Get Function URL

```bash
# Get function URL with key
az functionapp function show \
  --name dockey-notifications-function \
  --resource-group FRItest \
  --function-name comment-notifications \
  --query invokeUrlTemplate
```

## Integration with Comments Service

### Option 1: HTTP Webhook (Recommended for Development)

Modify the `comments-service` to call this function after publishing to Kafka:

```java
// In CommentService.java, after publishing Kafka event:

// Also send to notification function
try {
    String functionUrl = System.getenv().getOrDefault(
        "NOTIFICATION_FUNCTION_URL", 
        "https://dockey-notifications-function.azurewebsites.net/api/comment-notifications"
    );
    String functionKey = System.getenv("NOTIFICATION_FUNCTION_KEY");
    
    RestClient restClient = RestClient.builder()
        .baseUrl(functionUrl)
        .build();
    
    restClient.post()
        .uri("?code=" + functionKey)
        .body(event)
        .retrieve()
        .body(String.class);
        
} catch (Exception e) {
    LOG.warn("Failed to trigger notification function", e);
    // Don't fail the comment creation if notification fails
}
```

### Option 2: Azure Event Grid (Recommended for Production)

1. **Set up Event Grid Custom Topic**:
```bash
az eventgrid topic create \
  --name dockey-comments-events \
  --resource-group FRItest \
  --location eastus
```

2. **Create Event Subscription**:
```bash
az eventgrid event-subscription create \
  --name comment-notifications-subscription \
  --source-resource-id /subscriptions/<sub-id>/resourceGroups/FRItest/providers/Microsoft.EventGrid/topics/dockey-comments-events \
  --endpoint-type azurefunction \
  --endpoint /subscriptions/<sub-id>/resourceGroups/FRItest/providers/Microsoft.Web/sites/dockey-notifications-function/functions/comment-notifications
```

3. **Configure Kafka to Event Grid Bridge** (requires custom Kafka connector)

### Option 3: Azure Service Bus

1. Create Service Bus queue
2. Configure Kafka to Service Bus connector
3. Update function to use Service Bus trigger instead of HTTP

## Local Development

### 1. Install Dependencies

```bash
cd functions/comment-notifications
pip install -r requirements.txt
```

### 2. Configure Local Settings

```bash
cp local.settings.json.example local.settings.json
# Edit local.settings.json with your values
```

### 3. Run Locally

```bash
func start
```

### 4. Test Function

```bash
curl -X POST http://localhost:7071/api/comment-notifications?code=<function-key> \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "COMMENT_ADDED",
    "commentId": "507f1f77bcf86cd799439011",
    "docId": "doc-123",
    "lineNumber": 42,
    "userId": "user-456",
    "newCommentCount": 5,
    "timestamp": "2026-01-11T10:30:00"
  }'
```

## Request Format

The function expects a JSON payload matching the `CommentEventMessage` structure:

```json
{
  "eventType": "COMMENT_ADDED" | "COMMENT_DELETED" | "COMMENT_LIKED" | "COMMENT_UNLIKED",
  "commentId": "507f1f77bcf86cd799439011",
  "docId": "doc-123",
  "lineNumber": 42,
  "userId": "user-456",
  "newCommentCount": 5,
  "timestamp": "2026-01-11T10:30:00"
}
```

## Response Format

```json
{
  "status": "completed" | "skipped" | "no_recipients",
  "document": "doc-123",
  "event_type": "COMMENT_ADDED",
  "notifications_sent": 3,
  "notifications_failed": 0,
  "results": [
    {
      "user_id": "user-789",
      "email": "user-789@example.com",
      "success": true
    }
  ]
}
```

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `COMMUNICATION_SERVICES_CONNECTION_STRING` | Azure Communication Services connection string | Yes |
| `EMAIL_SENDER` | Email address to send from (must be verified in ACS) | Yes |
| `APP_BASE_URL` | Base URL of Dockey application | Yes |
| `KEYCLOAK_URL` | Keycloak server URL | No |
| `KEYCLOAK_REALM` | Keycloak realm name | No |

## Cost Considerations

- **Azure Functions**: Pay per execution (Consumption plan)
- **Azure Communication Services**: Pay per email sent
- **Application Insights**: Free tier includes 5GB/month

Estimated cost for 10,000 notifications/month:
- Functions: ~$0.20
- Email: ~$1.00 (at $0.10 per 1,000 emails)
- **Total: ~$1.20/month**

## Monitoring

View function execution logs:

```bash
# Stream logs
az functionapp log tail \
  --name dockey-notifications-function \
  --resource-group FRItest

# View in Application Insights
az monitor app-insights component show \
  --app dockey-notifications-function \
  --resource-group FRItest
```

## Extending the Function

### Add SMS Notifications

```python
from azure.communication.sms import SmsClient

def send_sms_notification(phone_number: str, message: str):
    sms_client = SmsClient.from_connection_string(COMMUNICATION_SERVICES_CONNECTION_STRING)
    sms_client.send(
        from_="+1234567890",  # Verified phone number
        to=[phone_number],
        message=message
    )
```

### Add Push Notifications

Integrate with Azure Notification Hubs or Firebase Cloud Messaging.

### Add Slack/Teams Integration

Use webhooks to send notifications to Slack or Microsoft Teams channels.

## Troubleshooting

### Function not receiving requests
- Check function URL and access key
- Verify CORS settings if calling from browser
- Check Application Insights for errors

### Emails not sending
- Verify Communication Services connection string
- Check email domain is verified
- Review email sending logs in Communication Services

### High latency
- Consider using Azure Service Bus for async processing
- Implement caching for user/document lookups
- Use Application Insights to identify bottlenecks

## Security

- Store connection strings in Azure Key Vault
- Use Managed Identity for authentication where possible
- Enable HTTPS only
- Validate and sanitize all inputs
- Rate limit function calls to prevent abuse
