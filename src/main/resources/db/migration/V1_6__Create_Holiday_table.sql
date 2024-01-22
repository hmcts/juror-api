CREATE TABLE juror_mod.holiday (
    id bigserial PRIMARY KEY,
	"owner" varchar(3) NULL,
	holiday DATE NOT NULL,
	description varchar(30) NOT NULL,
	"public" boolean NOT NULL,
	UNIQUE("owner", holiday)
);

CREATE INDEX holiday_index on juror_mod.holiday (holiday DESC);