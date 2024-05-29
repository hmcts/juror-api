INSERT INTO juror_mod.users (username, email, name, active, team_id, user_type)
VALUES ('AACTIVE123', 'AACTIVE123@email.gov.uk', 'Andy Active', true, 1, 'BUREAU'),
       ('AACTIVE5', 'AACTIVE5@email.gov.uk', 'Alison Active', true, 2, 'BUREAU'),
       ('JINACTIVE1', 'JINACTIVE1@email.gov.uk', 'Joe Inactive', false, 3, 'BUREAU'),
       ('MCBBOBBIE', 'MCBBOBBIE@email.gov.uk', 'Bobbie McActive', true, 1, 'BUREAU'),
       ('ACTIVEX', 'ACTIVEX@email.gov.uk', 'Xavier Activez', true, 2, 'BUREAU');

insert into juror_mod.user_courts (username, loc_code)
values ('AACTIVE123', '400'),
       ('AACTIVE5', '400'),
       ('JINACTIVE1', '400'),
       ('MCBBOBBIE', '400'),
       ('ACTIVEX', '400');
