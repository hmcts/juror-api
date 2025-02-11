-- add new column to capture date when reassigning jurors between locations
-- owned by same primary court
alter table juror_mod.juror_pool add column reassign_date date null;
