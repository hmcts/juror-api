-- existing juror users for staff
-- existing staff
-- staff doing the edit
INSERT INTO juror_mod.users (owner, username, name, level, active, password,team_id,version)
VALUES ('400','EXISTING1','Andy Active',1,true,'5BAA61E4C9B93F3F',1,0),
       ('400','EXISTING2','Alison Active',0,false,'5BAA61E4C9B93F3F',2,0);

-- juror user that will get a staff entry
