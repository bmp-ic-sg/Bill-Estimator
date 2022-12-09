import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import java.text.DecimalFormat

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import com.kms.katalon.core.testdata.reader.ExcelFactory
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import ic.sg.model.AfdCostModel
import ic.sg.model.BillEstimatorModel
import ic.sg.model.OOPPercentileModel
import ic.sg.oop.BillEstimatorComparison
import internal.GlobalVariable as GlobalVariable

'initiate keyword class'
BillEstimatorComparison billEstimatorComparison = new BillEstimatorComparison()

'Read xls payload source files'
def payloadSourceFile = ExcelFactory.getExcelDataWithDefaultSheet(GlobalVariable.PayloadSourceFile, GlobalVariable.PayloadSourceSheet,
		true)

DecimalFormat df = new DecimalFormat("#.##");


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

	def apiRequest = WS.sendRequest(findTestObject('Postman/Get Info', [('state') : state, ('languageCode') : languageCode
		, ('version') : version, ('hospitalCode') : hospitalName, ('tospCode') : tospCode, ('preAuth') : preAuth
		, ('ispItemUrl') : ispItemUrl]))

	if (WS.verifyResponseStatusCode(apiRequest, 200)) {
		KeywordUtil.markPassed('Success invoke API call')

		'Parsing response'
		def apiResponse = new groovy.json.JsonSlurper().parseText(apiRequest.getResponseBodyContent())

		if(apiResponse.oop != null || apiResponse.oop != ""){

			def oop50Percentile = new OOPPercentileModel()

			oop50Percentile.procedureCost = apiResponse.oop.'50thPercentile'.procedureCost
			oop50Percentile.panelOOPPrice = apiResponse.oop.'50thPercentile'.panelOOPPrice
			oop50Percentile.panelDeductible = apiResponse.oop.'50thPercentile'.panelDeductible
			oop50Percentile.panelCoInsurance = apiResponse.oop.'50thPercentile'.panelCoInsurance
			oop50Percentile.panelCoPayment = apiResponse.oop.'50thPercentile'.panelCoPayment

			def oop75Percentile = new OOPPercentileModel()

			oop75Percentile.procedureCost = apiResponse.oop.'75thPercentile'.procedureCost
			oop75Percentile.panelOOPPrice = apiResponse.oop.'75thPercentile'.panelOOPPrice
			oop75Percentile.panelDeductible = apiResponse.oop.'75thPercentile'.panelDeductible
			oop75Percentile.panelCoInsurance = apiResponse.oop.'75thPercentile'.panelCoInsurance
			oop75Percentile.panelCoPayment = apiResponse.oop.'75thPercentile'.panelCoPayment

			File billEstFile = new File(GlobalVariable.BillEstimateFile)

			FileInputStream billEstFis = new FileInputStream(billEstFile)

			XSSFWorkbook billEstWb = new XSSFWorkbook(billEstFis)

			XSSFSheet billEstSheet = billEstWb.getSheet(billEstimatorComparison.buildHospitalCode(hospitalName))

			def billEstimatorModel = new BillEstimatorModel()

			for (int rowIndex = 0; rowIndex < billEstSheet.getLastRowNum(); rowIndex++) {
				XSSFRow row = billEstSheet.getRow(rowIndex)

				if ((row != null) && (row.getCell(0) != null)) {
					String rowValue = row.getCell(0).getStringCellValue()

					if (rowValue.contains(tospCode)) {

						billEstimatorModel.tCode = row.getCell(0).getStringCellValue()
						billEstimatorModel.isDc = row.getCell(9).getNumericCellValue()
						billEstimatorModel.dc = row.getCell(11).getNumericCellValue()
						billEstimatorModel.dcP50 = row.getCell(12).getNumericCellValue()
						billEstimatorModel.dcP75 = row.getCell(13).getNumericCellValue()
						billEstimatorModel.sglP50 = row.getCell(14).getStringCellValue()
						billEstimatorModel.sglP75 = row.getCell(15).getStringCellValue()

						rowIndex = billEstSheet.getLastRowNum()
					}
				}
			}
			billEstWb.close();
			billEstFis.close();

			List <AfdCostModel> afdCostList50P = new ArrayList<>()
			List <AfdCostModel> afdCostList75P = new ArrayList<>()

			int maxloop = GlobalVariable.AfdEstimatorMaxLoop

			if(oop50Percentile != null) {
				int loop = 0
				billEstimatorComparison.setupAfdCostEstimate(billEstimatorModel.isDc, 1000,billEstimatorModel.sglP50)

				File afdEstFile = new File(GlobalVariable.AfdEstimatorFile)

				FileInputStream afdEstFis = new FileInputStream(afdEstFile)

				XSSFWorkbook afdEstWb = new XSSFWorkbook(afdEstFis)

				XSSFSheet afdEstSheet = afdEstWb.getSheet(billEstimatorComparison.validateAfdCostSheet(0))

				for (int rowIndex = 107; rowIndex < afdEstSheet.getLastRowNum(); rowIndex++) {
					println(loop)
					if(loop < maxloop) {
						XSSFRow afdRow = afdEstSheet.getRow(rowIndex)

						if ((afdRow != null) && (afdRow.getCell(1) != null)) {

							String afdRowValue = afdRow.getCell(1).getStringCellValue().replaceAll(" ", "-").toLowerCase()

							String preAuthorize = afdRow.getCell(4).getStringCellValue()

							String preAuthStr = preAuth ? "Yes" : "No"

							if (afdRowValue == ispItemUrl &&  preAuthorize == preAuthStr) {

								def afdCost50 = new AfdCostModel()
								afdCost50.ispItemUrl = afdRow.getCell(1).getStringCellValue()
								afdCost50.rider = afdRow.getCell(2).getStringCellValue()
								afdCost50.panel = afdRow.getCell(3).getStringCellValue()
								afdCost50.preAuth = afdRow.getCell(4).getStringCellValue()
								afdCost50.proRationFactor = Double.parseDouble(df.format(afdRow.getCell(5).getNumericCellValue()))
								afdCost50.deductible = Double.parseDouble(df.format(afdRow.getCell(6).getNumericCellValue()))
								afdCost50.coInsurance = afdRow.getCell(7).getNumericCellValue()
								afdCost50.coInsuranceWithCap = Double.parseDouble(df.format(afdRow.getCell(8).getNumericCellValue()))
								afdCost50.coPay = afdRow.getCell(9).getNumericCellValue()
								afdCost50.coPayCaping = afdRow.getCell(10).getNumericCellValue()
								afdCost50.coPayWithCap = Double.parseDouble(df.format(afdRow.getCell(11).getNumericCellValue()))
								afdCost50.stopLoss = afdRow.getCell(12).getStringCellValue()
								afdCost50.oopPayment = Double.parseDouble(df.format(afdRow.getCell(13).getNumericCellValue()))

								afdCostList50P.add(afdCost50)
								loop ++
							}
						}
					} else {
						rowIndex = afdEstSheet.getLastRowNum()
					}
				}

				afdEstWb.close();
				afdEstFis.close();


			}

			if(oop75Percentile != null) {
				int loop = 1

				billEstimatorComparison.setupAfdCostEstimate(billEstimatorModel.isDc, 2000,billEstimatorModel.sglP75)

				File afdEstFile = new File(GlobalVariable.AfdEstimatorFile)

				FileInputStream afdEstFis = new FileInputStream(afdEstFile)

				XSSFWorkbook afdEstWb = new XSSFWorkbook(afdEstFis)

				XSSFSheet afdEstSheet = afdEstWb.getSheet(billEstimatorComparison.validateAfdCostSheet(0))

				for (int rowIndex = 107; rowIndex < afdEstSheet.getLastRowNum(); rowIndex++) {
					println(loop)
					if(loop < maxloop) {
						XSSFRow afdRow = afdEstSheet.getRow(rowIndex)

						if ((afdRow != null) && (afdRow.getCell(1) != null)) {

							String afdRowValue = afdRow.getCell(1).getStringCellValue().replaceAll(" ", "-").toLowerCase()

							String preAuthorize = afdRow.getCell(4).getStringCellValue()

							String preAuthStr = preAuth ? "Yes" : "No"

							if (afdRowValue == ispItemUrl &&  preAuthorize == preAuthStr) {


								def afdCost75 = new AfdCostModel()
								afdCost75.ispItemUrl = afdRow.getCell(1).getStringCellValue()
								afdCost75.rider = afdRow.getCell(2).getStringCellValue()
								afdCost75.panel = afdRow.getCell(3).getStringCellValue()
								afdCost75.preAuth = afdRow.getCell(4).getStringCellValue()
								afdCost75.proRationFactor = Double.parseDouble(df.format(afdRow.getCell(5).getNumericCellValue()))
								afdCost75.deductible = Double.parseDouble(df.format(afdRow.getCell(6).getNumericCellValue()))
								afdCost75.coInsurance = afdRow.getCell(7).getNumericCellValue()
								afdCost75.coInsuranceWithCap = Double.parseDouble(df.format(afdRow.getCell(8).getNumericCellValue()))
								afdCost75.coPay = afdRow.getCell(9).getNumericCellValue()
								afdCost75.coPayCaping = afdRow.getCell(10).getNumericCellValue()
								afdCost75.coPayWithCap = Double.parseDouble(df.format(afdRow.getCell(11).getNumericCellValue()))
								afdCost75.stopLoss = afdRow.getCell(12).getStringCellValue()
								afdCost75.oopPayment = Double.parseDouble(df.format(afdRow.getCell(13).getNumericCellValue()))

								afdCostList75P.add(afdCost75)
								loop ++
							}
						}
					} else {
						rowIndex = afdEstSheet.getLastRowNum()
					}
				}

				afdEstWb.close();
				afdEstFis.close();
			}


			File outputFolder = new File(GlobalVariable.OutputFolder);

			if (!outputFolder.exists()) {
				outputFolder.mkdir();
			}

			String outputFilename = GlobalVariable.OutputFolder + hospitalName +'-'+ tospCode + '-' + ispItemUrl + '.xlsx';
			billEstimatorComparison.createOutputFile(outputFilename);

			XSSFWorkbook  exportWb = new XSSFWorkbook ();
			XSSFSheet  exportSheet = exportWb.createSheet("API Result");

			File sourceExcel = new File(outputFilename);
			FileInputStream fis = new FileInputStream(sourceExcel);

			// writing data into the sheet
			String payloadStr = billEstimatorComparison.buildPayload(languageCode, version, hospitalName, tospCode, preAuth, ispItemUrl);

			int resultIndex = 0;

			Map<Integer, Object[]> comparingResult = new HashMap<>()
			comparingResult.put(resultIndex +=1, ["Request", payloadStr])
			comparingResult.put(resultIndex +=1, ["Response",apiRequest.getResponseBodyContent()])
			comparingResult.put(resultIndex +=1, [])
			comparingResult.put(resultIndex +=1, ["TOSP Code", "TOSP Code Description", "Bill Estimator Procedure Naming", "Bill Estimator Procedure Description", "Body Part (Bill Estimator)", "Specific Body Part (Bill Estimator)", "Specialty", "Procedure Webpage URL (SMT)", "New Website URL", "DC", "IP-SGL", "DC?",  "DC P50 Total Bill Size", "DC P75 Total Bill Size", "SGL P50 Total Bill Size", "SGL P75 Total Bill Size" ])
			comparingResult.put(resultIndex +=1, [billEstimatorModel.tCode,'','','','','','','','','',billEstimatorModel.isDc, billEstimatorModel.dc, billEstimatorModel.dcP75 != "" ? df.format(billEstimatorModel.dcP50) : "", billEstimatorModel.dcP75 != "" ? df.format(billEstimatorModel.dcP75) : "", billEstimatorModel.sglP50 != "" ? df.format(billEstimatorModel.sglP50) : "",  billEstimatorModel.sglP75 != "" ? df.format(billEstimatorModel.sglP75):""])
			comparingResult.put(resultIndex +=1, [])
			comparingResult.put(resultIndex +=1, ["Insurer / Plan Name", "A) Rider", "B) Panel", "c) Pre-Authorisation", "1) Pro-Ration Factor", "2) Deductible (Day Surg)", "Co-Insurance", "3) Co-Insurance with capping", " Co-pay", "Co-pay capping", "(4) Co-pay with cap", "	Stop Loss", "OOP Payment"])

			if(afdCostList50P.size() >0) {
				comparingResult.put(resultIndex +=1, ["50 Percentile"])
				for (int i = 0; i < afdCostList50P.size(); i++) {
					comparingResult.put(resultIndex += 1, [afdCostList50P.get(i).ispItemUrl, afdCostList50P.get(i).rider, afdCostList50P.get(i).panel, afdCostList50P.get(i).preAuth,  df.format(afdCostList50P.get(i).proRationFactor),  df.format(afdCostList50P.get(i).deductible), afdCostList50P.get(i).coInsurance,  df.format(afdCostList50P.get(i).coInsuranceWithCap), afdCostList50P.get(i).coPay, afdCostList50P.get(i).coPayCaping,  df.format(afdCostList50P.get(i).coPayWithCap), afdCostList50P.get(i).stopLoss,  df.format(afdCostList50P.get(i).oopPayment)])
					comparingResult.put(resultIndex += 1, ["Comparing"])
					comparingResult.put(resultIndex += 1, ["Item","API Value", "AFD Cost Value", "Result"])
					comparingResult.put(resultIndex += 1, ["Pro-Ration Factor", oop50Percentile.procedureCost, afdCostList50P.get(i).proRationFactor, billEstimatorComparison.compare(Double.parseDouble(oop50Percentile.procedureCost), afdCostList50P.get(i).proRationFactor)])
					comparingResult.put(resultIndex += 1, ["Deductible (Day Surg)",oop50Percentile.panelDeductible, afdCostList50P.get(i).deductible, billEstimatorComparison.compare(Double.parseDouble(oop50Percentile.panelDeductible), afdCostList50P.get(i).deductible)])
					comparingResult.put(resultIndex += 1, ["Co-Insurance with capping",oop50Percentile.panelCoInsurance, afdCostList50P.get(i).coInsuranceWithCap, billEstimatorComparison.compare(Double.parseDouble(oop50Percentile.panelCoInsurance), afdCostList50P.get(i).coInsuranceWithCap)])
					comparingResult.put(resultIndex += 1, ["Co-pay with cap",oop50Percentile.panelCoPayment, afdCostList50P.get(i).coPayWithCap, billEstimatorComparison.compare(Double.parseDouble(oop50Percentile.panelCoPayment), afdCostList50P.get(i).coPayWithCap)])
					comparingResult.put(resultIndex += 1, ["OOP Payment",oop50Percentile.panelOOPPrice, afdCostList50P.get(i).oopPayment, billEstimatorComparison.compare(Double.parseDouble(oop50Percentile.panelOOPPrice), afdCostList50P.get(i).oopPayment)])
					comparingResult.put(resultIndex += 1, [])
				}
			}
			if(afdCostList75P.size() >0) {
				comparingResult.put(resultIndex += 1, ["75 Percentile"])

				for (int i = 0; i < afdCostList75P.size(); i++) {
					comparingResult.put(resultIndex += 1, [afdCostList75P.get(i).ispItemUrl, afdCostList75P.get(i).rider, afdCostList75P.get(i).panel, afdCostList75P.get(i).preAuth,  df.format(afdCostList75P.get(i).proRationFactor),  df.format(afdCostList75P.get(i).deductible), afdCostList75P.get(i).coInsurance,  df.format(afdCostList75P.get(i).coInsuranceWithCap), afdCostList75P.get(i).coPay, afdCostList75P.get(i).coPayCaping,  df.format(afdCostList75P.get(i).coPayWithCap), afdCostList75P.get(i).stopLoss,  df.format(afdCostList75P.get(i).oopPayment)])
					comparingResult.put(resultIndex += 1, ["Comparing"])
					comparingResult.put(resultIndex += 1, ["Item","API Value", "AFD Cost Value", "Result"])
					comparingResult.put(resultIndex += 1, ["Pro-Ration Factor", oop75Percentile.procedureCost, afdCostList75P.get(i).proRationFactor, billEstimatorComparison.compare(Double.parseDouble(oop75Percentile.procedureCost), afdCostList75P.get(i).proRationFactor)])
					comparingResult.put(resultIndex += 1, ["Deductible (Day Surg)",oop75Percentile.panelDeductible, afdCostList75P.get(i).deductible, billEstimatorComparison.compare(Double.parseDouble(oop75Percentile.panelDeductible), afdCostList75P.get(i).deductible)])
					comparingResult.put(resultIndex += 1, ["Co-Insurance with capping",oop75Percentile.panelCoInsurance, afdCostList75P.get(i).coInsuranceWithCap, billEstimatorComparison.compare(Double.parseDouble(oop75Percentile.panelCoInsurance), afdCostList75P.get(i).coInsuranceWithCap)])
					comparingResult.put(resultIndex += 1, ["Co-pay with cap",oop75Percentile.panelCoPayment, afdCostList75P.get(i).coPayWithCap, billEstimatorComparison.compare(Double.parseDouble(oop75Percentile.panelCoPayment), afdCostList75P.get(i).coPayWithCap)])
					comparingResult.put(resultIndex += 1, ["OOP Payment",oop75Percentile.panelOOPPrice, afdCostList75P.get(i).oopPayment, billEstimatorComparison.compare(Double.parseDouble(oop75Percentile.panelOOPPrice), afdCostList75P.get(i).oopPayment)])
					comparingResult.put(resultIndex += 1, [])
				}

			}
			Set<Integer> keyid = comparingResult.keySet();
			keyid.sort();
			println(keyid)
			int rowid = 0;

			for (Integer key : keyid) {

				row = exportSheet.createRow(rowid++);
				Object[] objectArr = comparingResult.get(key);
				int cellid = 0;

				for (Object obj : objectArr) {
					Cell cell = row.createCell(cellid++);
					cell.setCellValue((String)obj);
				}
			}


			File targetExcel = new File(outputFilename);
			FileOutputStream os = new FileOutputStream(targetExcel)
			exportWb.write(os)
			println "Writing into ${targetExcel}";

			os.close();
			exportWb.close();
			fis.close();

			KeywordUtil.markPassed('Finish')

		} else {
			KeywordUtil.markFailed('No OOP date to compare')
		}


	} else {
		KeywordUtil.markFailed('Failed to invoke API call')
	}
}

