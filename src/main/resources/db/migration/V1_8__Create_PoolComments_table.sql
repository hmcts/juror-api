CREATE TABLE juror_mod.pool_comments (
	id bigserial PRIMARY KEY,
	pool_no varchar(9) NOT NULL,
	user_id varchar(20) NOT NULL,
	last_update timestamp(0) NULL,
	pcomment varchar(80) NOT NULL,
	no_requested integer NULL DEFAULT 0
);

CREATE INDEX pool_comments_pool_idx ON juror_mod.pool_comments USING btree (pool_no);

-- juror.pool_comments foreign keys
ALTER TABLE juror_mod.pool_comments ADD CONSTRAINT pool_comments_fk FOREIGN KEY (pool_no) REFERENCES juror_mod.pool
--(pool_no);