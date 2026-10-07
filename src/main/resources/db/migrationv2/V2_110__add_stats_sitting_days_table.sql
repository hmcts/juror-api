
CREATE TABLE juror_mod.stats_sitting_days (
  service_year varchar(4) NOT NULL,
  service_month varchar(7) NOT NULL,
  court_code varchar(3) NOT NULL,
  sitting_days_category varchar(10) NOT NULL,
  court_name varchar(40) NULL,
  number_of_sitting_days int4 NULL,
  number_of_jurors int4 null,
  CONSTRAINT stats_sitting_days_pkey PRIMARY KEY (service_year, service_month, court_code, sitting_days_category),
  CONSTRAINT stats_sitting_days_court_code_fk FOREIGN KEY (court_code) REFERENCES juror_mod.court_location(loc_code)
)
