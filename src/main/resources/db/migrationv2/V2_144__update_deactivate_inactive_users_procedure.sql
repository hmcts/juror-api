CREATE OR REPLACE PROCEDURE juror_mod.deactivate_inactive_users(
    IN p_inactivity_months INT DEFAULT 6
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_revision          BIGINT;
    v_timestamp         BIGINT;
    v_deactivated_count INT;
    v_undatable_count   INT;
    v_cutoff            TIMESTAMPTZ;
    v_cutoff_ms         BIGINT;
BEGIN
    IF p_inactivity_months < 6 THEN
        RAISE EXCEPTION
            'p_inactivity_months (%) is below the minimum allowed threshold of 6 months', p_inactivity_months;
    END IF;

    v_cutoff    := CURRENT_TIMESTAMP - make_interval(months => p_inactivity_months);
    v_cutoff_ms := (EXTRACT(EPOCH FROM v_cutoff) * 1000)::BIGINT;  -- rev_info timestamps are epoch millis

    CREATE TEMP TABLE tmp_deactivated ON COMMIT DROP AS
    WITH updated AS (
        UPDATE juror_mod.users u
        SET active = false,
            updated_by = 'system'
        WHERE u.active = true
          AND (
                -- Has logged in, but not recently
                u.last_logged_in < v_cutoff
                OR (
                    -- Never logged in: use the earliest audit revision (ADD if present, otherwise
                    -- the first MOD) as proof the account has existed for longer than the threshold.
                    -- MIN() is NULL when there are no audit rows, so those accounts do not match.
                    u.last_logged_in IS NULL
                    AND (
                        SELECT MIN(ri.revision_timestamp)
                        FROM juror_mod.users_audit ua
                        JOIN juror_mod.rev_info ri ON ri.revision_number = ua.revision
                        WHERE ua.username = u.username
                    ) < v_cutoff_ms
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

    -- Active, never logged in, and no audit rows at all: nothing to date them by, so they are left alone.
    SELECT COUNT(*) INTO v_undatable_count
    FROM juror_mod.users u
    WHERE u.active = true
      AND u.last_logged_in IS NULL
      AND NOT EXISTS (SELECT 1 FROM juror_mod.users_audit ua WHERE ua.username = u.username);

    IF v_undatable_count > 0 THEN
        RAISE WARNING 'juror_mod.deactivate_inactive_users: % active user(s) have no last_logged_in and no audit history, so cannot be aged and were skipped',
            v_undatable_count;
    END IF;

    RAISE NOTICE 'juror_mod.deactivate_inactive_users: deactivated % user(s) inactive for over % months%',
        v_deactivated_count, p_inactivity_months,
        CASE WHEN v_deactivated_count > 0 THEN format(' (revision %s)', v_revision) ELSE '' END;
END;
$$;
