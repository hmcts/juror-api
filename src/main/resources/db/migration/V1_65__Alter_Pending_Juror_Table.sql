ALTER TABLE juror_mod.pending_juror RENAME COLUMN address TO address_line_1;
ALTER TABLE juror_mod.pending_juror RENAME COLUMN address2 TO address_line_2;
ALTER TABLE juror_mod.pending_juror RENAME COLUMN address3 TO address_line_3;
ALTER TABLE juror_mod.pending_juror RENAME COLUMN address4 TO address_line_4;
ALTER TABLE juror_mod.pending_juror RENAME COLUMN address5 TO address_line_5;

ALTER TABLE juror_mod.pending_juror DROP COLUMN address6;