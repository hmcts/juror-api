-- juror_mod.active_pools_bureau source
CREATE OR REPLACE VIEW juror_mod.active_pools_bureau
AS SELECT pr.pool_no,
    pr.no_requested AS jurors_requested,
        CASE
            WHEN ps.available IS NULL THEN 0
            ELSE ps.available
        END AS confirmed_jurors,
    cl.loc_name AS court_name,
    pt.pool_type_desc AS pool_type,
    pr.return_date AS service_start_date
   FROM juror_mod.pool pr
     LEFT JOIN juror_mod.pool_stats ps ON pr.pool_no = ps.pool_number
     JOIN juror_mod.court_location cl ON pr.loc_code = cl.loc_code
     JOIN juror_mod.t_pool_type pt ON pr.pool_type = pt.pool_type
  WHERE pr.owner = '400' AND pr.new_request = 'N' AND pr.no_requested <> 0;