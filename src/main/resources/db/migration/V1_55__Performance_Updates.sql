alter table juror_mod.voters
    add postcode_start VARCHAR(4);

UPDATE juror_mod.voters
SET postcode_start = split_part(zip, ' ', 1);

-- DROP FUNCTION juror_mod.get_voters(int8, text, text, text, text, text);

CREATE OR REPLACE FUNCTION juror_mod.get_voters(p_required bigint, p_mindate date, p_maxdate date, p_loccode text,
                                                p_areacode_list text, p_pool_type text)
    RETURNS TABLE(part_number character varying, juror_flags character varying)
    LANGUAGE plpgsql
    STABLE SECURITY DEFINER
AS $function$
DECLARE
begin
    return query select part_no, flags from juror_mod.voters
                 where loc_code=p_LocCode and date_selected1 is null
                   and ((dob is null) or dob > p_minDate and dob < p_maxDate)
                   and perm_disqual is null
                   and (postcode_start in (select unnest(string_to_array(p_areacode_list, ',')))) -- specified postcode areas
                   and (flags is null or p_pool_type = 'N') -- only coroner pools check flag
                 order by random()
                 limit p_required*1.2; -- grab 20% more than requested to allow for jurors with flags

END;
$function$
;
