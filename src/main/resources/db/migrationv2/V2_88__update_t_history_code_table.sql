-- JS-563 add RADD history code for reinstated juror

INSERT INTO juror_mod.t_history_code (history_code,description,"template") VALUES
	 ('RADD','Reinstated to Jury','Trial: {other_info_reference}');
