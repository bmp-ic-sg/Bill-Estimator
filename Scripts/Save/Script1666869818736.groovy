import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.kms.katalon.core.testdata.reader.ExcelFactory
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS

import internal.GlobalVariable as GlobalVariable

'1'
Object payloadSourceFile = ExcelFactory.getExcelDataWithDefaultSheet(GlobalVariable.PayloadSourceFile, GlobalVariable.PayloadSourceSheet, true);

''
for (int sourceRow = 0; sourceRow < payloadSourceFile.getRowNumbers(); sourceRow++) {
	
	'Generating random UUID for State'
	String state = UUID.randomUUID().toString();
	String languageCode = payloadSourceFile.getValue('languageCode', sourceRow +1);
	String version = payloadSourceFile.getValue('version', sourceRow +1);
	String hospitalName = payloadSourceFile.getValue('hospitalCode', sourceRow +1);
	String tospCode = payloadSourceFile.getValue('tospCode', sourceRow +1);
	boolean preAuth = Boolean.parseBoolean(payloadSourceFile.getValue('preAuth', sourceRow +1));
	String ispItemUrl = payloadSourceFile.getValue('ispItemUrl', sourceRow +1);
	
	
	
	'Calling API'
	def apiRequest = WS.sendRequest(findTestObject('Postman/Get Info', [('state') : state, ('languageCode') : languageCode, ('version') : version
				, ('hospitalCode') : hospitalName, ('tospCode') : tospCode, ('preAuth') : preAuth, ('ispItemUrl') : ispItemUrl]))
	
	'Parsing response'	
	def apiResponse = new groovy.json.JsonSlurper().parseText(apiRequest.getResponseBodyContent())
	
	if(apiResponse.hasProperty('oop'))
	{
		String hospitalCode="";
		switch(hospitalName)
		{
			case "Mount Elizabeth Novena Hospital":
			hospitalCode = 'MNH';
			break
			
			case "Mount Elizabeth Hospital":
			hospitalCode = 'MEH';
			break
			
			case "Gleneagles Hospital":
			hospitalCode = 'GEH';
			break
			
			case "Parkway East Hospital":
			hospitalCode = 'PEH';
			break
			
			default:
			break
		}
		
		Object billEstimateSourceFile = ExcelFactory.getExcelDataWithDefaultSheet(GlobalVariable.BillEstimateFile, hospitalCode, true);
		billEstimateSourceFile.getValue('TOSP Code', tospCode);
		println(billEstimateSourceFile.getValue('TOSP Code', tospCode))
		
	}
	
	File outputFolder = new File(GlobalVariable.OutputFolder);
	
	if (!outputFolder.exists()) {
		outputFolder.mkdir();
	}
	
	
	String outputFilename = GlobalVariable.OutputFolder + '-test.xlsx';
	createOutputFile(outputFilename);
	
	
	XSSFWorkbook  workbook = new XSSFWorkbook ();
	XSSFSheet  sheet = workbook.createSheet("API Result");
	
	/*
	 * Reading ./Employee.xlsx file
	 */
	File sourceExcel = new File(outputFilename);
	FileInputStream fis = new FileInputStream(sourceExcel);
	
	// writing data into the sheet
	String payloadStr = buildPayload(languageCode, version, hospitalName, tospCode, preAuth, ispItemUrl);
	println(payloadStr)
	
	Map<String, Object[]> apiPayloadData = new HashMap<>()	
	apiPayloadData.put("1", ["Request", payloadStr])
	apiPayloadData.put("2", ["Response", apiResult])
	
	Set newRows = apiPayloadData.keySet()
	//int rownum = sheet.getLastRowNum()
	int rownum = findLastRowNum(sheet)
	for (String key in newRows) {
		Row row = sheet.createRow(rownum++)
		Object[] objArr = apiPayloadData.get(key)
		int cellnum = 0
		for (Object obj : objArr) {
			Cell cell = row.createCell(cellnum++)
			if (obj instanceof String) {
				cell.setCellValue((String) obj)
			} else if (obj instanceof Boolean) {
				cell.setCellValue((Boolean) obj)
			} else if (obj instanceof Date) {
				cell.setCellValue((Date) obj)
			} else if (obj instanceof Double) {
				cell.setCellValue((Double) obj)
			}
		}
	}
	
	
	/*
	 * Writing into the ./Employee.out.xlsx file
	 */
	File targetExcel = new File(outputFilename);
	FileOutputStream os = new FileOutputStream(targetExcel)
	workbook.write(os)
	println "Writing into ${targetExcel}";
	
	// Close workbook, OutputStream and Excel file to prevent leak
	os.close();
	workbook.close();
	fis.close();

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

void createOutputFile(String fileName) {
	
	XSSFWorkbook  workbook = new XSSFWorkbook ();
	XSSFSheet  sheet = workbook.createSheet("Result");
	FileOutputStream fileOut = new FileOutputStream(fileName);
	workbook.write(fileOut);
	workbook.close();
	fileOut.close();
}

String buildPayload(String languageCode, String version, String hospitalName, String tospCode, boolean preAuth, String ispItemUrl){
	
	String payloadFormat = "{\r\n    \"securityHeader\": {\r\n        \"state\": \"{{\$guid}}\"\r\n    },\r\n    \"languageCode\": \"${languageCode}\",\r\n    \"version\": \"${version}\",\r\n    \"hospitalCode\": \"${hospitalName}\",\r\n    \"tospCode\": \"${tospCode}\",\r\n    \"preAuth\": ${preAuth},\r\n    \"ispItemUrl\": \"${ispItemUrl}\"\r\n}"
	return payloadFormat
}