package uk.gov.hmcts.juror.api.moj.utils;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionSort;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Iterator;
import java.util.stream.Stream;

@SuppressWarnings("checkstyle:MethodTypeParameterName")
public final class RevisionUtil {
    private static final int DEFAULT_PAGE_SIZE = 10;

    private RevisionUtil() {

    }

    public static <N extends Number & Comparable<N>, T, ID> Stream<Revision<N, T>> findRevisionsSorted(
        RevisionRepository<T, ID, N> revisionRepository,
        ID id,
        RevisionSort revisionSort) {
        return findRevisionsSorted(revisionRepository, id, revisionSort, DEFAULT_PAGE_SIZE);
    }

    public static <N extends Number & Comparable<N>, T, ID> Stream<Revision<N, T>> findRevisionsSorted(
        RevisionRepository<T, ID, N> revisionRepository,
        ID id,
        RevisionSort revisionSort,
        int pageSize) {
        return Streams.stream(new RevisionIterable<>(revisionRepository, id, revisionSort, pageSize));
    }


    @RequiredArgsConstructor
    @SuppressWarnings("checkstyle:MethodTypeParameterName")
    public static class RevisionIterable<N extends Number & Comparable<N>, T, I>
        implements Iterable<Revision<N, T>> {

        private final RevisionRepository<T, I, N> revisionRepository;
        private final I id;
        private final RevisionSort revisionSort;
        private final int pageSize;

        @Override
        public Iterator<Revision<N, T>> iterator() {
            return new RevisionIterator<>(revisionRepository, id, revisionSort, pageSize);
        }


        public static class RevisionIterator<N extends Number & Comparable<N>, T, I>
            implements Iterator<Revision<N, T>> {
            private final I id;
            private final RevisionRepository<T, I, N> revisionRepository;
            private Page<Revision<N, T>> page;

            private int index;

            public RevisionIterator(RevisionRepository<T, I, N> revisionRepository,
                                    I id,
                                    RevisionSort revisionSort,
                                    int pageSize) {
                this.revisionRepository = revisionRepository;
                this.id = id;
                this.page = this.revisionRepository.findRevisions(id, PageRequest.of(0, pageSize, revisionSort));
                this.index = 0;
            }

            private void nextPage() {
                this.page = this.revisionRepository.findRevisions(id, page.nextPageable());
                this.index = 0;
            }

            @Override
            public boolean hasNext() {
                if (index >= page.getNumberOfElements()) {
                    if (!page.hasNext() || !page.hasContent()) {
                        return false;
                    }
                    nextPage();
                }
                return true;
            }

            @Override
            public Revision<N, T> next() {
                return page.getContent().get(index++);
            }
        }
    }
}
