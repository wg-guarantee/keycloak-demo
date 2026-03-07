-- Create user table
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(100) NOT NULL UNIQUE,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `phone` VARCHAR(20),
  `email_verified` BOOLEAN DEFAULT FALSE,
  `phone_verified` BOOLEAN DEFAULT FALSE,
  `account_activated` BOOLEAN DEFAULT FALSE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create verification code table
CREATE TABLE IF NOT EXISTS `verification_code` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `type` ENUM('EMAIL_ACTIVATION', 'PASSWORD_RESET', 'SMS_VERIFICATION') NOT NULL,
  `code` VARCHAR(10) NOT NULL,
  `contact` VARCHAR(100) NOT NULL,
  `used` BOOLEAN DEFAULT FALSE,
  `expires_at` TIMESTAMP NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `used_at` TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_code (code),
  INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create email log table
CREATE TABLE IF NOT EXISTS `email_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT,
  `recipient` VARCHAR(100) NOT NULL,
  `subject` VARCHAR(255) NOT NULL,
  `template_type` VARCHAR(50) NOT NULL,
  `status` ENUM('SENDING', 'SUCCESS', 'FAILED') DEFAULT 'SENDING',
  `error_message` TEXT,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE SET NULL,
  INDEX idx_user_id (user_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create SMS log table
CREATE TABLE IF NOT EXISTS `sms_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT,
  `phone` VARCHAR(20) NOT NULL,
  `content` TEXT NOT NULL,
  `status` ENUM('SENDING', 'SUCCESS', 'FAILED') DEFAULT 'SENDING',
  `aliyun_message_id` VARCHAR(100),
  `error_message` TEXT,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE SET NULL,
  INDEX idx_user_id (user_id),
  INDEX idx_phone (phone),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create password history table
CREATE TABLE IF NOT EXISTS `password_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `old_password_hash` VARCHAR(255) NOT NULL,
  `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES `user`(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
