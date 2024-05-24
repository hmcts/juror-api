-- DROP PROCEDURE juror_mod.printfiles_to_clob(int4);

CREATE OR REPLACE PROCEDURE juror_mod.printfiles_to_clob(IN p_limit integer DEFAULT 1000000)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
	v_form_type VARCHAR(6);
	v_max_rec_len INTEGER;
	v_ext_date DATE;
	forms RECORD;
	v_count INTEGER:=0;

BEGIN
	v_ext_date :=	CASE
						WHEN TO_CHAR(NOW(),'SSSSS')::INTEGER < 64800
              				THEN NOW()::date - 1
               				ELSE NOW()
              		END;

    -- delete redundant letter requests
	CALL juror_mod.delete_printfiles();

	FOR forms IN
		SELECT	fr.form_type,
				fr.max_rec_len::integer
		FROM juror_mod.t_form_attr fr

		LOOP
			CALL juror_mod.write_printfiles(forms.form_type, forms.max_rec_len, v_ext_date, p_limit, v_count);
			IF v_count >= p_limit THEN
				EXIT;
			END IF;
	END LOOP;

END;
$procedure$
;

-- DROP PROCEDURE juror_mod.write_printfiles(in varchar, in int4, in date, in int4, inout int4);

CREATE OR REPLACE PROCEDURE juror_mod.write_printfiles(IN p_form_type character varying, IN p_max_rec_len integer, IN p_ext_date date, IN p_limit integer, INOUT p_count integer)
 LANGUAGE plpgsql
AS $procedure$
DECLARE
	form_details RECORD;
	v_filename VARCHAR(50);
	v_header VARCHAR(50);
	v_row_id INTEGER;
	v_count INTEGER:=1;
	v_strpos INTEGER:=1;
	v_max_loop_count float;
	v_data text:=null;
	v_max_data_len float;
	v_detail_rec text;
	v_revision int8;
   	v_text_var1 VARCHAR(255);
   	v_text_var2 VARCHAR(255);
   	v_text_var3 VARCHAR(255);
	v_print_msg VARCHAR(255);

BEGIN

	FOR form_details IN
		SELECT 	ROW_NUMBER() OVER () as counter,
				bpd.id,
				REPLACE(REPLACE(bpd.detail_rec,CHR(10),' '),CHR(13),' ') detail_rec
		FROM juror_mod.bulk_print_data bpd
		WHERE bpd.form_type = p_form_type
		AND COALESCE(bpd.extracted_flag,'N') = 'N'
		AND DATE(bpd.creation_date) <= p_ext_date

	LOOP
		-- increment the loop counter so that the process can be stopped if it reaches the threshold
		p_count := p_count + 1;
		IF p_count >= p_limit THEN
			EXIT;
		END IF;

		-- Identify the next sequence number
		SELECT  NEXTVAL('juror_mod.print_file_count') INTO v_revision;

		-- Form the data column details first
		SELECT 	form_details.id,
				'JURY'||LPAD(v_revision::VARCHAR(22),4,'0')||'01.0001',
				RPAD('   ' ||RPAD(p_form_type,16)||LPAD(form_details.counter::VARCHAR(20),6,'0')||LPAD(form_details.counter::VARCHAR(20),6,'0')||'50'||LPAD(p_max_rec_len::VARCHAR(20),8,'0'),256,' '),
				form_details.detail_rec
		INTO v_row_id, v_filename, v_header, v_detail_rec;

		SELECT LENGTH(form_details.detail_rec)
		INTO v_max_data_len;

		-- round up the division to the nearest whole number - e.g. 1.4 would equate to 2
		SELECT CEIL(v_max_data_len/p_max_rec_len)
		INTO v_max_loop_count;

		-- First line
		SELECT v_header||chr(10)
		INTO v_data;

		-- reset the starting string position
		v_strpos := 1;
		v_count := 1;
		-- Multiple lines for the details under the header if it exceeds the max record threshold
		FOR v_count IN 1..v_max_loop_count loop
			IF LENGTH(v_detail_rec) > 0  THEN
				v_data := v_data||v_detail_rec||CHR(10); -- add line details to the data column
			END IF;
			v_strpos := v_strpos + p_max_rec_len;  -- increment the string position identifier so that the next line starts from the end of the previous line counter
	 	END LOOP;

		-- Create the record
		INSERT INTO juror_mod.content_store(request_id,document_id,file_type,data)
		VALUES (NEXTVAL('juror_mod.content_store_seq'),v_filename,'PRINT',v_data);

		UPDATE juror_mod.bulk_print_data
		SET  extracted_flag = 'Y'
		WHERE id = v_row_id;

		COMMIT;

	END LOOP;

END;
$procedure$
;

-- DROP PROCEDURE juror_mod.delete_printfiles();

CREATE OR REPLACE PROCEDURE juror_mod.delete_printfiles()
 LANGUAGE plpgsql
AS $procedure$
BEGIN
	DELETE
	FROM juror_mod.bulk_print_data bpd
	WHERE COALESCE (bpd.extracted_flag,'N') = 'N'
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