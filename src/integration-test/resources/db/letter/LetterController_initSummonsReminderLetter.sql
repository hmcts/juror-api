-- Test Data
-- 555555570 - no record exits in Bulk Print Data table (letter not printed)

INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,
last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
('415220401','400',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,NULL,false,2,NULL),
('415220504','400',current_date + 14,4,'CRO','415','N','2022-03-02 09:22:09',NULL,NULL,false,4,NULL),
('415220402','400',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,NULL,false,2,NULL),
('415220403','400',current_date + 20,2,'CRO','415','N',current_date + 10,NULL,NULL,false,2,NULL),
('415220404','400',current_date + 25,2,'CRO','415','N',current_date + 10,NULL,NULL,false,2,NULL);

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,
                             address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,
                             responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,
                             no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,
                             completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,travel_time,mileage,financial_loss) VALUES
('555555561','540',NULL,'Juror561Surname','Juror561','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-21 01:00:00.000','C',NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555562','540',NULL,'Juror562Surname','Juror562','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-22 01:00:00.000','T',NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555563','540',NULL,'Juror563Surname','Juror563','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-25 00:00:00.000','C',NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555564','540',NULL,'Juror564Surname','Juror564','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-21 02:00:00.000','C',NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555565','540',NULL,'Juror565Surname','Juror565','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-22 02:00:00.000','A',NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555566','540',NULL,'Juror566Surname','Juror566','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555567','540',NULL,'Juror567Surname','Juror567','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-28 00:00:00.000','I','Y',NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555568','540',NULL,'Juror568Surname','Juror568','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-28 00:00:00.000','I','Y',NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555569','540',NULL,'Juror569Surname','Juror569','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-22 01:00:00.000','T','Y',NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555570','540',NULL,'Juror570Surname','Juror570','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-21 01:00:00.000','C',NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL),
('555555571','540',NULL,'Juror571Surname','Juror571','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,false,'2024-01-21 01:00:00.000','C',NULL,NULL,NULL,'BUREAU_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678','Mr','Test','Person',NULL,NULL,NULL);

INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
('555555561','415220401','400','BUREAU_USER',false,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,
 NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555561','415220504','400','BUREAU_USER',true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',
 false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555562','415220401','400','BUREAU_USER',true,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
 false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555563','415220401','400','BUREAU_USER',true,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
 false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555564','415220401','400','BUREAU_USER',true,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
 false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555565','415220401','400','BUREAU_USER',true,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
 false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555566','415220402','400','BUREAU_USER',true,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555567','415220401','400','BUREAU_USER',true,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
 false,NULL,NULL,'C',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555567','415220403','400','BUREAU_USER',true,1,NULL,current_date + 20,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
 false,NULL,NULL,'C',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555567','415220404','400','BUREAU_USER',true,2,NULL,current_date + 25,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555568','415220404','400','BUREAU_USER',true,1,NULL,current_date + 25,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
 false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555569','415220401','400','BUREAU_USER',true,2,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555570','415220401','400','BUREAU_USER',true,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555571','415220401','400','BUREAU_USER',true,1,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL);


INSERT INTO juror_mod.bulk_print_data (juror_no,creation_date,form_type,detail_rec,extracted_flag,digital_comms) VALUES
('555555561','2024-01-31 12:07','5228','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 ' ||
                                       'JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  ' ||
                                       'FREEPOST LON 19669                 POCOCK STREET                      LONDON ' ||
                                       '                                                                             ' ||
                                       '                     SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror61             Juror61Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',true,NULL),
('555555561','2024-01-31 12:06','5228C','18 JANUARY 2024   415THE CROWN COURT AT CHESTER JURY CENTRAL SUMMONING BUREAU     THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror61             Juror61Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',true,NULL),
('555555562','2024-02-01 12:07','5228','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK  STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror62             Juror62Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555563','2024-01-25 12:07','5228','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror63             Juror63Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555564','2024-01-20 12:07','5228','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                  JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror64             Juror64Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555565','2024-02-01 12:07','5228','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror65             Juror65Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555566','2024-02-01 12:07','5228C','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror66             Juror66Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555567','2024-01-27 12:07','5228','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror67             Juror67Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555567','2024-01-27 12:07','5228','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror67             Juror67Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555568','2024-01-27 12:07','5224AC','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror67             Juror67Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555569','2024-02-01 12:07','5226','18 JANUARY 2024   THE CROWN COURT AT CHESTER                                 JURY CENTRAL SUMMONING BUREAU           THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK  STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror62             Juror62Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',false,NULL),
('555555571','2024-01-31 12:06','5228C','18 JANUARY 2024   415THE CROWN COURT AT CHESTER JURY CENTRAL SUMMONING BUREAU     THE COURT SERVICE                  FREEPOST LON 19669                 POCOCK STREET                      LONDON                                                                                                   SE1 0YG   0845 3555567            MONDAY 12 JUNE, 2023            09:00             Juror61             Juror61Surname      540 STREET NAME                    ANYTOWN                                                                                                                                                                        CH1 2AN   555555561JURY MANAGER                  ',true,NULL);