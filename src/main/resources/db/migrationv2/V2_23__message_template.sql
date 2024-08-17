update table juror_mod.t_message_template
set text  = 'Eich Gwasanaeth Rheithgor','Cysylltwch â Swyddfa Rheithgor <welsh_courtname> drwy ffonio <court_phone>  ynghylch eich gwasanaeth rheithgor.'
where id = 26;

update table juror_mod.t_message_template
set text  = 'Eich Gwasanaeth Rheithgor','Nodyn Atgoffa: Cofiwch fynychu eich Gwasanaeth Rheithgor yn Llys <welsh_courtname> ar <attend_date> am <attend_time>. Os oes gennych unrhyw gwestiynau, cysylltwch â''r swyddfa rheithgor drwy ffonio <court_phone>.'
where id = 18;

update table juror_mod.t_message_template
set text  = 'Eich Gwasanaeth Rheithgor','Bu ichi fethu â mynychu eich Gwasanaeth Rheithgor yn Llys <welsh_courtname> ar <attend_date>. Cysylltwch â''r Swyddfa Rheithgor drwy ffonio <court_phone>.'
where id = 19;

update table juror_mod.t_message_template
set text  = 'Eich Gwasanaeth Rheithgor','Mae dyddiad mynychu eich Gwasanaeth Rheithgor wedi newid i <attend_date> am <attend_time> yn Llys <welsh_courtname>. Mae''r dyddiau pan na fydd eich angen yn y llys dal yn ffurfio rhan o''ch gwasanaeth rheithgor ac ni fyddant yn cael eu hychwanegu ar y diwedd. Os oes gennych unrhyw gwestiynau, cysylltwch â''r swyddfa rheithgor drwy ffonio <court_phone>.'
where id = 20;

update table juror_mod.t_message_template
set text  = 'Eich Gwasanaeth Rheithgor','Mae angen i chi fynychu''r llys eto ar gyfer eich Gwasanaeth Rheithgor ar <attend_date> am <attend_time> yn Llys <welsh_courtname>. Os oes gennych unrhyw gwestiynau, cysylltwch â''r swyddfa rheithgor drwy ffonio <court_phone>.'
where id = 24;

update table juror_mod.t_message_template
set text  = 'Eich Gwasanaeth Rheithgor','Rydych wedi cael eich dewis i fod yn rhan o banel a bydd Rheithgor yn cael ei ddewis o blith y panel hwnnw. Ewch i Lys <welsh_courtname> ar <attend_date> am <attend_time>. Os oes gennych unrhyw gwestiynau, cysylltwch â''r swyddfa rheithgor drwy ffonio <court_phone>.'
where id = 28;