/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.cafe24.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for managing the quick_start user cookie that remembers the signed-in user.
 * @author Keith Donald
 */
final class UserCookieGenerator {

	private static final Logger logger = LoggerFactory.getLogger(UserCookieGenerator.class);
	private final CookieGenerator userCookieGenerator = new CookieGenerator();


	public UserCookieGenerator() {
		logger.debug("UserCookieGenerator userCookieGenerator.getCookieName(): " + userCookieGenerator.getCookieName());
	}

	public void addCookie(String userId, HttpServletResponse response) {
		logger.debug("addCookie called...");
		logger.debug("addCookie userId: " + userId);

		userCookieGenerator.addCookie(response, userId);
	}
	
	public void removeCookie(HttpServletResponse response) {
		userCookieGenerator.addCookie(response, userCookieGenerator.getCookieName());
	}
	
	public String readCookieValue(HttpServletRequest request) {
		logger.debug("readCookieValue called...");

		Cookie[] cookies = request.getCookies();

		if (cookies == null) {
			logger.debug("readCookieValue cookies is null...");

			return null;
		}
		for (Cookie cookie : cookies) {
			logger.debug("readCookieValue cookies cookie.getName: " + cookie.getName());
			logger.debug("readCookieValue cookies cookie.getValue: " + cookie.getValue());
			logger.debug("readCookieValue cookies cookie.getPath: " + cookie.getPath());
			logger.debug("readCookieValue cookies cookie.getComment: " + cookie.getComment());
			logger.debug("readCookieValue cookies cookie.getDomain: " + cookie.getDomain());
			logger.debug("readCookieValue cookies cookie.getVersion: " + cookie.getVersion());
			logger.debug("readCookieValue cookies cookie.getSecure: " + cookie.getSecure());
			logger.debug(" ");

			if (cookie.getName().equals(userCookieGenerator.getCookieName())) {
				String cookieValue = cookie.getValue();
				logger.debug("readCookieValue cookie.getName().equals(userCookieGenerator.getCookieName()) cookieValue: " + cookieValue);

				return cookieValue;
			}
		}
		logger.debug("readCookieValue return null");

		return null;
	}

}
