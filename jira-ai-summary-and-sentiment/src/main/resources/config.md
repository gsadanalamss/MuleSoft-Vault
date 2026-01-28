# Configuration Files

## connections.yaml
```yaml
jira:
  user: your-email@example.com
  api-token: YOUR_JIRA_API_TOKEN
  url: https://your-domain.atlassian.net
```

## envVars.json

```json
{
  "OPENAI": {
    "OPENAI_API_KEY": "YOUR_OPENAI_API_KEY"
  },
  "MISTRAL_AI": {
    "MISTRAL_AI_API_KEY": "YOUR_MISTRAL_AI_API_KEY"
  },
  "OLLAMA": {
    "OLLAMA_BASE_URL": "http://baseurl.ollama.com"
  },
  "GROQAI_OPENAI": {
    "GROQ_API_KEY": "YOUR_GROQAI_API_KEY"
  },
  "ANTHROPIC": {
    "ANTHROPIC_API_KEY": "YOUR_ANTHROPIC_API_KEY"
  },
  "AZURE_OPENAI": {
    "AZURE_OPENAI_KEY": "YOUR_AZURE_OPENAI_KEY",
    "AZURE_OPENAI_ENDPOINT": "https://endpoint.azure.com",
    "AZURE_OPENAI_DEPLOYMENT_NAME": "YOUR_DEPLOYMENT_NAME"
  },
  "HUGGING_FACE": {
    "HUGGING_FACE_API_KEY": "YOUR_HUGGING_FACE_API_KEY"
  },
  "GEMINI_AI": {
    "GEMINI_AI_KEY": "YOUR_GEMINI_AI_KEY"
  }
}
```