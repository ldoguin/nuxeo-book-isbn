/*
 * (C) Copyright 2012 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     ldoguin
 */

package org.nuxeo.isbn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;

import sun.net.www.protocol.http.HttpURLConnection;

/**
 * @author ldoguin
 */
@Operation(id = ISBNQuery.ID, category = Constants.CAT_DOCUMENT, label = "ISBNQuery", description = "")
public class ISBNQuery {

    public static final String ID = "ISBNQuery";

    public static final String QUERY_ISBN_URL = "http://openlibrary.org/api/books?bibkeys=%s&format=json";

    @OperationMethod(collector = DocumentModelCollector.class)
    public DocumentModel run(DocumentModel input) throws MalformedURLException,
            PropertyException, ClientException {
        String isbn = input.getProperty("isbn:isbn").getValue(String.class);
        isbn = "ISBN:".concat(isbn);
        String query = String.format(QUERY_ISBN_URL, isbn);
        URL url = new URL(query);
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(sb.toString());
            JsonNode metadata = rootNode.get(isbn);

            JsonNode bib_key = metadata.get("bib_key");
            if (bib_key != null && bib_key.isValueNode()) {
                input.setPropertyValue("isbn:bib_key", bib_key.getValueAsText());
            }
            JsonNode info_url = metadata.get("info_url");
            if (info_url != null && info_url.isValueNode()) {
                input.setPropertyValue("isbn:info_url", info_url.getValueAsText());
            }
            JsonNode preview = metadata.get("preview");
            if (preview != null && preview.isValueNode()) {
                input.setPropertyValue("isbn:preview", preview.getValueAsText());
            }
            JsonNode preview_url = metadata.get("preview_url");
            if (preview_url != null && preview_url.isValueNode()) {
                input.setPropertyValue("isbn:preview_url", preview_url.getValueAsText());
            }
            JsonNode thumbnail_url = metadata.get("thumbnail_url");
            if (thumbnail_url != null && thumbnail_url.isValueNode()) {
                input.setPropertyValue("isbn:thumbnail_url", thumbnail_url.getValueAsText());
            }
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return input;
    }
}
