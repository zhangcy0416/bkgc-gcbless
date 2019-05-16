package com.bkgc.bless.core.advice;

import com.bkgc.common.exception.BusinessException;
import com.bkgc.common.exception.ResultCodeEnum;
import com.bkgc.common.utils.RWrapper;
import com.bkgc.common.utils.WrapperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>Title:      异常统一拦截 </p>
 * <p>Description  </p>
 *
 * @Author        zhangft
 * @CreateDate    2017/9/21 上午11:10
 */
@RestControllerAdvice("com.bkgc.bless.web.provider")
public class RestControllerExceptionAdvice {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    /**
     * 全局捕捉未知异常
     *
     */
    @ExceptionHandler(value = Exception.class)
    public RWrapper errorHandler(Exception e) {
        log.error("系统出现未知异常信息={}", e.getMessage(), e);
        return WrapperUtil.error(ResultCodeEnum.SERVICE_UNAVAILABLE);
    }

    /**
     * 拦截捕捉自定义异常 BusinessException
     *
     */
    @ExceptionHandler(value = BusinessException.class)
    public RWrapper myErrorHandler(BusinessException ex) {
        log.error("系统出现业务异常信息={}", ex.getMessage(), ex);
        return WrapperUtil.error(ex.getResultCode());
    }

}
