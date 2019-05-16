package com.bkgc.bless.core.advice;

import com.bkgc.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title:      异常统一拦截 </p>
 * <p>Description  </p>
 *
 * @Author        zhangft
 * @CreateDate    2017/9/21 上午11:10
 */
@ControllerAdvice("com.bkgc.bless.web.controller")
public class ControllerExceptionAdvice {

//    @Autowired
//    private LogService logService;

    private static Logger log = LoggerFactory.getLogger(ControllerExceptionAdvice.class);

    /**
     * 全局捕捉未知异常
     *
     */
    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Map errorHandler(Exception e) {
        Map map = new HashMap();
        map.put("code", "2000");
        map.put("msg", "服务不可用");
        log.error("系统出现未知异常信息={}", e.getMessage(), e);
        e.printStackTrace();
        return map;
    }

    /**
     * 拦截捕捉自定义异常 BusinessException
     *
     */
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public Map myErrorHandler(BusinessException ex) {
        Map map = new HashMap();
        map.put("code", ex.getResultCode().getCode());
        map.put("msg", ex.getResultCode().getMsg());
        log.error("系统出现业务异常信息={}", ex.getMessage(), ex);
        ex.printStackTrace();
        return map;
    }

}
