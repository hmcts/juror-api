alter table juror_mod.juror
   alter column reasonable_adj_msg type varchar(2000);

alter table juror_mod.juror_reasonable_adjustment
    alter column reasonable_adjustment_detail type varchar(2000);