-- procedure required for running integration tests
CREATE OR REPLACE FUNCTION juror_digital.reset_seq_bulk(p_name text, p_val bigint)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$

declare sequence_name text;
declare l_num bigint;
declare l_increment int;

c_sequence_names CURSOR for
select unnest(string_to_array(p_name, ',')) as value;

begin

	open c_sequence_names;

	loop
		fetch c_sequence_names into sequence_name;
	exit when not found;
	   select  nextval(sequence_name) INTO l_num;
	   l_increment := p_val - l_num - 1;
	   execute 'alter sequence ' || sequence_name || ' increment by ' ||  l_increment || ' minvalue 0';
	   select  nextval(sequence_name) INTO l_num;
	   execute 'alter sequence ' ||  sequence_name || ' increment by 1';
	end loop;

	return 1;

	END;
$function$
;
