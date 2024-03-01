delete
from juror_mod.court_location
where loc_code in ('001');

INSERT into juror_mod.court_location
(loc_code,
 public_transport_soft_limit,
 taxi_soft_limit)
values ('001', 13.01300, 14.01400);
;
