-- Updated function for returning voters for pool creation
CREATE OR REPLACE FUNCTION juror_mod.get_voters(p_required bigint, p_mindate text, p_maxdate text,
												p_loccode text, p_areacode_list text, p_pool_type text)
 RETURNS TABLE(part_number character varying, juror_flags character varying)
 LANGUAGE plpgsql
 STABLE SECURITY DEFINER
AS $function$
DECLARE
	      -- p_pool_type can be 'C'ORONER OR 'N'ON CORONER POOLS (REGULAR POOL)

	  	  -- using Julian Format for date comparison
          l_julian_min_dt bigint := (to_char(to_date(p_minDate, 'yyyy-mm-dd'),'J'))::numeric;
          l_julian_max_dt bigint := (to_char(to_date(p_maxDate, 'yyyy-mm-dd'),'J'))::numeric;
begin
 	return query select part_no, flags from juror_mod.voters
					where loc_code=p_LocCode and date_selected1 is null
					and ((dob is null) or to_number(to_char(dob,'J'),'9999999') > l_julian_min_dt
					and to_number(to_char(dob,'J'),'9999999') < l_julian_max_dt)
					and perm_disqual is null
					and (split_part(zip, ' ', 1) in (select unnest(string_to_array(p_areacode_list, ',')))) -- specified postcode areas
					and (flags is null or p_pool_type = 'N') -- only coroner pools check flag
					order by random()
					limit p_required*1.2; -- grab 20% more than requested to allow for jurors with flags

END;
$function$
;
