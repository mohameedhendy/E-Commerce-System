UPDATE local_user
SET role = 'USER'
WHERE role IS NULL
   OR BTRIM(role) = '';

UPDATE local_user
SET role = UPPER(BTRIM(role));

ALTER TABLE local_user
    ALTER COLUMN role SET DEFAULT 'USER';

ALTER TABLE local_user
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE local_user
    ADD CONSTRAINT chk_local_user_role
        CHECK (
            role IN (
                     'USER',
                     'ADMIN'
                )
            );