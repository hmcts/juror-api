package uk.gov.hmcts.juror.api;

import lombok.extern.slf4j.Slf4j;
import org.ajbrown.namemachine.Gender;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility for generating SQL scripts for test data.
 * <p> Avoids having to produce data manually, especially for tests which require large amounts of test data.
*/
@Slf4j
public class SqlUtil {

    private static final NameGenerator NAME_GENERATOR = new NameGenerator();
    private static final Random RANDOM = new Random();

    private static final List<String> MALE_TITLES = Arrays.asList("Mr", "Dr", "Prof", "Rev");
    private static final List<String> FEMALE_TITLES = Arrays.asList("Ms", "Miss", "Mrs", "Dr", "Prof", "Rev");

    public record Location(String country, String town, String postCode, String phoneAreaCode) {
        public Location {
            if (Arrays.asList(country, town, postCode, phoneAreaCode).parallelStream()
                .anyMatch(s -> s == null || s.trim().isEmpty())) {
                throw new IllegalArgumentException("All arguments must be non-null and non-empty");
            }
            if (postCode.contains(" ")) {
                throw new IllegalArgumentException("Postcode must not contain whitespace");
            }
        }
    }

    /**
     * Generates realistic name for use in test data
     *
     * @param numberOfNames  number of names to generate
     * @param namesMustMatch conditions, if any, that the generated names need to match
     * @return list of names
     */
    public static List<Name> generateNames(final int numberOfNames, final List<Predicate<Name>> namesMustMatch) {
        final List<Name> names = new ArrayList<>(numberOfNames);

        int namesLeftToGenerate = numberOfNames - names.size();

        while (namesLeftToGenerate > 0) {
            Stream<Name> candidateNames = NAME_GENERATOR.generateNames(numberOfNames).stream();
            if (!namesMustMatch.isEmpty()) {
                for (Predicate<Name> filter : namesMustMatch) {
                    candidateNames = candidateNames.filter(filter);
                }
            }
            candidateNames.limit(namesLeftToGenerate).forEach(names::add);
            namesLeftToGenerate = numberOfNames - names.size();
        }

        return names;
    }

    /**
     * Generates SQL for juror responses and matching juror pool data
     *
     * @param targetSqlFile  file to output data to (will overwrite existing contents, if any)
     * @param locations      the geographic locations jurors should be randomly associated with
     * @param numberOfJurors the number of juror entries to generate
     * @param namesMustMatch conditions, if any, that the juror names need to match
     * @throws IOException if unable to write to the file
     */
    public static void generateJurorResponseSql(final String targetSqlFile, final List<Location> locations,
                                                final int numberOfJurors,
                                                final List<Predicate<Name>> namesMustMatch) throws IOException {

        if (Strings.isBlank(targetSqlFile)) {
            throw new IllegalArgumentException("Target filename must be non-null and non-empty");
        }
        if (locations == null || locations.isEmpty()) {
            throw new IllegalArgumentException("Locations must be non-null and non-empty");
        }
        if (numberOfJurors <= 0) {
            throw new IllegalArgumentException("Number of jurors must be a positive integer");
        }
        List<String> staffInserts = new LinkedList<>();
        staffInserts.add("--Staff");

        List<Name> staffNames = generateNames(6, Collections.emptyList());

        List<String> standardUserLogins = new ArrayList<>(3);
        // Generates one 'team member' and one 'team leader' per team
        for (int i = 1;
             i <= staffNames.size();
             i++) {
            final Name name = staffNames.get(i - 1);
            final String fullName = String.format("%s %s", name.getFirstName(), name.getLastName());
            final String login = (name.getFirstName().charAt(0) + name.getLastName()).toLowerCase();
            if (i <= 3) {
                standardUserLogins.add(login);
            }

            staffInserts.add(String.format("INSERT INTO juror_mod.users "
                + "(username, name, level, enabled, team_id, version) "
                + "VALUES ('%s', '%s', %d, %b, %d, %d);", login, fullName,
                i <= 3 ? 0 : 1, true, i > 3 ? i - 3 : i, 1));
        }

        final List<Name> names = generateNames(numberOfJurors, namesMustMatch);

        final List<String> jurorPoolInserts = new LinkedList<>();
        jurorPoolInserts.add("--Juror Pool");
        final List<String> jurorResponseInserts = new LinkedList<>();
        jurorResponseInserts.add("");
        jurorResponseInserts.add("--Juror Response");

        final List<String> statuses = Arrays.asList("TODO", "TODO", "TODO", "TODO", "TODO", "TODO", "TODO", "TODO",
            "TODO", "TODO", "AWAITING_CONTACT", "AWAITING_TRANSLATION", "AWAITING_COURT_REPLY", "AWAITING_CONTACT",
            "CLOSED");

        final List<Character> urgentFlags = Arrays.asList('N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'Y', 'Y');
        final List<Character> superUrgentFlags = Arrays.asList('N', 'N', 'N', 'N', 'N', 'N', 'N', 'Y', 'N', 'Y');

        for (int i = 0;
             i < numberOfJurors;
             i++) {
            final String jurorPoolBaseQuery = "INSERT INTO JUROR.POOL (part_no, fname, lname, h_email, title, dob, " +
                "address, address2, address3, " +
                "address4, zip, h_phone, w_phone, is_active, owner, loc_code, m_phone, responded, poll_number, " +
                "pool_no, on_call, " +
                "completion_flag, read_only, contact_preference, reg_spc, ret_date, status) VALUES (%s, '%s', '%s', " +
                "'%s', '%s', TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS'), '%s', '%s', " +
                "'%s', '%s', '%s', '%s', '%s', '%s', '%d', '%d', '%s', " +
                "'%s', %d, %d, '%s', '%s', '%s', %d, '%s', %s, %d);";
            final String participantNumber = String.format("%09d", i + 111111000);
            final Name name = names.get(i);
            final String firstName = name.getFirstName();
            final String lastName = name.getLastName();
            final String homeEmail = lastName + i + "@email.com";

            // Give the juror a title that matches the gender of generated name
            final String title = name.getGender() == Gender.FEMALE
                ?
                FEMALE_TITLES.get(RANDOM.nextInt(FEMALE_TITLES.size()))
                :
                    MALE_TITLES.get(RANDOM.nextInt(MALE_TITLES.size()));
            final String dateOfBirth = String.format("%02d-%02d-%02d 00:00:01", RANDOM.nextInt(36) + 1960,
                RANDOM.nextInt(12) + 1, RANDOM.nextInt(28) + 1);
            final String street = String.format("%d Test Street", i + 1);

            // So that the postcode and country match the town
            final Location location = locations.get(RANDOM.nextInt(locations.size()));
            final String country = location.country();
            final String town = location.town();
            final String postCode = location.postCode();

            final String areaCode = location.phoneAreaCode();

            final int ext = i + 1110;
            final String homePhone = String.format("44%s101-%04d", areaCode, ext);
            final String workPhone = String.format("44%s201-%04d", areaCode, ext);
            final String mobilePhone = String.format("44776-301-%04d", ext);

            final String status = statuses.get(RANDOM.nextInt(statuses.size()));

            final String assignedStaffMember = standardUserLogins.get(RANDOM.nextInt(standardUserLogins.size()));

            final int urgencySeed = RANDOM.nextInt(urgentFlags.size());
            final Character urgent = urgentFlags.get(urgencySeed);
            final Character superUrgent = superUrgentFlags.get(urgencySeed);

            final String jurorPoolInsert = String.format(jurorPoolBaseQuery,participantNumber,firstName,lastName,homeEmail,title,dateOfBirth,
                    street,country,town,"United Kingdom",postCode,homePhone,workPhone,"Y",400,448,mobilePhone,"N",i,101,"N","N","N",0,"N"," CURRENT_DATE",1);
            jurorPoolInserts.add(jurorPoolInsert);
            final String jurorResponseBaseQuery = "INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (" +
                "JUROR_NUMBER,DATE_RECEIVED,TITLE,FIRST_NAME,LAST_NAME,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4," +
                "ADDRESS5,ADDRESS6,ZIP,PROCESSING_STATUS,DATE_OF_BIRTH,PHONE_NUMBER,ALT_PHONE_NUMBER,EMAIL," +
                "THIRDPARTY_REASON,RESIDENCY,RESIDENCY_DETAIL,MENTAL_HEALTH_ACT,MENTAL_HEALTH_ACT_DETAILS," +
                "BAIL,BAIL_DETAILS,CONVICTIONS,CONVICTIONS_DETAILS,DEFERRAL_REASON,DEFERRAL_DATE," +
                "SPECIAL_NEEDS_ARRANGEMENTS," +
                "EXCUSAL_REASON, VERSION, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS) " +
                "VALUES (%s,%s,'%s','%s','%s','%s','%s','%s','%s',null,null,'%s','TODO',null,null,null,null,null,'0'," +
                "null,'0',null,'0',null,'0',null,null,null,null,null,555,'Y', 'Y');";
            final String jurorResponseWithStaffBaseQuery = "INSERT INTO JUROR_DIGITAL.JUROR_RESPONSE (" +
                    "JUROR_NUMBER,DATE_RECEIVED,TITLE,FIRST_NAME,LAST_NAME,ADDRESS,ADDRESS2,ADDRESS3,ADDRESS4," +
                    "ADDRESS5,ADDRESS6,ZIP,PROCESSING_STATUS,DATE_OF_BIRTH,PHONE_NUMBER,ALT_PHONE_NUMBER,EMAIL," +
                    "THIRDPARTY_REASON,RESIDENCY,RESIDENCY_DETAIL,MENTAL_HEALTH_ACT,MENTAL_HEALTH_ACT_DETAILS," +
                    "BAIL,BAIL_DETAILS,CONVICTIONS,CONVICTIONS_DETAILS,DEFERRAL_REASON,DEFERRAL_DATE,SPECIAL_NEEDS_ARRANGEMENTS," +
                    "EXCUSAL_REASON, VERSION, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT) " +
                    "VALUES (%s,%s,'%s','%s','%s','%s','%s','%s','%s',null,null,'%s','%s',null,null,null,null,null,'0',null,'0',null,'0',null,'0',null,null,null,null,null,555,'Y', 'Y', '%s' , CURRENT_DATE, '%s', '%s');";

            final String dateReceived = String.format("(SELECT CURRENT_DATE - interval '%d' minute)",i);
            final String jurorResponse = assignedStaffMember == null ? String.format(jurorResponseBaseQuery,participantNumber,dateReceived, title, firstName, lastName, street, country, town, "United Kingdom", postCode) :
                    String.format(jurorResponseWithStaffBaseQuery,participantNumber,dateReceived, title, firstName, lastName, street, country, town, "United Kingdom", postCode, status, assignedStaffMember, urgent, superUrgent);

            jurorResponseInserts.add(jurorResponse);

        }

        final List<String> inserts = new LinkedList<>();

        // Order matters because of foreign keys
        inserts.addAll(staffInserts);
        inserts.addAll(jurorPoolInserts);
        inserts.addAll(jurorResponseInserts);

        Path out = Paths.get(targetSqlFile);

        Files.write(out, inserts, Charset.defaultCharset());

    }
}
