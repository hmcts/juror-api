-- juror
INSERT INTO JUROR.POOL (part_no, fname, lname, h_email, title, dob, address, address2, address3, address4, zip, h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, pool_no, on_call, completion_flag, read_only, contact_preference, reg_spc, ret_date, status, next_date) VALUES
(644892530, 'JANE', 'CASTILLO', 'jcastillo0@ed.gov', 'DR', TO_DATE('1984-07-24 16:04:09', 'YYYY-MM-DD HH24:MI:SS'), '4 Knutson Trail', 'Scotland', 'Aberdeen', 'United Kingdom', 'AB3 9RY', '44(703)209-6993', '44(109)549-5625', 'Y', '400', '448', '44(145)525-2390', 'N', 21112, 555, 'N', 'N', 'N', 0, 'N' , CURRENT_DATE, 1, (SELECT CURRENT_DATE+5));


INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
    ('555','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL);
INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,financial_loss,travel_time,bureau_transfer_date,claiming_subsistence_allowance,service_comp_comms_status,login_attempts,is_locked) VALUES
    ('644892530','21112','DR','CASTILLO','JANE','1984-07-24 00:00:00','4 Knutson Trail','Scotland','Aberdeen',NULL,NULL,'AB3 9RY','07032096993','01095495625',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,'2024-03-12 16:31:44',NULL,'01455252390','jcastillo0@ed.gov',0,0,'2024-03-12 16:31:44',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false);
INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
    ('644892530','555','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,(SELECT CURRENT_DATE+5),false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 16:31:44',false,NULL,NULL);

-- enable court
INSERT INTO JUROR_DIGITAL.COURT_WHITELIST (LOC_CODE) VALUES ('448');
