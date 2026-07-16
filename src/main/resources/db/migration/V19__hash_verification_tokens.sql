DELETE FROM verification_token;

ALTER TABLE verification_token
    RENAME COLUMN token TO token_hash;

ALTER TABLE verification_token
    ALTER COLUMN token_hash TYPE VARCHAR(64);