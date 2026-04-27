-- Add password reset token fields for forgot password feature
ALTER TABLE users ADD COLUMN password_reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN password_reset_token_expiry BIGINT;

-- Create index for token lookups
CREATE INDEX idx_user_password_reset_token ON users(password_reset_token);
