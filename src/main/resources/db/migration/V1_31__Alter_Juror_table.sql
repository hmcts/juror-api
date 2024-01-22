ALTER TABLE juror_mod.juror
	ADD CONSTRAINT excusal_code_fk FOREIGN KEY (excusal_code) REFERENCES juror_mod.t_exc_code,
	ADD CONSTRAINT disq_code_fk FOREIGN KEY (disq_code) REFERENCES juror_mod.t_disq_code;
