package com.freethebrain.util;

import java.util.Optional;
import java.io.IOException;
import java.util.Base64;
import org.springframework.util.SerializationUtils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtils {

    public static Optional<Cookie> getCookie(HttpServletRequest request, String cookieNameRequired) {
	Cookie[] cookieList = request.getCookies();

	if (cookieList != null && cookieList.length > 0) {
	    for (Cookie cookie : cookieList) {
		if (cookie.getName().equals(cookieNameRequired)) {
		    return Optional.of(cookie);
		}
	    }

	    System.out.println("COOKIES : \n");
	    for (Cookie cookie : cookieList) {
		System.out.println("name: " + cookie.getName() + " -- " + "value: " + cookie.getValue());
	    }
	}
	return Optional.empty();
    }

    public static void addCookie(HttpServletResponse respone, String name, String value, int maxAge) {
	Cookie cookie = new Cookie(name, value);
	cookie.setPath("/");
	cookie.setHttpOnly(true);
	cookie.setSecure(true);
	cookie.setMaxAge(maxAge);
	// cookie.setDomain("HouseMate.com");
	respone.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse respone, String name) {
	Cookie[] cookieList = request.getCookies();

	if (cookieList != null && cookieList.length > 0) {
	    for (Cookie cookie : cookieList) {
		if (cookie.getName().equals(name)) {
		    cookie.setValue("");
		    cookie.setPath("/");
		    cookie.setMaxAge(0);
		    respone.addCookie(cookie);

		}
	    }
	}
    }

    public static String serialize(Object object) {
	return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> theClass) {
	byte[] base64EncodedString = Base64.getUrlDecoder().decode(cookie.getValue());
	ObjectMapper objectMapper = new ObjectMapper();
	try {
	    return objectMapper.readValue(base64EncodedString, theClass);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

}
