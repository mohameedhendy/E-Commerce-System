UPDATE web_order
SET status = UPPER(BTRIM(status))
WHERE status IS NOT NULL;

UPDATE web_order
SET status = 'PENDING'
WHERE status IS NULL
   OR BTRIM(status) = '';

ALTER TABLE web_order
    ALTER COLUMN status SET DEFAULT 'PENDING';

ALTER TABLE web_order
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE web_order
    ADD CONSTRAINT chk_web_order_status
        CHECK (
            status IN (
                       'PENDING',
                       'CONFIRMED',
                       'CANCELLED'
                )
            );