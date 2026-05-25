package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.domain.PackageUnit;
import ch.packops.packopsbackend.domain.Process;
import ch.packops.packopsbackend.domain.ProductConfigurationTranslation;
import ch.packops.packopsbackend.dto.DistributionItemDto;
import ch.packops.packopsbackend.dto.ProcessOverviewDto;
import ch.packops.packopsbackend.dto.ProductOverviewDto;
import ch.packops.packopsbackend.dto.StatisticsDto;
import ch.packops.packopsbackend.repository.PackageRepository;
import ch.packops.packopsbackend.repository.ProcessRepository;
import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author David M.
 */
@Service
public class StatisticsService {
    private final ProcessRepository processRepository;
    private final PackageRepository packageRepository;
    private final ProductConfigurationRepository productRepository;
    private final LoggingService loggingService;

    public StatisticsService(ProcessRepository processRepository,
                             PackageRepository packageRepository,
                             ProductConfigurationRepository productRepository,
                             LoggingService loggingService) {
        this.processRepository = processRepository;
        this.packageRepository = packageRepository;
        this.productRepository = productRepository;
        this.loggingService = loggingService;
    }

    @Transactional(readOnly = true)
    public StatisticsDto getProcessStatistics(Long processId, String language) {
        loggingService.logInfo("Statistiken abgerufen für Prozess: " + processId, processId);
        Process process = processRepository.findById(processId)
                .orElseThrow(() -> new RuntimeException("Process not found with id: " + processId));

        List<PackageUnit> packages = packageRepository.findByProcessId(processId);
        List<Process> processList = Collections.singletonList(process);

        StatisticsDto dto = calculateStats(processList, packages);
        dto.setAvailableProcesses(getAvailableProcesses(language));
        dto.setAvailableProducts(getAvailableProducts(language));
        return dto;
    }

    @Transactional(readOnly = true)
    public StatisticsDto getProductStatistics(Long productId, String language) {
        loggingService.logInfo("Statistiken abgerufen für Produkt: " + productId, null);
        List<Process> processes;
        List<PackageUnit> packages;

        if (productId == 0L) {
            processes = processRepository.findByProductConfigurationIsNull();
            packages = packageRepository.findByProcessProductConfigurationIsNull();
        } else {
            processes = processRepository.findByProductConfigurationId(productId);
            packages = packageRepository.findByProcessProductConfigurationId(productId);
        }

        StatisticsDto dto = calculateStats(processes, packages);
        dto.setAvailableProcesses(getAvailableProcesses(language));
        dto.setAvailableProducts(getAvailableProducts(language));
        return dto;
    }

    @Transactional(readOnly = true)
    public StatisticsDto getOverviewStatistics(String language) {
        loggingService.logInfo("Umfassende Statistiken abgerufen (Alle Prozesse)", null);
        List<Process> processes = processRepository.findAll();
        List<PackageUnit> allPackages = packageRepository.findAll();

        StatisticsDto dto = calculateStats(processes, allPackages);
        dto.setAvailableProcesses(getAvailableProcesses(language));
        dto.setAvailableProducts(getAvailableProducts(language));
        return dto;
    }

    private List<ProcessOverviewDto> getAvailableProcesses(String language) {
        String langCode = language != null ? language.toLowerCase() : "de";
        String processPrefix = langCode.equals("en") ? "Process #" : (langCode.equals("fr") ? "Processus #" : "Prozess #");

        return processRepository.findAll().stream()
                .map(p -> new ProcessOverviewDto(p.getId(),
                        processPrefix + p.getId() + " (" + (p.getTargetWeight() != null ? p.getTargetWeight() : 0)
                                + "g)"))
                .collect(Collectors.toList());
    }

    private List<ProductOverviewDto> getAvailableProducts(String language) {
        String langCode = language != null ? language.toLowerCase() : "de";

        List<ProductOverviewDto> list = productRepository.findAll().stream()
                .map(p -> {
                    // Extrahiere den Namen für die gewünschte Sprache, Fallback auf Deutsch
                    String translatedName = p.getTranslations().stream()
                            .filter(t -> t.getLanguageCode().equals(langCode))
                            .map(ProductConfigurationTranslation::getName)
                            .findFirst()
                            .orElseGet(() -> p.getTranslations().stream()
                                    .filter(t -> t.getLanguageCode().equals("de"))
                                    .map(ProductConfigurationTranslation::getName)
                                    .findFirst()
                                    .orElse("Unbekannt"));

                    return new ProductOverviewDto(p.getId(), translatedName);
                })
                .collect(Collectors.toList());

        // Hardcodierte ID 0 Übersetzung
        String noProductText = langCode.equals("en") ? "No product assigned" :
                (langCode.equals("fr") ? "Sans produit assigné" : "Ohne Produktzuweisung");
        list.add(0, new ProductOverviewDto(0L, noProductText));

        return list;
    }

    private StatisticsDto calculateStats(List<Process> processes, List<PackageUnit> packages) {
        StatisticsDto dto = new StatisticsDto();
        dto.setTotalProcesses(processes.size());
        dto.setTotalPackages(packages.size());

        calculateProcessMetrics(processes, dto);

        if (!packages.isEmpty()) {
            calculatePackageMetrics(packages, dto);
            calculatePackagesPerMinute(processes, packages.size(), dto);
            calculateWeightDistribution(packages, dto);
        } else {
            setEmptyPackageMetrics(dto);
        }

        return dto;
    }

    private void calculateProcessMetrics(List<Process> processes, StatisticsDto dto) {
        int totalDeadlocks = processes.stream()
                .filter(p -> p.getDeadlocksDetected() != null)
                .mapToInt(Process::getDeadlocksDetected)
                .sum();
        dto.setDeadlocksDetected(totalDeadlocks);

        double avgTargetWeight = processes.stream()
                .filter(p -> p.getTargetWeight() != null)
                .mapToInt(Process::getTargetWeight)
                .average().orElse(80.0);
        dto.setTargetWeight((int) Math.round(avgTargetWeight));
    }

    private double getAverageTolerance(List<Process> processes) {
        return processes.stream()
                .filter(p -> p.getTolerance() != null)
                .mapToInt(Process::getTolerance)
                .average().orElse(2.0);
    }

    private void calculatePackageMetrics(List<PackageUnit> packages, StatisticsDto dto) {
        double avgWeight = packages.stream()
                .filter(p -> p.getMeasuredWeight() != null)
                .mapToInt(PackageUnit::getMeasuredWeight)
                .average().orElse(0.0);
        dto.setAverageWeight(Math.round(avgWeight * 10.0) / 10.0);

        long goodCount = packages.stream()
                .filter(p -> p.getDeviation() != null && p.getProcess() != null
                        && p.getProcess().getTolerance() != null)
                .filter(p -> Math.abs(p.getDeviation()) <= p.getProcess().getTolerance())
                .count();
        dto.setGoodPackages((int) goodCount);
        dto.setYieldPercent(Math.round((goodCount * 100.0 / packages.size()) * 10.0) / 10.0);

        double giveawaySum = packages.stream()
                .filter(p -> p.getDeviation() != null && p.getDeviation() > 0)
                .mapToDouble(PackageUnit::getDeviation)
                .sum();
        double avgGiveaway = giveawaySum / packages.size();
        dto.setAverageGiveaway(Math.round(avgGiveaway * 10.0) / 10.0);
    }

    private void calculatePackagesPerMinute(List<Process> processes, int totalPackages, StatisticsDto dto) {
        long totalSeconds = processes.stream()
                .filter(p -> p.getStartTimestamp() != null)
                .mapToLong(p -> {
                    LocalDateTime end = p.getEndTimestamp() != null ? p.getEndTimestamp() : LocalDateTime.now();
                    long sec = Duration.between(p.getStartTimestamp(), end).getSeconds();
                    return sec > 0 ? sec : 1;
                })
                .sum();

        if (totalSeconds <= 0)
            totalSeconds = 1;
        dto.setPackagesPerMinute((int) (totalPackages * 60 / totalSeconds));
    }

    private void calculateWeightDistribution(List<PackageUnit> packages, StatisticsDto dto) {
        int MAX_BINS = 15;
        int minDev = packages.stream().filter(p -> p.getDeviation() != null).mapToInt(PackageUnit::getDeviation).min()
                .orElse(0);
        int maxDev = packages.stream().filter(p -> p.getDeviation() != null).mapToInt(PackageUnit::getDeviation).max()
                .orElse(0);

        int range = maxDev - minDev;
        if (range == 0)
            range = 1;

        double binSize = range <= MAX_BINS ? 1.0 : (double) range / MAX_BINS;
        int actualBins = range <= MAX_BINS ? range + 1 : MAX_BINS;

        Map<Integer, Integer> distMap = new TreeMap<>();
        for (int i = 0; i < actualBins; i++) {
            int binCenter = range <= MAX_BINS ? minDev + i : minDev + (int) Math.round((i + 0.5) * binSize);
            distMap.put(binCenter, 0);
        }

        for (PackageUnit p : packages) {
            if (p.getDeviation() != null) {
                int dev = p.getDeviation();
                int binIndex = (int) Math.floor((dev - minDev) / binSize);
                if (binIndex >= actualBins)
                    binIndex = actualBins - 1;
                if (binIndex < 0)
                    binIndex = 0;

                int binCenter = range <= MAX_BINS ? dev : minDev + (int) Math.round((binIndex + 0.5) * binSize);
                distMap.merge(binCenter, 1, Integer::sum);
            }
        }

        List<DistributionItemDto> distList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : distMap.entrySet()) {
            boolean isTgt = range <= MAX_BINS ? entry.getKey() == 0 : Math.abs(entry.getKey()) <= (binSize / 2);
            String prefix = entry.getKey() > 0 ? "+" : "";
            distList.add(new DistributionItemDto(prefix + entry.getKey() + "g", entry.getValue(), isTgt));
        }
        dto.setWeightDistribution(distList);
    }

    private void setEmptyPackageMetrics(StatisticsDto dto) {
        dto.setAverageWeight(0.0);
        dto.setGoodPackages(0);
        dto.setYieldPercent(0.0);
        dto.setAverageGiveaway(0.0);
        dto.setPackagesPerMinute(0);
        dto.setWeightDistribution(new ArrayList<>());
    }
}