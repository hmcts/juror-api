-- -- happy user (username/password), in a team, has staff record with courts.
INSERT INTO juror_mod.users(owner, username, name, level, active, last_logged_in, version, team_id, password,
                            password_changed_date)
VALUES ('415', 'username', 'User McName', 3, true, CURRENT_DATE, 0, 1, '5BAA61E4C9B93F3F', CURRENT_DATE);

-- -- happy user with password expiry warning
INSERT INTO juror_mod.users(owner, username, name, level, active, last_logged_in, version, team_id, password, password_changed_date)
VALUES ('400', 'username2', 'User McName', 3, true, CURRENT_DATE, 0, 1, '5BAA61E4C9B93F3F', (SELECT CURRENT_DATE-85));

-- unhappy user with password expired
INSERT INTO juror_mod.users(owner, username, name, level, active, last_logged_in, version, team_id, password, password_changed_date)
VALUES ('400', 'username3', 'User McName', 3, true, CURRENT_DATE, 0, 1, '5BAA61E4C9B93F3F', (SELECT CURRENT_DATE-100));

INSERT INTO juror_mod.users(owner, username, name, level, active, last_logged_in, version, team_id, password,
                            password_changed_date)
VALUES ('400', 'username4', 'User McName', 1, true, CURRENT_DATE, 0, 1, '5BAA61E4C9B93F3F', CURRENT_DATE);
