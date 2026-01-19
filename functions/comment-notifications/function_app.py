"""
Azure Function for Dockey Comment Notifications

This serverless function sends email notifications when comments are added to documents.
It can be triggered via HTTP webhook from the comments-service or via Azure Event Grid.

Trigger Options:
1. HTTP Trigger: Comments service calls this function via HTTP POST
2. Event Grid Trigger: Can be configured to listen to Kafka events via Event Grid
"""

import logging
import json
import os
from datetime import datetime
from typing import Dict, Any
import azure.functions as func
from azure.communication.email import EmailClient
from azure.identity import DefaultAzureCredential

# Initialize logging
logger = logging.getLogger(__name__)

# Azure Communication Services connection string
# Set in Function App Configuration: AzureWebJobsStorage or COMMUNICATION_SERVICES_CONNECTION_STRING
COMMUNICATION_SERVICES_CONNECTION_STRING = os.environ.get(
    "COMMUNICATION_SERVICES_CONNECTION_STRING",
    ""
)

# Email sender address (configured in Azure Communication Services)
EMAIL_SENDER = os.environ.get("EMAIL_SENDER", "DoNotReply@dockey.example.com")

# Application base URL for generating document links
APP_BASE_URL = os.environ.get("APP_BASE_URL", "https://dockey.example.com")

# Keycloak URL for fetching user information
KEYCLOAK_URL = os.environ.get("KEYCLOAK_URL", "http://keycloak:8080")
KEYCLOAK_REALM = os.environ.get("KEYCLOAK_REALM", "dockey")


def get_email_client():
    """Initialize Azure Communication Services Email Client"""
    if not COMMUNICATION_SERVICES_CONNECTION_STRING:
        raise ValueError("COMMUNICATION_SERVICES_CONNECTION_STRING not configured")
    return EmailClient.from_connection_string(COMMUNICATION_SERVICES_CONNECTION_STRING)


def fetch_user_email(user_id: str) -> str:
    """
    Fetch user email from Keycloak or user-service
    
    In production, you would call the user-service API or Keycloak Admin API
    to get the user's email address.
    """
    # TODO: Implement actual API call to user-service or Keycloak
    # For now, return a placeholder
    logger.info(f"Fetching email for user: {user_id}")
    
    # Example: Call user-service API
    # import requests
    # response = requests.get(f"{APP_BASE_URL}/api/users/{user_id}")
    # return response.json()["email"]
    
    return f"user-{user_id}@example.com"


def fetch_document_title(doc_id: str) -> str:
    """
    Fetch document title from docs-service
    
    In production, you would call the docs-service API to get document metadata.
    """
    logger.info(f"Fetching document title for: {doc_id}")
    
    # Example: Call docs-service API
    # import requests
    # response = requests.get(f"{APP_BASE_URL}/api/documents/{doc_id}")
    # return response.json()["title"]
    
    return f"Document {doc_id}"


def get_document_watchers(doc_id: str) -> list:
    """
    Get list of users who should be notified about comments on this document.
    
    This could include:
    - Document owner
    - Users who previously commented on the document
    - Users who have the document in their watchlist
    
    Returns list of user IDs
    """
    logger.info(f"Fetching watchers for document: {doc_id}")
    
    # TODO: Implement actual logic to fetch watchers
    # Example: Query database or call an API
    # For now, return empty list
    return []


def create_notification_email(
    event: Dict[str, Any],
    recipient_email: str,
    recipient_name: str = None
) -> Dict[str, Any]:
    """
    Create email content for comment notification
    
    Args:
        event: Comment event message from Kafka
        recipient_email: Email address of the recipient
        recipient_name: Optional name of the recipient
    
    Returns:
        Email message dictionary for Azure Communication Services
    """
    event_type = event.get("eventType", "")
    doc_id = event.get("docId", "")
    line_number = event.get("lineNumber", 0)
    user_id = event.get("userId", "")
    comment_id = event.get("commentId", "")
    timestamp = event.get("timestamp", datetime.now().isoformat())
    
    # Fetch document title (in production, cache this)
    doc_title = fetch_document_title(doc_id)
    document_url = f"{APP_BASE_URL}/documents/{doc_id}?line={line_number}"
    
    # Determine email subject and content based on event type
    if event_type == "COMMENT_ADDED":
        subject = f"New comment on {doc_title}"
        html_content = f"""
        <html>
        <body>
            <h2>New Comment on Document</h2>
            <p>A new comment has been added to <strong>{doc_title}</strong>.</p>
            <ul>
                <li><strong>Document:</strong> {doc_title}</li>
                <li><strong>Line:</strong> {line_number}</li>
                <li><strong>Comment ID:</strong> {comment_id}</li>
                <li><strong>Time:</strong> {timestamp}</li>
            </ul>
            <p><a href="{document_url}">View Document</a></p>
            <hr>
            <p style="color: #666; font-size: 12px;">
                You are receiving this notification because you are watching this document.
                <a href="{APP_BASE_URL}/settings/notifications">Manage notification preferences</a>
            </p>
        </body>
        </html>
        """
    elif event_type == "COMMENT_LIKED":
        subject = f"Your comment was liked on {doc_title}"
        html_content = f"""
        <html>
        <body>
            <h2>Comment Liked</h2>
            <p>Someone liked your comment on <strong>{doc_title}</strong>.</p>
            <p><a href="{document_url}">View Comment</a></p>
        </body>
        </html>
        """
    else:
        # For other event types, create a generic notification
        subject = f"Activity on {doc_title}"
        html_content = f"""
        <html>
        <body>
            <h2>Document Activity</h2>
            <p>There was activity on <strong>{doc_title}</strong>.</p>
            <p><a href="{document_url}">View Document</a></p>
        </body>
        </html>
        """
    
    return {
        "senderAddress": EMAIL_SENDER,
        "content": {
            "subject": subject,
            "html": html_content
        },
        "recipients": {
            "to": [
                {
                    "address": recipient_email,
                    "displayName": recipient_name or recipient_email
                }
            ]
        }
    }


def send_notification_email(email_message: Dict[str, Any]) -> bool:
    """
    Send email notification using Azure Communication Services
    
    Returns:
        True if email was sent successfully, False otherwise
    """
    try:
        email_client = get_email_client()
        
        poller = email_client.begin_send(email_message)
        result = poller.result()
        
        logger.info(f"Email sent successfully. Message ID: {result.get('messageId')}")
        return True
        
    except Exception as e:
        logger.error(f"Failed to send email: {str(e)}", exc_info=True)
        return False


def process_comment_event(event: Dict[str, Any]) -> Dict[str, Any]:
    """
    Process a comment event and send notifications to relevant users
    
    Args:
        event: Comment event message from Kafka/webhook
    
    Returns:
        Processing result with status and details
    """
    event_type = event.get("eventType", "")
    doc_id = event.get("docId", "")
    user_id = event.get("userId", "")
    
    # Only send notifications for certain event types
    if event_type not in ["COMMENT_ADDED", "COMMENT_LIKED"]:
        logger.info(f"Skipping notification for event type: {event_type}")
        return {
            "status": "skipped",
            "reason": f"Event type {event_type} does not require notifications"
        }
    
    # Get list of users to notify
    watchers = get_document_watchers(doc_id)
    
    # Don't notify the user who created the comment
    watchers = [w for w in watchers if w != user_id]
    
    if not watchers:
        logger.info(f"No watchers found for document: {doc_id}")
        return {
            "status": "no_recipients",
            "document": doc_id
        }
    
    # Send notifications
    results = []
    for watcher_id in watchers:
        try:
            watcher_email = fetch_user_email(watcher_id)
            email_message = create_notification_email(event, watcher_email)
            
            success = send_notification_email(email_message)
            results.append({
                "user_id": watcher_id,
                "email": watcher_email,
                "success": success
            })
            
        except Exception as e:
            logger.error(f"Failed to send notification to user {watcher_id}: {str(e)}")
            results.append({
                "user_id": watcher_id,
                "success": False,
                "error": str(e)
            })
    
    return {
        "status": "completed",
        "document": doc_id,
        "event_type": event_type,
        "notifications_sent": len([r for r in results if r.get("success")]),
        "notifications_failed": len([r for r in results if not r.get("success")]),
        "results": results
    }


# HTTP Trigger Function
def main(req: func.HttpRequest) -> func.HttpResponse:
    """
    HTTP-triggered Azure Function for processing comment notifications
    
    Expected request body:
    {
        "eventType": "COMMENT_ADDED",
        "commentId": "507f1f77bcf86cd799439011",
        "docId": "doc-123",
        "lineNumber": 42,
        "userId": "user-456",
        "newCommentCount": 5,
        "timestamp": "2026-01-11T10:30:00"
    }
    """
    try:
        # Parse request body
        event = req.get_json()
        
        if not event:
            return func.HttpResponse(
                json.dumps({"error": "Request body is required"}),
                status_code=400,
                mimetype="application/json"
            )
        
        # Validate required fields
        required_fields = ["eventType", "docId", "userId"]
        missing_fields = [field for field in required_fields if field not in event]
        
        if missing_fields:
            return func.HttpResponse(
                json.dumps({
                    "error": "Missing required fields",
                    "missing": missing_fields
                }),
                status_code=400,
                mimetype="application/json"
            )
        
        # Process the event
        logger.info(f"Processing comment event: {event.get('eventType')} for doc: {event.get('docId')}")
        result = process_comment_event(event)
        
        # Return result
        status_code = 200 if result.get("status") in ["completed", "skipped", "no_recipients"] else 500
        
        return func.HttpResponse(
            json.dumps(result, default=str),
            status_code=status_code,
            mimetype="application/json"
        )
        
    except Exception as e:
        logger.error(f"Error processing request: {str(e)}", exc_info=True)
        return func.HttpResponse(
            json.dumps({
                "error": "Internal server error",
                "message": str(e)
            }),
            status_code=500,
            mimetype="application/json"
        )
