
-- LETTERS
-- CONFIRMATION
INSERT INTO juror_mod.notify_template_field (id,template_id,template_field,position_from,position_to,mapper_object) VALUES

(270,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','email address',NULL,NULL,'JUROR_EMAIL'),
(271,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','COURTADDRESS',NULL,NULL,'TEMPORARY_COURT_ADDRESS'),
(272,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','COURTNAME',NULL,NULL,'COURT_LOC_COURT_NAME'),
(273,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','SERVICESTARTDATE',365,396,'BULK_PRINT_DATA'),
(274,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','FIRSTNAME',415,434,'BULK_PRINT_DATA'),
(275,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','LASTNAME',435,454,'BULK_PRINT_DATA'),
(276,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','SERVICESTARTTIME',397,404,'BULK_PRINT_DATA'),
(277,'ea38af04-0631-4c7c-bfc8-0c491b7e98a2','JURORNUMBER',675,683,'BULK_PRINT_DATA'),




-- Confirmation Harrow

(278,'bdcb84c2-49c1-435f-9821-262446c98a1c','email address',NULL,NULL,'JUROR_EMAIL'),
(279,'bdcb84c2-49c1-435f-9821-262446c98a1c','COURTADDRESS',NULL,NULL,'TEMPORARY_COURT_ADDRESS'),
(280,'bdcb84c2-49c1-435f-9821-262446c98a1c','COURTNAME',NULL,NULL,'COURT_LOC_COURT_NAME'),
(281,'bdcb84c2-49c1-435f-9821-262446c98a1c','SERVICESTARTDATE',365,396,'BULK_PRINT_DATA'),
(282,'bdcb84c2-49c1-435f-9821-262446c98a1c','FIRSTNAME',415,434,'BULK_PRINT_DATA'),
(283,'bdcb84c2-49c1-435f-9821-262446c98a1c','LASTNAME',435,454,'BULK_PRINT_DATA'),
(284,'bdcb84c2-49c1-435f-9821-262446c98a1c','SERVICESTARTTIME',397,404,'BULK_PRINT_DATA'),
(285,'bdcb84c2-49c1-435f-9821-262446c98a1c','JURORNUMBER',675,683,'BULK_PRINT_DATA');

-- DEFERRAL DENIED

INSERT INTO juror_mod.notify_template_field (id,template_id,template_field,position_from,position_to,mapper_object) VALUES

(286,'63d636d3-4ca2-452d-baa2-a940e4dcc48a','JURORNUMBER',852,860,'BULK_PRINT_DATA'),
(287,'63d636d3-4ca2-452d-baa2-a940e4dcc48a','COURTNAME',NULL,NULL,'TEMPORARY_COURT_NAME'),
(288,'63d636d3-4ca2-452d-baa2-a940e4dcc48a','FIRSTNAME',592,611,'BULK_PRINT_DATA'),
(289,'63d636d3-4ca2-452d-baa2-a940e4dcc48a','LASTNAME',612,631,'BULK_PRINT_DATA'),
(290,'63d636d3-4ca2-452d-baa2-a940e4dcc48a','email address',NULL,NULL,'JUROR_EMAIL'),
(291,'63d636d3-4ca2-452d-baa2-a940e4dcc48a','SERVICESTARTDATE',NULL,NULL,'JUROR_POOL_NEXT_DATE'),
(292,'63d636d3-4ca2-452d-baa2-a940e4dcc48a','SERVICESTARTTIME',NULL,NULL,'POOL_ATTEND_TIME');




-- DEFERRAL GRANTED

INSERT INTO juror_mod.notify_template_field (id,template_id,template_field,position_from,position_to,mapper_object) VALUES

(298,'f5072da7-b250-4f02-b206-f176b1a0b80b','email address',NULL,NULL,'JUROR_EMAIL'),
(299,'f5072da7-b250-4f02-b206-f176b1a0b80b','JURORNUMBER',672,680,'BULK_PRINT_DATA'),
(300,'f5072da7-b250-4f02-b206-f176b1a0b80b','COURTNAME',NULL,NULL,'TEMPORARY_COURT_NAME'),
(301,'f5072da7-b250-4f02-b206-f176b1a0b80b','FIRSTNAME',412,431,'BULK_PRINT_DATA'),
(302,'f5072da7-b250-4f02-b206-f176b1a0b80b','LASTNAME',432,451,'BULK_PRINT_DATA'),
(303,'f5072da7-b250-4f02-b206-f176b1a0b80b','SERVICESTARTDATE',362,393,'BULK_PRINT_DATA'),
(304,'f5072da7-b250-4f02-b206-f176b1a0b80b','SERVICESTARTTIME',394,401,'BULK_PRINT_DATA');





--  EXCUSAL DENIED

INSERT INTO juror_mod.notify_template_field (id,template_id,template_field,position_from,position_to,mapper_object) VALUES

(305,'f5669ddd-4bb3-4092-b60b-45f410de74a7','email address',NULL,NULL,'JUROR_EMAIL'),
(306,'f5669ddd-4bb3-4092-b60b-45f410de74a7','SERVICESTARTDATE',NULL,NULL,'JUROR_POOL_NEXT_DATE'),
(307,'f5669ddd-4bb3-4092-b60b-45f410de74a7','SERVICESTARTTIME',NULL,NULL,'POOL_ATTEND_TIME'),
(308,'f5669ddd-4bb3-4092-b60b-45f410de74a7','JURORNUMBER',852,860,'BULK_PRINT_DATA'),
(309,'f5669ddd-4bb3-4092-b60b-45f410de74a7','COURTNAME',NULL,NULL,'TEMPORARY_COURT_NAME'),
(310,'f5669ddd-4bb3-4092-b60b-45f410de74a7','FIRSTNAME',592,611,'BULK_PRINT_DATA'),
(311,'f5669ddd-4bb3-4092-b60b-45f410de74a7','LASTNAME',612,631,'BULK_PRINT_DATA');





-- POSTPONEMENT

INSERT INTO juror_mod.notify_template_field (id,template_id,template_field,position_from,position_to,mapper_object) VALUES

 (312,'6504a964-0081-4b42-95da-9cccd26c1202','email address',NULL,NULL,'JUROR_EMAIL'),
 (313,'6504a964-0081-4b42-95da-9cccd26c1202','COURTNAME',NULL,NULL,'TEMPORARY_COURT_NAME'),
 (314,'6504a964-0081-4b42-95da-9cccd26c1202','JURORNUMBER',632,640,'BULK_PRINT_DATA'),
 (315,'6504a964-0081-4b42-95da-9cccd26c1202','FIRSTNAME',372,391,'BULK_PRINT_DATA'),
 (316,'6504a964-0081-4b42-95da-9cccd26c1202','LASTNAME',392,411,'BULK_PRINT_DATA'),
 (317,'6504a964-0081-4b42-95da-9cccd26c1202','SERVICESTARTDATE',671,702,'BULK_PRINT_DATA'),
 (318,'6504a964-0081-4b42-95da-9cccd26c1202','SERVICESTARTTIME',703,710,'BULK_PRINT_DATA');



-- WEEKLEY COMMS
-- 4 WEEK COMMS

INSERT INTO juror_mod.notify_template_field (id,template_id,template_field,position_from,position_to,mapper_object) VALUES

(319,'b17e55bb-d170-49c1-a22b-cd21c55d8039','email address',NULL,NULL,'JUROR_EMAIL'),
(320,'b17e55bb-d170-49c1-a22b-cd21c55d8039','JURORNUMBER',NULL,NULL,'JUROR_NUMBER'),
(321,'b17e55bb-d170-49c1-a22b-cd21c55d8039','SERVICESTARTDATE',NULL,NULL,'JUROR_POOL_NEXT_DATE'),
(322,'b17e55bb-d170-49c1-a22b-cd21c55d8039','FIRSTNAME',NULL,NULL,'JUROR_FIRST_NAME'),
(323,'b17e55bb-d170-49c1-a22b-cd21c55d8039','LASTNAME',NULL,NULL,'JUROR_LAST_NAME'),
(324,'b17e55bb-d170-49c1-a22b-cd21c55d8039','COURTADDRESS',NULL,NULL,'TEMPORARY_COURT_ADDRESS'),
(325,'b17e55bb-d170-49c1-a22b-cd21c55d8039','SERVICESTARTTIME',NULL,NULL,'POOL_ATTEND_TIME');



-- SENT TO COURT
INSERT INTO juror_mod.notify_template_field (id,template_id,template_field,position_from,position_to,mapper_object) VALUES

(326,'b6915247-ff69-4740-a4b7-22505be25ef4','email address',NULL,NULL,'JUROR_EMAIL'),
(327,'b6915247-ff69-4740-a4b7-22505be25ef4','JURORNUMBER',NULL,NULL,'JUROR_NUMBER'),
(328,'b6915247-ff69-4740-a4b7-22505be25ef4','SERVICESTARTDATE',NULL,NULL,'JUROR_POOL_NEXT_DATE'),
(329,'b6915247-ff69-4740-a4b7-22505be25ef4','FIRSTNAME',NULL,NULL,'JUROR_FIRST_NAME'),
(330,'b6915247-ff69-4740-a4b7-22505be25ef4','LASTNAME',NULL,NULL,'JUROR_LAST_NAME'),
(331,'b6915247-ff69-4740-a4b7-22505be25ef4','COURTADDRESS',NULL,NULL,'TEMPORARY_COURT_ADDRESS'),
(332,'b6915247-ff69-4740-a4b7-22505be25ef4','SERVICESTARTTIME',NULL,NULL,'POOL_ATTEND_TIME'),
(333,'b6915247-ff69-4740-a4b7-22505be25ef4','COURTPHONE',NULL,NULL,'TEMORARY_COURT_JURY_OFFICER_PHONE');
