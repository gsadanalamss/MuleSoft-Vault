# TLS Certificate Configuration for Mule Application

This document describes how to add a TLS certificate to a Mule application and configure the required properties in `config.yaml` using general, descriptive values.

---

## Step 1: Download and Add the TLS Certificate

1. Download the TLS certificate file (for example, a JKS keystore).
2. Copy the certificate file into the Mule project directory:

src/main/resources

This ensures the keystore is available on the application classpath.

---

## Step 2: Update config.yaml

Add the following entries to `config.yaml` using general, self-explanatory values:

```yaml
api:
  instanceid: "api-manager-instance-id"

tls:
  alias: "keystore-alias"
  keyPassword: "keystore-key-password"
  password: "keystore-password"
  path: "keystore-file-name"

http:
  port: "https-listener-port"
  ```

---

## Configuration Details

### api
- instanceid: API Manager instance identifier associated with the application

### tls
- alias: Alias name of the private key inside the keystore
- keyPassword: Password protecting the private key
- password: Password protecting the keystore
- path: Path or filename of the keystore located in `src/main/resources`

### http
- port: Port number on which the HTTPS listener is exposed

---

## Notes

- Values shown are placeholders intended for documentation clarity
- Do not commit real secrets or production keystores to source control
- Use secure properties or environment-specific configuration for sensitive values

---
