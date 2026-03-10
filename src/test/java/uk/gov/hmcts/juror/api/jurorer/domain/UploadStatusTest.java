package uk.gov.hmcts.juror.api.jurorer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class UploadStatusTest {

    @Test
    void values() {
        UploadStatus[] expected = {UploadStatus.NOT_UPLOADED, UploadStatus.UPLOADED};
            assertArrayEquals(expected, UploadStatus.values());
    }

    @Test
    void valueOf() {
        assertEquals(UploadStatus.NOT_UPLOADED, UploadStatus.valueOf("NOT_UPLOADED"));
        assertEquals(UploadStatus.UPLOADED, UploadStatus.valueOf("UPLOADED"));
    }
}
