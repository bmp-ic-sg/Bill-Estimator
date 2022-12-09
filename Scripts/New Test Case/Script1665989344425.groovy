import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.keyword.excel.ExcelKeywords




'Generating random UUID for State'
def state = UUID.randomUUID().toString()

'Building TOSPCode'
def topsCode = 'ABCDF'

'Building Hospital Code'
def hospitalCode = 'Mount Elizabeth Novena Hospital'

'Building ISP Item URL'
def ispItemUrl = 'prudential-prushield-premier'

'Calling API'
def responseData = WS.sendRequest(findTestObject('Postman/Get Info', [('state') : state, ('languageCode') : 'EN', ('version') : 'DRAFT'
            , ('hospitalCode') : hospitalCode, ('tospCode') : topsCode, ('preAuth') : true, ('ispItemUrl') : ispItemUrl]))

'Parsing response'
def result = new groovy.json.JsonSlurper().parseText(responseData.getResponseBodyContent())

'Log response'
KeywordUtil.logInfo(result.toString())

'Verify response from API'
if (result == null) {
    'An error was encountered while calling API'
    KeywordUtil.markErrorAndStop('An error was encountered while calling API')
}

'Mapping result 50%'
Map data50thPercentile = result.oop.'50thPercentile'

'Log response 50%'
KeywordUtil.logInfo(data50thPercentile.toString())

'Mapping result 75%'
Map data75thPercentile = result.oop.'75thPercentile'

'Log response 75%'
KeywordUtil.logInfo(data75thPercentile.toString())

'Location of Estimated Bill xls file'
String excelBillEstimateStr = 'C:\\Users\\wolfgang\\Documents\\bmp\\GreatestArtWork\\Automation Testing\\SDMS\\XLS Estimated Bill Size_AFD Campaign.xlsx'

'Opening Estimated Bill xls file'
def workBookBillEstimate = ExcelKeywords.getWorkbook(excelBillEstimateStr)

def billEstTab

'Verify Hospital Code'
switch (hospitalCode) {
	case('Mount Elizabeth Novena Hospital'):
	billEstTab = "MNH"
	KeywordUtil.logInfo(billEstTab.toString())
	break
	
    default:
        break
}

'Reading xls file with hospital code tab'
def sheetA = ExcelKeywords.getExcelSheet(workBookBillEstimate, billEstTab)

'Searching TOSPCode'
def searchPlace = ExcelKeywords.getCellValueByAddress(sheetA, 'A6')

if (searchPlace != topsCode) {
	
	'TOSPCode not found'
	KeywordUtil.markErrorAndStop('TOSPCode ('+topsCode+') not found in ' +billEstTab+ ' tab')
	
}

'Getting result of 50%'
def p50 = ExcelKeywords.getCellValueByAddress(sheetA, 'M6')

'result of 50% TOSPCode'
KeywordUtil.logInfo('result of 50% TOSPCode ('+topsCode+') = ' + p50)

'Getting result of 75%'
def p75 = ExcelKeywords.getCellValueByAddress(sheetA, 'N6')

'result of 75% TOSPCode'
KeywordUtil.logInfo('result of 75% TOSPCode ('+topsCode+') = ' + p75)

'Location of Cost Estimated xls file'
String excelCostEstimateStr = 'C:\\Users\\wolfgang\\Documents\\bmp\\GreatestArtWork\\Automation Testing\\SDMS\\AFD-cost-estimator-2022Apr28.xlsx'

'Opening  Cost Estimated xls file'
def workBookCostEstimate = ExcelKeywords.getWorkbook(excelCostEstimateStr)

'Opening  Cost Estimated Bill Estimator (DC) tab'
def sheetB = ExcelKeywords.getExcelSheet(workBookCostEstimate, 'Bill Estimator (DC)')

'Setup Bill Estimator with 50%'
ExcelKeywords.setValueToCellByAddress(sheetB, 'B107', p50)

'Execute Bill Estimator with 50%'
ExcelKeywords.saveWorkbook(excelCostEstimateStr, workBookCostEstimate)

'Searching Bill Estimator with 50% with ISP Item URL'
def searchPlace1 = ExcelKeywords.getCellValueByAddress(sheetB, 'B176')

'Find result Bill Estimator with 50% with ISP Item URL'
KeywordUtil.logInfo('Searching of 50% ' + searchPlace1)

'Found Deductible (Day Surg)'
def r502 = ExcelKeywords.getCellValueByAddress(sheetB, 'G176')

'Found Co-Insurance with capping'
def r503 = ExcelKeywords.getCellValueByAddress(sheetB, 'I176')

'Found Co-pay with cap'
def r504 = ExcelKeywords.getCellValueByAddress(sheetB, 'L176')

'Found OOP Payment'
def r505 = ExcelKeywords.getCellValueByAddress(sheetB, 'N176')


'Comparing Deductible (Day Surg)'
switch (data50thPercentile.panelDeductible) {
	case  r502:
	'Comparing Deductible (Day Surg) result'
	KeywordUtil.logInfo('PASS = '+data50thPercentile.panelDeductible+' : ' +r502)
	KeywordUtil.markPassed('PASS = '+data50thPercentile.panelDeductible+' : ' +r502)
		break
	
	default:
	'Comparing Deductible (Day Surg) result'
	KeywordUtil.logInfo('FAILED = '+data50thPercentile.panelDeductible+' : ' +r502)
	KeywordUtil.markFailed('FAILED = '+data50thPercentile.panelDeductible+' : ' +r502)
		break
}
'Comparing Co-Insurance with capping'
switch (data50thPercentile.panelCoInsurance) {
	case r503:
	'Comparing Co-Insurance with capping result'
	KeywordUtil.logInfo('PASS = '+data50thPercentile.panelCoInsurance+' : ' +r503)
	KeywordUtil.markPassed('PASS = '+data50thPercentile.panelCoInsurance+' : ' +r503)
		break
		
	default:
	'Comparing Co-Insurance with capping result'
	KeywordUtil.logInfo('FAILED = '+data50thPercentile.panelCoInsurance+' : ' +r503)
	KeywordUtil.markFailed('FAILED = '+data50thPercentile.panelCoInsurance+' : ' +r503)
	break
}

'Comparing Co-pay with cap'
switch (data50thPercentile.panelCoPayment) {
	case r504+'.0':
	'Comparing Co-pay with cap result'
	KeywordUtil.logInfo('PASS = '+data50thPercentile.panelCoPayment+' : ' +r504)
	KeywordUtil.markPassed('PASS = '+data50thPercentile.panelCoPayment+' : ' +r504)
	break
	
	default:
	'Comparing Co-pay with cap result'
	KeywordUtil.logInfo('FAILED = '+data50thPercentile.panelCoPayment+' : ' +r504)
	KeywordUtil.markFailed('FAILED = '+data50thPercentile.panelCoPayment+' : ' +r504)
		break
}

'Comparing OOP Payment'
switch (data50thPercentile.panelOOPPrice) {
	case r505:
	
	'Comparing OOP Payment result'
	KeywordUtil.logInfo('PASS = '+data50thPercentile.panelOOPPrice+' : ' +r505)
	KeywordUtil.markPassed('PASS = '+data50thPercentile.panelOOPPrice+' : ' +r505)
	break
	
	default:
	'Comparing OOP Payment result'
	KeywordUtil.logInfo('FAILED = '+data50thPercentile.panelOOPPrice+' : ' +r505)
	KeywordUtil.markFailed('FAILED = '+data50thPercentile.panelOOPPrice+' : ' +r505)
		break
}

'Setup Bill Estimator with 75%'
ExcelKeywords.setValueToCellByAddress(sheetB, 'B107', p75)

'Execute Bill Estimator with 75%'
ExcelKeywords.saveWorkbook(excelCostEstimateStr, workBookCostEstimate)

'Searching Bill Estimator with 75% with ISP Item URL'
def searchPlace2 = ExcelKeywords.getCellValueByAddress(sheetB, 'B176')

'Find result Bill Estimator with 75% with ISP Item URL'
KeywordUtil.logInfo('Searching of 75% ' + searchPlace2)

'Found Deductible (Day Surg)'
def r752 = ExcelKeywords.getCellValueByAddress(sheetB, 'G176')

'Found Co-Insurance with capping'
def r753 = ExcelKeywords.getCellValueByAddress(sheetB, 'I176')

'Found Co-pay with cap'
def r754 = ExcelKeywords.getCellValueByAddress(sheetB, 'L176')

'Found OOP Payment'
def r755 = ExcelKeywords.getCellValueByAddress(sheetB, 'N176')

'Comparing Deductible (Day Surg)'
switch (data75thPercentile.panelDeductible) {
	case  r752:
	'Comparing Deductible (Day Surg) result'
	KeywordUtil.logInfo('PASS = '+data75thPercentile.panelDeductible+' : ' +r752)
	KeywordUtil.markPassed('PASS = '+data75thPercentile.panelDeductible+' : ' +r752)
	break
	
	default:
	'Comparing Deductible (Day Surg) result'
	KeywordUtil.logInfo('FAILED = '+data75thPercentile.panelDeductible+' : ' +r752)
	KeywordUtil.markFailed('FAILED = '+data75thPercentile.panelDeductible+' : ' +r752)
		break
}
'Comparing Co-Insurance with capping'
switch (data75thPercentile.panelCoInsurance) {
	case r753:
	'Comparing Co-Insurance with capping result'
	KeywordUtil.logInfo('PASS = '+data75thPercentile.panelCoInsurance+' : ' +r753)
	KeywordUtil.markPassed('PASS = '+data75thPercentile.panelCoInsurance+' : ' +r753)
	break
		
	default:
	'Comparing Co-Insurance with capping result'
	KeywordUtil.logInfo('FAILED = '+data75thPercentile.panelCoInsurance+' : ' +r753)
	KeywordUtil.markFailed('FAILED = '+data75thPercentile.panelCoInsurance+' : ' +r753)
		break
}

'Comparing Co-pay with cap'
switch (data75thPercentile.panelCoPayment) {
	case r754+'.0':
	'Comparing Co-pay with cap result'
	KeywordUtil.logInfo('PASS = '+data75thPercentile.panelCoPayment+' : ' +r754)
	KeywordUtil.markPassed('PASS = '+data75thPercentile.panelCoPayment+' : ' +r754)
	break
	
	default:
	'Comparing Co-pay with cap result'
	KeywordUtil.logInfo('FAILED = '+data75thPercentile.panelCoPayment+' : ' +r754)
	KeywordUtil.markFailed('FAILED = '+data75thPercentile.panelCoPayment+' : ' +r754)
		break
}

'Comparing OOP Payment'
switch (data75thPercentile.panelOOPPrice) {
	case r755:
	'Comparing OOP Payment result'
	KeywordUtil.logInfo('PASS = '+data75thPercentile.panelOOPPrice+' : ' +r755)
	KeywordUtil.markPassed('PASS = '+data75thPercentile.panelOOPPrice+' : ' +r755)
	break
	
	default:
	'Comparing OOP Payment result'
	KeywordUtil.logInfo('FAILED = '+data75thPercentile.panelOOPPrice+' : ' +r755)
	KeywordUtil.markFailed('FAILED = '+data75thPercentile.panelOOPPrice+' : ' +r755)
		break
}

