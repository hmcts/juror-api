package uk.gov.hmcts.juror.api.moj.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class BulkServiceImpl implements BulkService {
    @Override
    public <I, O> List<O> process(List<I> inputs, Function<I, O> function) {
        return inputs.stream()
            .map(function)
            .toList();
    }

    @Override
    public <I> void processVoid(List<I> inputs, Consumer<I> consumer) {
        inputs.forEach(consumer);
    }
}
