INSERT INTO juror_mod.users (created_by, updated_by,username,email, name, active, team_id)
VALUES ('jmcbob','jmcbob','jmcbob','jmcbob@email.gov.uk', 'Joe McBob', true, 1),
       ('jmcbob','jmcbob','smcbob','smcbob@email.gov.uk', 'Sarah McBob', true, 2),
       ('jmcbob','jmcbob','jbobson','jbobson@email.gov.uk', 'Joe Bobson', false, 3);

insert into juror_mod.user_courts (username, loc_code)
values ('jmcbob', '400'), ('smcbob', '400'), ('jbobson', '400');
