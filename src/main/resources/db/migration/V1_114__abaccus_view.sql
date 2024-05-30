-- juror_mod.abaccus source

create or replace view juror_mod.abaccus as

select      form_type,
            creation_date,
            count(juror_no) as number_of_items
from        juror_mod.bulk_print_data pf
group by    form_type,
            creation_date;