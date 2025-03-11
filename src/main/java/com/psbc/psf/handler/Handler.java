package com.psbc.psf.handler;

import com.psbc.psf.context.ChainContext;
import com.psbc.psf.exception.PsfException;
import reactor.core.publisher.Mono;

/**
 * 2025/3/6 16:43
 * auth: dahua
 * desc:
 */
public interface Handler extends Comparable<Handler> {

    void handle(ChainContext chainContext) throws PsfException;

    int order();

    @Override
    default int compareTo(Handler other) {
        return Integer.compare(this.order(), other.order());
    }
}
