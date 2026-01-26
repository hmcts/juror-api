
CREATE SCHEMA IF NOT EXISTS juror_er;


CREATE TABLE juror_er.local_authority (
      la_code varchar(3) NOT NULL,
      la_name varchar(100) NULL,
      is_active boolean,
      upload_status varchar(40) NULL,  -- NOT_UPLOADED, UPLOADED
      notes varchar(2000) NULL,
      inactive_reason varchar(2000) NULL,
      updated_by varchar(30) NULL,
      last_updated timestamp(3) NULL,
      CONSTRAINT local_authority_pkey PRIMARY KEY (la_code)
);


ALTER TABLE juror_er.local_authority
    ADD CONSTRAINT upload_status_value_check CHECK (((upload_status)::text = ANY (
        (ARRAY ['UPLOADED'::character varying, 'NOT_UPLOADED'::character varying]))));


CREATE TABLE juror_er.user (
     username varchar(200) NOT NULL, -- this is the email address of the user
     la_code  varchar(3) NOT NULL,
     active bool NOT NULL DEFAULT true, -- depends on state of the local authority
     last_logged_in timestamp(3) NULL,
   	 CONSTRAINT local_authority_fk FOREIGN KEY (la_code) REFERENCES juror_er.local_authority (la_code),
     CONSTRAINT user_pkey PRIMARY KEY (username)
);


CREATE TABLE juror_er.deadline (
    id smallint PRIMARY KEY CHECK (id = 1),
   	deadline_date date NULL,
   	updated_by varchar(30) NULL,
   	last_updated timestamp(3) NULL
);


CREATE TABLE juror_er.file_uploads (
    id bigserial NOT NULL,
    la_code varchar(3) NOT NULL,
	  la_username varchar(200) NOT NULL,
   	filename varchar(200) NOT NULL,
   	file_format varchar(20) NOT NULL,
   	file_size_bytes bigint NULL,
   	other_information varchar(1000) NULL, -- need to confirm how big this should be
   	upload_date timestamp NOT null,
   	CONSTRAINT file_uploads_pkey PRIMARY KEY (id),
   	CONSTRAINT file_uploads_la_fk FOREIGN KEY (la_code) REFERENCES juror_er.local_authority (la_code),
   	CONSTRAINT file_uploads_username_fk FOREIGN KEY (la_username) REFERENCES juror_er.user (username)
);



-- local authorities data insertions
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('001','West Oxfordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('002','Broxtowe',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('003','Eastleigh',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('004','Blackburn',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('005','Harrogate',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('006','Folkestone & Hythe',true,'NOT_UPLOADED','previously Shepway',NULL,NULL,NULL),
	 ('007','Bradford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('008','Arun',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('009','Bolsover',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('011','Bassetlaw',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('012','Boston',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('013','Redcar & Cleveland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('015','Breckland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('017','Fylde',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('018','Slough',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('019','Eden',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('020','Brighton & Hove',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('021','North Warwickshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('022','Broadland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('023','Forest Heath (West Suffolk)',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('024','Brentwood',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('025','Bromley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('026','Hackney',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('028','Hammersmith & Fulham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('029','Blaby',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('030','Erewash',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('031','Harlow',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('032','Guildford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('034','Harborough',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('035','Gedling',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('036','Fenland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('037','Dartford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('038','Hambleton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('039','Coventry',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('040','Great Yarmouth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('041','Mansfield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('042','South Holland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('043','East Riding of Yorkshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('044','Darlington',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('045','Bracknell Forest',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('046','Carlisle',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('047','Carmarthenshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('049','Swindon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('050','Dorset (All Areas)',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('051','East Hertfordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('052','Ipswich',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('053','Medway Towns',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('054','Leicester',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('055','Mid Devon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('056','Mid Suffolk',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('057','Wokingham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('058','Chichester',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('059','Adur',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('060','West Devon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('061','Bedford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('062','Braintree',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('063','Bath & North East Somerset',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('064','Amber Valley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('065','Caerphilly',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('066','Cheshire West & Chester',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('067','Chelmsford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('068','Conwy',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('069','Croydon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('070','Hart',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('074','Bolton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('075','Ashfield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('076','Chorley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('077','Barnsley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('079','Brent ',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('080','Cheltenham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('081','Derby',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('082','Neath Port Talbot',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('083','Christchurch',true,'NOT_UPLOADED',NULL,'merged with 073 - Poole (BCP)',NULL,NULL),
	 ('084','Gravesham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('085','Crawley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('086','Lincoln',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('087','Cornwall',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('088','Craven District',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('089','Forest of Dean',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('090','Nottingham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('091','Dacorum',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('092','Castle Point',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('093','East Devon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('095','Ashford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('096','Epping Forest',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('097','High Peak',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('098','South Cambridgeshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('099','Gloucester',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('100','Uttlesford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('101','Staffordshire Moorlands',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('102','Basildon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('103','Selby',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('104','Staffordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('105','Gosport',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('106','North Devon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('107','Haringey',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('108','Wychavon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('109','Lambeth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('110','Wyre Forest',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('111','Lichfield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('112','Dover',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('113','Dudley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('114','Ealing',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('115','Ceredigion',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('116','Exeter',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('117','East Lindsey',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('118','Greenwich',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('119','Gwynedd',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('120','Harrow',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('121','Herefordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('122','Bury',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('123','Hillingdon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('124','Huntingdon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('125','Islington',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('126','Barrow-in-Furness',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('128','Kings Lynn & W.Norfolk',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('129','Leeds',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('130','Epsom & Ewell',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('131','South Tyneside',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('132','Kirklees',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('133','Lewisham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('134','Sandwell',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('135','Havering',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('136','Eastbourne',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('033','Durham (Bishop Auckland)',false,'NOT_UPLOADED',NULL,'merged with 404 Durham',NULL,NULL),
	 ('073','Poole',true,'NOT_UPLOADED','contains merged 072,083',NULL,NULL,NULL),
	 ('078','Cherwell',false,'NOT_UPLOADED',NULL,'merged with 151 Cherwell & South Northants',NULL,NULL),
	 ('072','Bournemouth',false,'NOT_UPLOADED',NULL,'merged with 073 poole (BCP)',NULL,NULL),
	 ('137','Windsor & Maidenhead',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('138','Hounslow',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('139','Southend-on-Sea',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('141','Middlesborough',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('142','Plymouth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('143','Broxbourne',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('144','Richmond upon Thames',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('145','Hinckley & Bosworth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('146','Northamptonshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('147','Thanet',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('148','Wigan',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('149','Basingstoke & Deane',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('151','Cherwell & South Northants',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('152','Cotswold',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('153','Denbighshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('154','Wolverhampton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('155','Swale',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('156','Welwyn Hatfield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('157','Cardiff',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('158','Bexley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('159','East Northamptonshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('161','Maldon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('162','Corby',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('163','Sefton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('164','West Lindsey',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('165','South Kesteven',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('166','Charnwood',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('167','Enfield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('168','Canterbury',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('169','Liverpool',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('170','New Forest',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('172','Knowsley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('173','Mole Valley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('174','Lewes',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('175','Nuneaton & Bedworth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('176','Monmouthshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('177','Isle of Wight',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('178','North East Lincolnshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('179','Manchester',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('181','Kingston Upon Thames',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('182','Havant',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('183','Merthyr Tydfil',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('184','Hertsmere',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('185','Newport',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('186','Preston',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('187','Scarborough',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('188','Sevenoaks',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('189','Rochford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('190','Rutland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('191','Reigate & Banstead',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('192','Sedgemoor',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('195','Newcastle-upon-Tyne',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('196','Redbridge',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('197','Rossendale',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('198','Sheffield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('199','Halton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('201','Reading',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('202','North Hertfordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('203','Hyndburn',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('205','Rother',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('206','Rushmoor',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('207','Camden',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('208','Allerdale',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('210','Worthing',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('212','Thurrock',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('213','Salford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('214','North W Leicestershire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('215','Melton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('216','Wiltshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('217','Oadby & Wigston',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('218','North East Derbyshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('219','Stevenage',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('221','Horsham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('223','East Hampshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('224','Test Valley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('225','Hastings',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('228','Powys',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('229','Colchester',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('230','Gateshead',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('231','Portsmouth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('232','Derbyshire Dales',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('234','Babergh',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('235','East Cambridgeshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('236','Copeland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('238','Newark & Sherwood',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('239','Swansea',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('241','North Tyneside',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('242','Wakefield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('243','Vale of Glamorgan',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('244','Daventry',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('247','Barking & Dagenham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('248','Kingston-upon-Hull',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('249','Blackpool',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('250','Pendle',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('251','Flintshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('254','City of London',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('255','Barnet',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('256','Calderdale',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('257','Doncaster',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('258','Ryedale',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('259','Milton Keynes',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('260','York',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('261','West Berkshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('262','South Gloucestershire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('263','Bromsgrove & Redditch',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('264','Torfaen',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('265','Peterborough',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('266','Rhondda, Cynon, Taff',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('267','Bristol',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('268','North Kesteven',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('269','Ribble Valley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('270','Cambridge',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('271','Waltham Forest',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('272','Rotherham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('273','Stroud',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('274','North Norfolk',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('275','Surrey Heath',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('277','South Somerset',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('278','Westminster City',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('194','Redditch',false,'NOT_UPLOADED',NULL,'merged with 263 Bromsgrove & Redditch',NULL,NULL),
	 ('220','North Dorset',false,'NOT_UPLOADED',NULL,'merged with 050 Dorset (All Areas)',NULL,NULL),
	 ('222','Durham (Easington)',false,'NOT_UPLOADED',NULL,'merged with 404 durham',NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('233','Durham (Sedgefield)',false,'NOT_UPLOADED',NULL,'Merged with 404 durham',NULL,NULL),
	 ('245','Chiltern',false,'NOT_UPLOADED',NULL,'merged with 297 Wycombe (Buckinghamshire)',NULL,NULL),
	 ('204','Purbeck',false,'NOT_UPLOADED',NULL,'merged with 050 Dorset (All Areas)',NULL,NULL),
	 ('279','Stratford-on-Avon',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('280','Tandridge',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('281','Rushcliffe',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('284','Southampton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('285','Maidstone',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('286','Kettering',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('287','Stockton-on-Tees',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('288','South Lakeland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('289','Tendring',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('292','Rugby',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('293','South Derbyshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('294','Isle of Anglesey',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('295','St Helens',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('296','South Hams',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('298','Wandsworth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('299','Wellingborough',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('300','Newcastle-under-Lyme',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('301','Tamworth',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('302','Oxford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('303','Winchester',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('305','Tewkesbury',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('306','Merton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('307','Solihull',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('308','Stoke-on-Trent',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('310','Warrington',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('311','Wirral',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('312','Malvern Hills',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('313','Walsall',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('314','Birmingham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('315','Rochdale',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('317','Richmondshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('318','Fareham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('319','Watford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('320','EAST Suffolk (Suffolk Coastal & Waveney)',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('321','Pembrokeshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('322','South Norfolk',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('323','Lancaster',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('324','Southwark',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('325','Hartlepool',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('326','Woking',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('327','Trafford',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('329','Tameside',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('331','Torridge',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('332','Newham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('333','Wealden',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('334','Wrexham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('335','Three Rivers',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('336','Tunbridge Wells',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('338','Elmbridge',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('339','Chesterfield',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('340','Torbay',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('341','West Lancashire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('342','Burnley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('343','North Somerset',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('344','Mendip',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('345','Tonbridge & Malling',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('346','North Lincolnshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('347','Bridgend',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('348','Mid Sussex',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('349','South Staffordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('350','East Staffordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('351','Runnymede',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('353','Cannock Chase',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('356','Luton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('358','Sutton',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('359','St Albans',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('360','Spelthorne',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('361','Tower Hamlets',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('362','Warwick',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('363','Waverley',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('364','Wyre',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('365','Oldham',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('366','Worcestershire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('367','Kensington & Chelsea',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('368','Teignbridge',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('369','Telford & Wrekin',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('370','Norwich',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('371','Blaenau Gwent',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('372','South Ribble',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('373','Sunderland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('375','Stockport',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('400','Northumberland',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('401','Central Bedfordshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('402','Shropshire',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('403','Cheshire East',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('404','Durham County Council',true,'NOT_UPLOADED',NULL,NULL,NULL,NULL),
	 ('016','Aylesbury Vale',false,'NOT_UPLOADED',NULL,'merged with 297-Wycombe (Buckinghamshire)',NULL,NULL);
INSERT INTO juror_er.local_authority (la_code,la_name,is_active,upload_status,notes,inactive_reason,updated_by,last_updated) VALUES
	 ('352','South Oxfordshire',true,'NOT_UPLOADED',' Inc Vale of White Horse',NULL,NULL,NULL),
	 ('297','Wycombe (Buckinghamsire council)',true,'NOT_UPLOADED','now contains Wycombe, Aylesbury, Chiltern, South Bucks.',NULL,NULL,NULL),
	 ('304','Taunton Deane',true,'NOT_UPLOADED',' Includes 355 W.Somerset',NULL,NULL,NULL),
	 ('283','West Dorset',false,'NOT_UPLOADED',NULL,'merged with 050 Dorset (All Areas)',NULL,NULL),
	 ('290','Weymouth & Portland',false,'NOT_UPLOADED',NULL,'merged with 050 Dorset (All Areas)',NULL,NULL),
	 ('140','St Edmundsbury',false,'NOT_UPLOADED',NULL,'Merged with 023 forerst heath (West Sussex)',NULL,NULL),
	 ('337','South Bucks',false,'NOT_UPLOADED',NULL,'merged with 297 Wycombe (Buckinghamshire)',NULL,NULL),
	 ('355','West Somerset',true,'NOT_UPLOADED',NULL,'MERGED WITH 304 taunton',NULL,NULL),
	 ('291','Waveney',false,'NOT_UPLOADED',NULL,'MERGED with 320 Suffolk Coastal',NULL,NULL),
	 ('328','Vale of White Horse',false,'NOT_UPLOADED',NULL,'MERGED with 352 -South Oxfordshire (April 19)',NULL,NULL);
