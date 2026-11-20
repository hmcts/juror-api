-- Update juror_mod.t_exc_code with Social engagement

INSERT INTO juror_mod.t_exc_code(exc_code,description,by_right,enabled,for_excusal,for_deferral)
VALUES ('SE','SOCIAL ENGAGEMENT',false,true,false,false);
