CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL DEFAULT 'USER',
  created_at DATETIME(6) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_metadata (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  file_name VARCHAR(255) NULL,
  file_size BIGINT NULL,
  file_path VARCHAR(255) NULL,
  encrypted_aes_key VARCHAR(1024) NULL,
  owner_id BIGINT NULL,
  uploaded_at DATETIME(6) NULL,
  status VARCHAR(255) NOT NULL DEFAULT 'ACTIVE',
  INDEX idx_file_owner_status (owner_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS file_shares (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  file_id BIGINT NULL,
  owner_id BIGINT NULL,
  shared_with_user_id BIGINT NULL,
  permission VARCHAR(255) NOT NULL DEFAULT 'READ',
  shared_at DATETIME(6) NULL,
  UNIQUE KEY uq_share_recipient (file_id, shared_with_user_id),
  INDEX idx_shares_recipient (shared_with_user_id),
  INDEX idx_shares_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS security_events (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NULL,
  action VARCHAR(255) NULL,
  ip_address VARCHAR(255) NULL,
  result VARCHAR(255) NULL,
  risk_level VARCHAR(255) NULL,
  timestamp DATETIME(6) NULL,
  INDEX idx_events_user_time (user_id, timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
