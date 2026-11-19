# SecureShare

SecureShare is a Spring Boot and browser-based file sharing application. The backend now enforces signed bearer authentication, server-side ownership and share checks, per-file AES-256-GCM encryption, RSA-OAEP key wrapping, and no longer creates demo users or embeds credentials.

## Requirements

- JDK 17+
- MySQL 8+
- OpenSSL or the included `RsaKeyGenerator.java`
- Maven 3.8+

## First-time setup

1. Create a database and a dedicated application user with access only to this database. Do not use MySQL root for the app.
2. Apply `backend/src/main/resources/db/schema.sql` using an administrative database account.
3. Build a 3072-bit RSA key pair. With OpenSSL:

   ```powershell
   openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:3072 -out secure-share-private.pk8.pem
   openssl pkey -in secure-share-private.pk8.pem -pubout -out secure-share-public.pem
   openssl pkcs8 -topk8 -nocrypt -in secure-share-private.pk8.pem -outform DER -out secure-share-private.pk8.der
   openssl pkey -pubin -in secure-share-public.pem -pubout -outform DER -out secure-share-public.der
   ```

   Keep the private key in a secret manager. The application accepts base64 DER values. Convert them in PowerShell:

   ```powershell
   $env:SECURESHARE_RSA_PRIVATE_KEY = [Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path .\secure-share-private.pk8.der)))
   $env:SECURESHARE_RSA_PUBLIC_KEY = [Convert]::ToBase64String([IO.File]::ReadAllBytes((Resolve-Path .\secure-share-public.der)))
   ```

   Or compile and run `RsaKeyGenerator.java` in a private working directory. It writes the key files there and never prints the private key.

4. Set the required runtime secrets and connection settings in the process environment. Example PowerShell for local development:

   ```powershell
   $env:DB_URL = 'jdbc:mysql://localhost:3306/secure_file_sharing?sslMode=VERIFY_IDENTITY&allowPublicKeyRetrieval=false&serverTimezone=UTC'
   $env:DB_USERNAME = 'sfsuser'
   $env:DB_PASSWORD = '<secret from your password manager>'
   $env:SECURESHARE_TOKEN_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
   $env:SECURESHARE_RSA_PRIVATE_KEY = '<base64 DER private key from secret manager>'
   $env:SECURESHARE_RSA_PUBLIC_KEY = '<base64 DER public key from secret manager>'
   $env:APP_CORS_ALLOWED_ORIGIN = 'http://localhost:3000'
   ```

   `DB_PASSWORD`, `SECURESHARE_TOKEN_SECRET`, and both RSA key variables are required. Never commit their values, paste them into logs, or put them in `application.properties`. Configure MySQL with a trusted TLS certificate for the host in `DB_URL`; do not downgrade to unverified TLS for shared environments.

5. Start the backend from `backend/`:

   ```powershell
   mvn spring-boot:run
   ```

6. Serve `frontend/` over HTTP at the configured CORS origin. For local development, from `frontend/` run `python -m http.server 3000`, then open `http://localhost:3000/login.html`.
7. Register the first account. Registration only grants the USER role. To provision an administrator, use a trusted database admin session to set the role for the intended account:

   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'your-admin-username';
   ```

   Never expose a public role change endpoint.

## Security notes

- All routes except login and registration require an Authorization bearer token. Tokens expire after one hour.
- File owner IDs and acting user IDs are derived from the verified token; callers cannot select another identity through request fields.
- Only owners can create or revoke shares. Recipients receive READ access only; WRITE shares are rejected because file editing is not implemented.
- Every upload gets a random AES-256 key. AES-GCM authenticates file contents; the AES key is wrapped with RSA-OAEP SHA-256 / MGF1-SHA-256. RSA keys must match and be at least 2048 bits; newly generated keys are 3072 bits.
- Uploads are capped at 25 MB per request and the application enforces a 250 MB active-file quota per account.
- File downloads use an ephemeral decrypted file, deleted when the response stream closes. Ensure the operating system's temp directory is private and has adequate cleanup monitoring.
- The included legacy archive had credentials and a static key. Assume they are compromised. Rotate the database password and token secret. Existing encrypted files can only be migrated while their original RSA private key is available; do not discard it before migration.
- The app expects HTTPS in any shared or production deployment. Terminate TLS at a trusted reverse proxy, restrict network access to the backend, set the frontend origin explicitly, and store runtime secrets in a managed secret store. The in-memory login limiter is per process; use an edge/WAF or shared rate limiter for multi-instance deployments.
- JPA schema mode is `validate`; apply explicit schema migrations before startup. SQL logging and application DEBUG logging are disabled.

## API overview

- Public: `POST /api/auth/register`, `POST /api/auth/login`
- Authenticated: `/api/files`, `/api/shares`, `/api/users`, `/api/security-events/me`
- Admin role: `/api/admin/statistics`, `/api/security-events`

Pass the returned `data.token` as `Authorization: Bearer <token>` on protected requests. The browser client adds it automatically. Configure a trusted proxy to preserve the actual client IP for login rate limiting and security-event records; do not trust arbitrary forwarded headers.

## Build and run

Run the backend checks from `backend/` with `mvn verify`. To build an executable JAR, run `mvn -DskipTests package`, then configure the runtime environment variables and start `java -jar target/secure-file-sharing-0.0.1-SNAPSHOT.jar`.
