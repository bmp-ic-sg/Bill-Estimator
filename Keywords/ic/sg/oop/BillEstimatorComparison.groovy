package ic.sg.oop
import java.text.DecimalFormat

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.google.common.collect.ArrayTable.Row
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.RequestObject
import com.kms.katalon.core.testobject.ResponseObject
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.TestObjectProperty
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords
import com.kms.katalon.keyword.excel.ExcelKeywords

import internal.GlobalVariable



class BillEstimatorComparison {
	/**
	 * Send request and verify status code
	 * @param request request object, must be an instance of RequestObject
	 * @param expectedStatusCode
	 * @return a boolean to indicate whether the response status code equals the expected one
	 */
	@Keyword
	def verifyStatusCode(TestObject request, int expectedStatusCode) {
		if (request instanceof RequestObject) {
			RequestObject requestObject = (RequestObject) request
			ResponseObject response = WSBuiltInKeywords.sendRequest(requestObject)
			if (response.getStatusCode() == expectedStatusCode) {
				KeywordUtil.markPassed("Response status codes match")
			} else {
				KeywordUtil.markFailed("Response status code not match. Expected: " +
						expectedStatusCode + " - Actual: " + response.getStatusCode() )
			}
		} else {
			KeywordUtil.markFailed(request.getObjectId() + " is not a RequestObject")
		}
	}

	/**
	 * Add Header basic authorization field,
	 * this field value is Base64 encoded token from user name and password
	 * @param request object, must be an instance of RequestObject
	 * @param username username
	 * @param password password
	 * @return the original request object with basic authorization header field added
	 */
	@Keyword
	def addBasicAuthorizationProperty(TestObject request, String username, String password) {
		if (request instanceof RequestObject) {
			String authorizationValue = username + ":" + password
			authorizationValue = "Basic " + authorizationValue.bytes.encodeBase64().toString()

			// Find available basic authorization field and change its value to the new one, if any
			List<TestObjectProperty> headerProperties = request.getHttpHeaderProperties()
			boolean fieldExist = false
			for (int i = 0; i < headerProperties.size(); i++) {
				TestObjectProperty headerField = headerProperties.get(i)
				if (headerField.getName().equals('Authorization')) {
					KeywordUtil.logInfo("Found existent basic authorization field. Replacing its value.")
					headerField.setValue(authorizationValue)
					fieldExist = true
					break
				}
			}

			if (!fieldExist) {
				TestObjectProperty authorizationProperty = new TestObjectProperty("Authorization",
						ConditionType.EQUALS, authorizationValue, true)
				headerProperties.add(authorizationProperty)
			}
			KeywordUtil.markPassed("Basic authorization field has been added to request header")
		} else {
			KeywordUtil.markFailed(request.getObjectId() + "is not a RequestObject")
		}
		return request
	}

	@Keyword
	String buildHospitalCode(def hospitalName) {
		String hospitalCode = ''

		switch (hospitalName) {
			case 'Mount Elizabeth Novena Hospital':
				hospitalCode = 'MNH'

				break
			case 'Mount Elizabeth Hospital':
				hospitalCode = 'MEH'

				break
			case 'Gleneagles Hospital':
				hospitalCode = 'GEH'

				break
			case 'Parkway East Hospital':
				hospitalCode = 'PEH'

				break
			default:
				break
		}

		return hospitalCode
	}

	@Keyword
	String validateAfdCostSheet(def isDc) {
		String sheet = ''
		//still dummy
		switch (isDc) {
			case 0:
				sheet = 'Bill Estimator (DC)'
				break
			case 1:
				sheet = 'Bill Estimator (SGL)'
				break
		}

		return sheet
	}

	void setupAfdCostEstimate(def isDc, def dcVal, def sglVal)
	{
		def workBookCostEstimate = ExcelKeywords.getWorkbook(GlobalVariable.AfdEstimatorFile)
		def sheet = ExcelKeywords.getExcelSheet(workBookCostEstimate, validateAfdCostSheet(isDc))
		if(isDc == 0) {
			ExcelKeywords.setValueToCellByAddress(sheet, 'B107', dcVal)
		}else {
			ExcelKeywords.setValueToCellByAddress(sheet, 'B107', sglVal)
		}

		ExcelKeywords.saveWorkbook(GlobalVariable.AfdEstimatorFile, workBookCostEstimate)
	}

	String buildPayload(String languageCode, String version, String hospitalName, String tospCode, boolean preAuth, String ispItemUrl){

		String payloadFormat = "{\r\n    \"securityHeader\": {\r\n        \"state\": \"{{\$guid}}\"\r\n    },\r\n    \"languageCode\": \"${languageCode}\",\r\n    \"version\": \"${version}\",\r\n    \"hospitalCode\": \"${hospitalName}\",\r\n    \"tospCode\": \"${tospCode}\",\r\n    \"preAuth\": ${preAuth},\r\n    \"ispItemUrl\": \"${ispItemUrl}\"\r\n}"
		return payloadFormat;
	}

	void createOutputFile(String fileName) {

		XSSFWorkbook  workbook = new XSSFWorkbook ();
		XSSFSheet  sheet = workbook.createSheet("Result");
		FileOutputStream fileOut = new FileOutputStream(fileName);
		workbook.write(fileOut);
		workbook.close();
		fileOut.close();
	}

	int findLastRowNum(Sheet sheet) {
		int index = 0
		int rx = 0
		for (Row row in sheet) {
			rx += 1
			Cell cell = row.getCell(0)
			if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				index = rx
			}
		}
		return index
	}

	String compare(double api, double excel) {

		String result = ''
		int compareResult = Double.compare(api, excel)

		switch (compareResult) {
			case 0 :
				result = 'PASS'
				break
			default :
				result = 'FAILED'
				break
		}

		return result
	}



}