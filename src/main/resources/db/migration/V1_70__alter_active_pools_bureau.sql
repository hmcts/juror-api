-- juror_mod.active_pools_bureau - optimised for pvt
drop view juror_mod.active_pools_bureau;
CREATE OR REPLACE VIEW juror_mod.active_pools_bureau
AS SELECT pr.pool_no,
       pr.no_requested AS jurors_requested,
       sum(
           CASE
               WHEN jp.owner::text = '400'::text AND jp.status = 2 THEN 1
               ELSE 0
           END) AS confirmed_jurors,
       cl.loc_name AS court_name,
       pt.pool_type_desc AS pool_type,
       pr.return_date AS service_start_date
      FROM juror_mod.pool pr
        LEFT JOIN juror_mod.juror_pool jp ON jp.pool_number::text = pr.pool_no::text
        JOIN juror_mod.court_location cl ON pr.loc_code::text = cl.loc_code::text
        JOIN juror_mod.t_pool_type pt ON pr.pool_type::text = pt.pool_type::text
     WHERE pr.owner::text = '400'::text
     and pr.new_request::text = 'N'::text
     AND pr.no_requested <> 0
     GROUP BY pr.pool_no, cl.loc_name, pt.pool_type_desc, pr.return_date;
