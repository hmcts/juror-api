--Staff
INSERT INTO JUROR_DIGITAL.STAFF (LOGIN, NAME, RANK, ACTIVE, TEAM_ID, VERSION) VALUES ('carneson', 'Chad Arneson', 0, 1, 1, 1);
INSERT INTO JUROR_DIGITAL.STAFF (LOGIN, NAME, RANK, ACTIVE, TEAM_ID, VERSION) VALUES ('sgomez', 'Susie Gomez', 0, 1, 2, 1);
INSERT INTO JUROR_DIGITAL.STAFF (LOGIN, NAME, RANK, ACTIVE, TEAM_ID, VERSION) VALUES ('mruby', 'Martin Ruby', 0, 1, 3, 1);
INSERT INTO JUROR_DIGITAL.STAFF (LOGIN, NAME, RANK, ACTIVE, TEAM_ID, VERSION) VALUES ('cbeasley', 'Charles Beasley', 1, 1, 1, 1);
INSERT INTO JUROR_DIGITAL.STAFF (LOGIN, NAME, RANK, ACTIVE, TEAM_ID, VERSION) VALUES ('tgarrett', 'Timothy Garrett', 1, 1, 2, 1);
INSERT INTO JUROR_DIGITAL.STAFF (LOGIN, NAME, RANK, ACTIVE, TEAM_ID, VERSION) VALUES ('ksalazar', 'Kris Salazar', 1, 1, 3, 1);

INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
                                                                                                                                                                                            ('555','400','2022-05-03',5,'CRO','446','N',NULL,NULL,NULL,false,5,NULL),
                                                                                                                                                                                            ('101','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL);

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,financial_loss,travel_time,bureau_transfer_date,claiming_subsistence_allowance,service_comp_comms_status,login_attempts,is_locked) VALUES
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111000','0','Rev','Milne','Eleanor','1976-07-18 00:00:00','1 Test Street','Scotland','Strathaven','United Kingdom',NULL,'ML106AD','44135101-1110','44135201-1110',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1110','Milne0@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111001','1','Prof','Ambers','Ada','1985-05-15 00:00:00','2 Test Street','England','Aldershot','United Kingdom',NULL,'GU124AL','44125101-1111','44125201-1111',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1111','Ambers1@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111002','2','Rev','Jones','Frank','1989-05-09 00:00:00','3 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','44154101-1112','44154201-1112',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1112','Jones2@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111003','3','Prof','Linares','Barbara','1987-03-02 00:00:00','4 Test Street','Scotland','Strathaven','United Kingdom',NULL,'ML106AD','44135101-1113','44135201-1113',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1113','Linares3@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111004','4','Mr','Kennedy','Wayne','1989-03-02 00:00:00','5 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','44154101-1114','44154201-1114',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1114','Kennedy4@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111005','5','Mr','Brickett','Clint','1960-12-15 00:00:00','6 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','44154101-1115','44154201-1115',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1115','Brickett5@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111006','6','Prof','Keifer','William','1980-04-03 00:00:00','7 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','44154101-1116','44154201-1116',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1116','Keifer6@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111007','7','Dr','Sweeney','George','1975-05-18 00:00:00','8 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','44154101-1117','44154201-1117',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1117','Sweeney7@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111008','8','Prof','Wiles','Willie','1984-06-19 00:00:00','9 Test Street','Scotland','Strathaven','United Kingdom',NULL,'ML106AD','44135101-1118','44135201-1118',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1118','Wiles8@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('111111009','9','Rev','Turner','Dorsey','1981-10-10 00:00:00','10 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','44154101-1119','44154201-1119',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:12',NULL,'44776-301-1119','Turner9@email.com',0,0,'2024-03-13 01:06:12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('123251234','21112','Mr','Hoola','Gypsey','1984-07-24 00:00:00','27 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,'AB21 3RY','44(703)209-6991','44(109)549-5621',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:16',NULL,'44(145)525-2391','jhoola@ed.gov',0,0,'2024-03-13 01:06:16',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('209092530','21112','Dr','Castillo','Jane','1984-07-24 00:00:00','4 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,'AB21 3RY','44(703)209-6993','44(109)549-5625',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-13 01:06:16',NULL,'44(145)525-2390','jcastillo0@ed.gov',0,0,'2024-03-13 01:06:16',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false);
INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
                                                                                                                                                                                                                                                                                                                                                  ('111111000','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111001','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111002','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111003','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111004','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111005','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111006','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111007','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111008','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('111111009','101','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:12',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('123251234','555','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-05-12',false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:16',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('209092530','555','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-05-12',false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-13 01:06:16',false,NULL,NULL);
INSERT INTO juror_mod.juror_response (juror_number,date_received,title,first_name,last_name,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,processing_status,date_of_birth,phone_number,alt_phone_number,email,residency,residency_detail,mental_health_act,mental_health_capacity,mental_health_act_details,bail,bail_details,convictions,convictions_details,deferral,deferral_reason,deferral_date,reasonable_adjustments_arrangements,excusal,excusal_reason,processing_complete,signed,"version",thirdparty_fname,thirdparty_lname,relationship,main_phone,other_phone,email_address,thirdparty_reason,thirdparty_other_reason,juror_phone_details,juror_email_details,staff_login,staff_assignment_date,urgent,super_urgent,completed_at,welsh,reply_type) VALUES
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111000','2024-03-13 00:00:00','Rev','Eleanor','Milne','1 Test Street','Scotland','Strathaven','United Kingdom',NULL,'ML106AD','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,true,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111001','2024-03-12 23:59:00','Prof','Ada','Ambers','2 Test Street','England','Aldershot','United Kingdom',NULL,'GU124AL','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,true,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111002','2024-03-12 23:58:00','Rev','Frank','Jones','3 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,true,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111003','2024-03-12 23:57:00','Prof','Barbara','Linares','4 Test Street','Scotland','Strathaven','United Kingdom',NULL,'ML106AD','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111004','2024-03-12 23:56:00','Mr','Wayne','Kennedy','5 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111005','2024-03-12 23:55:00','Mr','Clint','Brickett','6 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111006','2024-03-12 23:54:00','Prof','William','Keifer','7 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,false,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111007','2024-03-12 23:53:00','Dr','George','Sweeney','8 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','AWAITING_COURT_REPLY',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,false,true,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111008','2024-03-12 23:52:00','Prof','Willie','Wiles','9 Test Street','Scotland','Strathaven','United Kingdom',NULL,'ML106AD','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,'carneson','2024-03-13 00:00:00',true,true,NULL,false,'Digital'),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          ('111111009','2024-03-12 23:51:00','Rev','Dorsey','Turner','10 Test Street','Wales','Old Radnor','United Kingdom',NULL,'LD82RG','TODO',NULL,NULL,NULL,NULL,false,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,555,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,'carneson','2024-03-13 00:00:00',false,false,NULL,false,'Digital');
