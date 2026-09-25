CREATE OR REPLACE PROCEDURE juror_mod.deactivate_inactive_users(
    IN p_inactivity_months INT DEFAULT 6
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_revision          BIGINT;
    v_timestamp         BIGINT;
    v_deactivated_count INT;
BEGIN
    CREATE TEMP TABLE tmp_deactivated ON COMMIT DROP AS
    WITH updated AS (
        UPDATE juror_mod.users u
        SET active = false,
            updated_by = 'system'
        WHERE u.active = true
          AND (
                u.last_logged_in < (CURRENT_TIMESTAMP - (p_inactivity_months || ' months')::interval)
                OR (
                    u.last_logged_in IS NULL
                    AND EXISTS (
                        SELECT 1
                        FROM juror_mod.users_audit ua
                        JOIN juror_mod.rev_info ri ON ri.revision_number = ua.revision
                        WHERE ua.username = u.username
                          AND ua.rev_type = 0   -- Envers RevisionType.ADD
                          AND to_timestamp(ri.revision_timestamp / 1000.0)
                              < (CURRENT_TIMESTAMP - (p_inactivity_months || ' months')::interval)
                    )
                )
              )
        RETURNING username, name, active, approval_limit, user_type, email, created_by, updated_by
    )
    SELECT * FROM updated;

    SELECT COUNT(*) INTO v_deactivated_count FROM tmp_deactivated;

    IF v_deactivated_count > 0 THEN
        v_revision  := nextval('juror_mod.rev_info_seq');
        v_timestamp := (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000)::BIGINT;

        INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp, changed_by)
        VALUES (v_revision, v_timestamp, 'system');

        INSERT INTO juror_mod.users_audit
            (revision, rev_type, owner, username, name, active, approval_limit,
             user_type, email, created_by, updated_by)
        SELECT
            v_revision, 1, NULL,
            username, name, active, approval_limit, user_type, email, created_by, updated_by
        FROM tmp_deactivated;
    END IF;

    RAISE NOTICE 'juror_mod.deactivate_inactive_users: deactivated % user(s) inactive for over % months%',
        v_deactivated_count, p_inactivity_months,
        CASE WHEN v_deactivated_count > 0 THEN format(' (revision %s)', v_revision) ELSE '' END;
END;
$$;
