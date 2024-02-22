ALTER TABLE juror_mod.appearance
    ALTER COLUMN food_and_drink_claim_type TYPE character varying(30);
ALTER TABLE juror_mod.appearance_audit
    ALTER COLUMN food_and_drink_claim_type TYPE character varying(30);

ALTER TABLE juror_mod.appearance
    DROP CONSTRAINT food_and_drink_claim_type_val;

ALTER TABLE juror_mod.appearance
    ADD CONSTRAINT food_and_drink_claim_type_val CHECK (((food_and_drink_claim_type)::text = ANY (
        (ARRAY ['NONE'::character varying, 'LESS_THAN_OR_EQUAL_TO_10_HOURS'::character varying,
            'MORE_THAN_10_HOURS'::character varying])::text[])));

ALTER TABLE juror_mod.appearance_audit
    DROP CONSTRAINT food_and_drink_claim_type_val;

ALTER TABLE juror_mod.appearance_audit
    ADD CONSTRAINT food_and_drink_claim_type_val CHECK (((food_and_drink_claim_type)::text = ANY (
        (ARRAY ['NONE'::character varying, 'LESS_THAN_OR_EQUAL_TO_10_HOURS'::character varying,
            'MORE_THAN_10_HOURS'::character varying])::text[])));