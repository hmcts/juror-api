CREATE OR REPLACE FUNCTION update_bulk_print_data()
RETURNS TRIGGER AS $$
BEGIN
    -- Redirect the update to the underlying table
    UPDATE bulk_print_data
    SET digital_comms = NEW.digital_comms
    WHERE juror_no = NEW.juror_no
      AND id = NEW.id
      AND creation_date = NEW.creation_date;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER instead_of_update_bulkprintdatanotifycommsview
INSTEAD OF UPDATE ON juror_mod.bulk_print_data_notify_comms
FOR EACH ROW
EXECUTE FUNCTION update_bulk_print_data();
