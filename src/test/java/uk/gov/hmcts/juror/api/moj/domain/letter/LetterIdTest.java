package uk.gov.hmcts.juror.api.moj.domain.letter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class LetterIdTest {

    @Test
    public void getJurorNumber() {
        String jurorNumber = "123456789";
        String owner = "415";

        LetterId letterId = new LetterId(owner, jurorNumber);

        assertThat(letterId.getJurorNumber())
            .as("Getter function should return the value of the jurorNumber property")
            .isEqualTo(jurorNumber);
    }

    @Test
    public void getOwner() {
        String jurorNumber = "123456789";
        String owner = "415";

        LetterId letterId = new LetterId(owner, jurorNumber);

        assertThat(letterId.getOwner())
            .as("Getter function should return the value of the owner property")
            .isEqualTo(owner);
    }

    @Test
    public void equals_allPropertiesMatch() {
        String jurorNumber = "123456789";
        String owner = "415";

        LetterId letterIdOne = new LetterId(owner, jurorNumber);
        LetterId letterIdTwo = new LetterId(owner, jurorNumber);

        assertThat(letterIdOne.equals(letterIdTwo))
            .as("Equals function should evaluate the objects as the same")
            .isTrue();
    }

    @Test
    public void equals_differentObjectTypes() {
        String jurorNumber = "123456789";
        String poolNumber = "415221001";
        String owner = "415";

        LetterId letterId = new LetterId(owner, jurorNumber);

        assertThat(letterId.equals(poolNumber))
            .as("Equals function should evaluate the objects as NOT the same (different object types")
            .isFalse();
    }

    @Test
    public void equals_differentJurorNumber() {
        String jurorNumber = "123456789";
        String owner = "415";

        LetterId letterIdOne = new LetterId(owner, jurorNumber);
        LetterId letterIdTwo = new LetterId(owner, "987654321");

        assertThat(letterIdOne.equals(letterIdTwo))
            .as("Equals function should evaluate the objects as NOT the same (different juror numbers)")
            .isFalse();
    }

    @Test
    public void equals_differentOwners() {
        String jurorNumber = "123456789";
        String owner = "415";

        LetterId letterIdOne = new LetterId(owner, jurorNumber);
        LetterId letterIdTwo = new LetterId("400", jurorNumber);

        assertThat(letterIdOne.equals(letterIdTwo))
            .as("Equals function should evaluate the objects as NOT the same (different owners)")
            .isFalse();
    }

    @Test
    public void hashCode_valid() {
        String jurorNumber = "123456789";
        String owner = "415";

        LetterId letterIdOne = new LetterId(owner, jurorNumber);
        LetterId letterIdTwo = new LetterId(owner, jurorNumber);

        assertThat(letterIdOne)
            .as("The same Hash Code should be generated for 2 object instances with the same property values")
            .isEqualTo(letterIdTwo);
    }

}
