-- JS-621 Excluded voters

CREATE TABLE juror_mod.excluded_voters (
	firstname varchar(20) NOT NULL,
	lastname varchar(25) NOT NULL,
	address_line1 varchar(35) NOT NULL,
	postcode varchar(10) null,
	CONSTRAINT excluded_voters_pk PRIMARY KEY (firstname, lastname, address_line1, postcode)
);

