-- update the lengths in the juror_mod.t_form_attr table

update juror_mod.t_form_attr set max_rec_len = 735 where form_type = '5229A' and dir_name = 'ENG_DEFER';
update juror_mod.t_form_attr set max_rec_len = 716 where form_type = '5229AC' and dir_name = 'WEL_DEFER';
update juror_mod.t_form_attr set max_rec_len = 676 where form_type = '5225C' and dir_name = 'WEL_EXCUSE';
update juror_mod.t_form_attr set max_rec_len = 695 where form_type = '5225' and dir_name = 'ENG_EXCUSE';
update juror_mod.t_form_attr set max_rec_len = 915 where form_type = '5226A' and dir_name = 'ENG_NON_DEFER';
update juror_mod.t_form_attr set max_rec_len = 896 where form_type = '5226AC' and dir_name = 'WEL_NON_DEFER';
update juror_mod.t_form_attr set max_rec_len = 896 where form_type = '5226C' and dir_name = 'WEL_NON_EXCUSE';
update juror_mod.t_form_attr set max_rec_len = 915 where form_type = '5226' and dir_name = 'ENG_NON_EXCUSE';
update juror_mod.t_form_attr set max_rec_len = 727 where form_type = '5228' and dir_name = 'ENG_NON_RESP';
update juror_mod.t_form_attr set max_rec_len = 748 where form_type = '5228C' and dir_name = 'WEL_NON_RESP';
update juror_mod.t_form_attr set max_rec_len = 716 where form_type = '5229C' and dir_name = 'WEL_POSTPONE';
update juror_mod.t_form_attr set max_rec_len = 735 where form_type = '5229' and dir_name = 'ENG_POSTPONE';
update juror_mod.t_form_attr set max_rec_len = 1020 where form_type = '5221' and dir_name = 'ENG_SUMMONS';
update juror_mod.t_form_attr set max_rec_len = 1284 where form_type = '5221C' and dir_name = 'BI_SUMMONS';
update juror_mod.t_form_attr set max_rec_len = 886 where form_type = '5227C' and dir_name = 'WEL_REQUEST';
update juror_mod.t_form_attr set max_rec_len = 905 where form_type = '5227' and dir_name = 'ENG_REQUEST';
update juror_mod.t_form_attr set max_rec_len = 695 where form_type = '5224' and dir_name = 'ENG_WITHDRAW';
update juror_mod.t_form_attr set max_rec_len = 676 where form_type = '5224C' and dir_name = 'WEL_WITHDRAW';
update juror_mod.t_form_attr set max_rec_len = 719 where form_type = '5224AC' and dir_name = 'WEL_CONFIRM';
update juror_mod.t_form_attr set max_rec_len = 738 where form_type = '5224A' and dir_name = 'ENG_CONFIRM';
