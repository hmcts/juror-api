-- Add foreign key constraints where necessary (JM-4906)

alter table juror_mod.welsh_court_location
	add constraint welsh_court_loc_code_fk foreign key (loc_code) references juror_mod.court_location;

alter table juror_mod.holiday
	add constraint holiday_loc_code_fk foreign key ("owner") references juror_mod.court_location (loc_code);

alter table juror_mod.pool
	add constraint pool_loc_code_fk foreign key (loc_code) references juror_mod.court_location;

alter table juror_mod.pool
    add constraint pool_pool_type_fk foreign key (pool_type) references juror_mod.t_pool_type;

alter table juror_mod.pool_comments
    add constraint pool_comments_pool_no_fk foreign key (pool_no) references juror_mod.pool;

alter table juror_mod.juror_pool
    add constraint juror_pool_pool_no_fk foreign key (pool_number) references juror_mod.pool (pool_no);

alter table juror_mod.juror_pool
    add constraint juror_pool_status_fk foreign key (status) references juror_mod.t_juror_status;

alter table juror_mod.bulk_print_data
    add constraint bulk_print_data_juror_no_fk foreign key (juror_no) references juror_mod.juror (juror_number);

alter table juror_mod.coroner_pool
    add constraint coroner_pool_loc_code_fk foreign key (cor_court_loc) references juror_mod.court_location (loc_code);

alter table juror_mod.coroner_pool_detail
    add constraint coroner_pool_detail_pool_no_fk foreign key (cor_pool_no) references juror_mod.coroner_pool
    (cor_pool_no);