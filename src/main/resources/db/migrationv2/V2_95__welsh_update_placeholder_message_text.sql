INSERT INTO juror_mod.message_to_placeholders (message_id,placeholder_name)
VALUES (31,'<attend_date>');

UPDATE juror_mod.t_message_template
 SET "text" = 'Mae eich panel rheithgor yn debygol o gychwyn y broses trafod ar <DD-MM-YYYY>. Dewch â phecyn bwyd gyda chi i'r llys oherwydd ni chaniateir i chi adael yr ystafell ymneilltuo i nôl bwyd. Peidiwch â dod â chyllell a fforc metel nac unrhyw eitemau gwydr gyda chi. Os oes gennych unrhyw gwestiynau, cysylltwch â'r swyddfa rheithgor drwy ffonio <court_phone>.'
 WHERE id = 31;
