create or replace view juror_mod.juror_expense_subtotals as
select	    a.juror_number, a.pool_number, j.first_name, j.last_name, a.loc_code,
            -- travel expenses
            sum(coalesce(public_transport_total_due, 0)) as public_transport_total_due_total,
            sum(coalesce(public_transport_total_paid, 0)) as public_transport_total_paid_total,
            sum(coalesce(hired_vehicle_total_due, 0)) as hired_vehicle_total_due_total,
            sum(coalesce(hired_vehicle_total_paid, 0)) as hired_vehicle_total_paid_total,
            sum(coalesce(motorcycle_total_due, 0)) as motorcycle_total_due_total,
            sum(coalesce(motorcycle_total_paid, 0)) as motorcycle_total_paid_total,
            sum(coalesce(car_total_due, 0)) as car_total_due_total,
            sum(coalesce(car_total_paid, 0)) as car_total_paid_total,
            sum(coalesce(pedal_cycle_total_due, 0)) as pedal_cycle_total_due_total,
            sum(coalesce(pedal_cycle_total_paid, 0)) as pedal_cycle_total_paid_total,
            sum(coalesce(parking_total_due, 0)) as parking_total_due_total,
            sum(coalesce(parking_total_paid, 0)) as parking_total_paid_total,
            -- financial loss - loss of earnings
            sum(coalesce(loss_half_day_due, 0)) as loss_half_day_due_total,
            sum(coalesce(loss_half_day_paid, 0)) as loss_half_day_paid_total,
            sum(coalesce(loss_full_day_due, 0)) as loss_full_day_due_total,
            sum(coalesce(loss_full_day_paid, 0)) as loss_full_day_paid_total,
            sum(coalesce(loss_half_day_long_service_due, 0)) as loss_half_day_long_service_due_total,
            sum(coalesce(loss_half_day_long_service_paid, 0)) as loss_half_day_long_service_paid_total,
            sum(coalesce(loss_full_day_long_service_due, 0)) as loss_full_day_long_service_due_total,
            sum(coalesce(loss_full_day_long_service_paid, 0)) as loss_full_day_long_service_paid_total,
            -- financial loss - childcare expenses
            sum(coalesce(childcare_total_due, 0)) as childcare_total_due_total,
            sum(coalesce(childcare_total_paid, 0)) as childcare_total_paid_total,
            -- financial loss - misc expenses
            sum(coalesce(misc_total_due, 0)) as misc_total_due_total,
            sum(coalesce(misc_total_paid, 0)) as misc_total_paid_total,
            -- subsistence
            sum(coalesce(subs_half_day_due, 0)) as subs_half_day_due_total,
            sum(coalesce(subs_half_day_paid, 0)) as subs_half_day_paid_total,
            sum(coalesce(subs_full_day_due, 0)) as subs_full_day_due_total,
            sum(coalesce(subs_full_day_paid, 0)) as subs_full_day_paid_total,
            sum(coalesce(subs_long_day_due, 0)) as subs_long_day_due_total,
            sum(coalesce(subs_long_day_paid, 0)) as subs_long_day_paid_total,
            sum(coalesce(subs_overnight_due, 0)) as subs_overnight_due_total,
            sum(coalesce(subs_overnight_paid, 0)) as subs_overnight_paid_total,
            -- deductions
            sum(coalesce(smart_card_spend, 0)) as smart_card_spend_total
from        juror_mod.appearance a
inner join  juror_mod.juror j
    on      j.juror_number = a.juror_number
group by    a.juror_number, a.pool_number, j.first_name, j.last_name, a.loc_code;


create or replace view juror_mod.juror_expense_totals as
select	    juror_number, pool_number, first_name, last_name, loc_code,
            -- travel expenses
            public_transport_total_due_total + hired_vehicle_total_due_total + motorcycle_total_due_total +
            car_total_due_total + pedal_cycle_total_due_total + parking_total_due_total as travel_unapproved,
            public_transport_total_paid_total + hired_vehicle_total_paid_total + motorcycle_total_paid_total +
            car_total_paid_total + pedal_cycle_total_paid_total + parking_total_paid_total as travel_approved,
            -- financial loss
            loss_half_day_due_total + loss_full_day_due_total + loss_half_day_long_service_due_total +
            loss_full_day_long_service_due_total + childcare_total_due_total + misc_total_due_total as
            financial_loss_unapproved,
            loss_half_day_paid_total + loss_full_day_paid_total + loss_half_day_long_service_paid_total +
            loss_full_day_long_service_paid_total + childcare_total_paid_total + misc_total_paid_total as
            financial_loss_approved,
            -- subsistence
            subs_half_day_due_total + subs_full_day_due_total + subs_long_day_due_total + subs_overnight_due_total as
            subsistence_unapproved,
            subs_half_day_paid_total + subs_full_day_paid_total + subs_long_day_paid_total + subs_overnight_paid_total
            as subsistence_approved,
            -- deductions
            smart_card_spend_total,
            -- totals
            public_transport_total_due_total + hired_vehicle_total_due_total + motorcycle_total_due_total +
            car_total_due_total + pedal_cycle_total_due_total + parking_total_due_total + childcare_total_due_total +
            misc_total_due_total + loss_half_day_due_total + loss_full_day_due_total +
            loss_half_day_long_service_due_total + loss_full_day_long_service_due_total + subs_half_day_due_total +
            subs_full_day_due_total + subs_long_day_due_total + subs_overnight_due_total as total_unapproved,
            public_transport_total_paid_total + hired_vehicle_total_paid_total + motorcycle_total_paid_total +
            car_total_paid_total + pedal_cycle_total_paid_total + parking_total_paid_total + childcare_total_paid_total
            + misc_total_paid_total + loss_half_day_paid_total + loss_full_day_paid_total +
            loss_half_day_long_service_paid_total + loss_full_day_long_service_paid_total + subs_half_day_paid_total + 
            subs_full_day_paid_total + subs_long_day_paid_total + subs_overnight_paid_total as total_approved
from	    juror_mod.juror_expense_subtotals;