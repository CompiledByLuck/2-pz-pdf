package com.example.sales.web;

import com.example.sales.port.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * REST API for sales reports – PDF download and on‑demand email delivery.
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** GET /reports/sales.pdf?month=YYYY-MM – returns the PDF inline. */
    @GetMapping("/sales.pdf")
    public ResponseEntity<byte[]> pdf(@RequestParam String month) {
        YearMonth period = parseMonth(month);
        log.info("PDF request for period {}", period);
        byte[] body = reportService.generate(period);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=sales-" + month + ".pdf")
                .body(body);
    }

    /** POST /reports/send?month=YYYY-MM – generate PDF and send it by email. */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@RequestParam String month) {
        YearMonth period = parseMonth(month);
        log.info("Email send request for period {}", period);
        reportService.generateAndSend(period);
    }

    private YearMonth parseMonth(String month) {
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            log.warn("Invalid month format received: {}", month);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid month format, expected YYYY-MM", ex);
        }
    }
}
