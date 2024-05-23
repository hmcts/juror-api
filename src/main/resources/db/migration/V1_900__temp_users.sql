INSERT INTO juror_mod.users (user_type, username, email, name, active)
VALUES
       ('ADMINISTRATOR', 'neil.perry', 'neil.perry@hmcts.net', 'Neil Perry', true),
       ('ADMINISTRATOR', 'ricardo.rocha', 'ricardo.freitasrocha@hmcts.net', 'Ricardo Rocha', true),
       ('ADMINISTRATOR', 'akhlaqur.rahman1', 'akhlaqur.rahman1@hmcts.net', 'Akhlaqur Rahman', true),
       ('ADMINISTRATOR', 'cade.faulkner', 'cade.faulkner@hmcts.net', 'Cade Faulkner', true);


INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES
       ('neil.perry', '400'),
       ('ricardo.rocha', '400'),
       ('akhlaqur.rahman1', '400'),
       ('cade.faulkner', '400');
