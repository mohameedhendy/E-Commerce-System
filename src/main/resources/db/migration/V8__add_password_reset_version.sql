ALTER TABLE local_user
    ADD COLUMN password_reset_version BIGINT NOT NULL DEFAULT 0;