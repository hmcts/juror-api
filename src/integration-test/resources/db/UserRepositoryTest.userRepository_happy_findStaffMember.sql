INSERT INTO juror_mod.users (username, email, name, active, team_id,user_type)
VALUES ('jmcbob','jmcbob@email.gov.uk', 'Joe McBob', true,  1,'BUREAU'),
       ('smcbob','smcbob@email.gov.uk', 'Sarah McBob', true,  2,'BUREAU'),
       ('jbobson','jbobson@email.gov.uk', 'Joe Bobson', false,  3,'BUREAU');

insert into juror_mod.user_courts (username, loc_code)
values ('jmcbob', '400'),
       ('smcbob', '400'),
       ('jbobson', '400');

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('smcbob', 'MANAGER'),
       ('jbobson', 'MANAGER');