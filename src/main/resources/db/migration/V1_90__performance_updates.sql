DROP VIEW juror_mod.juror_expense_totals;
DROP VIEW juror_mod.juror_expense_subtotals;

DROP INDEX juror_mod.juror_response_staff_login_idx;
CREATE INDEX juror_response_staff_login_idx ON juror_mod.juror_response (staff_login, processing_status, completed_at);
CREATE INDEX juror_pool_pool_number_idx ON juror_mod.juror_pool (pool_number, juror_number);

DROP index juror_mod.juror_response_last_name_idx;
CREATE INDEX juror_response_last_name_idx ON juror_mod.juror_response (lower(last_name), date_received);
CREATE INDEX trial_loc_code_idx ON juror_mod.trial (loc_code, trial_start_date, trial_end_date);
CREATE INDEX juror_trial_loc_code_result_idx ON juror_mod.juror_trial (loc_code, "result", empanelled_date, return_date);


alter table juror_mod.appearance
    add column total_due numeric(8, 2) generated always as (
        COALESCE(public_transport_total_due, 0::numeric)
            + COALESCE(hired_vehicle_total_due, 0::numeric)
            + COALESCE(motorcycle_total_due, 0::numeric)
            + COALESCE(car_total_due, 0::numeric)
            + COALESCE(pedal_cycle_total_due, 0::numeric)
            + COALESCE(parking_total_due, 0::numeric)
            + COALESCE(childcare_total_due, 0::numeric)
            + COALESCE(misc_total_due, 0::numeric)
            + COALESCE(loss_of_earnings_due, 0::numeric)
            + COALESCE(subsistence_due, 0::numeric)
            - COALESCE(smart_card_due, 0::numeric)
        ) STORED,
    add column total_paid numeric(8, 2) generated always as (
        COALESCE(public_transport_total_paid, 0::numeric)
            + COALESCE(hired_vehicle_total_paid, 0::numeric)
            + COALESCE(motorcycle_total_paid, 0::numeric)
            + COALESCE(car_total_paid, 0::numeric)
            + COALESCE(pedal_cycle_total_paid, 0::numeric)
            + COALESCE(parking_total_paid, 0::numeric)
            + COALESCE(childcare_total_paid, 0::numeric)
            + COALESCE(misc_total_paid, 0::numeric)
            + COALESCE(loss_of_earnings_paid, 0::numeric)
            + COALESCE(subsistence_paid, 0::numeric)
            - COALESCE(smart_card_paid, 0::numeric)
        ) STORED;
