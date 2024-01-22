DELETE FROM juror_pool.t_pool_type;

INSERT INTO juror_pool.t_pool_type
(pool_type, pool_type_desc) VALUES
('CRO', 'CROWN COURT'),
('COR', 'CORONER''S COURT'),
('CIV', 'CIVIL COURT');
