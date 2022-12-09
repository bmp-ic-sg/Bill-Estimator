import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import internal.GlobalVariable

//
//
///*
// * Reading ./Employee.xlsx file
// */
//File sourceExcel = new File(GlobalVariable.OutputFolder + "test.xlsx");
//FileInputStream fis = new FileInputStream(sourceExcel);
//
//// Open the .xlsx file and construct a workbook object
//XSSFWorkbook book = new XSSFWorkbook(fis)
//
//// Get the top sheet out of the workbook
//XSSFSheet sheet = book.getSheetAt(0)
//
//// writing data into the sheet
//int resultIndex = 0;
//Map<Integer, Object[]> newData = new HashMap<>()
////newData.put(String.valueOf(resultIndex += 1), [ "Name", "Wort", "Type", "Lead"])
////newData.put(String.valueOf(resultIndex += 1), [ "Sonya S", "75K", "SALES", "Rupert"])
////newData.put(String.valueOf(resultIndex += 1), [ "Kris S", "85K", "SALES", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "Dave S", "90K", "SALES", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "","","",""], )
////newData.put(String.valueOf(resultIndex += 1), [ "Name", "Wort", "Type", "Lead"])
////newData.put(String.valueOf(resultIndex += 1), [ "Sonya C", "75K", "CUSTOMER", "Rupert"])
////newData.put(String.valueOf(resultIndex += 1), [ "Kris C", "85K", "CUSTOMER", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "Dave C", "90K", "CUSTOMER", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "ASD C", "90K", "CUSTOMER", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "BDF C", "90K", "CUSTOMER", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "Dave D", "90K", "D", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "ASD D", "90K", "D", "Rupert" ])
////newData.put(String.valueOf(resultIndex += 1), [ "BDF D", "90K", "D", "Rupert" ])
//
//newData.put(resultIndex +=1, [ "Name", "Wort", "Type", "Lead"])
//newData.put(resultIndex +=1, [ "Sonya S", "75K", "SALES", "Rupert"])
//newData.put(resultIndex +=1, [ "Kris S", "85K", "SALES", "Rupert" ])
//newData.put(resultIndex +=1, [ "Dave S", "90K", "SALES", "Rupert" ])
//newData.put(resultIndex +=1, [ "Name", "Wort", "Type", "Lead"])
//newData.put(resultIndex +=1, [ "Sonya C", "75K", "CUSTOMER", "Rupert"])
//newData.put(resultIndex +=1, [ "Kris C", "85K", "CUSTOMER", "Rupert" ])
//newData.put(resultIndex +=1, [ "Dave C", "90K", "CUSTOMER", "Rupert" ])
//newData.put(resultIndex +=1, [ "ASD C", "90K", "CUSTOMER", "Rupert" ])
//newData.put(resultIndex +=1, [ "BDF C", "90K", "CUSTOMER", "Rupert" ])
//newData.put(resultIndex +=1, [ "Dave D", "90K", "D", "Rupert" ])
//newData.put(resultIndex +=1, [ "ASD D", "90K", "D", "Rupert" ])
//newData.put(resultIndex +=1, [ "BDF D", "90K", "D", "Rupert" ])
//
//Set<Integer> keyid = newData.keySet();
//keyid.sort();
//println(keyid)
//int rowid = 0;
//
//for (Integer key : keyid) {
//
//	row = sheet.createRow(rowid++);
//	Object[] objectArr = newData.get(key);
//	int cellid = 0;
//
//	for (Object obj : objectArr) {
//		Cell cell = row.createCell(cellid++);
//		cell.setCellValue((String)obj);
//	}
//}
//
////Set newRows = newData.keySet()
////int rownum = sheet.getLastRowNum()
//////int rownum = findLastRowNum(sheet)
////for (String key in newRows) {
////	Row row = sheet.createRow(rownum ++)
////	Object[] objArr = newData.get(key)
////	int cellnum = 0
////	for (Object obj : objArr) {
////		Cell cell = row.createCell(cellnum ++)
////		cell.setCellValue((String)obj);
////	}
////}
//
///*
// * Writing into the ./Employee.out.xlsx file
// */
//File targetExcel = new File(GlobalVariable.OutputFolder + "test.xlsx");
//FileOutputStream os = new FileOutputStream(targetExcel)
//book.write(os)
//println "Writing into ${targetExcel}";
//
//// Close workbook, OutputStream and Excel file to prevent leak
//os.close();
//book.close();
//fis.close();
//
//

int maxloop=GlobalVariable.AfdEstimatorMaxLoop

for(int index = 0; index < 10; index++)
{
	if(index < maxloop) {
		println "OK : " + index;
	} else {
		println "MAX LOOP REACHED";
	}
}


/**
 * find the index of the last row with a valid numeric value in the 1st column ("ID")
 */
//int findLastRowNum(Sheet sheet) {
//	int index = 0
//	int rx = 0
//	for (Row row in sheet) {
//		rx += 1
//		Cell cell = row.getCell(0)
//		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
//			index = rx
//		}
//	}
//	return index
//}