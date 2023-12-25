package com.freethebrain.util;

import java.util.Optional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
public class CookieUtils {
	private static final ObjectMapper objectMapper = new ObjectMapper();

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

    //delete cookie by setting 0 max age of that cookie to automatic erased by the browser
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

	// Serialize object into byte streams for the main purpose here is transmitting
	// data on url then encode into URL base 64 - a series of 6 bits.
	public static String serialize(Object object)  {
		//return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
		byte[] objectByteStream;
		try {
			System.out.println("1 - " + objectMapper.writeValueAsString(object));
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bo);
			bo.close();
			os.close();
			os.writeObject(object);
			objectByteStream = bo.toByteArray();
			return Base64.getUrlEncoder().withoutPadding().encodeToString(objectByteStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	//Cookie value argument currently has the format of URL Base64 String
	//Decode from the URL Base 64 String into byte streams,
	//Then deserialize byte stream to reconstruct the object instance of theClass	
	public static <T> T deserialize(Cookie cookie, Class<T> theClass) {
		byte[] byteStreamString = Base64.getUrlDecoder().decode(cookie.getValue()); 
		try {
			ByteArrayInputStream bi = new ByteArrayInputStream(byteStreamString);
			ObjectInputStream oi = new ObjectInputStream(bi);
			Object obj = oi.readObject();
			bi.close();
			oi.close();
			System.out.println("RESULT " + OAuth2AuthorizationRequest.class.cast(obj).getAuthorizationRequestUri());
			return theClass.cast(obj);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
