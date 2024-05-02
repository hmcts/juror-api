DELETE
FROM juror_mod.user_roles_audit
WHERE username LIKE 'test_%';

DELETE
FROM juror_mod.user_courts_audit
WHERE username LIKE 'test_%';

DELETE
FROM juror_mod.users_audit
WHERE username LIKE 'test_%';

DELETE
FROM juror_mod.user_roles
WHERE username LIKE 'test_%';

DELETE
FROM juror_mod.user_courts
WHERE username LIKE 'test_%';

DELETE
FROM juror_mod.users
WHERE username LIKE 'test_%';