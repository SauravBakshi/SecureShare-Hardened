# Security implementation notes

## Current protections

- Stateless HMAC-SHA256 bearer tokens with one-hour expiry. The HMAC key is required from `SECURESHARE_TOKEN_SECRET` and must be at least 32 bytes.
- Authenticated routes by default; admin statistics and global security events require `ROLE_ADMIN`.
- Authorization checks bind file access, sharing, deletion, and recipient event access to the principal in the verified token.
- BCrypt password hashing and 12-character minimum for new passwords.
- Login attempts are limited per source address to 10 per rolling minute per application instance.
- Uploads are capped at 25 MB per request and the application enforces a 250 MB active-file quota per account.
- Random per-file 256-bit AES keys; AES-GCM file encryption; RSA keys must match and be at least 2048 bits (new keys are generated at 3072 bits). RSA-OAEP parameters are explicitly SHA-256 and MGF1-SHA-256.
- Database password, signing secret, and RSA keys are external configuration only. No demo accounts are created.
- User password hashes and internal encryption/storage metadata are excluded from JSON serialization.
- Upload names are normalized for display, storage names are random, storage paths are constrained to the configured root, and downloads use safely constructed content-disposition headers.
- Decrypted download copies are deleted when the response stream closes and on decryption failure.
- CORS is restricted to one configured origin; Spring Security's standard security headers remain enabled.

## Operational requirements

- The secrets included in the original archive are compromised and must not be reused.
- Keep the RSA private key in a managed secret store and maintain protected backups. Loss of this key makes wrapped per-file keys unrecoverable.
- If legacy files exist, migrate them using the old key pair before rotating or discarding that key pair.
- Require TLS, protect the OS temp directory and upload volume, back up encrypted blobs and metadata together, and test restore/key recovery procedures.
- Use a shared rate limiter and request size limits at the reverse proxy in a multi-instance deployment.
- Apply `db/schema.sql` to a new database or use reviewed migrations; Hibernate is configured to validate schema only.

## Remaining review work before production

The backend compiled and packaged successfully on JDK 17 with Spring Boot 3.5.16. Five unit tests pass for token signing/tampering and authenticated AES encryption/decryption. The application has not been exercised against a live MySQL instance or browser. Run authorization-focused integration review in the target environment. Add centralized key management/rotation, paginated event queries and retention, a shared login limiter, malware quarantine if file policy requires it, dependency scanning, and HTTPS deployment checks before accepting real user data.
