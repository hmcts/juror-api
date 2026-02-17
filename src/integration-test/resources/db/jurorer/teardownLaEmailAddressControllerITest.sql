-- Teardown for LaEmailAddressController tests
-- Delete in order respecting foreign key constraints

DELETE FROM juror_er."user" WHERE la_code IN ('001', '002', '003', '004', '005');
DELETE FROM juror_er.local_authority WHERE la_code IN ('001', '002', '003', '004', '005');
