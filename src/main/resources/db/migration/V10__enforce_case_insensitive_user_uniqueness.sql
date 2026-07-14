CREATE UNIQUE INDEX ux_local_user_username_lower
    ON local_user (LOWER(username));

CREATE UNIQUE INDEX ux_local_user_email_lower
    ON local_user (LOWER(email));
