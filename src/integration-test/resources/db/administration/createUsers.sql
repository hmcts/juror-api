INSERT INTO juror_mod.users (user_type, username, email, name, active, version)
VALUES ('BUREAU', 'test_bureau_inactive', 'test_bureau_inactive@email.gov.uk', 'Bureau Inactive', false, 1),
       ('BUREAU', 'test_bureau_standard', 'test_bureau_standard@email.gov.uk', 'Bureau Standard', true, 1),
       ('BUREAU', 'test_bureau_lead', 'test_bureau_lead@email.gov.uk', 'Bureau Team Lead', true, 1);

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('test_bureau_lead', 'MANAGER');

INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES ('test_bureau_inactive', '400'),
       ('test_bureau_standard', '400'),
       ('test_bureau_lead', '400');


INSERT INTO juror_mod.users (user_type, username, email, name, active, version)
VALUES ('COURT', 'test_court_no_courts', 'test_court_no_courts@email.gov.uk', 'Court No Courts', false, 1),
       ('COURT', 'test_court_inactive', 'test_court_inactive@email.gov.uk', 'Court Inactive', false, 1),
       ('COURT', 'test_court_standard', 'test_court_standard@email.gov.uk', 'Court Standard', true, 1),
       ('COURT', 'test_court_manager', 'test_court_manager@email.gov.uk', 'Court Manager', true, 1),
       ('COURT', 'test_court_sjo', 'test_court_sjo@email.gov.uk', 'Court SJO', true, 1),
       ('COURT', 'test_court_sjo_mangr', 'test_court_sjo_mangr@email.gov.uk', 'Court SJO & Manager', true, 1),
       ('COURT', 'test_court_primary', 'test_court_primary@email.gov.uk', 'Court Primary Only', true, 1),
       ('COURT', 'test_court_multi', 'test_court_multi@email.gov.uk', 'Court Multiple Linked', true, 1)
;

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('test_court_manager', 'MANAGER'),
       ('test_court_sjo', 'SENIOR_JUROR_OFFICER'),
       ('test_court_sjo_mangr', 'MANAGER'),
       ('test_court_sjo_mangr', 'SENIOR_JUROR_OFFICER');

INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES ('test_court_inactive', '415'),
       ('test_court_standard', '415'),
       ('test_court_manager', '415'),
       ('test_court_sjo', '415'),
       ('test_court_sjo_mangr', '415'),
       ('test_court_primary', '408'),
       ('test_court_multi', '415'),
       ('test_court_multi', '421');

INSERT INTO juror_mod.users (user_type, username, email, name, active, version)
VALUES ('ADMINISTRATOR', 'test_admin_inactive', 'test_admin_inactive@email.gov.uk', 'Admin Inactive', false, 1),
       ('ADMINISTRATOR', 'test_admin_standard', 'test_admin_standard@email.gov.uk', 'Admin Standard', true, 1);


INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES ('test_admin_inactive', '400'),
       ('test_admin_standard', '400');

INSERT INTO juror_mod.users (user_type, username, email, name, active, version)
VALUES ('SYSTEM', 'test_system', 'test_system@email.gov.uk', 'System User', false, 1);
