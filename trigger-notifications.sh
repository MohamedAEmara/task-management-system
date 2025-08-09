#!/bin/bash

# Script to manually trigger email notifications for incomplete tasks
# Usage: ./trigger-notifications.sh

echo "Triggering email notifications for incomplete tasks..."

# Default localhost URL (change if your app runs on different port/host)
APP_URL="http://localhost:8080"

# Make the API call
response=$(curl -s -w "\n%{http_code}" -X POST "${APP_URL}/api/notifications/send-incomplete-tasks")

# Extract response body and status code
http_code=$(echo "$response" | tail -n1)
response_body=$(echo "$response" | head -n -1)

echo "Response: $response_body"

# Check if successful
if [ "$http_code" -eq 200 ]; then
    echo "✅ Success! Notifications sent successfully."
    exit 0
else
    echo "❌ Failed! HTTP Status Code: $http_code"
    exit 1
fi
