/* --------------------------------------------------------------------------------
 * This is an example "helper" class that is used to execute a REST API.
 * In this example we are going to execute Deployment Automation's own REST API but your would typically
 *  call a third party REST API like JIRA or Ansible Tower here.
 * --------------------------------------------------------------------------------
 */

package com.serena.air.plugin.example

import com.serena.air.StepFailedException
import com.serena.air.http.HttpBaseClient
import com.serena.air.http.HttpResponse
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import groovy.xml.StreamingMarkupBuilder
import org.apache.log4j.Logger
import org.apache.log4j.pattern.FullLocationPatternConverter

//
// This is an example of a helper class around a REST API
// In this case we are calling Deployment Automation's own REST API but it could be any third party API
// We use Basic Authentication by default but you could add any authentication strategy
//
class ExampleRESTHelper extends HttpBaseClient {
    private static final Logger log = Logger.getLogger(ExampleRESTHelper.class)

    boolean debug = false

    ExampleRESTHelper(String serverUrl, String username, String password) {
        super(serverUrl, username, password)
    }

    @Override
    protected String getFullServerUrl(String serverUrl) {
        return serverUrl + "/rest"
    }

    // get all of DA's components
    def getComponents() {
        HttpResponse response = execGet("/deploy/component")
        checkStatusCode(response.code)
        def json = new JsonSlurper().parseText(response.body)
        return json
    }

    // get a single DA components
    def getComponent(String uuid) {
        HttpResponse response = execGet("/deploy/component/${uuid}")
        checkStatusCode(response.code)
        def json = new JsonSlurper().parseText(response.body)
        return json
    }

    // create a new component
    def createComponent(String cName, String cDescription) {

        List<JsonBuilder> propJson = new ArrayList<>()
        JsonBuilder cJson = new JsonBuilder()
        cJson {
            name cName
            defaultVersionType "FULL"
            importAutomatically false
            inheritSystemCleanup true
            runVersionCreationProcess false
            useVfs true
            properties propJson
            if (cDescription) {
                description cDescription
            }
        }

        log.debug("Creating component using JSON:\n" + cJson.toPrettyString())
        JsonBuilder body = new JsonBuilder(data: [cJson.getContent()])

        HttpResponse response = execPut("/deploy/component", body)
        checkStatusCode(response.code)

        if (response.code != 200) {
            def json = new JsonSlurper().parseText(response.body)
            def jsonErrors = json?.errors
            def errString = ""
            for (def jsonErr : jsonErrors) {
                errString += jsonErr.description_translated
            }
            if (jsonErrors) {
                throw new StepFailedException(errString)
            }
        } else {
            def json = new JsonSlurper().parseText(response.body)
            return json
        }
    }

    // --------------------------------------------------------------------------------

    private HttpResponse execMethod(def method) {
        try {
            return exec(method)
        } catch (UnknownHostException e) {
            throw new StepFailedException("Unknown host: ${e.message}")
        } catch (HttpHostConnectException ignore) {
            throw new StepFailedException('Connection refused!')
        }
    }

    private HttpResponse execGet(def url) {
        HttpGet method = new HttpGet(getUriBuilder(url.toString()).build())
        //method.addHeader("DirectSsoInteraction", "true");
        return execMethod(method)
    }

    private HttpResponse execPost(def url, def json) {
        HttpPost method = new HttpPost(getUriBuilder(url.toString()).build())
        //method.addHeader("DirectSsoInteraction", "true");
        HttpEntity body = new StringEntity(json.toString(), ContentType.APPLICATION_JSON)
        method.entity = body
        return execMethod(method)
    }

    private HttpResponse execPut(def url, def json) {
        HttpPut method = new HttpPut(getUriBuilder(url.toString()).build())
        //method.addHeader("DirectSsoInteraction", "true");
        HttpEntity body = new StringEntity(json.toString(), ContentType.APPLICATION_JSON)
        method.entity = body
        return execMethod(method)
    }

}
