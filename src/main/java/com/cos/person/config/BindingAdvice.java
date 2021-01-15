package com.cos.person.config;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.cos.person.domain.CommonDto;
import com.sun.tools.sjavac.Log;

@Aspect
@Component
public class BindingAdvice {
	
	// 어떤함수가 언제 몇번 실행됐는지 횟수같은거 로그 남기기
	@Before("execution(* com.cos.person.web..*Controller.*(..))")
	public void testCheck() {
		// request 값 처리
		HttpServletRequest request =((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		System.out.println("주소 : "+request.getRequestURI());
		//log 처리는? 파일로 남기죠?
		System.out.println("전 처리 로그 남겼습니다.");
	}
	
	@After("execution(* com.cos.person.web..*Controller.*(..))")
	public void testCheck2() {
	
		System.out.println("후 처리 로그 남겼습니다.");
	}
	
	@Around("execution(* com.cos.person.web..*Controller.*(..))")
	public Object validCheck(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		String type = proceedingJoinPoint.getSignature().getDeclaringTypeName();
		String method = proceedingJoinPoint.getSignature().getName();
		
		System.out.println("type : "+type);
		System.out.println("method : "+method);
		
		Object[] args = proceedingJoinPoint.getArgs();
		
		for (Object arg : args) {
			if (arg instanceof BindingResult) {
				BindingResult bindingResult =(BindingResult) arg;
				// 서비스 : 정상적인 화면 -> 사용자요청 
				// 잘못된 접근을 할 때 동작
				if(bindingResult.hasErrors()) {
					Map<String, String> errorMap = new HashMap<>();
					
					for(FieldError error : bindingResult.getFieldErrors()) {
						errorMap.put(error.getField(), error.getDefaultMessage());
						// 로그 레벨 error warn info debug
						Log.warn(type+"."+method+"()=>필드 : "+error.getField()+", 메시지 : "+error.getDefaultMessage());
					}
					
					return new CommonDto<>(HttpStatus.BAD_REQUEST.value(),errorMap);
				}
			}
		}
		return proceedingJoinPoint.proceed(); // 함수의 스택을 진행해라.
	}
}
