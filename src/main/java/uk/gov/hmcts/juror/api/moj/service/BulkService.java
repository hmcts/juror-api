package uk.gov.hmcts.juror.api.moj.service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BulkService {
    <I, O> List<O> process(List<I> request, Function<I, O> function);

    <I> void processVoid(List<I> request, Consumer<I> function);
}
