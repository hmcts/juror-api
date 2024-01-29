ALTER TABLE juror_mod.t_exc_code
    ALTER COLUMN exc_code type varchar(2);
INSERT INTO juror_mod.t_exc_code (exc_code, description, by_right, enabled)
VALUES ('PE', 'Personal engagement', false, true),
       ('CE', 'CJS employee (unable to transfer)', false, true),
       ('DC', 'Deferred by court - too many jurors ', false, true);

UPDATE juror_mod.t_reasonable_adjustments
SET description = 'HEARING LOSS'
WHERE code = 'H';

UPDATE juror_mod.t_reasonable_adjustments
SET description = 'ALLERGIES'
WHERE code = 'D';

UPDATE juror_mod.t_reasonable_adjustments
SET description = 'PREGNANCY/BREASTFEEDING'
WHERE code = 'P';

UPDATE juror_mod.t_reasonable_adjustments
SET description = 'CARING RESPONSIBILITIES'
WHERE code = 'C';

UPDATE juror_mod.t_reasonable_adjustments
SET description = 'MEDICATION'
WHERE code = 'U';

INSERT INTO juror_mod.t_reasonable_adjustments (code, description)
VALUES ('J', 'CJS EMPLOYEE'),
       ('E', 'EPILEPSY'),
       ('A', 'RELIGIOUS REASONS'),
       ('T', 'TRAVELLING DIFFICULTIES')




