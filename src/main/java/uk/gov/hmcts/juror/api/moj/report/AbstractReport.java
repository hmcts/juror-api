package uk.gov.hmcts.juror.api.moj.report;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Getter;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.TooManyMethods"
})
public abstract class AbstractReport {
    static final Map<EntityPath<?>, Map<EntityPath<?>, Predicate[]>> CLASS_TO_JOIN;

    static {
        CLASS_TO_JOIN = new ConcurrentHashMap<>();
        CLASS_TO_JOIN.put(QJuror.juror, Map.of(
            QJurorPool.jurorPool, new Predicate[]{QJuror.juror.eq(QJurorPool.jurorPool.juror)},
            QAppearance.appearance, new Predicate[]{QJuror.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber)}
        ));
        CLASS_TO_JOIN.put(QJurorPool.jurorPool, Map.of(
            QJuror.juror, new Predicate[]{QJurorPool.jurorPool.juror.eq(QJuror.juror)}
        ));
        CLASS_TO_JOIN.put(QAppearance.appearance, Map.of(
            QJuror.juror, new Predicate[]{QAppearance.appearance.jurorNumber.eq(QJuror.juror.jurorNumber)},
            QJurorPool.jurorPool, new Predicate[]{
                QJurorPool.jurorPool.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber),
                QJurorPool.jurorPool.pool.poolNumber.eq(QAppearance.appearance.poolNumber)
            }
        ));
    }

    @PersistenceContext
    private EntityManager entityManager;

    final PoolRequestRepository poolRequestRepository;
    final List<DataType> dataTypes;
    final Set<EntityPath<?>> requiredTables;
    final List<DataType> effectiveDataTypes;
    final EntityPath<?> from;

    public AbstractReport(PoolRequestRepository poolRequestRepository, EntityPath<?> from, DataType... dataType) {
        this.poolRequestRepository = poolRequestRepository;
        this.from = from;
        this.dataTypes = List.of(dataType);
        this.effectiveDataTypes = dataTypes.stream()
            .map(this::getDataType)
            .flatMap(List::stream)
            .toList();
        this.requiredTables = effectiveDataTypes.stream()
            .map(DataType::getRequiredTables)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    }

    public abstract Class<?> getRequestValidatorClass();

    public final String getName() {
        return getClass().getSimpleName();
    }

    public StandardReportResponse getStandardReportResponse(StandardReportRequest request) {
        List<Tuple> data = getData(request);
        StandardReportResponse.TableData tableData = tupleToTableData(data);

        StandardReportResponse standardReportResponse = new StandardReportResponse();
        Map<String, StandardReportResponse.DataTypeValue> headings =
            new ConcurrentHashMap<>(getHeadings(request, tableData));
        headings.put("report_created", StandardReportResponse.DataTypeValue.builder()
            .value(DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()))
            .dataType(LocalDateTime.class.getSimpleName())
            .build());
        standardReportResponse.setHeadings(headings);
        standardReportResponse.setTableData(tableData);
        return standardReportResponse;
    }

    StandardReportResponse.TableData tupleToTableData(List<Tuple> data) {
        StandardReportResponse.TableData tableData = new StandardReportResponse.TableData();

        tableData.setHeadings(dataTypes.stream()
            .map(this::getHeading).toList());
        tableData.setData(data.stream()
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

    StandardReportResponse.TableData.Heading getHeading(DataType dataType) {
        StandardReportResponse.TableData.Heading heading = StandardReportResponse.TableData.Heading.builder()
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

    public Map.Entry<String, Object> getDataFromReturnType(Tuple tuple, DataType dataType) {
        Object value;
        if (dataType.getReturnTypes() == null) {
            value = tuple.get(dataType.getExpression());

            if (value != null) {
                if (value instanceof LocalDate localDate) {
                    value = DateTimeFormatter.ISO_DATE.format(localDate);
                } else if (value instanceof LocalTime localTime) {
                    value = DateTimeFormatter.ISO_TIME.format(localTime);
                } else if (value instanceof LocalDateTime localDateTime) {
                    value = DateTimeFormatter.ISO_DATE_TIME.format(localDateTime);
                }
            }
        } else {
            Map<String, Object> data = new LinkedHashMap<>();
            for (DataType subType : dataType.getReturnTypes()) {
                Map.Entry<String, Object> valueEntry = getDataFromReturnType(tuple, subType);
                if (valueEntry.getValue() != null) {
                    data.put(valueEntry.getKey(), valueEntry.getValue());
                }
            }
            value = data;
        }
        return new AbstractMap.SimpleEntry<>(dataType.getId(), value);
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
                .map(DataType::getExpression)
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
                query.join(requiredTable).on(joinOptions.get(from));
            } else {
                throw new MojException.InternalServerError(
                    "Not Implemented yet: " + requiredTable + " from " + from.getClass(), null);
            }
        });
    }

    List<DataType> getDataType(DataType dataType) {
        List<DataType> data = new ArrayList<>();
        if (dataType.getReturnTypes() == null) {
            data.add(dataType);
        } else {
            for (DataType subType : dataType.getReturnTypes()) {
                data.addAll(getDataType(subType));
            }
        }
        return data;
    }

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    public void addGroupBy(JPAQuery<Tuple> query, DataType... dataTypes) {
        query.groupBy(Arrays.stream(dataTypes)
            .map(this::getDataType)
            .flatMap(List::stream)
            .map(DataType::getExpression)
            .toArray(Expression[]::new));
    }

    protected abstract void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request);

    public abstract Map<String, StandardReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        StandardReportResponse.TableData tableData);

    void checkOwnership(PoolRequest poolRequest, boolean allowBureau) {
        if (!poolRequest.getOwner().equals(SecurityUtil.getActiveOwner())
            && !(SecurityUtil.isBureau() && allowBureau)) {
            throw new MojException.Forbidden("User not allowed to access this pool", null);
        }
    }

    protected ConcurrentHashMap<String, StandardReportResponse.DataTypeValue> loadStandardPoolHeaders(
        StandardReportRequest request, boolean ownerMustMatch, boolean allowBureau) {
        PoolRequest poolRequest = getPoolRequest(request.getPoolNumber());
        if (ownerMustMatch) {
            checkOwnership(poolRequest, allowBureau);
        }
        return new ConcurrentHashMap<>(Map.of(
            "pool_number", StandardReportResponse.DataTypeValue.builder()
                .displayName("Pool Number")
                .dataType(String.class.getSimpleName())
                .value(request.getPoolNumber())
                .build(),
            "pool_type", StandardReportResponse.DataTypeValue.builder()
                .displayName("Pool Type")
                .dataType(String.class.getSimpleName())
                .value(poolRequest.getPoolType().getDescription())
                .build(),
            "service_start_date", StandardReportResponse.DataTypeValue.builder()
                .displayName("Service Start Date")
                .dataType(LocalDate.class.getSimpleName())
                .value(DateTimeFormatter.ISO_DATE.format(poolRequest.getReturnDate()))
                .build(),
            "court_name", StandardReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType(String.class.getSimpleName())
                .value(
                    poolRequest.getCourtLocation().getName() + " (" + poolRequest.getCourtLocation().getLocCode() + ")")
                .build()
        ));
    }

    PoolRequest getPoolRequest(String poolNumber) {
        Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber(poolNumber);
        if (poolRequest.isEmpty()) {
            throw new MojException.NotFound("Pool not found", null);
        }
        return poolRequest.get();
    }

    public static class Validators {
        public interface AbstractRequestValidator {

        }

        public interface RequirePoolNumber {

        }
    }
}
