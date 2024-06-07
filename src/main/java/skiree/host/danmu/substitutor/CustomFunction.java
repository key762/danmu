package skiree.host.danmu.substitutor;

import org.springframework.beans.factory.InitializingBean;

public abstract class CustomFunction implements InitializingBean {

    public abstract String functionName();

    public void register() {
        CustomSubstitutor.getInstance().register(this);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        register();
    }
}