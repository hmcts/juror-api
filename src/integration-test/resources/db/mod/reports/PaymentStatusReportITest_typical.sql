INSERT into juror_mod.payment_data
(loc_code, expense_total, extracted, expense_file_name, creation_date, loc_cost_centre, auth_code, juror_number,
 juror_name)
values ('415', 12.12, false, null, current_date - interval '10 days', 'tbc', 'tbc', '100000001', 'FName LName'),
       ('415', 13.12, false, null, current_date - interval '10 days', 'tbc', 'tbc', '100000002', 'FName LName'),
       ('415', 14.12, false, null, current_date - interval '10 days', 'tbc', 'tbc', '100000003', 'FName LName'),
       ('415', 15.12, false, null, current_date - interval '9 days', 'tbc', 'tbc', '100000004', 'FName LName'),
       ('415', 16.19, false, null, current_date - interval '9 days', 'tbc', 'tbc', '100000005', 'FName LName'),
       ('415', 17.18, false, null, current_date - interval '9 days', 'tbc', 'tbc', '100000006', 'FName LName'),
       ('415', 11.17, true, 'File Name 1', current_date - interval '10 days', 'tbc', 'tbc', '100000007', 'FName LName'),
       ('415', 12.16, true, 'File Name 1', current_date - interval '8 days', 'tbc', 'tbc', '100000009', 'FName LName'),
       ('415', 13.15, true, 'File Name 1', current_date - interval '9 days', 'tbc', 'tbc', '100000010', 'FName LName'),
       ('415', 14.14, true, 'File Name 2', current_date - interval '8 days', 'tbc', 'tbc', '100000008', 'FName LName'),
       ('415', 15.13, true, 'File Name 2', current_date - interval '10 days', 'tbc', 'tbc', '100000011', 'FName LName');