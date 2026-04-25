package Control_inventario.control_inventario.controlador;

import Control_inventario.control_inventario.entidad.Movimiento;
import Control_inventario.control_inventario.entidad.Producto;
import Control_inventario.control_inventario.repositorio.MovimientoRepositorio;
import Control_inventario.control_inventario.repositorio.ProductoRepositorio;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/reportes")
public class ReporteControlador {

    @Autowired
    private ProductoRepositorio productoRepo;

    @Autowired
    private MovimientoRepositorio movimientoRepo;

    @GetMapping("/inventario/pdf")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR')")
    public ResponseEntity<byte[]> generarReporteInventario() {
        try {
            List<Producto> productos = productoRepo.findAll();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
            Font pie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

            Paragraph empresa = new Paragraph("NEXSTOCK", titulo);
            empresa.setAlignment(Element.ALIGN_LEFT);
            doc.add(empresa);

            doc.add(new Paragraph("Sistema de Control de Inventario", subtitulo));
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            doc.add(new Paragraph("Fecha de generación: " + fecha, subtitulo));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setWidths(new float[]{1.2f, 2.5f, 1.6f, 1.2f, 1f, 1f, 1.5f, 1.6f});

            String[] headers = {
                    "SKU", "Nombre", "Categoría", "Stock",
                    "Mín.", "Máx.", "Valor Unitario", "Valor Total"
            };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(120, 103, 255));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(7);
                table.addCell(cell);
            }

            double totalInventario = 0;

            for (Producto p : productos) {
                int stock = p.getStock() != null ? p.getStock() : 0;
                int minimo = p.getMinimo() != null ? p.getMinimo() : 0;
                int maximo = p.getStockMaximo() != null ? p.getStockMaximo() : 0;
                double precio = p.getPrecioUnitario() != null ? p.getPrecioUnitario().doubleValue() : 0;
                double total = stock * precio;
                totalInventario += total;

                table.addCell(valorTexto(p.getSku()));
                table.addCell(valorTexto(p.getNombre()));
                table.addCell(valorTexto(p.getCategoria()));
                table.addCell(String.valueOf(stock));
                table.addCell(String.valueOf(minimo));
                table.addCell(String.valueOf(maximo));
                table.addCell(formatearMoneda(precio));
                table.addCell(formatearMoneda(total));
            }

            doc.add(table);
            doc.add(new Paragraph(" "));

            Paragraph total = new Paragraph("Valor total del inventario: " + formatearMoneda(totalInventario), totalFont);
            total.setAlignment(Element.ALIGN_RIGHT);
            doc.add(total);

            doc.add(new Paragraph(" "));
            Paragraph footer = new Paragraph("Nexstock © - Reporte generado automáticamente", pie);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario_nexstock.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error al generar reporte: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/inventario/excel")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR')")
    public ResponseEntity<byte[]> generarReporteInventarioExcel() {
        try {
            List<Producto> productos = productoRepo.findAll();

            Workbook workbook = new XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Inventario Nexstock");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            org.apache.poi.ss.usermodel.Row titulo = sheet.createRow(0);
            titulo.createCell(0).setCellValue("NEXSTOCK - REPORTE DE INVENTARIO");

            org.apache.poi.ss.usermodel.Row fecha = sheet.createRow(1);
            fecha.createCell(0).setCellValue("Fecha de generación:");
            fecha.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            org.apache.poi.ss.usermodel.Row header = sheet.createRow(3);
            String[] titulos = {
                    "SKU", "Nombre", "Categoría", "Stock Actual",
                    "Mínimo", "Máximo", "Precio Unitario", "Valor Total"
            };

            for (int i = 0; i < titulos.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(i);
                cell.setCellValue(titulos[i]);
                cell.setCellStyle(headerStyle);
            }

            double totalInventario = 0;
            int rowNum = 4;

            for (Producto p : productos) {
                int stock = p.getStock() != null ? p.getStock() : 0;
                double precio = p.getPrecioUnitario() != null ? p.getPrecioUnitario().doubleValue() : 0;
                double valorTotal = stock * precio;
                totalInventario += valorTotal;

                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(valorTexto(p.getSku()));
                row.createCell(1).setCellValue(valorTexto(p.getNombre()));
                row.createCell(2).setCellValue(valorTexto(p.getCategoria()));
                row.createCell(3).setCellValue(stock);
                row.createCell(4).setCellValue(p.getMinimo() != null ? p.getMinimo() : 0);
                row.createCell(5).setCellValue(p.getStockMaximo() != null ? p.getStockMaximo() : 0);
                row.createCell(6).setCellValue(precio);
                row.createCell(7).setCellValue(valorTotal);
            }

            org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowNum + 1);
            totalRow.createCell(6).setCellValue("Total Inventario");
            totalRow.createCell(7).setCellValue(totalInventario);

            for (int i = 0; i < titulos.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventario_nexstock.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error al generar Excel: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/movimientos/pdf")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','SUPERVISOR','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_SUPERVISOR')")
    public ResponseEntity<byte[]> generarReporteMovimientos() {
        try {
            List<Movimiento> movs = movimientoRepo.findAll();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            Paragraph empresa = new Paragraph("NEXSTOCK", titulo);
            empresa.setAlignment(Element.ALIGN_LEFT);
            doc.add(empresa);

            doc.add(new Paragraph("Reporte de Movimientos de Inventario", subtitulo));
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            doc.add(new Paragraph("Fecha de generación: " + fecha, subtitulo));
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.8f, 1.1f, 2.2f, 1.1f, 1.2f, 1.2f, 1.5f});

            String[] headers = {
                    "Fecha", "Tipo", "Producto", "Cantidad",
                    "Stock Antes", "Stock Después", "Usuario"
            };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(new Color(120, 103, 255));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(7);
                table.addCell(cell);
            }

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
            doc.add(new Paragraph(" "));

            Font pie = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
            Paragraph footer = new Paragraph("Nexstock © - Reporte generado automáticamente", pie);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movimientos_nexstock.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(("Error al generar reporte: " + e.getMessage()).getBytes());
        }
    }

    private String valorTexto(String valor) {
        return valor != null && !valor.isBlank() ? valor : "-";
    }

    private String formatearMoneda(double valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        formato.setMaximumFractionDigits(0);
        return formato.format(valor);
    }
}