-- DROP PROCEDURE juror_mod.payment_files_to_clob_write_to_clob(in date, in numeric, inout varchar);

DROP PROCEDURE IF EXISTS juror_mod.payment_files_to_clob_write_to_clob(IN date, IN numeric, INOUT varchar);

CREATE OR REPLACE PROCEDURE juror_mod.payment_files_to_clob_write_to_clob(
    IN p_creation_date DATE,
    IN p_total NUMERIC(8, 2),
    OUT p_file_name VARCHAR
)
LANGUAGE plpgsql
AS $procedure$
DECLARE
    v_header     VARCHAR(255);
    out_rec      VARCHAR(450);
    out_rec2     VARCHAR(450);
    out_rec3     VARCHAR(450);
    c_lob        TEXT;
    v_date_prefix VARCHAR(20);
    v_max_suffix INTEGER;
    v_new_suffix INTEGER;
    c_extract    RECORD;
BEGIN
    -- Build date prefix
    v_date_prefix := TO_CHAR(p_creation_date, 'FMDDMONYYYY');

    -- Get the current max suffix for today's file name
    SELECT MAX(CAST(SUBSTRING(expense_file_name FROM 10 FOR 9) AS INTEGER))
    INTO v_max_suffix
    FROM juror_mod.payment_data
    WHERE expense_file_name LIKE v_date_prefix || '%.dat';

    -- If no files yet, start from 0
    IF v_max_suffix IS NULL THEN
        v_max_suffix := 0;
    END IF;

    -- Increment
    v_new_suffix := v_max_suffix + 1;

    -- Create the full file name
    p_file_name := v_date_prefix || LPAD(v_new_suffix::TEXT, 9, '0') || '.dat';

    -- Build the header line
    v_header := 'HEADER' || '|' || LPAD(v_new_suffix::TEXT, 9, '0') || '|' || LPAD(TO_CHAR(p_total, '9999990.90'), 11);

    -- Initialize the CLOB with header
    c_lob := v_header || CHR(10);

    -- Extract payment data for the file
    FOR c_extract IN
        SELECT pd.loc_code,
               pd.unique_id,
               pd.creation_date,
               pd.expense_total,
               pd.juror_number || pd.invoice_id AS part_invoice,
               RPAD(COALESCE(pd.bank_sort_code, ''), 6) AS bank_sort_code,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.bank_ac_name, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS bank_ac_name,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.bank_ac_number, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS bank_ac_number,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.build_soc_number, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS build_soc_number,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.address_line_1, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS address_line1,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.address_line_2, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS address_line2,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.address_line_3, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS address_line3,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.address_line_4, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS address_line4,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.address_line_5, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS address_line5,
               UPPER(COALESCE(REPLACE(REPLACE(REPLACE(pd.postcode, '|', ' '), CHR(10), ' '), CHR(13), ' '), '')) AS postcode,
               pd.auth_code,
               UPPER(TRIM(COALESCE(REPLACE(REPLACE(REPLACE(pd.juror_name, '|', ' '), CHR(10), ' '), CHR(13), ' '), ''))) AS name,
               UPPER(RPAD(COALESCE(pd.loc_cost_centre, ''), 5)) AS loc_cost_centre,
               pd.travel_total,
               pd.subsistence_total AS sub_total,
               pd.financial_loss_total AS floss_total,
               pd.creation_date AS sub_date
        FROM juror_mod.payment_data pd
        WHERE DATE_TRUNC('day', pd.creation_date) <= DATE_TRUNC('day', p_creation_date)
          AND pd.expense_file_name IS NULL
    LOOP
        out_rec := c_extract.loc_code || LPAD(c_extract.unique_id, 7, '0') || '|'
                   || TO_CHAR(c_extract.creation_date, 'DD-Mon-YYYY') || '|'
                   || LPAD(TO_CHAR(c_extract.expense_total, '9999990.90'), 11) || '|'
                   || RPAD(c_extract.loc_code || c_extract.part_invoice, 50) || '|'
                   || TO_CHAR(c_extract.creation_date, 'DD-Mon-YYYY') || '|'
                   || c_extract.bank_sort_code || '|'
                   || RPAD(c_extract.bank_ac_name, 18) || '|'
                   || RPAD(c_extract.bank_ac_number, 8) || '|'
                   || RPAD(c_extract.build_soc_number, 18);

        out_rec2 := '|' || RPAD(c_extract.address_line1, 35) || '|'
                         || RPAD(c_extract.address_line2, 35) || '|'
                         || RPAD(c_extract.address_line3, 35) || '|'
                         || RPAD(c_extract.address_line4, 35);

        IF c_extract.travel_total IS NOT NULL AND c_extract.travel_total > 0 THEN
            out_rec3 := '|' || RPAD(c_extract.address_line5, 35) || '|'
                             || RPAD(c_extract.postcode, 20) || '|'
                             || RPAD(c_extract.auth_code, 9) || '|'
                             || RPAD(c_extract.name, 50) || '|'
                             || c_extract.loc_cost_centre || '|2|'
                             || LPAD(TO_CHAR(c_extract.travel_total, '9999990.90'), 11) || '|'
                             || TO_CHAR(c_extract.sub_date, 'DD-Mon-YYYY');
            c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || CHR(10);
        END IF;

        IF c_extract.sub_total IS NOT NULL AND c_extract.sub_total > 0 THEN
            out_rec3 := '|' || RPAD(c_extract.address_line5, 35) || '|'
                             || RPAD(c_extract.postcode, 20) || '|'
                             || RPAD(c_extract.auth_code, 9) || '|'
                             || RPAD(c_extract.name, 50) || '|'
                             || c_extract.loc_cost_centre || '|1|'
                             || LPAD(TO_CHAR(c_extract.sub_total, '9999990.90'), 11) || '|'
                             || TO_CHAR(c_extract.sub_date, 'DD-Mon-YYYY');
            c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || CHR(10);
        END IF;

        IF c_extract.floss_total IS NOT NULL AND c_extract.floss_total > 0 THEN
            out_rec3 := '|' || RPAD(c_extract.address_line5, 35) || '|'
                             || RPAD(c_extract.postcode, 20) || '|'
                             || RPAD(c_extract.auth_code, 9) || '|'
                             || RPAD(c_extract.name, 50) || '|'
                             || c_extract.loc_cost_centre || '|0|'
                             || LPAD(TO_CHAR(c_extract.floss_total, '9999990.90'), 11) || '|'
                             || TO_CHAR(c_extract.sub_date, 'DD-Mon-YYYY');
            c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || CHR(10);
        END IF;
    END LOOP;

    -- Append footer
    c_lob := c_lob || '****' || CHR(10);

    -- Store into content store
    INSERT INTO juror_mod.content_store(request_id, document_id, file_type, data)
    VALUES (NEXTVAL('juror_mod.content_store_seq'), p_file_name, 'PAYMENT', c_lob);
END;
$procedure$;
;
