package com.contactmanagement.service;

import com.contactmanagement.dto.ContactEmailDto;
import com.contactmanagement.dto.ContactPhoneDto;
import com.contactmanagement.dto.CreateContactRequest;
import com.contactmanagement.model.Contact;
import com.contactmanagement.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactImportExportService {

    private final ContactRepository contactRepository;
    private final ContactService    contactService;

    private static final String CSV_HEADER =
        "firstName,lastName,title,company,address,notes,phone1Label,phone1Number,phone2Label,phone2Number,email1Label,email1Address,email2Label,email2Address";

    public void exportToCsv(Long userId, PrintWriter writer) {
        writer.println(CSV_HEADER);

        int page = 0;
        List<Contact> batch;

        do {
            batch = contactRepository
                .findByUserId(userId, PageRequest.of(page++, 200))
                .getContent();

            for (Contact c : batch) {
                StringBuilder sb = new StringBuilder();
                sb.append(escape(c.getFirstName())).append(',');
                sb.append(escape(c.getLastName())).append(',');
                sb.append(escape(c.getTitle())).append(',');
                sb.append(escape(c.getCompany())).append(',');
                sb.append(escape(c.getAddress())).append(',');
                sb.append(escape(c.getNotes())).append(',');

                List<?> phones = c.getPhones();
                sb.append(phones.size() > 0 ? escape(c.getPhones().get(0).getLabel()) : "").append(',');
                sb.append(phones.size() > 0 ? escape(c.getPhones().get(0).getNumber()) : "").append(',');
                sb.append(phones.size() > 1 ? escape(c.getPhones().get(1).getLabel()) : "").append(',');
                sb.append(phones.size() > 1 ? escape(c.getPhones().get(1).getNumber()) : "").append(',');

                List<?> emails = c.getEmails();
                sb.append(emails.size() > 0 ? escape(c.getEmails().get(0).getLabel()) : "").append(',');
                sb.append(emails.size() > 0 ? escape(c.getEmails().get(0).getAddress()) : "").append(',');
                sb.append(emails.size() > 1 ? escape(c.getEmails().get(1).getLabel()) : "").append(',');
                sb.append(emails.size() > 1 ? escape(c.getEmails().get(1).getAddress()) : "");

                writer.println(sb);
            }
            writer.flush();
        } while (batch.size() == 200);

        log.info("Exported contacts for userId={}", userId);
    }

    public ImportResult importFromCsv(Long userId, MultipartFile file) throws IOException {
        int imported = 0;
        int skipped  = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine();
            if (line == null) {
                return new ImportResult(0, 0, List.of("File is empty."));
            }

            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    String[] cols = parseCsvLine(line);
                    if (cols.length < 2) {
                        errors.add("Row " + lineNum + ": too few columns, skipped.");
                        skipped++;
                        continue;
                    }

                    String firstName = col(cols, 0);
                    String lastName  = col(cols, 1);

                    if (firstName.isEmpty() || lastName.isEmpty()) {
                        errors.add("Row " + lineNum + ": first/last name required, skipped.");
                        skipped++;
                        continue;
                    }

                    CreateContactRequest req = new CreateContactRequest();
                    req.setFirstName(firstName);
                    req.setLastName(lastName);
                    req.setTitle(col(cols, 2));
                    req.setCompany(col(cols, 3));
                    req.setAddress(col(cols, 4));
                    req.setNotes(col(cols, 5));

                    List<ContactPhoneDto> phones = new ArrayList<>();
                    String p1Label = col(cols, 6);
                    String p1Num   = col(cols, 7);
                    if (!p1Num.isEmpty()) {
                        phones.add(ContactPhoneDto.builder()
                            .label(p1Label.isEmpty() ? "mobile" : p1Label)
                            .number(p1Num).build());
                    }
                    String p2Label = col(cols, 8);
                    String p2Num   = col(cols, 9);
                    if (!p2Num.isEmpty()) {
                        phones.add(ContactPhoneDto.builder()
                            .label(p2Label.isEmpty() ? "mobile" : p2Label)
                            .number(p2Num).build());
                    }
                    req.setPhones(phones);

                    List<ContactEmailDto> emails = new ArrayList<>();
                    String e1Label = col(cols, 10);
                    String e1Addr  = col(cols, 11);
                    if (!e1Addr.isEmpty()) {
                        emails.add(ContactEmailDto.builder()
                            .label(e1Label.isEmpty() ? "personal" : e1Label)
                            .address(e1Addr).build());
                    }
                    String e2Label = col(cols, 12);
                    String e2Addr  = col(cols, 13);
                    if (!e2Addr.isEmpty()) {
                        emails.add(ContactEmailDto.builder()
                            .label(e2Label.isEmpty() ? "personal" : e2Label)
                            .address(e2Addr).build());
                    }
                    req.setEmails(emails);

                    contactService.createContact(userId, req);
                    imported++;

                } catch (Exception e) {
                    errors.add("Row " + lineNum + ": " + e.getMessage());
                    skipped++;
                }
            }
        }

        log.info("Import for userId={}: {} imported, {} skipped", userId, imported, skipped);
        return new ImportResult(imported, skipped, errors);
    }

    private String escape(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String col(String[] cols, int i) {
        if (i >= cols.length) return "";
        return cols[i].trim().replaceAll("^\"|\"$", "").replace("\"\"", "\"");
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }

    public record ImportResult(int imported, int skipped, List<String> errors) {}
}
