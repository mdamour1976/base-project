package org.damour.base.client.ui.permalink;

import java.util.ArrayList;
import java.util.List;

import org.damour.base.client.objects.PermissibleObject;
import org.damour.base.client.utils.ParameterParser;
import org.damour.base.client.utils.StringUtils;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;

public class PermaLinkBuilder {

  public static String getLink(PermissibleObject permissibleObject, List<String> ignoredParameters) {
    // build a hashmap of pathInfo parameters, query parameters and history token parameters
    ParameterParser pathParameters = new ParameterParser(ParameterParser.convertRESTtoQueryString(Window.Location.getPath()));
    ParameterParser queryStringParameters = new ParameterParser(Window.Location.getQueryString());
    ParameterParser historyParameters = new ParameterParser(History.getToken());

    List<String> parameterOrder = new ArrayList<String>(historyParameters.getOrderedParameterNames());
    if (StringUtils.isEmpty(History.getToken())) {
      for (String queryStringParam : queryStringParameters.getOrderedParameterNames()) {
        if (!parameterOrder.contains(queryStringParam)) {
          parameterOrder.add(queryStringParam);
        }
      }
      for (String pathParam : pathParameters.getOrderedParameterNames()) {
        if (!parameterOrder.contains(pathParam)) {
          parameterOrder.add(pathParam);
        }
      }
    }
    String permaLinkStr = Window.Location.getProtocol() + "//" + Window.Location.getHostName()
        + ((Window.Location.getPort().equals("80") || Window.Location.getPort().equals("")) ? "" : ":" + Window.Location.getPort());
    for (String parameterName : parameterOrder) {
      if (ignoredParameters != null && ignoredParameters.contains(parameterName)) {
        // skip ignored parameters
        continue;
      }
      if ("name".equalsIgnoreCase(parameterName) && permissibleObject != null && !StringUtils.isEmpty(permissibleObject.getName())) {
        // skip the name parameter until later
        continue;
      }
      if (!StringUtils.isEmpty(historyParameters.getParameter(parameterName))) {
        permaLinkStr += "/" + parameterName + "/" + historyParameters.getParameter(parameterName);
      } else if (!StringUtils.isEmpty(queryStringParameters.getParameter(parameterName))) {
        permaLinkStr += "/" + parameterName + "/" + queryStringParameters.getParameter(parameterName);
      } else if (!StringUtils.isEmpty(pathParameters.getParameter(parameterName))) {
        permaLinkStr += "/" + parameterName + "/" + pathParameters.getParameter(parameterName);
      }
    }
    if (permissibleObject != null && !StringUtils.isEmpty(permissibleObject.getName())) {
      permaLinkStr += "/name/" + StringUtils.patchURL(permissibleObject.getName()) + ".html";
    }
    return permaLinkStr;
  }

}
