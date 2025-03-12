package com.psbc.psf.handler;

import com.psbc.psf.context.ChainContext;
import com.psbc.psf.exception.PsfException;
import com.psbc.psf.exception.PsfFlowControlException;
import com.psbc.psf.flowControl.FlowControlProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 2025/3/6 16:46
 * auth: dahua
 * desc:
 */
@Component
public class FlowControlHandler extends AbstractHandler {

    private static final Logger logger = LoggerFactory.getLogger(FlowControlHandler.class);
    @Autowired
    private FlowControlProcessor flowControlProcessor;

    @Override
    public void doHandler(ChainContext chainContext) throws PsfException {
        String routerId = chainContext.getRouterId();
        boolean isFlowControl = flowControlProcessor.flowControl(routerId);
        if (!isFlowControl) {
            throw new PsfFlowControlException("psf flow control");
        }
    }

    @Override
    public int order() {
        return 2000;
    }
}
