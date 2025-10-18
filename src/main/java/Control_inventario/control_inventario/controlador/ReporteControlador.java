package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.entidad.Producto;
import Control_inventario.control_inventario.repositorio.MovimientoRepositorio;
import Control_inventario.control_inventario.repositorio.ProductoRepositorio;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    @Autowired
    private ProductoRepositorio productoRepo;

    @Autowired
    private MovimientoRepositorio movimientoRepo;

    @GetMapping("/inventario/pdf")
    public ResponseEntity<byte[]> generarReporteInventario() {
        try {
            List<Producto> productos = productoRepo.findAll();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            doc.add(new Paragraph("REPORTE DE INVENTARIO", titulo));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("SKU");
            table.addCell("Nombre");
            table.addCell("Categoría");
            table.addCell("Stock Actual");
            table.addCell("Mínimo");
            table.addCell("Máximo");

            for (Producto p : productos) {
                table.addCell(p.getSku() != null ? p.getSku() : "-");
                table.addCell(p.getNombre() != null ? p.getNombre() : "-");
                table.addCell(p.getCategoria() != null ? p.getCategoria() : "-");
                table.addCell(String.valueOf(p.getStock() != null ? p.getStock() : 0));
                table.addCell(String.valueOf(p.getMinimo() != null ? p.getMinimo() : 0));

                table.addCell(String.valueOf(p.getStockMaximo() != null ? p.getStockMaximo() : 0));
            }

            doc.add(table);
            doc.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error al generar reporte: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/movimientos/pdf")
    public ResponseEntity<byte[]> generarReporteMovimientos() {
        try {
            List<Movimiento> movs = movimientoRepo.findAll();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            doc.add(new Paragraph("REPORTE DE MOVIMIENTOS", titulo));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.addCell("Fecha");
            table.addCell("Tipo");
            table.addCell("Producto");
            table.addCell("Cantidad");
            table.addCell("Stock Antes");
            table.addCell("Stock Después");
            table.addCell("Usuario");

            for (Movimiento m : movs) {
                table.addCell(m.getFecha() != null ? m.getFecha().toString() : "-");
                table.addCell(m.getTipo() != null ? m.getTipo() : "-");
                table.addCell(m.getProductoNombre() != null ? m.getProductoNombre() : "-");
                table.addCell(String.valueOf(m.getCantidad() != null ? m.getCantidad() : 0));
                table.addCell(String.valueOf(m.getStockAntes() != null ? m.getStockAntes() : 0));
                table.addCell(String.valueOf(m.getStockDespues() != null ? m.getStockDespues() : 0));
                table.addCell(m.getUsuario() != null ? m.getUsuario() : "-");
            }

            doc.add(table);
            doc.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movimientos.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error al generar reporte: " + e.getMessage()).getBytes());
        }
    }
    @GetMapping("/inventario/excel")
    public ResponseEntity<byte[]> generarReporteInventarioExcel() {
        try {
            List<Producto> productos = productoRepo.findAll();

            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Inventario");


            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            String[] titulos = {"SKU", "Nombre", "Categoría", "Stock Actual", "Mínimo", "Máximo", "Precio Unitario"};
            for (int i = 0; i < titulos.length; i++) {
                header.createCell(i).setCellValue(titulos[i]);
            }


            int rowNum = 1;
            for (Producto p : productos) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getSku());
                row.createCell(1).setCellValue(p.getNombre());
                row.createCell(2).setCellValue(p.getCategoria());
                row.createCell(3).setCellValue(p.getStock());
                row.createCell(4).setCellValue(p.getMinimo());
                row.createCell(5).setCellValue(p.getStockMaximo());
                row.createCell(6).setCellValue(p.getPrecioUnitario());
            }


            for (int i = 0; i < titulos.length; i++) {
                sheet.autoSizeColumn(i);
            }


            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error al generar Excel: " + e.getMessage()).getBytes());
        }
    }
}