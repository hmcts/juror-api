DROP PROCEDURE IF EXISTS juror_mod.printfiles_to_clob(integer);

CREATE OR REPLACE PROCEDURE juror_mod.printfiles_to_clob(IN p_document_limit integer DEFAULT 1000000)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
	v_ext_date DATE;
	forms RECORD;
	v_count INTEGER:=0;

BEGIN
	v_ext_date :=	CASE
						WHEN TO_CHAR(NOW(),'SSSSS')::INTEGER < 64800
              				THEN NOW()::date - 1 -- cast is needed as you can't do arrhythmic with a timestamp as 1 becomes ambiguous
               				ELSE NOW()
              		END;

    -- delete redundant letter requests
	CALL juror_mod.delete_printfiles();

	FOR forms IN
		SELECT	fr.form_type,
				fr.max_rec_len::integer as rec_len
		FROM juror_mod.t_form_attr fr

		loop
			CALL juror_mod.write_printfiles(forms.form_type, forms.rec_len, v_ext_date, p_document_limit);
		END LOOP;

END;
$procedure$
;

DROP PROCEDURE IF EXISTS juror_mod.write_printfiles(in varchar, in int4, in date, in int4, inout int4);

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
	v_page_size integer; -- total pages for form type
	v_total_remaining_records integer; -- total amount of remaining records to write
	v_documents_to_write integer; -- total amount of documents to write to current file
	v_records_count integer; -- total records to be extracted for printing
	v_count integer;

begin
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
			SELECT RPAD('   ' ||RPAD(p_form_type,16)||LPAD(v_records_count::VARCHAR(20),6,'0')||LPAD(v_records_count::VARCHAR(20),6,'0')||'50'||LPAD(p_rec_len::VARCHAR(20),8,'0'),256,' ')
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
			SELECT RPAD('   ' ||RPAD(p_form_type,16)||LPAD(v_records_count::VARCHAR(6),6,'0')||LPAD(v_records_count::VARCHAR(6),6,'0')||'50'||LPAD(p_rec_len::VARCHAR(20),8,'0'),256,' ')
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

--DROP PROCEDURE juror_mod.delete_printfiles();

CREATE OR REPLACE PROCEDURE juror_mod.delete_printfiles()
 LANGUAGE plpgsql
AS $procedure$
BEGIN
	DELETE
	FROM juror_mod.bulk_print_data bpd
	WHERE bpd.extracted_flag = false
	AND EXISTS 	(
					SELECT 1
					FROM juror_mod.juror_pool jp
					WHERE jp.juror_number = bpd.juror_no
					AND jp.is_active = true
					AND (
							(bpd.form_type IN ('5224','5224C') AND jp.status <> 6) -- withdrawal letters
			                OR
			                (bpd.form_type IN ('5225','5225C') AND jp.status <> 5) -- excusal letters
			                OR
			                (bpd.form_type IN ('5226','5226C') AND jp.status <> 2) -- excusal denied letters
			                OR
			                (bpd.form_type IN ('5226A','5226AC') AND jp.status <> 2) -- deferral denied letters
			                OR
			                (bpd.form_type IN ('5227','5227C') AND jp.status <> 1) -- request for info letters
			                OR
			                (bpd.form_type IN ('5229','5229C') AND jp.status <> 7) -- postpone letters
			                OR
			                (bpd.form_type IN ('5229A','5229AC') AND jp.status not in (2,7)) -- deferral letters
			             	OR
			             	(bpd.form_type IN ('5229A','5229AC') AND jp.status = 2 AND COALESCE(jp.was_deferred,false) = false) -- deferral letters, deferral deleted
						)
				);
END;
$procedure$
;