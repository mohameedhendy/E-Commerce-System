ALTER TABLE public.review
    ALTER COLUMN rating SET NOT NULL;

ALTER TABLE public.review
    ALTER COLUMN comment SET NOT NULL;

ALTER TABLE public.review
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE public.review
    ADD CONSTRAINT ck_review_rating
        CHECK (rating BETWEEN 1 AND 5);

ALTER TABLE public.review
    ADD CONSTRAINT uk_review_user_product
        UNIQUE (user_id, product_id);

CREATE INDEX idx_review_product_created_at
    ON public.review (
        product_id,
        created_at DESC
    );