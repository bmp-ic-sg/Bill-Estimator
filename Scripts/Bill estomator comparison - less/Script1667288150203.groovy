import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.text.DecimalFormat

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.kms.katalon.core.testdata.reader.ExcelFactory
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import ic.sg.model.OOPPercentileModel
import ic.sg.oop.BillEstimatorComparison
import internal.GlobalVariable as GlobalVariable

'initiate keyword class'
BillEstimatorComparison billEstimatorComparison = new BillEstimatorComparison()

'Read xls payload source files'
def payloadSourceFile = ExcelFactory.getExcelDataWithDefaultSheet(GlobalVariable.PayloadSourceFile, GlobalVariable.PayloadSourceSheet,
		true)

'Build decimal format'
DecimalFormat df = new DecimalFormat("#.##");

int resultIndex = 0;
int numbering = 0;

'Build API call result file'
Map<Integer, Object[]> apiCallMap = new HashMap<>()
'Build API call result header'
apiCallMap.put(resultIndex +=1, [ "No","Request","","","","","", "Response","","","","","","","","",""])
apiCallMap.put(resultIndex +=1, [ "No","Language Code", "Version", "Hospital Code", "TOSP Code","Pre Auth", "ISP Item Url", "50 Percentile","","","","", "75 Percentile","","","",""])
apiCallMap.put(resultIndex +=1, [ "No","Language Code", "Version", "Hospital Code", "TOSP Code","Pre Auth", "ISP Item Url", "procedureCost", "panelOOPPrice", "panelDeductible", "panelCoInsurance", "panelCoPayment", "procedureCost", "panelOOPPrice", "panelDeductible", "panelCoInsurance", "panelCoPayment"])
'iterating payload source files'
for (int payloadRow = 0; payloadRow < payloadSourceFile.getRowNumbers(); payloadRow++) {
	'generating random state'
	String state = UUID.randomUUID().toString()

	String languageCode = payloadSourceFile.getValue('languageCode', payloadRow + 1)

	String version = payloadSourceFile.getValue('version', payloadRow + 1)

	String hospitalName = payloadSourceFile.getValue('hospitalCode', payloadRow + 1)

	String tospCode = payloadSourceFile.getValue('tospCode', payloadRow + 1)

	boolean preAuth = Boolean.parseBoolean(payloadSourceFile.getValue('preAuth', payloadRow + 1))

	String ispItemUrl = payloadSourceFile.getValue('ispItemUrl', payloadRow + 1)

	WebUI.comment('Build paylod = ' + billEstimatorComparison.buildPayload(languageCode, version, hospitalName, tospCode,
			preAuth, ispItemUrl))

	WebUI.comment('Calling API')

	String payloadStr = billEstimatorComparison.buildPayload(languageCode, version, hospitalName, tospCode, preAuth, ispItemUrl);

	def apiRequest = WS.sendRequest(findTestObject('Postman/Get Info', [('state') : state, ('languageCode') : languageCode
		, ('version') : version, ('hospitalCode') : hospitalName, ('tospCode') : tospCode, ('preAuth') : preAuth
		, ('ispItemUrl') : ispItemUrl]))

	KeywordUtil.markPassed('Success invoke API call')

	def oop50Percentile = new OOPPercentileModel()
	def oop75Percentile = new OOPPercentileModel()

	def apiResponse = new groovy.json.JsonSlurper().parseText(apiRequest.getResponseBodyContent())

	boolean has50Prop = apiResponse.oop != null  && apiResponse.oop.'50thPercentile' != null 
	println(has50Prop)

	oop50Percentile.procedureCost = has50Prop ? apiResponse.oop.'50thPercentile'.procedureCost : ""
	oop50Percentile.panelOOPPrice = has50Prop ? apiResponse.oop.'50thPercentile'.panelOOPPrice : ""
	oop50Percentile.panelDeductible = has50Prop ? apiResponse.oop.'50thPercentile'.panelDeductible : ""
	oop50Percentile.panelCoInsurance = has50Prop ? apiResponse.oop.'50thPercentile'.panelCoInsurance : ""
	oop50Percentile.panelCoPayment = has50Prop ? apiResponse.oop.'50thPercentile'.panelCoPayment : ""

	boolean has75Prop =  apiResponse.oop != null  && apiResponse.oop.'75thPercentile' != null
	println(has75Prop)
	oop75Percentile.procedureCost = has75Prop ?apiResponse.oop.'75thPercentile'.procedureCost : ""
	oop75Percentile.panelOOPPrice = has75Prop ?apiResponse.oop.'75thPercentile'.panelOOPPrice : ""
	oop75Percentile.panelDeductible = has75Prop ? apiResponse.oop.'75thPercentile'.panelDeductible : ""
	oop75Percentile.panelCoInsurance = has75Prop ? apiResponse.oop.'75thPercentile'.panelCoInsurance : ""
	oop75Percentile.panelCoPayment = has75Prop ? apiResponse.oop.'75thPercentile'.panelCoPayment : ""


	apiCallMap.put(resultIndex +=1, [
		numbering +=1,languageCode, version, hospitalName, tospCode, preAuth, ispItemUrl,
		oop50Percentile.procedureCost, oop50Percentile.panelOOPPrice, oop50Percentile.panelDeductible, oop50Percentile.panelCoInsurance, oop50Percentile.panelCoPayment,
		oop75Percentile.procedureCost, oop75Percentile.panelOOPPrice, oop75Percentile.panelDeductible, oop75Percentile.panelCoInsurance, oop75Percentile.panelCoPayment
	])

}
'Build timestamp'
Date todaysDate = new Date();
def formattedDate = todaysDate.format("dd-MMM-yyyy");

'Build output fileName'
String resultFileName = GlobalVariable.OutputFolder + "API Call_"+formattedDate+".xlsx"

'Create Output file'
billEstimatorComparison.createOutputFile(resultFileName);

File templateSource = new File(GlobalVariable.APIResultTemplate);
FileInputStream templateFis = new FileInputStream(templateSource);

'Open the .xlsx file and construct a workbook object'
XSSFWorkbook templateWb = new XSSFWorkbook(templateFis)

'Get the top sheet out of the workbook'
XSSFSheet templateSheet = templateWb.getSheetAt(0)

Set<Integer> keyid = apiCallMap.keySet();
keyid.sort();
int rowid = 0;

'Build values into row'
for (Integer key : keyid) {

	row = templateSheet.createRow(rowid++);
	Object[] objectArr = apiCallMap.get(key);
	int cellid = 0;

	for (Object obj : objectArr) {
		Cell cell = row.createCell(cellid++);
		cell.setCellValue((String)obj);
	}
}


'Write into the output file'
File targetExcel = new File(resultFileName);

'Build file output'
FileOutputStream os = new FileOutputStream(targetExcel)
'Writing file output'
templateWb.write(os)

'Closing output file stream'
os.close();
'Closing output file'
templateWb.close();
'Closing input file stream'
templateFis.close();



