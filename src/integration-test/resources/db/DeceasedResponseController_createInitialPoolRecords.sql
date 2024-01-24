DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;

DELETE FROM JUROR_DIGITAL.PAPER_RESPONSE;

INSERT INTO juror_mod.pool (pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, total_no_required)
VALUES('415220502', '400', current_date + interval '6 weeks', 100, 'CRO', '415', 'N'::character varying, 99);

INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
     ('222222224','543','LNAME','FNAME', current_date - interval '30 years','549 STREET NAME','ANYTOWN','CH1 2AN','N', 'BUREAU_USER',0,0,''),
	 ('222222225','543','LNAME','FNAME', current_date - interval '30 years','549 STREET NAME','ANYTOWN','CH1 2AN','N', 'BUREAU_USER',0,0,''),
	 ('222222226','543','LNAME','FNAME', current_date - interval '30 years','549 STREET NAME','ANYTOWN','CH1 2AN','N', 'BUREAU_USER',0,0,'');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
     ('400','222222224','415220502', 'BUREAU_USER',2,true,'0109','415','N'),
	 ('400','222222225','415220502', 'BUREAU_USER',2,true,'0109','415','N'),
	 ('415','222222226','415220502', 'BUREAU_USER',2,true,'0109','415','N');
