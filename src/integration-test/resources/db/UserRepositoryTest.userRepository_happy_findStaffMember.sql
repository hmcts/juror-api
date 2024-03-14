INSERT INTO juror_mod.users (owner, username, email, name, active, team_id, version)
VALUES ('400', 'jmcbob','jmcbob@email.gov.uk', 'Joe McBob', true,  1, 0),
       ('400', 'smcbob','smcbob@email.gov.uk', 'Sarah McBob', true,  2, 0),
       ('400', 'jbobson','jbobson@email.gov.uk', 'Joe Bobson', false,  3, 0);

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('smcbob', 'TEAM_LEADER'),
       ('jbobson', 'TEAM_LEADER');