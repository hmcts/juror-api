
CREATE TABLE juror_mod.user_permissions (
	username varchar(30) NOT NULL,
	"permission" varchar(30) NOT NULL,
	CONSTRAINT user_permissions_pkey PRIMARY KEY (username, permission),
	CONSTRAINT user_permissions_permission_val CHECK (((permission)::text = ANY (ARRAY[('CREATE_JUROR'::character varying)::text])))
);

ALTER TABLE juror_mod.user_permissions ADD CONSTRAINT user_permissions_user_fkey FOREIGN KEY (username) REFERENCES juror_mod.users(username);