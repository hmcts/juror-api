INSERT INTO juror_mod.users (username,email, name, active, team_id, version)
VALUES ('jpowers','jpowers@email.gov.uk', 'Joanna Powers', true, 1, 0),
       ('tsanchez','tsanchez@email.gov.uk', 'Todd Sanchez', true, 2, 0),
       ('gbeck','gbeck@email.gov.uk', 'Grant Beck', true, 3, 0),
       ('rprice','rprice@email.gov.uk', 'Roxanne Price', true, 1, 0),
       ('pbrewer','pbrewer@email.gov.uk', 'Preston Brewer', true, 2, 0),
       ('acopeland','acopeland@email.gov.uk', 'Amelia Copeland', true, 3, 0),
       ('jphillips','jphillips@email.gov.uk', 'Joan Phillips', false, 1, 0),
       ('srogers','srogers@email.gov.uk', 'Shawn Rogers', false, 2, 0),
       ('pbrooks','pbrooks@email.gov.uk', 'Paul Brooks', false, 3, 0);

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