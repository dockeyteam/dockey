# Dockey Serverless Functions

This directory contains Azure Functions for extending the Dockey platform with serverless capabilities.

## Available Functions

### comment-notifications

Sends email notifications when comments are added to documents. See [comment-notifications/README.md](./comment-notifications/README.md) for details.

## Why Serverless?

Serverless functions are ideal for:

- **Event-driven processing**: React to events (comments, document updates) without maintaining always-on services
- **Cost efficiency**: Pay only for execution time, perfect for sporadic workloads
- **Scalability**: Automatically scales to handle traffic spikes
- **Separation of concerns**: Keep notification logic separate from core services
- **Easy integration**: Can be triggered via HTTP, Event Grid, Service Bus, or Kafka

## Common Use Cases

1. **Notifications**: Email, SMS, push notifications for user activities
2. **Document Processing**: PDF conversion, OCR, image processing
3. **Analytics**: Event aggregation, reporting, data transformation
4. **Webhooks**: Handle external service integrations
5. **Scheduled Tasks**: Cleanup jobs, backups, report generation
6. **API Gateway**: Rate limiting, authentication, request transformation

## Adding New Functions

1. Create a new directory under `functions/`
2. Include:
   - `function_app.py` - Function code
   - `function.json` - Function binding configuration
   - `requirements.txt` - Python dependencies
   - `host.json` - Function app configuration
   - `README.md` - Documentation
3. Deploy using Azure Functions Core Tools or Azure DevOps pipeline

## Deployment

Functions can be deployed:

1. **Manually**: Using Azure Functions Core Tools
2. **Via CI/CD**: Add to `azure-pipelines.yml` to deploy on code changes
3. **Via Azure Portal**: Upload ZIP file through portal

See individual function READMEs for deployment instructions.
