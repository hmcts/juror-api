DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;


INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '400', '2022-05-03', 5, 5, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,
optic_reference)
VALUES ('123456789', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '11111111');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status, next_date)
VALUES ('400', '123456789', '415220502', true, 2, current_date + 1);

INSERT INTO juror_mod.bulk_print_data (juror_no,creation_date,form_type,detail_rec,extracted_flag,digital_comms) VALUES
	 ('123456789',current_date,'5221','415250901          FNAME            LNAME            543 STREET NAME                        ANYTOWN                                      RRRRRTTWWJJ                        CH1 2AN                                                                                                            12345678912345678921 JULY 2025      TUESDAY 16 SEPTEMBER, 2025      9:00AM  415Y                   THE CROWN COURT AT CHESTER                                 THE CASTLE                         CHESTER                            CH1 2AN                                                                                                                                               01244 356726            JURY MANAGER                  JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                             SE1 0YG                                                                         0845 3555567            LNAME                 ',false,false),
	 ('123456789',current_date,'5224','21 JULY 2025      THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                             SE1 0YG                                                                         0845 3555567                      FNAME            LNAME            543 STREET NAME                        ANYTOWN                                      RRRRRTTWWJJ                        CH1 2AN                                                                                                            123456789JURY MANAGER                  LNAME                 ',false,false),
	 ('123456789',current_date,'5228','21 JULY 2025      THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                             SE1 0YG                                                                         0845 3555567                      FNAME            LNAME            543 STREET NAME                        ANYTOWN                                      RRRRRTTWWJJ                        CH1 2AN                                                                                                            123456789JURY MANAGER                  TUESDAY 16 SEPTEMBER, 2025      LNAME                 ',false,false),
	 ('123456789',current_date,'5224A','21 JULY 2025      415THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                             SE1 0YG                                                                         0845 3555567            TUESDAY 16 SEPTEMBER, 2025      9:00AM            FNAME            LNAME            543 STREET NAME                        ANYTOWN                                      RRRRRTTWWJJ                        CH1 2AN                                                                                                            123456789JURY MANAGER                  LNAME                 ',false,false);
