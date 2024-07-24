drop function juror_mod.get_voters(bigint,text, text, text, text, text);

alter table juror_mod.voters
    drop column postcode_start,
    add column postcode_start VARCHAR(10) generated always as (split_part(zip, ' ', 1)) stored;

DROP INDEX juror_mod.voters_postcode_start_idx;
CREATE INDEX voters_postcode_start_idx ON juror_mod.voters (postcode_start,loc_code,perm_disqual,flags,dob);



CREATE OR REPLACE FUNCTION juror_mod.get_voters(p_required bigint, p_mindate date, p_maxdate date, p_loccode text, p_areacode_list text, p_pool_type text)
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
                 limit p_required*1.4; -- grab 40% more than requested to allow for jurors with flags

END;
$function$
;
