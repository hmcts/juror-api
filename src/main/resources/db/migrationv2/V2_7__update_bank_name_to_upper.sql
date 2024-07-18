-- DROP PROCEDURE juror_mod.payment_files_to_clob_write_to_clob(in date, in numeric, inout varchar);

CREATE OR REPLACE PROCEDURE juror_mod.payment_files_to_clob_write_to_clob(IN p_creation_date date, IN p_total numeric,
                                                                          INOUT p_file_name character varying)
    LANGUAGE plpgsql
    SECURITY DEFINER
AS
$procedure$
DECLARE
    v_header   VARCHAR(255);
    out_rec    varchar(450);
    out_rec2   varchar(450);
    out_rec3   varchar(450);
    c_lob      text;
    i          integer;
    v_revision int8;
    c_extract  RECORD;

BEGIN
    -- set the sequence id, filename and header
    SELECT NEXTVAL('juror_mod.payment_file_count')
    INTO v_revision;

    SELECT TO_CHAR(p_creation_date, 'FMDDMONYYYY') || LPAD(v_revision::VARCHAR(20), 9, '0') || '.dat',
           'HEADER' || '|' || LPAD(v_revision::VARCHAR(20), 9, '0') || '|' || LPAD(TO_CHAR(p_total, '9999990.90'), 11)
    INTO p_file_name, v_header;

    c_lob := v_header || chr(10);

    FOR c_extract IN
        SELECT pd.loc_code,
               pd.unique_id,
               pd.creation_date,
               pd.expense_total,
               pd.juror_number || pd.invoice_id AS                                                                 part_invoice,
               rpad(coalesce(pd.bank_sort_code, ''), 6)                                                            bank_sort_code,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.bank_ac_name, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 bank_ac_name,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.bank_ac_number, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 bank_ac_number,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.build_soc_number, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 build_soc_number,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.address_line_1, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 address_line1,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.address_line_2, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 address_line2,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.address_line_3, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 address_line3,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.address_line_4, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 address_line4,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.address_line_5, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 address_line5,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.postcode, '|', ' '), chr(10), ' '), chr(13), ' '),
                              ''))                                                                                 postcode,
               pd.auth_code,
               UPPER(coalesce(REPLACE(REPLACE(REPLACE(pd.juror_name, '|', ' '), chr(10), ' '), chr(13), ' '), '')) name,
               UPPER(rpad(coalesce(pd.loc_cost_centre, ''), 5))                                                    loc_cost_centre,
               pd.travel_total,
               pd.subsistence_total             as                                                                 sub_total,
               pd.financial_loss_total          as                                                                 floss_total,
               pd.creation_date                 as                                                                 sub_date
        FROM juror_mod.payment_data pd
        WHERE date_trunc('day', pd.creation_date) = p_creation_date
          and pd.expense_file_name IS NULL

        LOOP
            out_rec := c_extract.loc_code || lpad(c_extract.unique_id, 7, '0') || '|'
                           || to_char(c_extract.creation_date, 'DD-Mon-YYYY') || '|'
                           || lpad(to_char(c_extract.expense_total, '9999990.90'), 11) || '|' ||
                       rpad(c_extract.loc_code || c_extract.part_invoice, 50) || '|'
                           || to_char(c_extract.creation_date, 'DD-Mon-YYYY') || '|' || c_extract.bank_sort_code || '|'
                           || rpad(c_extract.bank_ac_name, 18) || '|' || rpad(c_extract.bank_ac_number, 8) || '|' ||
                       rpad(c_extract.build_soc_number, 18);

            out_rec2 := '|' || rpad(c_extract.address_line1, 35) || '|'
                            || rpad(c_extract.address_line2, 35) || '|'
                            || rpad(c_extract.address_line3, 35) || '|'
                || rpad(c_extract.address_line4, 35);

            IF c_extract.travel_total IS NOT NULL and c_extract.travel_total > 0 then
                out_rec3 := '|' || rpad(c_extract.address_line5, 35) || '|'
                                || rpad(c_extract.postcode, 20) || '|'
                                || rpad(c_extract.auth_code, 9) || '|'
                                || rpad(c_extract.name, 50) || '|'
                                || c_extract.loc_cost_centre || '|' || '2' || '|'
                                || lpad(to_char(c_extract.travel_total, '9999990.90'), 11) || '|'
                    || to_char(c_extract.sub_date, 'DD-Mon-YYYY');
                c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || chr(10);
            END IF;

            IF c_extract.sub_total IS NOT NULL and c_extract.sub_total > 0 THEN
                out_rec3 := '|' || rpad(c_extract.address_line5, 35) || '|' || rpad(c_extract.postcode, 20) ||
                            '|' || rpad(c_extract.auth_code, 9) ||
                            '|' || rpad(c_extract.name, 50) || '|' || c_extract.loc_cost_centre || '|' || '1' || '|' ||
                            lpad(to_char(c_extract.sub_total, '9999990.90'), 11) || '|' ||
                            to_char(c_extract.sub_date, 'DD-Mon-YYYY');
                c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || chr(10);
            END IF;

            IF c_extract.floss_total IS NOT NULL and c_extract.floss_total > 0 THEN
                out_rec3 := '|' || rpad(c_extract.address_line5, 35) || '|' || rpad(c_extract.postcode, 20) ||
                            '|' || rpad(c_extract.auth_code, 9) ||
                            '|' || rpad(c_extract.name, 50) || '|' || c_extract.loc_cost_centre || '|' || '0' || '|' ||
                            lpad(to_char(c_extract.floss_total, '9999990.90'), 11) || '|' ||
                            to_char(c_extract.sub_date, 'DD-Mon-YYYY');
                c_lob := c_lob || (out_rec || out_rec2 || out_rec3) || chr(10);
            END IF;
        END LOOP;
    c_lob := c_lob || '****' || chr(10);
    -- Write header line into CLOB
    INSERT INTO juror_mod.content_store(request_id, document_id, file_type, data)
    VALUES (NEXTVAL('juror_mod.content_store_seq'), p_File_Name, 'PAYMENT', c_lob);
END;
$procedure$
;
