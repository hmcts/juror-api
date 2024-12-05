-- update to printfiles delete procedure to include new form types and rules as part of JS-130

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
			             (bpd.form_type IN ('5221','5221C') AND jp.status <> 1) -- summons letters
			             OR
                   (bpd.form_type IN ('5228','5228C') AND jp.status <> 1) -- summons reminder letters
                   OR
                   (bpd.form_type IN ('5224A','5224AC') AND jp.status <> 2) -- confirmation letters
                   OR
                   (bpd.form_type IN ('5225','5225C') AND jp.status <> 5) -- excusal letters
			             OR
			             (bpd.form_type IN ('5226','5226C') AND jp.status not in (1,2,6,7)) -- excusal denied letters
			             OR
			             (bpd.form_type IN ('5226A','5226AC') AND jp.status not in (1,2,5,6)) -- deferral denied letters
			             OR
			             (bpd.form_type IN ('5227','5227C') AND jp.status <> 1) -- request for info letters
			             OR
			             (bpd.form_type IN ('5229','5229C') AND jp.status not in (2,7)) -- postpone letters
			             OR
			             (bpd.form_type IN ('5229A','5229AC') AND jp.status not in (2,7)) -- deferral letters
			             OR
			             (bpd.form_type IN ('5229A','5229AC') AND jp.status = 2 AND COALESCE(jp.was_deferred,false) = false) -- deferral letters, deferral deleted
						)
				);
END;
$procedure$
;
