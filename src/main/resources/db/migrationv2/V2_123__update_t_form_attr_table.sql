
-- Add the new form types to the t_form_attr table for digital by default letters

INSERT INTO juror_mod.t_form_attr (form_type,dir_name,max_rec_len) VALUES
('6220','ENG_DBD_SUMMONS',610),
('6220C','BI_DBD_SUMMONS',873),
('6221','ENG_DBD_RESPONSE',610),
('6221C','BI_DBD_RESPONSE',873),
('6228','ENG_DBD_SUMMONS_REM',326),
('6228C','BI_DBD_SUMMONS_REM',347);
