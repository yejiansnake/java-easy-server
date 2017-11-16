package easy.net.factory;

import java.security.InvalidParameterException;

public class FactoryCreator {

    private static FactoryCreator ourInstance = new FactoryCreator();

    public static FactoryCreator getInstance() {
        return ourInstance;
    }

    public ChannelFactory getChannelFactory(FactoryType type) throws Exception {
        switch (type) {
            case NIO:
                return getNioChannelFactory();
            case EPOLL:
                return getEpollChannelFactory();
        }

        throw new InvalidParameterException("type not exist");
    }

    public ChannelFactory getNioChannelFactory() {
        return new NioChannelFactory();
    }

    public ChannelFactory getEpollChannelFactory() {
        return new EpollChannelFactory();
    }
}
