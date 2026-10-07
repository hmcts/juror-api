-- Create Table filename court map attachments

CREATE TABLE juror_mod.court_email_attachment (
    loc_code      VARCHAR(6)  PRIMARY KEY REFERENCES juror_mod.court_location(loc_code),
    file_name_en  VARCHAR(255) NOT NULL,
    file_name_cy  VARCHAR(255)
);

-- Map file name attachments
INSERT INTO juror_mod.court_email_attachment (loc_code, file_name_en, file_name_cy) VALUES
    ('411',  'EX104_cardiff.pdf',               'EX104_cardiff_CY.pdf'),
    ('415',  'EX104_chester.pdf',                NULL),
    ('419',  'EX104_derby.pdf',                  NULL),
    ('423',  'EX104_exeter.pdf',                 NULL),
    ('403',  'EX104_kingston_upon_hull.pdf',     NULL),
    ('431',  'EX104_lewes.pdf',                  NULL),
    ('478',  'EX104_newport_IOW.pdf',            NULL),
    ('441',  'EX104_newport_south_wales.pdf',    'EX104_newport_south_wales_CY.pdf'),
    ('443',  'EX104_norwich.pdf',                NULL),
    ('453',  'EX104_snaresbrook.pdf',            NULL);
