INSERT INTO juror_mod.pool (pool_no, "owner", return_date, total_no_required)
VALUES ('415170403', '400', current_date, 4);

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, address_line_1, responded)
VALUES ('333333333', 'LASTNAME', 'FIRSTNAME', 'ADDRESS', false),
       ('333333334', 'LASTNAME', 'FIRSTNAME', 'ADDRESS', false),
       ('444444444', 'LASTNAME', 'FIRSTNAME', 'ADDRESS', false),
       ('444444445', 'LASTNAME', 'FIRSTNAME', 'ADDRESS', false);

INSERT INTO juror_mod.juror_pool (juror_number, pool_number, "owner", is_active, status)
VALUES ('333333333', '415170403', '400', true, 1),
       ('333333334', '415170403', '400', true, 1),
       ('444444444', '415170403', '400', true, 2),
       ('444444445', '415170403', '400', true, 5);

INSERT INTO juror_mod.bulk_print_data (juror_no, creation_date, form_type, detail_rec, extracted_flag, digital_comms,
                                       communication_channel)
VALUES ('333333333', current_date, '6228', 'N/A', false, false, 'LETTER'),
       ('333333334', current_date, '6228C', 'N/A', false, false, 'LETTER'),
       ('444444444', current_date, '6228', 'N/A', false, false, 'LETTER'),
       ('444444445', current_date, '6228C', 'N/A', false, false, 'LETTER');
