/*
 * Copyright (C) 2015, The Max Planck Institute for
 * Psycholinguistics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License is included in the file
 * LICENSE-gpl-3.0.txt. If that file is missing, see
 * <http://www.gnu.org/licenses/>.
 */

package nl.mpi.oai.harvester.control;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import nl.mpi.oai.harvester.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class reads information from the REST service of the CLARIN Centre
 * Registry (see http://www.clarin.eu/content/centres for more information).
 *
 * @author Lari Lampen (MPI-PL)
 */
public class RegistryReader {
  private static final Logger logger = LogManager.getLogger(RegistryReader.class);
  private static URL registryUrl = null;

  /**
   * Create a new registry reader object.
   */
  public RegistryReader(URL url) {
    this.registryUrl = url;
  }

  private HttpURLConnection getConnection(URL url,String contentType) throws IOException {
    HttpURLConnection connection = null;
    Boolean redirect = false;

    do {
      connection = (HttpURLConnection) url.openConnection();
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", contentType);
      connection.connect();
      
      int status = connection.getResponseCode();
      if (status != HttpURLConnection.HTTP_OK) {
        if (status == HttpURLConnection.HTTP_MOVED_TEMP
            || status == HttpURLConnection.HTTP_MOVED_PERM
            || status == HttpURLConnection.HTTP_SEE_OTHER) {
            redirect = true;
             // get redirect url from "location" header field
            url = new URL(connection.getHeaderField("Location"));
            logger.debug("Center Registry redirect to URL : " + url);
         } else
            redirect = false;
        }
      
    } while (redirect);
    return connection;
  }
  
  private HttpURLConnection getConnection(String contentType) throws IOException {
    return getConnection(registryUrl,contentType);
  }

  private String getJSONString(InputStream stream) throws IOException {
    BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = rd.readLine()) != null) {
      sb.append(line);
    }
    rd.close();
    return sb.toString();
  }

  private JSONArray getModelAsJSONArray(String model) throws IOException {
    URL regUrl = new URL(registryUrl.toString() + (registryUrl.toString().endsWith("/")?"/":"") + model);
    HttpURLConnection connection = getConnection(regUrl, "application/json");
    String jsonString = getJSONString(connection.getInputStream());
    return JSONArray.fromObject(jsonString);
  }

  private JSONObject getJSONObject(String model, int key) throws IOException {
    JSONArray jsonArr = getModelAsJSONArray(model);
    for(int i=0; i<jsonArr.size(); i++) {
      JSONObject json = jsonArr.getJSONObject(i);
      if(json.get("pk").equals(key)) {
        return json;
      }
    }
    return null;
  }

    /**
     * Get a list of all OAI-PMH endpoint URLs defined in the
     * specified registry.
     *
     * @return list of all OAI-PMH endpoint URLs
     */
    public List<String> getEndpoints() throws IOException {
      List<String> endpoints = new ArrayList<>();
      JSONArray jsonArr = getModelAsJSONArray("OAIPMHEndpoint");
      for (int i = 0; i < jsonArr.size(); i++) {
        JSONObject json = jsonArr.getJSONObject(i);
        JSONObject jsonObj = json.getJSONObject("fields");
        String res = (String) jsonObj.get("uri");
        endpoints.add(res);
      }
      logger.info("Found " + endpoints.size() + " endpoints");
    return endpoints;
  }

  private JSONObject getEndpoint(String url) throws IOException {
    JSONArray jsonArr = getModelAsJSONArray("OAIPMHEndpoint");
    for(int i=0; i<jsonArr.size(); i++) {
      JSONObject json = jsonArr.getJSONObject(i);
      if(json.getJSONObject("fields").get("uri").equals(url))
        return json;
    }
    return null;
  }

  void printEndpointMapping(PrintWriter m, Provider provider) throws IOException {
    String endpointUrl = provider.getOaiUrl();
    String directoryName = Util.toFileFormat(provider.getName()).replaceAll("/", "");

    JSONObject endPoint = getEndpoint(endpointUrl);
    JSONObject fields = endPoint.getJSONObject("fields");
    int centreKey = (int) fields.get("centre");

    String centreName = "";
    JSONObject centre = getJSONObject("Centre", centreKey);
    fields = centre.getJSONObject("fields");
    centreName = (String) fields.get("name");
    int consortiumKey = (int) fields.get("consortium");

    String nationalProject = "";
    JSONObject consortium = getJSONObject("Consortium", consortiumKey);
    fields = consortium.getJSONObject("fields");
    nationalProject = (String) fields.get("name");
    m.printf("%s,%s,%s,%s", endpointUrl, directoryName, centreName, nationalProject);
    m.println();
  }

  public Map<String, Collection<CentreRegistrySetDefinition>> getEndPointOaiPmhSetMap() {
    final Map<String, Collection<CentreRegistrySetDefinition>> map = new HashMap<>();
    try {
      final List<String> provUrls = getEndpoints();
      
      
      for (String provUrl : provUrls) {
        JSONObject endPoint = getEndpoint(provUrl);
        JSONObject fields = endPoint.getJSONObject("fields");
        JSONArray sets = fields.getJSONArray("oai_phm_sets");
        Set<CentreRegistrySetDefinition> setdef = new HashSet<>();
        for (int i = 0; i < sets.size(); i++) {
            int key = sets.getInt(i);
            JSONObject set = getJSONObject("OAIPMHEndpointSet",key);
            fields = set.getJSONObject("fields");
            String setSpec = fields.getString("set_spec");
            String setType = fields.getString("set_type");
            setdef.add(new CentreRegistrySetDefinition(setSpec, setType));
        }
        map.put(provUrl,setdef);
      }
    } catch (IOException e) {
      logger.error("Error reading from centre registry", e);
    }
    return map;
  }

}
