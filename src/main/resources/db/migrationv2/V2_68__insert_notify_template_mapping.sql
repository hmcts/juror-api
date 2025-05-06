-- LETTERS
--CONFIRMATION OF Service

INSERT INTO juror_mod.notify_template_mapping (template_id,template_name,notify_name,form_type,notification_type,"version") VALUES

   ('ea38af04-0631-4c7c-bfc8-0c491b7e98a2','CONFRIM_JUROR_ENG_TAUTON','CONFIRMATION OF SERVICE TAUNTON',null,1,0),

	 ('bdcb84c2-49c1-435f-9821-262446c98a1c','CONFRIM_JUROR_ENG_HARROW','CONFIRMATION OF SERVICE HARROW',null,1,0);


-- DEFERRAL DENIED

   INSERT INTO juror_mod.notify_template_mapping (template_id,template_name,notify_name,form_type,notification_type,"version") VALUES

   ('63d636d3-4ca2-452d-baa2-a940e4dcc48a','TEMP_DEF_DENIED_ENG','DEFERRAL DENIED (ENGLISH_LIVE)',null,1,0);





-- DEFERRAL GRANTED


INSERT INTO juror_mod.notify_template_mapping (template_id,template_name,notify_name,form_type,notification_type,"version") VALUES

 ('f5072da7-b250-4f02-b206-f176b1a0b80b','TEMP_DEF_GRANTED_ENG','DEFERRAL GRANTED (ENGLISH_LIVE)',null,1,0);


-- EXCUSAL DENIED

INSERT INTO juror_mod.notify_template_mapping (template_id,template_name,notify_name,form_type,notification_type,"version") VALUES

('f5669ddd-4bb3-4092-b60b-45f410de74a7','TEMP_EXC_DENIED_ENG','EXCUSAL DENIED (ENGLISH_LIVE)',null,1,0);

-- POSTPONEMENT

INSERT INTO juror_mod.notify_template_mapping (template_id,template_name,notify_name,form_type,notification_type,"version") VALUES

 ('6504a964-0081-4b42-95da-9cccd26c1202','TEMP_POSTPONE_JUROR_ENG','Postponement (English_Live)',null,1,0);



-- WEEKLEY COMM

--4 WEEK COMMS

INSERT INTO juror_mod.notify_template_mapping (template_id,template_name,notify_name,form_type,notification_type,"version") VALUES

('b17e55bb-d170-49c1-a22b-cd21c55d8039','TEMP_1ST_COMMS_ENG','-4 weeks until service start (English_Live) v2',null,5,0);



--SENT TO COURT


INSERT INTO juror_mod.notify_template_mapping (template_id,template_name,notify_name,form_type,notification_type,"version") VALUES


('b6915247-ff69-4740-a4b7-22505be25ef4','TEMP_SENT_TO_COURT_EMAIL_ENG','Sent to court (English_Live) v2',null,6,0);
