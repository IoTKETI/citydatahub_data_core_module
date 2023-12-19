package kr.re.keti.sc.datacoreusertool.util;

import org.springframework.context.ApplicationContext;

import kr.re.keti.sc.datacoreusertool.config.ApplicationContextProvider;

/**
 * BeanUtil class
 * @FileName BeanUtil.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
public class BeanUtil {
	/**
	 * Get bean
	 * @param beanName	Bean name
	 * @return	Bean class object
	 */
    public static Object getBean(String beanName) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        return applicationContext.getBean(beanName);
    }
}
