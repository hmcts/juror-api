
-- adding history codes for resending letters and response packs JS-1086

INSERT INTO juror_mod.t_history_code (history_code, description, template) VALUES
 ('RLDL', 'Resend Deferred Letter', '{other_information}'),
 ('RLND', 'Resend Non-Deferred Letter', '{other_information}'),
 ('RLWL', 'Resend Withdrawal Letter', '{other_information}'),
 ('RLNE', 'Resend Non-Excused Letter', '{other_information}'),
 ('RLEL', 'Resend Excusal Letter', '{other_information}'),
 ('RLPT', 'Resend Postponed Letter', '{other_information}'),
 ('RLNR', 'Resend Non Responded Letter', '{other_information}'),
 ('RLRL', 'Resend Responded Letter', '{other_information}'),
 ('RLPK', 'Resend Response Pack', '{other_information}');
