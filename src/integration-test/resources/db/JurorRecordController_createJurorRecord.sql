-- create a pool record owned by 415
INSERT INTO juror_mod.pool
(pool_no, "owner", return_date,  total_no_required, no_requested, pool_type, loc_code, new_request, last_update)
VALUES ('415220502', '415', current_date + 10, 5, 5, 'CRO', '415', 'N',
        TIMESTAMP'2023-11-29T09:00:00');