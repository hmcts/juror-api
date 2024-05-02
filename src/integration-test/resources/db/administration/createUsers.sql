INSERT INTO juror_mod.users (user_type, username, email, name, active, created_by, updated_by)
VALUES ('BUREAU', 'test_bureau_inactive', 'test_bureau_inactive@email.gov.uk', 'Bureau Inactive', false, 'test_system', 'test_system'),
       ('BUREAU', 'test_bureau_standard', 'test_bureau_standard@email.gov.uk', 'Bureau Standard', true, 'test_system', 'test_system'),
       ('BUREAU', 'test_bureau_lead', 'test_bureau_lead@email.gov.uk', 'Bureau Team Lead', true, 'test_system', 'test_system');

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('test_bureau_lead', 'MANAGER');

INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES ('test_bureau_inactive', '400'),
       ('test_bureau_standard', '400'),
       ('test_bureau_lead', '400');


INSERT INTO juror_mod.users (owner, user_type, username, email, name, active, created_by, updated_by)
VALUES (null, 'COURT', 'test_court_no_courts', 'test_court_no_courts@email.gov.uk', 'Court No Courts', false, 'test_system', 'test_system'),
       ('415', 'COURT', 'test_court_inactive', 'test_court_inactive@email.gov.uk', 'Court Inactive', false, 'test_system', 'test_system'),
       ('415', 'COURT', 'test_court_standard', 'test_court_standard@email.gov.uk', 'Court Standard', true, 'test_system', 'test_system'),
       ('415', 'COURT', 'test_court_manager', 'test_court_manager@email.gov.uk', 'Court Manager', true, 'test_system', 'test_system'),
       ('415', 'COURT', 'test_court_sjo', 'test_court_sjo@email.gov.uk', 'Court SJO', true, 'test_system', 'test_system'),
       ('415', 'COURT', 'test_court_sjo_mangr', 'test_court_sjo_mangr@email.gov.uk', 'Court SJO & Manager', true, 'test_system', 'test_system'),
       ('408', 'COURT', 'test_court_primary', 'test_court_primary@email.gov.uk', 'Court Primary Only', true, 'test_system', 'test_system'),
       ('415', 'COURT', 'test_court_multi', 'test_court_multi@email.gov.uk', 'Court Multiple Linked', true, 'test_system', 'test_system')
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

INSERT INTO juror_mod.users (user_type, username, email, name, active, created_by, updated_by)
VALUES ('ADMINISTRATOR', 'test_admin_inactive', 'test_admin_inactive@email.gov.uk', 'Admin Inactive', false, 'test_system', 'test_system'),
       ('ADMINISTRATOR', 'test_admin_standard', 'test_admin_standard@email.gov.uk', 'Admin Standard', true, 'test_system', 'test_system');


INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES ('test_admin_inactive', '400'),
       ('test_admin_standard', '400');

INSERT INTO juror_mod.users (user_type, username, email, name, active, created_by, updated_by)
VALUES ('SYSTEM', 'test_system', 'test_system@email.gov.uk', 'System User', false, 'test_system', 'test_system');
