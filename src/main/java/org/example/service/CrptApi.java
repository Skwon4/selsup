package org.example.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class CrptApi {

    private final static String BASE_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final RestClient restClient;
    private final Limiter limiter;

    public CrptApi(TimeUnit timeUnit, int requestLimit, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(BASE_URL).build();
        limiter = new Limiter(timeUnit, requestLimit);
    }

    public String create(Document document, String certificate) {
        limiter.limit();
        document.setCertificate(certificate); //not sure what is this for
        return restClient.post()
                .contentType(APPLICATION_JSON)
                .body(document)
                .retrieve()
                .body(String.class);
    }


    public static class Limiter {
        private final TimeUnit timeUnit;
        private final int requestLimit;
        private final Semaphore semaphore;
        private final LinkedList<ZonedDateTime> buffer;

        public Limiter(TimeUnit timeUnit, int requestLimit) {
            this.timeUnit = timeUnit;
            this.requestLimit = requestLimit;
            semaphore = new Semaphore(1, true);
            buffer = new LinkedList<>();
        }

        public void limit() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            while (true) {
                if (checkLimit()) break;
            }

            if (buffer.size() < requestLimit) {
                buffer.addFirst(ZonedDateTime.now());
            } else {
                buffer.removeLast();
                buffer.addFirst(ZonedDateTime.now());
            }

            semaphore.release();
        }

        private boolean checkLimit() {
            if (buffer.size() < requestLimit) {
                return true;
            }

            ZonedDateTime lowLimit = ZonedDateTime.now()
                    .minusNanos(TimeUnit.NANOSECONDS.convert(1, timeUnit));

            return lowLimit.isAfter(buffer.getLast());
        }

    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Document {
        Map<String, String> description;
        String docId;
        String docStatus;
        String docType;
        @JsonProperty("importRequest")
        Boolean importRequest;
        String ownerInn;
        String participantInn;
        String producerInn;
        LocalDate productionDate;
        String productionType;
        List<Product> products;
        LocalDate regDate;
        String regNumber;
        String certificate;

        public void setCertificate(String certificate) {
            this.certificate = certificate;
        }

        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Product {
            String certificateDocument;
            LocalDate certificateDocumentDate;
            String certificateDocumentNumber;
            String ownerInn;
            String producerInn;
            LocalDate productionDate;
            String tnvedCode;
            String uitCode;
            String uituCode;
        }
    }
}
