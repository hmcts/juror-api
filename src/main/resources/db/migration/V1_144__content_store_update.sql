ALTER TABLE juror_mod.content_store
    DROP CONSTRAINT content_store_pkey,
    ADD CONSTRAINT content_store_pkey PRIMARY KEY (request_id, file_type);