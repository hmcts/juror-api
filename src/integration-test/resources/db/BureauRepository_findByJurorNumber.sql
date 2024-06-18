-- STAFF
INSERT INTO juror_mod.users (created_by, updated_by,username,email, name, active)
VALUES ('STAFF1','STAFF1','STAFF1','STAFF1@email.gov.uk','Staffy McStaff1',true);
insert into juror_mod.user_courts (username, loc_code)
values ('STAFF1', '400');

INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
                                                                                                                                                                                            ('555','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL),
                                                                                                                                                                                            ('444','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL),
                                                                                                                                                                                            ('333','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL),
                                                                                                                                                                                            ('222','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL);


INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,financial_loss,travel_time,bureau_transfer_date,claiming_subsistence_allowance,service_comp_comms_status,login_attempts,is_locked) VALUES
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('209092530','21112','Dr','CASTILLO','Jane','1984-07-24 00:00:00','4 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,'AB39RY','44(703)209-6993','44(109)549-5625',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(145)525-2390','jcastillo0@ed.gov',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('275852838','27201','Mrs','WILSON','Amy','1969-12-06 00:00:00','63720 Warbler Circle','England','London','United Kingdom',NULL,'WC1BNH','44(765)464-4583','44(463)659-9522',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(555)703-0191','awilson5@msn.com',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('472008411','93382','Honorable','ROBERTSON','Denise','1962-03-07 00:00:00','57 Westerfield Parkway','England','Charlton','United Kingdom',NULL,'OX12RV','44(202)757-0949','44(665)723-2987',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(170)995-1393','drobertson3@hibu.com',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('487498307','93315','Mr','PIERCE','Kimberly','1975-02-11 00:00:00','9371 Superior Avenue','England','Newbiggin','United Kingdom',NULL,'NE46TG','44(513)905-6359','44(965)467-9567',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(373)639-7785','kpierce2@plala.or.jp',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('586856851','19917','Rev','BROWN','Cynthia','1987-05-08 00:00:00','7 Lunder Park','England','Weston','United Kingdom',NULL,'GU32SW','44(211)698-2662','44(551)736-7419',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(520)949-8284','cbrown1@yellowpages.com',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('811923115','92835','Ms','MURPHY','Ronald','1978-03-08 00:00:00','13 Jenifer Place','England','Bradford','United Kingdom',NULL,'BD7EN','44(520)655-4108','44(506)190-9791',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(504)912-7033','rmurphy6@lycos.com',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('827761086','41755','Dr','WASHINGTON','Laura','1994-07-07 00:00:00','8497 Michigan Road','England','Sutton','United Kingdom',NULL,'RH5XV','44(893)993-2064','44(160)893-3475',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(560)504-4476','lwashington7@printfriendly.com',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('845814425','18693','Mrs','LARSON','James','1954-01-10 00:00:00','9 Myrtle Point','England','Whitchurch','United Kingdom',NULL,'BS14QP','44(750)536-5360','44(725)985-1004',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 21:54:14',NULL,'44(591)514-5758','jlarson4@comsenz.com',0,0,'2024-03-12 21:54:14',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false);
INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
                                                                                                                                                                                                                                                                                                                                                  ('209092530','555','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('275852838','555','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('472008411','444','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('487498307','333','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('586856851','222','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('811923115','222','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('827761086','444','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('845814425','222','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 21:54:14',false,NULL,NULL);
INSERT INTO juror_mod.juror_response (juror_number,date_received,title,first_name,last_name,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,processing_status,date_of_birth,phone_number,alt_phone_number,email,residency,residency_detail,mental_health_act,mental_health_capacity,mental_health_act_details,bail,bail_details,convictions,convictions_details,deferral,deferral_reason,deferral_date,reasonable_adjustments_arrangements,excusal,excusal_reason,processing_complete,signed,"version",thirdparty_fname,thirdparty_lname,relationship,main_phone,other_phone,email_address,thirdparty_reason,thirdparty_other_reason,juror_phone_details,juror_email_details,staff_login,staff_assignment_date,urgent,completed_at,welsh,reply_type) VALUES
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('209092530','2024-03-12 00:00:00',NULL,'Janey',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('275852838','2024-03-07 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'AWAITING_COURT_REPLY',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('472008411','2024-03-02 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'AWAITING_CONTACT',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('487498307','2024-03-03 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('586856851','2024-03-09 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('811923115','2024-03-08 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'AWAITING_CONTACT',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('827761086','2024-03-10 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'CLOSED',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('845814425','2024-03-01 00:00:00',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'AWAITING_TRANSLATION',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,NULL,false,'Digital');
INSERT INTO juror_mod.contact_log (juror_number,user_id,notes,last_update,start_call,end_call,enquiry_type,repeat_enquiry) VALUES
                                                                                                                               ('209092530','COURTDUDE1','Some court notes','2024-03-12 21:54:14','2024-03-10 00:00:00','2024-03-11 00:00:00','IN',false),
                                                                                                                               ('209092530','STAFF1','Some bureau notes','2024-03-12 21:54:14','2024-03-08 00:00:00','2024-03-09 00:00:00','IN',false);
INSERT INTO JUROR_DIGITAL.CHANGE_LOG (ID, JUROR_NUMBER, TIMESTAMP, STAFF, TYPE, NOTES, VERSION) VALUES (1, '209092530', (SELECT current_date), 'STAFF1', 'JUROR_DETAILS', 'notes1', 0);
INSERT INTO JUROR_DIGITAL.CHANGE_LOG_ITEM (ID, CHANGE_LOG, OLD_KEY, OLD_VALUE, NEW_KEY, NEW_VALUE, VERSION) VALUES (1, 1, 'firstName', null, 'firstName', 'Janey', 0);
INSERT INTO JUROR_DIGITAL.CHANGE_LOG_ITEM (ID, CHANGE_LOG, OLD_KEY, OLD_VALUE, NEW_KEY, NEW_VALUE, VERSION) VALUES (2, 1, 'lastName', null, 'lastName', 'Castilio', 0);
--redundant phone log
INSERT INTO JUROR.PHONE_LOG(OWNER, PART_NO, USER_ID, START_CALL, END_CALL, PHONE_CODE, NOTES, LAST_UPDATE) VALUES ('400', '209092530', 'STAFF1', (SELECT current_date) - 4, (SELECT current_date) - 3, 'IN', 'Some bureau notes', (SELECT current_date) - 3);
-- redundant phone log (with court owner)
INSERT INTO JUROR.PHONE_LOG(OWNER, PART_NO, USER_ID, START_CALL, END_CALL, PHONE_CODE, NOTES, LAST_UPDATE) VALUES ('415', '209092530', 'COURTDUDE1', (SELECT current_date) - 2, (SELECT current_date) - 1, 'IN', 'Some court notes', (SELECT current_date) - 1);
