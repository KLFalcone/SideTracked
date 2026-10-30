-- ============================================================
-- V10: SIGN-IN DETAILS
-- ============================================================
--
-- A user signs in with an email + password, with Google, or both.
--
--   email           unique; the sign-in name for passwords
--   password_hash   a BCrypt hash, never the password itself
--   google_subject  Google's permanent id for the person ("sub")
--
-- All are empty for the existing local user until an account
-- claims it (see AuthService).

ALTER TABLE users
    ADD COLUMN email          VARCHAR(255) NULL,
    ADD COLUMN password_hash  VARCHAR(100) NULL,
    ADD COLUMN google_subject VARCHAR(255) NULL,
    ADD CONSTRAINT uk_users_email UNIQUE (email),
    ADD CONSTRAINT uk_users_google_subject UNIQUE (google_subject);
