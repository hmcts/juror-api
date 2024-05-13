ALTER TABLE juror_mod.content_store
	ADD PRIMARY KEY (request_id),
	ADD column failed_file_transfer boolean null;