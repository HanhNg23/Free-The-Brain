package com.example.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Target({
	ElementType.PARAMETER, 
	ElementType.TYPE //for class and inteface
})
@Retention(RetentionPolicy.RUNTIME) 
//Runtime: Annotations are to be recorded in the class file
//by the compiler and retained by the VM at run time,
//so they may be read reflectively.
@Documented
@AuthenticationPrincipal 
// Up Annotation is used to resolve Authentication.getPrincipal()
//to a methodargument.
public @interface CurrentUser {

	
}
