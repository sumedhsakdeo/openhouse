package com.linkedin.openhouse.common.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class KeyCloakTokenInterceptor implements HandlerInterceptor {
  private static final String TOKEN_TYPE = "Bearer";
  private static final String AUTHORIZATION_HEADER = "Authorization";

  /**
   * Called before every HTTP Request enters the main request processing logic in the Controller. On
   * successful access token validation, we extract the principal and set it in the request as an
   * attribute. This information is later leveraged in checking if the principal has permission to
   * access the given resource. If unsuccessful, we promptly return error on the {@link
   * HttpServletResponse} parameter
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param handler chosen handler to execute, for type and/or instance evaluation
   * @return true if the execution chain should proceed with the next interceptor or the handler
   *     itself, false otherwise.
   */
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
    if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_TYPE)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }
    String token = authorizationHeader.substring(TOKEN_TYPE.length() + 1);

    return !token.isEmpty();
  }
}
