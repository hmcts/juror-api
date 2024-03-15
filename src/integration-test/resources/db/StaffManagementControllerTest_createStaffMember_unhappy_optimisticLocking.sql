INSERT INTO juror_mod.users (owner, username, email, name, active, team_id, version)
VALUES ('400', 'EXISTING1', 'EXISTING1@email.gov.uk', 'Andy Active', true, 1, 0),
       ('400', 'EXISTING2', 'EXISTING2@email.gov.uk', 'Alison Active', false, 2, 2);

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('EXISTING1', 'TEAM_LEADER');