-- create a pool record owned by 415
INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request, last_update)
VALUES ('415220502', '415', current_date + 10, 5, 5, 'CRO', '415', 'N', current_date);

-- create existing pending juror records
INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
	 ('416220503','416',current_date + 10,5,'CRO','416','N',current_date,NULL,NULL,false,5,NULL);

INSERT INTO juror_mod.pending_juror (juror_number,pool_number,title,last_name,first_name,dob,address_line_1,
address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,m_phone,h_email,
contact_preference,responded,next_date,date_added,mileage,pool_seq,status,is_active,added_by,notes,date_created) VALUES
 ('041600001','416220503','Mr','Smitha','Johna','1990-01-01','1 High Street','Test','Test','Test','Test','TE1 1ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'Q',NULL,NULL,'Notes on record',current_date);

INSERT INTO juror_mod.pending_juror (juror_number,pool_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,m_phone,h_email,contact_preference,responded,next_date,date_added,mileage,pool_seq,status,is_active,added_by,notes,date_created) VALUES
 ('041600002','416220503','Mr','Smithb','Johnb','1990-01-01','2 High Street','Test','Test','Test','Test','TE1 2ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'Q',NULL,NULL,NULL,current_date);

INSERT INTO juror_mod.pending_juror (juror_number,pool_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,m_phone,h_email,contact_preference,responded,next_date,date_added,mileage,pool_seq,status,is_active,added_by,notes,date_created) VALUES
 ('041600003','416220503','Mr','Smithc','Johnc','1990-01-01','3 High Street','Test','Test','Test','Test','TE1 3ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'R',NULL,NULL,'Notes on record 3',current_date);

INSERT INTO juror_mod.pending_juror (juror_number,pool_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,m_phone,h_email,contact_preference,responded,next_date,date_added,mileage,pool_seq,status,is_active,added_by,notes,date_created) VALUES
 ('041600004','416220503','Mr','Smithd','Johnd','1990-01-01','4 High Street','Test','Test','Test','Test','TE1 4ST',
 '01234567890',NULL,NULL,NULL,'test@mail.com',NULL,true,current_date + 10,current_date,NULL,NULL,'A',NULL,NULL,'Notes on record 4',current_date);