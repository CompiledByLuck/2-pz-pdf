package com.example.sales.service;

import com.example.sales.domain.ReportData;
import com.example.sales.domain.SalesSummary;
import com.example.sales.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;

import java.time.YearMonth;

@Service
@Primary
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final SaleRepository saleRepository;
    private final SummaryCalculator summaryCalculator;
    private final ReportGenerator reportGenerator;
    private final ReportSender reportSender;

    public ReportServiceImpl(SaleRepository saleRepository,
                             SummaryCalculator summaryCalculator,
                             ReportGenerator reportGenerator,
                             ReportSender reportSender) {
        this.saleRepository = saleRepository;
        this.summaryCalculator = summaryCalculator;
        this.reportGenerator = reportGenerator;
        this.reportSender = reportSender;
    }

    @Override
    public ReportData collect(YearMonth period) {
        log.info("Collecting sales data for {}", period);
        var sales = saleRepository.findByMonth(period.getYear(), period.getMonthValue());
        log.debug("Found {} sales records", sales.size());
        SalesSummary summary = summaryCalculator.calculate(sales);
        return new ReportData(period, sales, summary);
    }

    @Override
    public byte[] generate(YearMonth period) {
        log.info("Generating PDF for {}", period);
        ReportData data = collect(period);
        return reportGenerator.generate(data);
    }

    @Override
    public void generateAndSend(YearMonth period) {
        log.info("Generating and sending report for {}", period);
        ReportData data = collect(period);
        byte[] document = reportGenerator.generate(data);
        String filename = "sales-" + period + "." + reportGenerator.fileExtension();
        try {
            reportSender.send(data, document, filename);
            log.info("Report email sent successfully for {}", period);
        } catch (Exception e) {
            log.error("Failed to send report email for {}", period, e);
            throw e;
        }
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void scheduledReport() {
        YearMonth previous = YearMonth.now().minusMonths(1);
        log.info("Running scheduled report for previous month {}", previous);
        generateAndSend(previous);
    }
}
