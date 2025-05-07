-- update the lengths in the juror_mod.t_form_attr table
-- ** will need to flush out all pending comms before running this script as the lastname position is changing

-- update CONFRIM_JUROR_ENG_TAUTON, CONFRIM_JUROR_ENG_HARROW, CONFRIM_JUROR_ENG
update juror_mod.notify_template_field set position_from = 714, position_to = 738
where template_field = 'LASTNAME' and template_id in ('ea38af04-0631-4c7c-bfc8-0c491b7e98a2', 'bdcb84c2-49c1-435f-9821-262446c98a1c', '00afe3f3-28cb-4ae0-9776-9b78556ae8e7');

-- update TEMP_DEF_DENIED_ENG, DEF_DENIED_ENG, EXC_DENIED_ENG, TEMP_EXC_DENIED_ENG
update juror_mod.notify_template_field set position_from = 891, position_to = 915
where template_field = 'LASTNAME' and template_id  in ('63d636d3-4ca2-452d-baa2-a940e4dcc48a', '7e6f2099-6fb7-4179-b968-e9c867e73c64', '26d3232e-09cd-47a8-afaa-8d0d0d0dd2a2', 'f5669ddd-4bb3-4092-b60b-45f410de74a7');

-- update TEMP_DEF_GRANTED_ENG, DEF_GRANTED_ENG, TEMP_POSTPONE_JUROR_ENG, POSTPONE_JUROR_ENG
update juror_mod.notify_template_field set position_from = 711, position_to = 735
where template_field = 'LASTNAME' and template_id  in ('f5072da7-b250-4f02-b206-f176b1a0b80b', '399c27ff-9651-4a49-9398-99c990db1a34','6504a964-0081-4b42-95da-9cccd26c1202', '7857b20c-3582-4de2-9f1a-c906096d3c73');

-- EXC_GRANTED_ENG_2, DISQ_ENG
update juror_mod.notify_template_field set position_from = 671, position_to = 695
where template_field = 'LASTNAME' and template_id  in ('f470c51a-354e-4841-a1d8-59a03fc825d4','c8f1c394-7b40-4313-b679-9cbdcd7028a3');

-- EXC_GRANTED_WELSH_2, DISQ_CY
update juror_mod.notify_template_field set position_from = 652, position_to = 676
where template_field = 'LASTNAME' and template_id  in ('0f246e68-28b2-4f3b-be8d-4523bdbefc3c', '4c264993-ea71-45f8-8d05-4ec48cd1ac9d');

-- CONFRIM_JUROR_CY
update juror_mod.notify_template_field set position_from = 695, position_to = 719
where template_field = 'LASTNAME' and template_id  in ('30d7c170-75df-4aed-a0e8-62ee39ff344b');

-- DEF_GRANTED_CY, POSTPONE_JUROR_CY
update juror_mod.notify_template_field set position_from = 692, position_to = 716
where template_field = 'LASTNAME' and template_id  in ('1d8b1a28-6fd6-4a57-b851-4dcb908ebe24', '0d628e8b-5e3b-472f-8e40-ae234a7b729c');

-- DEF_DENIED_CY, EXC_DENIED_CY
update juror_mod.notify_template_field set position_from = 872, position_to = 896
where template_field = 'LASTNAME' and template_id  in ('96edec1e-54bb-4a29-9b80-9e84f5096f15', '940fa42a-3d4f-4da3-aead-ceace4348080');
