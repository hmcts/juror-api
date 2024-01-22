-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415','N', '2023-01-05 09:30:00.000');

-- create juror record
insert into juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode,
responded, smart_card, mileage, travel_time, amount_spent, financial_loss) values
('641500020', 'Lnametwozero', 'Fnametwozero', current_date - interval '20 years', '520 Street Name', 'Any town', 'CH1 2AN',
 'Y', '12345678', '5', 4.5, '20.0', '0.0');

-- create juror_pool associative record
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active) values
('415', '641500020', '415230101', 2, true);