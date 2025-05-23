package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.JoinType;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPendingJuror;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.service.report.IReport;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts;

@Getter
@Slf4j
@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports"
})
public abstract class AbstractReport<T> implements IReport {
    static final Map<EntityPath<?>, Map<EntityPath<?>, Predicate[]>> CLASS_TO_JOIN;


    static {
        CLASS_TO_JOIN = new ConcurrentHashMap<>();
        CLASS_TO_JOIN.put(QJuror.juror, Map.of(
            QJurorPool.jurorPool, new Predicate[]{QJuror.juror.eq(QJurorPool.jurorPool.juror)},
            QAppearance.appearance, new Predicate[]{QJuror.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber)},
            QPanel.panel, new Predicate[]{QPanel.panel.juror.eq(QJuror.juror)},
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts,
            new Predicate[]{
                lowLevelFinancialAuditDetailsIncludingApprovedAmounts
                    .jurorNumber.eq(QJuror.juror.jurorNumber)
            },
            QPendingJuror.pendingJuror, new Predicate[]{
                QPendingJuror.pendingJuror.jurorNumber.eq(QJuror.juror.jurorNumber)
            }
        ));
        CLASS_TO_JOIN.put(QJurorPool.jurorPool, Map.of(
            QJuror.juror, new Predicate[]{QJurorPool.jurorPool.juror.eq(QJuror.juror)},
            QPanel.panel, new Predicate[]{QPanel.panel.juror.eq(QJurorPool.jurorPool.juror)}
        ));
        CLASS_TO_JOIN.put(QPoolRequest.poolRequest, Map.of(
            QJurorPool.jurorPool,
            new Predicate[]{QPoolRequest.poolRequest.poolNumber.eq(QJurorPool.jurorPool.pool.poolNumber)},
            QAppearance.appearance,
            new Predicate[]{QPoolRequest.poolRequest.poolNumber.eq(QAppearance.appearance.poolNumber)}
        ));
        CLASS_TO_JOIN.put(QAppearance.appearance, Map.of(
            QJuror.juror, new Predicate[]{QAppearance.appearance.jurorNumber.eq(QJuror.juror.jurorNumber)},
            QJurorPool.jurorPool, new Predicate[]{
                QJurorPool.jurorPool.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber),
                QJurorPool.jurorPool.pool.poolNumber.eq(QAppearance.appearance.poolNumber)
            }
        ));
        CLASS_TO_JOIN.put(QPanel.panel, Map.of(
            QJuror.juror, new Predicate[]{
                QPanel.panel.juror.eq(QJuror.juror)
            }
        ));
        CLASS_TO_JOIN.put(QReasonableAdjustments.reasonableAdjustments, Map.of(
            QJuror.juror,
            new Predicate[]{QReasonableAdjustments.reasonableAdjustments.code.eq(
                QJuror.juror.reasonableAdjustmentCode)},
            QJurorPool.jurorPool,
            new Predicate[]{QReasonableAdjustments.reasonableAdjustments.code.eq(
                QJurorPool.jurorPool.juror.reasonableAdjustmentCode)}
        ));
    }

    @PersistenceContext
    private EntityManager entityManager;

    final PoolRequestRepository poolRequestRepository;
    final List<IDataType> dataTypes;
    final Set<EntityPath<?>> requiredTables;
    final List<IDataType> effectiveDataTypes;
    final EntityPath<?> from;
    final Map<EntityPath<?>, Map<EntityPath<?>, JoinOverrideDetails>> classToJoinOverrides = new HashMap<>();

    final List<Consumer<StandardReportRequest>> authenticationConsumers;

    public AbstractReport(EntityPath<?> from, IDataType... dataType) {
        this(null, from, dataType);
    }

    public AbstractReport(PoolRequestRepository poolRequestRepository,
                          EntityPath<?> from, IDataType... dataType) {
        this.poolRequestRepository = poolRequestRepository;
        this.from = from;
        this.dataTypes = List.of(dataType);
        this.effectiveDataTypes = dataTypes.stream()
            .map(this::getDataType)
            .flatMap(List::stream)
            .toList();
        this.requiredTables = effectiveDataTypes.stream()
            .map(IDataType::getRequiredTables)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
        this.authenticationConsumers = new ArrayList<>();
    }

    @Builder
    @Getter
    public static class JoinOverrideDetails {
        private EntityPath<?> from;
        private EntityPath<?> to;
        private JoinType joinType;
        private List<Predicate> predicatesToAdd;
        private List<Predicate> predicatesOverride;
    }

    public void addJoinOverride(JoinOverrideDetails joinDetails) {
        if (!classToJoinOverrides.containsKey(joinDetails.getFrom())) {
            classToJoinOverrides.put(joinDetails.getFrom(), new HashMap<>());
        }
        classToJoinOverrides.get(joinDetails.getFrom()).put(joinDetails.getTo(), joinDetails);
    }

    public void addAuthenticationConsumer(Consumer<StandardReportRequest> consumer) {
        authenticationConsumers.add(consumer);
    }

    public void isCourtUserOnly() {
        addAuthenticationConsumer(request -> {
            if (!SecurityUtil.isCourt()) {
                throw new MojException.Forbidden("User not allowed to access this report", null);
            }
        });
    }

    public void isBureauUserOnly() {
        addAuthenticationConsumer(request -> {
            if (!SecurityUtil.isBureau()) {
                throw new MojException.Forbidden("User not allowed to access this report", null);
            }
        });
    }

    public void isSeniorJurorOfficerOnly() {
        addAuthenticationConsumer(request -> {
            if (!SecurityUtil.hasRole(Role.SENIOR_JUROR_OFFICER)) {
                throw new MojException.Forbidden("User not allowed to access this report", null);
            }
        });
    }

    public abstract Class<? extends Validators.AbstractRequestValidator> getRequestValidatorClass();

    @Override
    public final Class<?> getRequestValidatorClass(StandardReportRequest standardReportRequest) {
        return getRequestValidatorClass();
    }

    public AbstractReportResponse<T> getStandardReportResponse(StandardReportRequest request) {
        authenticationConsumers.forEach(consumer -> consumer.accept(request));
        List<Tuple> data = getData(request);
        AbstractReportResponse.TableData<T> tableData = tupleToTableData(data);

        postProcessTableData(request, tableData);

        AbstractReportResponse<T> report = createBlankResponse();
        Map<String, AbstractReportResponse.DataTypeValue> headings =
            new ConcurrentHashMap<>(getHeadings(request, tableData));
        headings.put("report_created", AbstractReportResponse.DataTypeValue.builder()
            .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
            .dataType(LocalDateTime.class.getSimpleName())
            .build());
        report.setHeadings(headings);
        report.setTableData(tableData);
        return report;
    }


    protected void postProcessTableData(StandardReportRequest request, AbstractReportResponse.TableData<T> tableData) {
        //This method does noting unless overridden
    }

    protected abstract AbstractReportResponse<T> createBlankResponse();

    AbstractReportResponse.TableData<T> tupleToTableData(List<Tuple> data) {
        AbstractReportResponse.TableData<T> tableData =
            new AbstractReportResponse.TableData<>();
        tableData.setHeadings(new ArrayList<>(dataTypes.stream()
            .map(this::getHeading).toList()));
        tableData.setData(getTableData(data));
        return tableData;
    }

    protected abstract T getTableData(List<Tuple> data);


    protected AbstractReportResponse.TableData.Heading getHeading(IDataType dataType) {
        AbstractReportResponse.TableData.Heading heading = AbstractReportResponse.TableData.Heading.builder()
            .id(dataType.getId())
            .name(dataType.getDisplayName())
            .dataType(dataType.getDataType().getSimpleName())
            .build();
        if (dataType.getReturnTypes() != null) {
            heading.setHeadings(Arrays.stream(dataType.getReturnTypes())
                .map(this::getHeading)
                .toList());
        }
        return heading;
    }

    public Map.Entry<String, Object> getDataFromReturnType(Tuple tuple, IDataType dataType) {
        Object value;
        if (dataType.getReturnTypes() == null) {
            value = getSimpleValue(tuple, dataType);
        } else {
            value = getComplexValue(tuple, dataType);
        }
        return new AbstractMap.SimpleEntry<>(dataType.getId(), value);
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private Object getComplexValue(Tuple tuple, IDataType dataType) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (IDataType subType : dataType.getReturnTypes()) {
            Map.Entry<String, Object> valueEntry = getDataFromReturnType(tuple, subType);
            if (valueEntry.getValue() != null) {
                data.put(valueEntry.getKey(), valueEntry.getValue());
            }
        }
        return data;
    }

    private Object getSimpleValue(Tuple tuple, IDataType dataType) {
        Object value = tuple.get(dataType.getExpression());

        if (value != null) {
            if (value instanceof LocalDate localDate) {
                value = DateTimeFormatter.ISO_DATE.format(localDate);
            } else if (value instanceof LocalTime localTime) {
                value = DateTimeFormatter.ISO_TIME.format(localTime);
            } else if (value instanceof LocalDateTime localDateTime) {
                value = DateTimeFormatter.ISO_DATE_TIME.format(localDateTime);
            }
        }
        return value;
    }

    List<Tuple> getData(StandardReportRequest request) {
        JPAQuery<Tuple> query = getQuery();
        addJoins(query);
        preProcessQuery(query, request);
        return query.fetch();
    }

    JPAQuery<Tuple> getQuery() {
        return getQueryFactory()
            .select(effectiveDataTypes.stream()
                .map(IDataType::getExpression)
                .toArray(Expression[]::new)).from(from);
    }

    void addJoins(JPAQuery<Tuple> query) {
        requiredTables.forEach(requiredTable -> {
            if (from.equals(requiredTable)) {
                return;
            }
            if (!CLASS_TO_JOIN.containsKey(requiredTable)) {
                throw new MojException.InternalServerError("No join found for " + requiredTable, null);
            }
            Map<EntityPath<?>, Predicate[]> joinOptions = CLASS_TO_JOIN.get(requiredTable);

            if (joinOptions.containsKey(from)) {
                JoinOverrideDetails joinOverrideDetails = JoinOverrideDetails.builder().joinType(JoinType.DEFAULT)
                    .build();
                log.info("Searching for join overrides from: {} to {}", from, requiredTable);
                if (classToJoinOverrides.containsKey(from) && classToJoinOverrides.get(from)
                    .containsKey(requiredTable)) {
                    log.info("Join override found");
                    joinOverrideDetails = classToJoinOverrides.get(from).get(requiredTable);
                }

                final ArrayList<Predicate> predicates;
                if (joinOverrideDetails.getPredicatesOverride() != null) {
                    predicates = new ArrayList<>(joinOverrideDetails.getPredicatesOverride());
                } else {
                    predicates = new ArrayList<>(Arrays.asList(joinOptions.get(from)));
                }

                if (joinOverrideDetails.getPredicatesToAdd() != null) {
                    predicates.addAll(joinOverrideDetails.getPredicatesToAdd());
                }

                log.info("Joining {} using {} on {}", requiredTable, joinOverrideDetails.getJoinType(), predicates);
                switch (joinOverrideDetails.getJoinType()) {
                    case DEFAULT:
                    case FULLJOIN:
                        query.join(requiredTable).on(predicates.toArray(new Predicate[0]));
                        break;
                    case LEFTJOIN:
                        query.leftJoin(requiredTable).on(predicates.toArray(new Predicate[0]));
                        break;
                    case RIGHTJOIN:
                        query.rightJoin(requiredTable).on(predicates.toArray(new Predicate[0]));
                        break;
                    case INNERJOIN:
                        query.innerJoin(requiredTable).on(predicates.toArray(new Predicate[0]));
                        break;
                    default:
                        throw new MojException.InternalServerError(
                            "Join type not supported: " + joinOverrideDetails.getJoinType(), null);
                }
            } else {
                throw new MojException.InternalServerError(
                    "Not Implemented yet: " + requiredTable.getClass() + " from " + from.getClass(), null);
            }
        });
    }

    List<IDataType> getDataType(IDataType dataType) {
        List<IDataType> data = new ArrayList<>();
        if (dataType.getReturnTypes() == null) {
            data.add(dataType);
        } else {
            for (IDataType subType : dataType.getReturnTypes()) {
                data.addAll(getDataType(subType));
            }
        }
        return data;
    }

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    public void addGroupBy(JPAQuery<Tuple> query, IDataType... dataTypes) {
        query.groupBy(Arrays.stream(dataTypes)
            .map(this::getDataType)
            .flatMap(List::stream)
            .map(IDataType::getExpression)
            .toArray(Expression[]::new));
    }

    protected abstract void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request);

    public abstract Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<T> tableData);

    protected void checkOwnership(PoolRequest poolRequest, boolean allowBureau) {
        if (!poolRequest.getOwner().equals(SecurityUtil.getActiveOwner())
            && !(SecurityUtil.isBureau() && allowBureau)) {
            throw new MojException.Forbidden("User not allowed to access this pool", null);
        }
    }

    protected void checkOwnership(String locCode, boolean allowBureau) {
        if (!SecurityUtil.getCourts().contains(locCode)
            && !(SecurityUtil.isBureau() && allowBureau)) {
            throw new MojException.Forbidden("User not allowed to access this court", null);
        }
    }


    public Map.Entry<String, AbstractReportResponse.DataTypeValue> getCourtNameHeader(CourtLocation courtLocation) {
        return new AbstractMap.SimpleEntry<>("court_name", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Court Name")
            .dataType(String.class.getSimpleName())
            .value(courtLocation.getNameWithLocCode())
            .build());
    }


    public ConcurrentHashMap<String, AbstractReportResponse.DataTypeValue> loadStandardPoolHeaders(
        StandardReportRequest request, boolean ownerMustMatch, boolean allowBureau) {

        PoolRequest poolRequest = getPoolRequest(request.getPoolNumber());
        if (ownerMustMatch) {
            checkOwnership(poolRequest, allowBureau);
        }
        return new ConcurrentHashMap<>(Map.of(
            "pool_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Pool Number")
                .dataType(String.class.getSimpleName())
                .value(request.getPoolNumber())
                .build(),
            "pool_type", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Pool Type")
                .dataType(String.class.getSimpleName())
                .value(poolRequest.getPoolType().getDescription())
                .build(),
            "service_start_date", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Service Start Date")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(poolRequest.getReturnDate()))
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType(String.class.getSimpleName())
                .value(getCourtNameString(poolRequest.getCourtLocation()))
                .build()
        ));
    }

    public Map<String, AbstractReportResponse.DataTypeValue> loadStandardTrialHeaders(
        StandardReportRequest request, TrialRepository trialRepository) {
        return loadStandardTrailHeaders(request, trialRepository, false);
    }

    public Map<String, AbstractReportResponse.DataTypeValue> loadStandardTrailHeaders(
        StandardReportRequest request, TrialRepository trialRepository, boolean addTrialStartDate) {

        Trial trial = getTrial(request.getTrialNumber(), trialRepository);
        Map<String, AbstractReportResponse.DataTypeValue> trialHeaders = new HashMap<>(Map.of(
            "trial_number", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Trial Number")
                .dataType(String.class.getSimpleName())
                .value(request.getTrialNumber())
                .build(),
            "names", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Names")
                .dataType(String.class.getSimpleName())
                .value(trial.getDescription())
                .build(),
            "court_room", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court Room")
                .dataType(String.class.getSimpleName())
                .value(trial.getCourtroom().getDescription())
                .build(),
            "judge", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Judge")
                .dataType(String.class.getSimpleName())
                .value(trial.getJudge().getName())
                .build(),
            "court_name", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType(String.class.getSimpleName())
                .value(
                    getCourtNameString(trial.getCourtLocation()))
                .build()
        ));

        if (addTrialStartDate) {
            trialHeaders.put("trial_start_date", AbstractReportResponse.DataTypeValue.builder()
                .displayName("Trial Start Date")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(trial.getTrialStartDate()))
                .build());
        }

        return trialHeaders;
    }


    public void addCourtNameHeader(Map<String, AbstractReportResponse.DataTypeValue> headings,
                                   CourtLocation courtLocation) {
        Map.Entry<String, GroupedReportResponse.DataTypeValue> entry = getCourtNameHeader(courtLocation);
        headings.put(entry.getKey(), entry.getValue());
    }

    protected String getCourtNameString(CourtLocationRepository courtLocationRepository, String locCode) {
        Optional<CourtLocation> courtLocation = courtLocationRepository.findByLocCode(locCode);
        if (courtLocation.isEmpty()) {
            throw new MojException.NotFound("Court not found", null);
        }
        return getCourtNameString(courtLocation.get());
    }

    public String getCourtNameString(CourtLocation courtLocation) {
        return courtLocation.getName() + " (" + courtLocation.getLocCode() + ")";
    }

    public PoolRequest getPoolRequest(String poolNumber) {
        Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber(poolNumber);
        if (poolRequest.isEmpty()) {
            throw new MojException.NotFound("Pool not found", null);
        }
        return poolRequest.get();
    }

    public Trial getTrial(String trialNumber, TrialRepository trialRepository) {
        Optional<Trial> trial = trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber,
            SecurityUtil.getLocCode());
        if (trial.isEmpty()) {
            throw new MojException.NotFound("Trial not found", null);
        }
        return trial.get();
    }

    protected StandardTableData getTableDataAsList(List<Tuple> data) {
        StandardTableData tableData = new StandardTableData();
        tableData.addAll(data.stream()
            .map(tuple -> dataTypes
                .stream()
                .map(dataType -> getDataFromReturnType(tuple, dataType))
                .filter(entry -> {
                    Object value = entry.getValue();
                    return value != null && !(value instanceof Map && ((Map<?, ?>) value).isEmpty());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new))
            ).toList());
        return tableData;
    }


    public static class Validators {
        public interface AbstractRequestValidator {

        }

        public interface RequirePoolNumber {

        }

        public interface RequireTrialNumber {

        }

        public interface RequireFromDate {
        }

        public interface RequireToDate {
        }

        public interface RequireLocCode {
        }

        public interface RequireDate {
        }

        public interface RequireIncludeSummoned {
        }

        public interface RequiredJurorNumber {
        }

        public interface RequireRespondedJurorsOnly {
        }

        public interface RequireIncludePanelMembers {
        }

        public interface RequireIncludeJurorsOnCall {
        }

        public interface RequireJuryAuditNumber {
        }

        public interface RequirePoolAuditNumber {
        }

        public interface RequireCourts {
        }

        public interface RequireFilterOwnedDeferrals {
        }
    }
}
