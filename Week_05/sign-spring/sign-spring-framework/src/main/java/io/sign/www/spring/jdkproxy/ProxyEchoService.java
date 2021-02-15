package io.sign.www.spring.jdkproxy;

import io.sign.www.spring.jdkproxy.EchoService;

public class ProxyEchoService implements EchoService {

    private final EchoService echoService;

    public ProxyEchoService(EchoService echoService) {
        this.echoService = echoService;
    }

    @Override
    public String echo(String message) throws NullPointerException {
        String result = echoService.echo(message);
        return result;
    }
}
