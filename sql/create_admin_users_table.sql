-- ============================================
-- Caffe Bar 919 - Admin Users Table Setup
-- ============================================
-- Run this SQL in Supabase Dashboard > SQL Editor
-- URL: https://supabase.com/dashboard/project/wswizqibfqrmujlcfrav/sql

-- Create admin_users table
CREATE TABLE IF NOT EXISTS admin_users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  last_login TIMESTAMPTZ
);

-- Create index for username lookups
CREATE INDEX IF NOT EXISTS idx_admin_users_username ON admin_users(username);

-- Enable Row Level Security
ALTER TABLE admin_users ENABLE ROW LEVEL SECURITY;

-- Only service role can access admin_users (no public access)
CREATE POLICY "Service role only" ON admin_users
  FOR ALL
  USING (false);

-- Insert default admin user
-- IMPORTANT: You MUST change this password after first login!
-- Default credentials: username='admin', password='CaffeBar919!'
-- Password hash is bcrypt with 10 rounds
INSERT INTO admin_users (username, password_hash)
VALUES ('admin', '$2b$10$rZ7QEqKj8vJ9YGxF0nR.XeF5Z6KhVQqxBz7YVWZcJKNXTYGH1L3JG')
ON CONFLICT (username) DO NOTHING;

-- Note: To create a new password hash, use bcrypt with 10 rounds
-- In Node.js: const bcrypt = require('bcryptjs'); bcrypt.hashSync('your-password', 10);

-- Verify table creation
SELECT id, username, created_at FROM admin_users;
