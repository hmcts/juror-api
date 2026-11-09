
-- adding history codes for resending letters and response packs JS-1086

INSERT INTO juror_mod.t_history_code (history_code, description, template) VALUES
 ('RLDL', 'Reissue Deferred Letter', '{other_information}'),
 ('RLND', 'Reissue Non-Deferred Letter', '{other_information}'),
 ('RLWL', 'Reissue Withdrawal Letter', '{other_information}'),
 ('RLNE', 'Reissue Non-Excused Letter', '{other_information}'),
 ('RLEL', 'Reissue Excusal Letter', '{other_information}'),
 ('RLPT', 'Reissue Postponed Letter', '{other_information}'),
 ('RLNR', 'Reissue Non Responded Letter', '{other_information}'),
 ('RLRL', 'Reissue Responded Letter', '{other_information}'),
 ('RLPK', 'Reissue Response Pack', '{other_information}');

-- update history code description for RSUP to match the other reissue letters
Update juror_mod.t_history_code set description = 'Reissue Summons Letter' where history_code = 'RSUP';
