INSERT INTO juror_mod.users (username,email, name, active, team_id, version,user_type)
VALUES ('jpowers','jpowers@email.gov.uk', 'Joanna Powers', true, 1, 0,'BUREAU'),
       ('tsanchez','tsanchez@email.gov.uk', 'Todd Sanchez', true, 2, 0,'BUREAU'),
       ('gbeck','gbeck@email.gov.uk', 'Grant Beck', true, 3, 0,'BUREAU'),
       ('rprice','rprice@email.gov.uk', 'Roxanne Price', true, 1, 0,'BUREAU'),
       ('pbrewer','pbrewer@email.gov.uk', 'Preston Brewer', true, 2, 0,'BUREAU'),
       ('acopeland','acopeland@email.gov.uk', 'Amelia Copeland', true, 3, 0,'BUREAU'),
       ('jphillips','jphillips@email.gov.uk', 'Joan Phillips', false, 1, 0,'BUREAU'),
       ('srogers','srogers@email.gov.uk', 'Shawn Rogers', false, 2, 0,'BUREAU'),
       ('pbrooks','pbrooks@email.gov.uk', 'Paul Brooks', false, 3, 0,'BUREAU');

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('rprice', 'MANAGER'),
       ('pbrewer', 'MANAGER'),
       ('acopeland', 'MANAGER');

insert into juror_mod.user_courts (username, loc_code)
values ('jpowers', '400'),
       ('tsanchez', '400'),
       ('gbeck', '400'),
       ('rprice', '415'),
       ('pbrewer', '400'),
       ('acopeland', '400'),
       ('jphillips', '400'),
       ('srogers', '400'),
       ('pbrooks', '400');