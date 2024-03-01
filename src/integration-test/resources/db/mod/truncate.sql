--DELETE FROM juror_mod.accused;
DELETE FROM juror_mod.payment_data;
DELETE FROM juror_mod.app_settings;
DELETE FROM juror_mod.appearance;
DELETE FROM juror_mod.appearance_audit;
DELETE FROM juror_mod.financial_audit_details_appearances;
DELETE FROM juror_mod.financial_audit_details;
DELETE FROM juror_mod.bulk_print_data;
DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.coroner_pool_detail;
DELETE FROM juror_mod.coroner_pool;
DELETE FROM juror_mod.court_location_audit;
DELETE FROM juror_mod.court_catchment_area;
--DELETE FROM juror_mod.court_region;
DELETE FROM juror_mod.expense_rates;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.pool_comments;
DELETE FROM juror_mod.pool_history;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.pool;
DELETE FROM juror_mod.message;

DELETE FROM juror_mod.staff_juror_response_audit;
DELETE FROM juror_mod.juror_response_aud;
DELETE FROM juror_mod.juror_response_cjs_employment;
DELETE FROM juror_mod.juror_reasonable_adjustment;
DELETE FROM juror_mod.juror_response;
--
DELETE FROM juror_mod.juror;

--DELETE FROM juror_mod.message;
--DELETE FROM juror_mod.notify_template_field;
--DELETE FROM juror_mod.notify_template_mapping;
--DELETE FROM juror_mod.password;
--DELETE FROM juror_mod.payment_data;

--DELETE FROM juror_mod.region_notify_template;
DELETE FROM juror_mod.users;

DELETE FROM juror_mod.rev_info;
--DELETE FROM JUROR_DIGITAL.STAFF_AUDIT;

--DELETE FROM juror_mod.utilisation_stats;

DELETE FROM juror_mod.juror_trial;
DELETE FROM juror_mod.trial;
DELETE FROM juror_mod.judge;
DELETE FROM juror_mod.courtroom;
DELETE FROM juror_mod.pending_juror;
DELETE FROM juror_mod.court_location where loc_code in ('001', '002', '003');