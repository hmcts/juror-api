insert into juror_mod.users(created_by, updated_by, username, name, email)
values ('BUREAU1', 'BUREAU1', 'BUREAU_USER', 'BUREAU_USER', 'BUREAU_USER@test.net');

INSERT INTO juror_mod.user_roles (username,"role") VALUES
	 ('BUREAU_USER','MANAGER');

INSERT INTO juror_mod.user_permissions (username,"permission") VALUES
	 ('BUREAU_USER','CREATE_JUROR');

-- create a pool record owned by Bureau
INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request, last_update)
VALUES ('415220502', '400', current_date + 10, 5, 5, 'CRO', '415', 'N', current_date - 1);