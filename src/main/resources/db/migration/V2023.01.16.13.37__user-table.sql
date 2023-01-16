CREATE TABLE IF NOT EXISTS usr
(
    id               UUID PRIMARY KEY,

    created_by       TEXT,
    created_at       TIMESTAMP WITH TIME ZONE,
    modified_by      TEXT,
    modified_at      TIMESTAMP WITH TIME ZONE,

    email               TEXT UNIQUE NOT NULL,
    password            TEXT        NOT NULL,
    display_name        TEXT        NOT NULL,
    locale              TEXT        NOT NULL,
    roles               TEXT[]      NOT NULL,

    new_email           TEXT UNIQUE,
    tokens_valid_from   TIMESTAMP WITH TIME ZONE NOT NULL,
    version             INTEGER,

    CONSTRAINT email_len CHECK ( char_length(email) <= 1024 ),
    CONSTRAINT display_name_len CHECK ( char_length(display_name) <= 1024 ),
    CONSTRAINT locale_len CHECK ( char_length(locale) <= 255 ),
    CONSTRAINT new_email_len CHECK ( char_length(new_email) <= 1024 )
);


