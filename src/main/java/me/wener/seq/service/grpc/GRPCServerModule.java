package me.wener.seq.service.grpc;

import com.google.inject.AbstractModule;

import javax.inject.Named;

/**
 * Expose a SequenceManager to gRPC
 *
 * @author wener
 * @since 15/11/18
 */
@Named("grpc")
public class GRPCServerModule extends AbstractModule {
    @Override
    protected void configure() {

    }
}
