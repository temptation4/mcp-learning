# mcp-auth-server

A small local Spring Boot 4.1.1 OAuth 2.0 Authorization Server for learning the MCP Client Credentials flow.

## Configuration

- Server: `http://localhost:9000`
- Token endpoint: `http://localhost:9000/oauth2/token`
- Client ID: `mcp-client`
- Client secret: `mcp-secret`
- Grant type: `client_credentials`
- Scope: `mcp.read`
- Access-token lifetime: 10 minutes

## Start

```bash
cd mcp-auth-server
mvn spring-boot:run
```

## Get an access token

In another terminal:

```bash
curl -u mcp-client:mcp-secret \
  -X POST \
  http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&scope=mcp.read"
```

A successful response contains an `access_token` JWT.

## Who uses this

- `mcp-time-server` validates every incoming request's JWT against this server's issuer (`http://localhost:9000`) — see its `SecurityConfig.java`.
- `mcp-client-demo` requests tokens from here via the `client_credentials` grant and attaches them to its requests to `mcp-time-server` — see its `McpOAuthSecurityConfig.java`.

Start this server before either of those two, or their OAuth2-dependent calls will fail.

## Notes

This project is for local learning only. The client secret is intentionally stored in `application.yml` and should not be used this way in production.
