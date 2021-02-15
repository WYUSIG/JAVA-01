package io.sign.www.spring.jdkproxy;

public class DefaultEchoService implements EchoService {

    @Override
    public String echo(String message) throws NullPointerException {
        System.out.println("[DefaultEcho] " + message);
        return "[DefaultEcho] " + message;
    }
}
