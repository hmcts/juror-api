DELETE from juror_mod.welsh_court_location
where loc_code in ('002');
DELETE from juror_mod.court_location
    where loc_code in ('001','002','003','004','005');

INSERT INTO juror_mod.court_location
(owner, loc_code, loc_name, loc_address1, loc_address2, loc_address3, loc_address4, loc_address5, loc_address6,
 loc_zip, loc_attend_time, cost_centre, loc_signature, loc_phone)
VALUES ('001', '001', 'COURT1', 'COURT1 ADDRESS1', 'COURT1 ADDRESS2', 'COURT1 ADDRESS3', 'COURT1 ADDRESS4',
        'COURT1 ADDRESS5', 'COURT1 ADDRESS6',
        'AB1 2CD', '09:00:00', 'CSTCNR1', 'COURT1 SIGNATURE', '0123456789'),
       ('001', '002', 'COURT2', 'COURT2 ADDRESS1', 'COURT2 ADDRESS2', 'COURT2 ADDRESS3', 'COURT2 ADDRESS4',
        'COURT2 ADDRESS5', 'COURT2 ADDRESS6',
        'AB2 3CD', '09:15:00', 'CSTCNR2', 'COURT2 SIGNATURE', '0123458888'),
       ('003', '003', 'COURT3', 'COURT3 ADDRESS1', 'COURT3 ADDRESS2', 'COURT3 ADDRESS3', 'COURT3 ADDRESS4',
        'COURT3 ADDRESS5', 'COURT3 ADDRESS6',
        'AB3 4CD', '09:15:00', 'CSTCNR3', 'COURT3 SIGNATURE', '0123458887'),
       ('003', '004', 'COURT4', 'COURT4 ADDRESS1', 'COURT4 ADDRESS2', 'COURT4 ADDRESS3', 'COURT4 ADDRESS4',
        'COURT4 ADDRESS5', 'COURT4 ADDRESS6',
        'AB4 5CD', '09:15:00', 'CSTCNR4', 'COURT4 SIGNATURE', '0123458886'),
       ('005', '005', 'COURT5', 'COURT5 ADDRESS1', 'COURT5 ADDRESS2', 'COURT5 ADDRESS3', 'COURT5 ADDRESS4',
        'COURT5 ADDRESS5', 'COURT5 ADDRESS6',
        'AB5 6CD', '09:15:00', 'CSTCNR5', 'COURT5 SIGNATURE', '0123458885')
    ;


INSERT INTO juror_mod.welsh_court_location(loc_code, loc_name, loc_address1, loc_address2, loc_address3,
                                           loc_address4, loc_address5, loc_address6)
VALUES ('002', 'WELSH_COURT2', 'WELSH_COURT2 ADDRESS1', 'WELSH_COURT2 ADDRESS2', 'WELSH_COURT2 ADDRESS3',
        'WELSH_COURT2 ADDRESS4', 'WELSH_COURT2 ADDRESS5', 'WELSH_COURT2 ADDRESS6');


INSERT INTO juror_mod.courtroom(id, loc_code, room_number, description)
VALUES (999991, '001', 'ROOM1', 'Courtroom 1'),
       (999992, '001', 'ROOM2', 'Courtroom 2'),
       (999993, '002', 'ROOM3', 'Courtroom 3'),
       (999994, '002', 'ROOM4', 'Courtroom 4');


update juror_mod.court_location
set assembly_room = '999991'
where loc_code = '001';
update juror_mod.court_location
set assembly_room = '999993'
where loc_code = '002';