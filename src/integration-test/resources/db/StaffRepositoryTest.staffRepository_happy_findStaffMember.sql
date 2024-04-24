INSERT INTO juror_mod.users (username,email, name, active, team_id, version)
VALUES ('jmcbob','jmcbob@email.gov.uk', 'Joe McBob', true, 1, 0),
       ('smcbob','smcbob@email.gov.uk', 'Sarah McBob', true, 2, 0),
       ('jbobson','jbobson@email.gov.uk', 'Joe Bobson', false, 3, 0);

insert into juror_mod.user_courts (username, loc_code)
values ('jmcbob', '400'), ('smcbob', '400'), ('jbobson', '400');
