
-- JM-7885 Update the printfile header record to show the total number of records in the file instead of the total for the form type
-- so that the correct count is given when multiple files of the same form type are generated due to exceeding the max records per file.

CREATE OR REPLACE PROCEDURE juror_mod.write_printfiles(IN p_form_type character varying, IN p_rec_len integer, IN p_ext_date date, IN p_document_limit integer)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
    form_details RECORD; -- current record from bulk print data table.
    v_filename VARCHAR(50); -- current filename
    v_header VARCHAR(50); -- header for print file
    v_row_id INTEGER; -- row id of current record from bulk print data
    v_data text:=''; -- data to be written to file
    v_detail_rec text; -- row of data from bulk print data
    v_revision int8; -- sequence number for file name
    v_total_remaining_records integer; -- total amount of remaining records to write
    v_documents_to_write integer; -- total amount of documents to write to current file
    v_records_count integer; -- total records to be extracted for printing
    v_count integer;

begin
    v_count := 0;
    select count(bpd.id)
    from juror_mod.bulk_print_data bpd
    WHERE bpd.form_type = p_form_type
      AND bpd.extracted_flag = false
      AND DATE(bpd.creation_date) <= p_ext_date
    into v_total_remaining_records;

    v_records_count := v_total_remaining_records;

    -- Identify the next sequence number
    SELECT  NEXTVAL('juror_mod.print_file_count') INTO v_revision;
    v_filename := 'JURY'||LPAD(v_revision::VARCHAR(22),4,'0')||'01.0001';

    -- Calculate documents to write
    if v_total_remaining_records - p_document_limit < 0 then
        v_documents_to_write := v_total_remaining_records;
    else
        v_total_remaining_records := v_total_remaining_records - p_document_limit;
        v_documents_to_write := p_document_limit;
    end if;

    FOR form_details IN
        SELECT 	ROW_NUMBER() OVER () as counter,
                  bpd.id,
                  REPLACE(REPLACE(bpd.detail_rec,CHR(10),' '),CHR(13),' ') detail_rec
        FROM juror_mod.bulk_print_data bpd
        WHERE bpd.form_type = p_form_type
          AND bpd.extracted_flag = false
          AND DATE(bpd.creation_date) <= p_ext_date
        loop

            -- setting header for file first time only
            IF v_header IS null or v_header = '' THEN
                SELECT RPAD('   ' ||RPAD(p_form_type,16)||LPAD(v_documents_to_write::VARCHAR(6),6,'0')||LPAD(v_documents_to_write::VARCHAR(6),6,'0')||'50'||LPAD(p_rec_len::VARCHAR(8),8,'0'),256,' ')
                INTO v_header;
                v_data := '' || v_header || chr(10);
            END if;

            IF v_count >= p_document_limit then
                -- Create the record
                INSERT INTO juror_mod.content_store(request_id,document_id,file_type,data)
                VALUES (NEXTVAL('juror_mod.content_store_seq'),v_filename,'PRINT',v_data);
                COMMIT;

                -- setting new file name as last file has filled up to the limit
                SELECT  NEXTVAL('juror_mod.print_file_count') INTO v_revision;
                v_filename := 'JURY'||LPAD(v_revision::VARCHAR(22),4,'0')||'01.0001';


                -- Calculate documents to write
                if v_total_remaining_records - p_document_limit < 0 then
                    v_documents_to_write := v_total_remaining_records;
                else
                    v_total_remaining_records := v_total_remaining_records - p_document_limit;
                    v_documents_to_write := p_document_limit;
                end if;

                -- create new header for new file
                SELECT RPAD('   ' ||RPAD(p_form_type,16)||LPAD(v_documents_to_write::VARCHAR(6),6,'0')||LPAD(v_documents_to_write::VARCHAR(6),6,'0')||'50'||LPAD(p_rec_len::VARCHAR(8),8,'0'),256,' ')
                INTO v_header;

                -- erasing data for new file
                v_data := '' || v_header || chr(10);
                v_count := 0;

            END IF;

            -- Form the data column details first
            SELECT 	form_details.id,
                      form_details.detail_rec
            INTO v_row_id, v_detail_rec;


            -- If data is empty then the first line will be the header plus newline otherwise it will be the current data plus header plus newline
            v_data := v_data||v_detail_rec||CHR(10);

            UPDATE juror_mod.bulk_print_data
            SET  extracted_flag = true
            WHERE id = v_row_id;

            -- increment the loop counter so that the process can be stopped if it reaches the threshold
            v_count := v_count + 1;

        END LOOP;

    -- flush data to content store if the limit hasn't been reached
    IF v_data <> '' THEN
        INSERT INTO juror_mod.content_store(request_id,document_id,file_type,data)
        VALUES (NEXTVAL('juror_mod.content_store_seq'),v_filename,'PRINT',v_data);
        COMMIT;
    END IF;
END;
$procedure$
;
