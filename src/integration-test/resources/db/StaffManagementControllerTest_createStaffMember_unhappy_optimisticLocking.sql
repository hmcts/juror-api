INSERT INTO juror_mod.users (created_by, updated_by,username, email, name, active, team_id,user_type)
VALUES ('EXISTING1','EXISTING1','EXISTING1', 'EXISTING1@email.gov.uk', 'Andy Active', true, 1,'BUREAU'),
       ('EXISTING1','EXISTING1','EXISTING2', 'EXISTING2@email.gov.uk', 'Alison Active', false, 2,'BUREAU');

insert into juror_mod.user_courts (username, loc_code)
values ('EXISTING1', '400'),
       ('EXISTING2', '400');

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('EXISTING1', 'MANAGER');