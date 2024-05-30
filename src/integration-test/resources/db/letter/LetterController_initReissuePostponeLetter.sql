INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST) VALUES
 ('400', '415220401', CURRENT_DATE, 4, 2, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode,
responded) VALUES
('555555551', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', TIMESTAMP '1990-07-25 00:00:00.000000', '540 STREET NAME',
 'ANYTOWN', 'CH1 2AN', true);


INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, def_date, deferral_code) VALUES
('400', '555555551', '415220401',  7, true, '2024-01-01', 'P');

INSERT INTO juror_mod.bulk_print_data (juror_no,creation_date,form_type,detail_rec,extracted_flag,digital_comms) VALUES
('555555551',current_date - 1,'5229','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             FNAMEFIVEFOURZERO   LNAMEFIVEFOURZERO   540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',true,false);