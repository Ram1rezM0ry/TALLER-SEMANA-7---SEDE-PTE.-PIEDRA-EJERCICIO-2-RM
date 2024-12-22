package com.example.Semana7MascotasRM.controller;

import com.example.Semana7MascotasRM.model.Mascota;
import com.example.Semana7MascotasRM.service.MascotaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.properties.VerticalAlignment; // Importación correcta

import static com.itextpdf.kernel.pdf.PdfName.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping("/mascotas")
public class MascotaController {

    private final MascotaService service;

    public MascotaController(MascotaService service) {
        this.service = service;
    }

    @GetMapping
    public String listarMascotas(Model model) {
        model.addAttribute("mascotas", service.listarMascotas());
        return "lista_mascotas";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioMascota(Model model) {
        model.addAttribute("mascota", new Mascota());
        return "formulario_mascotas";
    }

   @PostMapping
public String guardarMascota(@ModelAttribute Mascota mascota) {
    try {
        // Verificamos si hay un archivo de imagen
        if (mascota.getArchivoImagen() != null && !mascota.getArchivoImagen().isEmpty()) {
            // Convertimos el archivo MultipartFile a byte[]
            mascota.setImagen(mascota.getArchivoImagen().getBytes());
        } else if (mascota.getId() != null) {
            // Si es una actualización y no hay nueva imagen, mantenemos la imagen existente
            Mascota mascotaExistente = service.buscarMascotaPorId(mascota.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Mascota no encontrada: " + mascota.getId()));
            mascota.setImagen(mascotaExistente.getImagen());
        }

        // Guardamos la mascota (se manejará el id automáticamente si es nuevo)
        service.guardarMascota(mascota);
    } catch (Exception e) {
        e.printStackTrace();
        return "error"; // Redirigir a una página de error si algo falla
    }
    return "redirect:/mascotas";
}


    @GetMapping("/editar/{id}")
    public String editarMascota(@PathVariable Long id, Model model) {
        model.addAttribute("mascota", service.buscarMascotaPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id)));
        return "formulario_mascotas";
    }


    @GetMapping("/eliminar/{id}")
    public String eliminarMascota(@PathVariable Long id) {
        service.eliminarMascota(id);
        return "redirect:/mascotas";
    }

   @GetMapping("/reporte/pdf")
public void generarPdf(HttpServletResponse response) throws IOException {
    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "inline; filename=mascotas_reporte.pdf");

    PdfDocument pdfDoc = new PdfDocument(new PdfWriter(response.getOutputStream()));
    Document document = new Document(pdfDoc);

    // Título con estilo moderno
    document.add(new Paragraph("Reporte de Mascotas")
            .setBold()
            .setFontSize(22)
            .setFontColor(ColorConstants.DARK_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20));

    // Creación de la tabla con un diseño más elegante
    Table table = new Table(6);
    table.setMarginBottom(20);

    // Encabezados con colores y bordes
    table.addCell(createHeaderCell("ID").setWidth(100)); // Ajuste el ancho
    table.addCell(createHeaderCell("Nombre").setWidth(150)); // Ajuste el ancho
    table.addCell(createHeaderCell("Especie").setWidth(150)); // Ajuste el ancho
    table.addCell(createHeaderCell("Raza").setWidth(150)); // Ajuste el ancho
    table.addCell(createHeaderCell("Edad").setWidth(100)); // Ajuste el ancho
    table.addCell(createHeaderCell("Imagen").setWidth(200)); // Ajuste el ancho

    List<Mascota> mascotas = StreamSupport.stream(service.listarMascotas().spliterator(), false)
                                          .collect(Collectors.toList());

    // Celdas con un espaciado adecuado y diseño dinámico
    for (Mascota mascota : mascotas) {
        table.addCell(createCell(String.valueOf(mascota.getId())).setWidth(100)); // Ajuste el ancho
        table.addCell(createCell(mascota.getNombre()).setWidth(150)); // Ajuste el ancho
        table.addCell(createCell(mascota.getEspecie()).setWidth(150)); // Ajuste el ancho
        table.addCell(createCell(mascota.getRaza()).setWidth(150)); // Ajuste el ancho
        table.addCell(createCell(String.valueOf(mascota.getEdad())).setWidth(100)); // Ajuste el ancho

        // Imagen con un tamaño adecuado
        if (mascota.getImagen() != null) {
            Image image = new Image(ImageDataFactory.create(mascota.getImagen()));
            image.setAutoScale(true);
            Cell imageCell = new Cell().add(image.setWidth(50).setHeight(50))
                                       .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)); // Bordes suaves
            table.addCell(imageCell.setWidth(200)); // Ajuste el ancho
        } else {
            table.addCell(createCell("Sin Imagen").setWidth(200)); // Ajuste el ancho
        }
    }

    document.add(table);
    document.close();
}

// Método para crear una celda de encabezado con estilo
private Cell createHeaderCell(String text) {
    return new Cell()
            .add(new Paragraph(text).setBold().setFontSize(12).setTextAlignment(TextAlignment.CENTER))
            .setBackgroundColor(new DeviceRgb(52, 152, 219)) // Color de fondo azul
            .setPadding(10)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(ColorConstants.WHITE)
            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)); // Bordes suaves
}

// Método para crear celdas normales con espaciado adecuado
private Cell createCell(String text) {
    return new Cell()
            .add(new Paragraph(text).setFontSize(10))
            .setPadding(10)
            .setTextAlignment(TextAlignment.CENTER) // Asegura que el texto esté centrado
            .setVerticalAlignment(VerticalAlignment.MIDDLE) // Alineación vertical centrada
            .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)); // Bordes suaves
}



 @GetMapping("/reporte/excel")
public void generarExcel(HttpServletResponse response) throws IOException {
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=mascotas_reporte.xlsx");

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Mascotas");

    // Ajustar ancho de las columnas
    sheet.setColumnWidth(0, 6000); // ID
    sheet.setColumnWidth(1, 8000); // Nombre
    sheet.setColumnWidth(2, 8000); // Especie
    sheet.setColumnWidth(3, 8000); // Raza
    sheet.setColumnWidth(4, 4000); // Edad
    sheet.setColumnWidth(5, 10000); // Imagen

    // Crear estilo para encabezados
    CellStyle headerStyle = workbook.createCellStyle();
    Font headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerFont.setFontHeightInPoints((short) 16);  // Aumentar aún más el tamaño de la fuente
    headerStyle.setFont(headerFont);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER); // Alineación vertical en Excel
    headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()); // Color de fondo más suave (azul claro)
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND); // Aplicar el color de fondo

    // Crear estilo para contenido
    CellStyle contentStyle = workbook.createCellStyle();
    Font contentFont = workbook.createFont();
    contentFont.setFontHeightInPoints((short) 12);
    contentStyle.setFont(contentFont);
    contentStyle.setBorderTop(BorderStyle.THIN);
    contentStyle.setBorderBottom(BorderStyle.THIN);
    contentStyle.setBorderLeft(BorderStyle.THIN);
    contentStyle.setBorderRight(BorderStyle.THIN);
    contentStyle.setAlignment(HorizontalAlignment.CENTER);
    contentStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER); // Alineación vertical en Excel

    // Crear fila de encabezados
    Row headerRow = sheet.createRow(0);
    String[] columnHeaders = {"ID", "Nombre", "Especie", "Raza", "Edad", "Imagen"};
    for (int i = 0; i < columnHeaders.length; i++) {
        org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
        cell.setCellValue(columnHeaders[i]);
        cell.setCellStyle(headerStyle);
    }

    // Llenar datos de mascotas
    List<Mascota> mascotas = StreamSupport
            .stream(service.listarMascotas().spliterator(), false)
            .collect(Collectors.toList());

    int rowIndex = 1;
    for (Mascota mascota : mascotas) {
        Row row = sheet.createRow(rowIndex++);
        row.setHeightInPoints(80); // Ajustar altura de filas de contenido

        org.apache.poi.ss.usermodel.Cell cellId = row.createCell(0);
        cellId.setCellValue(mascota.getId());
        cellId.setCellStyle(contentStyle);

        org.apache.poi.ss.usermodel.Cell cellNombre = row.createCell(1);
        cellNombre.setCellValue(mascota.getNombre());
        cellNombre.setCellStyle(contentStyle);

        org.apache.poi.ss.usermodel.Cell cellEspecie = row.createCell(2);
        cellEspecie.setCellValue(mascota.getEspecie());
        cellEspecie.setCellStyle(contentStyle);

        org.apache.poi.ss.usermodel.Cell cellRaza = row.createCell(3);
        cellRaza.setCellValue(mascota.getRaza());
        cellRaza.setCellStyle(contentStyle);

        org.apache.poi.ss.usermodel.Cell cellEdad = row.createCell(4);
        cellEdad.setCellValue(mascota.getEdad());
        cellEdad.setCellStyle(contentStyle);

        // Agregar imagen si está presente
        if (mascota.getImagen() != null) {
            int pictureIdx = workbook.addPicture(mascota.getImagen(), Workbook.PICTURE_TYPE_JPEG);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
            anchor.setCol1(5);
            anchor.setCol2(6); // Define el ancho fijo de la imagen
            anchor.setRow1(row.getRowNum());
            anchor.setRow2(row.getRowNum() + 1); // Define la altura fija de la imagen
            drawing.createPicture(anchor, pictureIdx);
        }
    }

    // Escribir el archivo
    workbook.write(response.getOutputStream());
    workbook.close();
    }
}

