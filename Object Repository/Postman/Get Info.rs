<?xml version="1.0" encoding="UTF-8"?>
<WebServiceRequestEntity>
   <description></description>
   <name>Get Info</name>
   <tag></tag>
   <elementGuidId>d4b1fa2d-8c4f-4973-abe3-1faa0a7090ee</elementGuidId>
   <selectorMethod>BASIC</selectorMethod>
   <useRalativeImagePath>false</useRalativeImagePath>
   <connectionTimeout>0</connectionTimeout>
   <followRedirects>false</followRedirects>
   <httpBody></httpBody>
   <httpBodyContent>{
  &quot;text&quot;: &quot;{\n \&quot;securityHeader\&quot;: {\n        \&quot;state\&quot;: \&quot;${state}\&quot;\n    },\n\&quot;languageCode\&quot;: \&quot;${languageCode}\&quot;,\n\&quot;version\&quot;: \&quot;${version}\&quot;,\n\&quot;hospitalCode\&quot;: \&quot;${hospitalCode}\&quot;,\n\&quot;tospCode\&quot;: \&quot;${tospCode}\&quot;,\n\&quot;preAuth\&quot;: ${preAuth},\n\&quot;ispItemUrl\&quot;: \&quot;${ispItemUrl}\&quot;,\n\&quot;riderItemUrl\&quot;: \&quot;\&quot;\n}&quot;,
  &quot;contentType&quot;: &quot;application/json&quot;,
  &quot;charset&quot;: &quot;UTF-8&quot;
}</httpBodyContent>
   <httpBodyType>text</httpBodyType>
   <httpHeaderProperties>
      <isSelected>true</isSelected>
      <matchCondition>equals</matchCondition>
      <name>Content-Type</name>
      <type>Main</type>
      <value>application/json</value>
      <webElementGuid>7e361bce-636b-4f85-b682-774b99781fbb</webElementGuid>
   </httpHeaderProperties>
   <httpHeaderProperties>
      <isSelected>true</isSelected>
      <matchCondition>equals</matchCondition>
      <name>x-api-key</name>
      <type>Main</type>
      <value>SPB6rhAPOuqvUTXS6DRmVVNfYhnNG1xO</value>
      <webElementGuid>1b5f67bc-6b7f-4264-8b74-9d9dba03944e</webElementGuid>
   </httpHeaderProperties>
   <katalonVersion>8.5.1</katalonVersion>
   <maxResponseSize>0</maxResponseSize>
   <migratedVersion>5.4.1</migratedVersion>
   <restRequestMethod>POST</restRequestMethod>
   <restUrl>http://192.168.200.142:6060/sdms/api/afd/oop</restUrl>
   <serviceType>RESTful</serviceType>
   <soapBody></soapBody>
   <soapHeader></soapHeader>
   <soapRequestMethod></soapRequestMethod>
   <soapServiceEndpoint></soapServiceEndpoint>
   <soapServiceFunction></soapServiceFunction>
   <socketTimeout>0</socketTimeout>
   <useServiceInfoFromWsdl>true</useServiceInfoFromWsdl>
   <variables>
      <defaultValue>'e90e8e2b-c6b2-4fe2-a2d0-9e7597f624a1'</defaultValue>
      <description></description>
      <id>1beecd96-2d27-4667-94c0-04dab60a4ac9</id>
      <masked>false</masked>
      <name>state</name>
   </variables>
   <variables>
      <defaultValue>'EN'</defaultValue>
      <description></description>
      <id>8d92d46b-e364-42fa-98b1-e852083665b9</id>
      <masked>false</masked>
      <name>languageCode</name>
   </variables>
   <variables>
      <defaultValue>'DRAFT'</defaultValue>
      <description></description>
      <id>a744883c-6c87-4a40-992b-b6cbf977c237</id>
      <masked>false</masked>
      <name>version</name>
   </variables>
   <variables>
      <defaultValue>'Mount Elizabeth Novena Hospital'</defaultValue>
      <description></description>
      <id>295aa8ea-4c5b-463e-b6c3-6b007214623d</id>
      <masked>false</masked>
      <name>hospitalCode</name>
   </variables>
   <variables>
      <defaultValue>'ABCDF'</defaultValue>
      <description></description>
      <id>97b23ae0-659e-4299-9885-8d156c511d6b</id>
      <masked>false</masked>
      <name>tospCode</name>
   </variables>
   <variables>
      <defaultValue>true</defaultValue>
      <description></description>
      <id>8751a380-a3fd-485b-9c20-ce29fad203d0</id>
      <masked>false</masked>
      <name>preAuth</name>
   </variables>
   <variables>
      <defaultValue>'prudential-prushield-premier'</defaultValue>
      <description></description>
      <id>a667b77e-1b69-4447-9205-864006e6c023</id>
      <masked>false</masked>
      <name>ispItemUrl</name>
   </variables>
   <verificationScript>import static org.assertj.core.api.Assertions.*

import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webservice.verification.WSResponseManager

import groovy.json.JsonSlurper
import internal.GlobalVariable as GlobalVariable

RequestObject request = WSResponseManager.getInstance().getCurrentRequest()

ResponseObject response = WSResponseManager.getInstance().getCurrentResponse()</verificationScript>
   <wsdlAddress></wsdlAddress>
</WebServiceRequestEntity>
