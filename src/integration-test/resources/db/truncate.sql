---- Do not truncate standing data tables in JUROR.
-- Order is important to avoid issues with foreign key constraints!
DELETE FROM JUROR.PASSWORD;
DELETE FROM JUROR_DIGITAL.BUREAU_AUTH;
DELETE FROM JUROR.PART_HIST;
DELETE FROM JUROR.PART_AMENDMENTS;
DELETE FROM JUROR.PHONE_LOG;
DELETE FROM JUROR_DIGITAL.POOL_EXTEND;
DELETE FROM JUROR.POOL;
DELETE FROM JUROR.UNIQUE_POOL;
DELETE FROM JUROR.DISQ_LETT;
DELETE FROM JUROR.EXC_LETT;
DELETE FROM JUROR.EXC_DENIED_LETT;
DELETE FROM JUROR_DIGITAL.JUROR_RESPONSE_AUD;
DELETE FROM JUROR_DIGITAL.APP_SETTINGS;
DELETE FROM JUROR_DIGITAL.CHANGE_LOG_ITEM;
DELETE FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT;
DELETE FROM JUROR_DIGITAL.STAFF_AUDIT;
DELETE FROM JUROR_DIGITAL.CHANGE_LOG;
DELETE FROM JUROR_DIGITAL.STAFF;
DELETE FROM JUROR_DIGITAL.TEAM;
DELETE FROM JUROR_DIGITAL.JUROR_RESPONSE_CJS_EMPLOYMENT;
DELETE FROM JUROR_DIGITAL.JUROR_RESPONSE_SPECIAL_NEEDS;
DELETE FROM JUROR_DIGITAL.JUROR_RESPONSE;
DELETE FROM JUROR.DEFER_DBF;
DELETE FROM JUROR.DEF_LETT;
DELETE FROM JUROR.DEF_DENIED;
DELETE FROM JUROR.COURT_CATCHMENT_AREA;
DELETE FROM JUROR_DIGITAL.TEAM;
DELETE FROM JUROR_DIGITAL.NOTIFY_TEMPLATE_FIELD;
DELETE FROM JUROR_DIGITAL.NOTIFY_TEMPLATE_MAPPING;
DELETE FROM JUROR_DIGITAL.EXPENSES_RATES;
DELETE FROM JUROR_DIGITAL.STATS_RESPONSE_TIMES;
DELETE FROM JUROR_DIGITAL.STATS_NOT_RESPONDED;
DELETE FROM JUROR_DIGITAL.STATS_UNPROCESSED_RESPONSES;
DELETE FROM JUROR_DIGITAL.STATS_WELSH_ONLINE_RESPONSES;
DELETE FROM JUROR_DIGITAL.STATS_AUTO_PROCESSED;
DELETE FROM JUROR_DIGITAL.STATS_THIRDPARTY_ONLINE;



DELETE FROM juror_mod.appearance;
DELETE FROM juror_mod.appearance_audit;
DELETE FROM JUROR_MOD.financial_audit_details_appearances;
DELETE FROM JUROR_MOD.financial_audit_details;
DELETE FROM JUROR_MOD.user_roles;
DELETE FROM JUROR_MOD.user_courts;
DELETE FROM JUROR_MOD.users;
DELETE FROM juror_mod.app_setting;
DELETE FROM juror_mod.notify_template_field;
DELETE FROM juror_mod.notify_template_mapping;
DELETE FROM juror_mod.expense_rates_public;
-- Reset the sequences as part of the begin step.  See README.md database setup for details.
