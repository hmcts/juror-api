INSERT INTO juror_mod.message_to_placeholders (message_id,placeholder_name)
VALUES (14,'<attend_date>');

 UPDATE juror_mod.t_message_template
 SET "text" = 'Your jury panel is likely to go into deliberation on <attend_date>. Please bring a packed lunch to court as you will not be able to leave the deliberation room to get refreshments. Please do not bring metal cutlery or glass. If you have any questions, please contact the jury office on <court_phone>.'
 WHERE id = 14;
