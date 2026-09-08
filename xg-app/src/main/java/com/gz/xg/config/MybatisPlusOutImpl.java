package com.gz.xg.config;

import org.apache.ibatis.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MybatisPlusOutImpl implements Log {

    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisPlusOutImpl.class);

    public MybatisPlusOutImpl(String clazz) {

    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public void error(String s, Throwable e) {
        e.printStackTrace(System.err);
    }

    @Override
    public void error(String s) {
        System.err.println("MybatisPlusOutImpl::"+s);
        LOGGER.error("MybatisPlusOutImpl::{}",s);
    }

    @Override
    public void debug(String s) {
        LOGGER.info("MybatisPlusOutImpl::{}",s);
        //System.out.println("MybatisPlusOutImpl::"+s);
    }

    @Override
    public void trace(String s) {

    }

    @Override
    public void warn(String s) {

    }

}
