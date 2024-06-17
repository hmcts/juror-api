INSERT INTO juror_mod.users (created_by, updated_by,username,email, name, active, team_id,user_type)
VALUES ('jpowers','jpowers','jpowers','jpowers@email.gov.uk', 'Joanna Powers', true, 1,'BUREAU'),
       ('jpowers','jpowers','tsanchez','tsanchez@email.gov.uk', 'Todd Sanchez', true, 2,'BUREAU'),
       ('jpowers','jpowers','gbeck','gbeck@email.gov.uk', 'Grant Beck', true, 3,'BUREAU'),
       ('jpowers','jpowers','rprice','rprice@email.gov.uk', 'Roxanne Price', true, 1,'BUREAU'),
       ('jpowers','jpowers','pbrewer','pbrewer@email.gov.uk', 'Preston Brewer', true, 2,'BUREAU'),
       ('jpowers','jpowers','acopeland','acopeland@email.gov.uk', 'Amelia Copeland', true, 3,'BUREAU'),
       ('jpowers','jpowers','jphillips','jphillips@email.gov.uk', 'Joan Phillips', false, 1,'BUREAU'),
       ('jpowers','jpowers','srogers','srogers@email.gov.uk', 'Shawn Rogers', false, 2,'BUREAU'),
       ('jpowers','jpowers','pbrooks','pbrooks@email.gov.uk', 'Paul Brooks', false, 3,'BUREAU');

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