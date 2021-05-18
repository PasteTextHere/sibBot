package sib.bot.serviceAndRepository

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.stereotype.Controller
import sib.bot.model.DataForExcel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

@Controller
class ExcelService {

    fun editExcelFile(dataForExcel: DataForExcel) {
        val excelFile = HSSFWorkbook(FileInputStream("ExcelFile.xls"))
        val excelSheet = excelFile.getSheet("Sheet1")
        val lastRow = excelSheet.lastRowNum
        val dateFormat = excelSheet.getRow(0).getCell(10).cellStyle
        val excelRow = excelSheet.createRow(lastRow + 1)
        with(excelRow) {
            createCell(0)
            getCell(0).setCellStyle(dateFormat)
            getCell(0).setCellValue(Date(dataForExcel.date))
            createCell(1).setCellValue(dataForExcel.carNumber.toString())
            createCell(2).setCellValue(dataForExcel.driver)
            createCell(3).setCellValue(dataForExcel.odoStart.toString())
            createCell(4).setCellValue(dataForExcel.odoFinish.toString())
            createCell(5).setCellValue(dataForExcel.odoTotal.toString())
            createCell(6).setCellValue(dataForExcel.fuelAdded)
            createCell(7).setCellValue(dataForExcel.fuelStart)
            createCell(8).setCellValue(dataForExcel.fuelFinish)
        }
        excelFile.write(FileOutputStream("ExcelFile.xls", false))
        excelFile.close()
    }

    fun createExcel(){
        val excelFile = HSSFWorkbook()
        val excelSheet = excelFile.createSheet("Sheet1")
        val excelRowTitle = excelSheet.createRow(0)
        val dateFormat = excelFile.createCellStyle()
        dateFormat.dataFormat = excelFile.createDataFormat().getFormat("dd.mm.yyyy")
        excelRowTitle.createCell(10)
        excelRowTitle.getCell(10).setCellStyle(dateFormat)

        with(excelRowTitle) {
            createCell(0).setCellValue("Дата")
            createCell(1).setCellValue("Номер авто")
            createCell(2).setCellValue("Водитель")
            createCell(3).setCellValue("Пробег начало")
            createCell(4).setCellValue("Пробег конец")
            createCell(5).setCellValue("Общий пробег")
            createCell(6).setCellValue("Заправка")
            createCell(7).setCellValue("Бензин начало")
            createCell(8).setCellValue("Бензин конец")
        }
        excelSheet.autoSizeColumn(0)
        excelFile.write(FileOutputStream("./ExcelFile.xls", false))
        excelFile.close()
    }
}
