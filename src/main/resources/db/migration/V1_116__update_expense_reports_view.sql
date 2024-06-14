drop view juror_mod.reports_juror_payments_summary;
CREATE OR REPLACE VIEW juror_mod.reports_juror_payments_summary
AS with payment_audits as (
	select		fad.juror_number,
				fad.loc_code,
				fad.id as latest_payment_f_audit_id
	from		juror_mod.financial_audit_details fad
	where		fad."type" in ('APPROVED_BACS', 'APPROVED_CASH', 'REAPPROVED_CASH', 'REAPPROVED_BACS')
)
, attendance_dates as (
	select		pa.juror_number,
				fada.loc_code,
				max(fada.financial_audit_id) as latest_payment_f_audit_id,
				fada.attendance_date
	from		juror_mod.financial_audit_details_appearances fada
	inner join	payment_audits pa
		on		pa.loc_code = fada.loc_code
				and pa.latest_payment_f_audit_id = fada.financial_audit_id
	group by 	juror_number, fada.loc_code, attendance_date
	order by	juror_number
)
select	a.trial_number,
				a.non_attendance,
				a.loc_code,
				a.attendance_date,
				a.juror_number,
				j.first_name,
				j.last_name,
				a.pool_number,
				ad.latest_payment_f_audit_id,
				a.time_in as checked_in,
				a.time_out as checked_out,
				a.time_out - a.time_in as hours_attended,
				a.attendance_audit_number as attendance_audit,
				fad.created_on as payment_date,
				(
					coalesce(a.public_transport_total_due,0) +
					coalesce(a.hired_vehicle_total_due,0) +
					coalesce(a.motorcycle_total_due,0) +
					coalesce(a.car_total_due,0) +
					coalesce(a.pedal_cycle_total_due,0) +
					coalesce(a.parking_total_due,0)
				) as total_travel_due,
				(
					coalesce(a.loss_of_earnings_due,0) +
					coalesce(a.childcare_total_due,0) +
					coalesce(a.misc_total_due, 0)
				) as total_financial_loss_due,
				coalesce(a.subsistence_due, 0) as subsistence_due,
				coalesce(a.smart_card_due,0) as smart_card_due,
				(
					coalesce(a.public_transport_total_due, 0) +
					coalesce(a.hired_vehicle_total_due, 0) +
					coalesce(a.motorcycle_total_due, 0) +
					coalesce(a.car_total_due, 0) +
					coalesce(a.pedal_cycle_total_due, 0) +
					coalesce(a.parking_total_due, 0) +
					coalesce(a.loss_of_earnings_due, 0) +
					coalesce(a.childcare_total_due, 0) +
					coalesce(a.misc_total_due, 0) +
					coalesce(a.subsistence_due, 0) -
					coalesce(a.smart_card_due, 0)
				) as total_due,
					(
					coalesce(a.public_transport_total_paid, 0) +
					coalesce(a.hired_vehicle_total_paid, 0) +
					coalesce(a.motorcycle_total_paid, 0) +
					coalesce(a.car_total_paid, 0) +
					coalesce(a.pedal_cycle_total_paid, 0) +
					coalesce(a.parking_total_paid, 0) +
					coalesce(a.loss_of_earnings_paid, 0) +
					coalesce(a.childcare_total_paid, 0) +
					coalesce(a.misc_total_paid, 0) +
					coalesce(a.subsistence_paid, 0) -
					coalesce(a.smart_card_paid, 0)
				) as total_paid
from			juror_mod.appearance a
join			juror_mod.juror j
	on			a.juror_number = j.juror_number
left join		attendance_dates ad
	on			ad.juror_number = a.juror_number
				and ad.attendance_date = a.attendance_date
				and ad.loc_code = a.loc_code
left join		juror_mod.financial_audit_details fad
	on			fad.juror_number = j.juror_number
				and fad.loc_code = a.loc_code
				and fad.id = ad.latest_payment_f_audit_id;


update juror_mod.t_history_code set description = 'Expenses submitted for approval' where history_code = 'FADD';
update juror_mod.t_history_code set description = 'Jury attendance confirmed' where history_code = 'AJUR';
update juror_mod.t_history_code set description = 'Juror record updated' where history_code = 'PDET';
update juror_mod.t_history_code set description = 'Returned from panel' where history_code = 'VRET';
update juror_mod.t_history_code set description = 'Added to panel' where history_code = 'VCRE';
update juror_mod.t_history_code set description = 'Pool attendance confirmed' where history_code = 'APOL';
update juror_mod.t_history_code set description = 'Juror summoned' where history_code = 'RSUM';
update juror_mod.t_history_code set description = 'Juror''s service completed' where history_code = 'SCOM';
update juror_mod.t_history_code set description = 'Confirmation letter issued' where history_code = 'RRES';
update juror_mod.t_history_code set description = 'Police check requested' where history_code = 'POLE';
update juror_mod.t_history_code set description = 'Police check completed' where history_code = 'POLG';
update juror_mod.t_history_code set description = 'Message sent' where history_code = 'RNOT';
update juror_mod.t_history_code set description = 'Juror responded' where history_code = 'RESP';
update juror_mod.t_history_code set description = 'Expenses approved' where history_code = 'AEDF';
update juror_mod.t_history_code set description = 'Juror''s contact details exported' where history_code = 'RMES';
update juror_mod.t_history_code set description = 'Empanelled on jury' where history_code = 'TADD';
update juror_mod.t_history_code set description = 'Juror deferral' where history_code = 'PDEF';
update juror_mod.t_history_code set description = 'Expenses edited' where history_code = 'FEDT';
update juror_mod.t_history_code set description = 'Juror ID confirmed' where history_code = 'CHID';
update juror_mod.t_history_code set description = 'Deferral letter issued' where history_code = 'RDEF';
update juror_mod.t_history_code set description = 'Juror reassigned' where history_code = 'PREA';
update juror_mod.t_history_code set description = 'Certificate of attendance issued' where history_code = 'RCER';
update juror_mod.t_history_code set description = 'Juror excusal' where history_code = 'PEXC';
update juror_mod.t_history_code set description = 'Added to panel' where history_code = 'VADD';
update juror_mod.t_history_code set description = 'Notes added' where history_code = 'PEDT';
update juror_mod.t_history_code set description = 'Summons reminder letter issued' where history_code = 'RNRE';
update juror_mod.t_history_code set description = 'Assigned to another panel' where history_code = 'VREA';
update juror_mod.t_history_code set description = 'Excusal refused letter issued' where history_code = 'REDL';
update juror_mod.t_history_code set description = 'Excusal letter issued' where history_code = 'REXC';
update juror_mod.t_history_code set description = 'Awaiting further information' where history_code = 'AWFI';
update juror_mod.t_history_code set description = 'Failed to attend letter sent' where history_code = 'RFTA';
update juror_mod.t_history_code set description = 'Deferral changed' where history_code = 'DCHG';
update juror_mod.t_history_code set description = 'Juror transferred to another court' where history_code = 'PTRA';
update juror_mod.t_history_code set description = 'Summons letter reissued' where history_code = 'RSUP';
update juror_mod.t_history_code set description = 'Postponement letter issued' where history_code = 'RPST';
update juror_mod.t_history_code set description = 'Juror disqualification' where history_code = 'PDIS';
update juror_mod.t_history_code set description = 'Request for information deleted' where history_code = 'DWFI';
update juror_mod.t_history_code set description = 'Police check completed' where history_code = 'POLD';
update juror_mod.t_history_code set description = 'Certificate of exemption issued' where history_code = 'REXE';
update juror_mod.t_history_code set description = 'Withdrawal letter issued' where history_code = 'RDIS';
update juror_mod.t_history_code set description = 'Deferral refused letter issued' where history_code = 'RDDL';
update juror_mod.t_history_code set description = 'Cash payment approved' where history_code = 'CASH';
update juror_mod.t_history_code set description = 'Police check deleted' where history_code = 'POLX';
update juror_mod.t_history_code set description = 'Juror failed to attend' where history_code = 'PFTA';
update juror_mod.t_history_code set description = 'Pending juror approved' where history_code = 'AUTH';
update juror_mod.t_history_code set description = 'Summons undeliverable' where history_code = 'PUND';
update juror_mod.t_history_code set description = 'Police check completed' where history_code = 'POLF';
update juror_mod.t_history_code set description = 'Police check completed' where history_code = 'POLC';
update juror_mod.t_history_code set description = 'Police check requested' where history_code = 'POLI';
update juror_mod.t_history_code set description = 'Police check requested' where history_code = 'POLA';