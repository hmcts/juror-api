-- JS-621 Excluded voters

CREATE TABLE juror_mod.excluded_voters (
	firstname varchar(20) not null,
	lastname varchar(25) not null,
	address_line1 varchar(35) not null,
	postcode varchar(10) not null,
	CONSTRAINT excluded_voters_pk PRIMARY KEY (firstname, lastname, address_line1, postcode)
);

