INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
    ('222','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL);
INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,financial_loss,travel_time,bureau_transfer_date,claiming_subsistence_allowance,service_comp_comms_status,login_attempts,is_locked) VALUES
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('122444503','76024','Mr','Wilson','Wade','1987-05-25 00:00:00','123 Fake Road','England','London','United Kingdom',NULL,'BC3M 2ND','44(406)759-6616','44(322)292-4490',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 12:58:37',NULL,'44(362)527-9947','captain_deadpool@gmail.com',0,0,'2024-03-12 12:58:37',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('152004504','76024','Mr','Reynolds','Frank','1987-05-25 00:00:00','123 Fake Street','England','London','United Kingdom',NULL,'BC3M 2NY','44(406)759-6616','44(322)292-4490',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 12:58:37',NULL,'44(362)527-9947','frankie_fast_hands@gmail.com',0,0,'2024-03-12 12:58:37',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('209092530','76024','Dr','Castillo','Jane','1984-07-24 00:00:00','4 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,'AB39RY','44(703)209-6993','44(109)549-5625',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 12:58:37',NULL,'44(362)527-9947','jcastillo0@ed.gov',0,0,'2024-03-12 12:58:37',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        ('352004504','76024','Rev','Rivera','Jose','1987-05-25 00:00:00','22177 Redwing Way','England','London','United Kingdom',NULL,'EC3M 2NY','44(406)759-6616','44(322)292-4490',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 12:58:37',NULL,'44(362)527-9947','jriverac@myspace.com',0,0,'2024-03-12 12:58:37',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false);
INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
                                                                                                                                                                                                                                                                                                                                                  ('122444503','222','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2017-11-23',false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 12:58:37',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('152004504','222','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2017-11-23',false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 12:58:37',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('209092530','222','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2017-11-13',false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 12:58:37',false,NULL,NULL),
                                                                                                                                                                                                                                                                                                                                                  ('352004504','222','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2017-11-13',false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 12:58:37',false,NULL,NULL);
INSERT INTO juror_mod.juror_response (juror_number,date_received,title,first_name,last_name,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,processing_status,date_of_birth,phone_number,alt_phone_number,email,residency,residency_detail,mental_health_act,mental_health_capacity,mental_health_act_details,bail,bail_details,convictions,convictions_details,deferral,deferral_reason,deferral_date,reasonable_adjustments_arrangements,excusal,excusal_reason,processing_complete,signed,"version",thirdparty_fname,thirdparty_lname,relationship,main_phone,other_phone,email_address,thirdparty_reason,thirdparty_other_reason,juror_phone_details,juror_email_details,staff_login,staff_assignment_date,urgent,completed_at,welsh,reply_type) VALUES
    ('352004504','2017-10-23 00:00:00','Rev','Jose','Rivera','22177 Redwing Way','England','London','United Kingdom',NULL,'EC3M 2NY','TODO','1995-08-08','11111111111','00000000000','email@email.com',true,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,NULL,NULL,true,NULL,false,'Digital');

-- staff
INSERT INTO juror_mod.users (created_by, updated_by,username,email, name, active,team_id)
VALUES ('BUREAUGUY1','BUREAUGUY1','BUREAUGUY1','BUREAUGUY1@email.gov.uk','Bureau Guy',true,1),
       ('BUREAUGUY1','BUREAUGUY1','BUREAULADY9','BUREAULADY9@email.gov.uk','Bureau Lady',true,1);

insert into juror_mod.user_courts (username, loc_code)
values ('BUREAUGUY1', '448'),
       ('BUREAULADY9', '448');

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('BUREAUGUY1', 'MANAGER'),
       ('BUREAULADY9', 'MANAGER');