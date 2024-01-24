-- create reference tables in the juror_mod schema

select * into juror_mod.t_form_attr from juror.form_attr;
alter table juror_mod.t_form_attr
add constraint t_form_attr_pkey primary key (form_type);

alter table juror_mod.bulk_print_data
add constraint bulk_print_data_fk_form_type foreign key (form_type) references juror_mod.t_form_attr;

select * into juror_mod.system_parameter from juror_digital_user.system_parameter;
alter table juror_mod.system_parameter
add constraint system_parameter_pkey primary key (sp_id);

-- need to remove the rogue 715 location code if it exists
delete from juror.court_catchment_area where loc_code = '715';

select * into juror_mod.court_catchment_area from juror.court_catchment_area;
alter table juror_mod.court_catchment_area
add constraint court_catchment_area_pkey primary key (postcode,loc_code);
alter table juror_mod.court_catchment_area
add constraint court_catchment_area_fk_loc_code foreign key (loc_code) references juror_mod.court_location;