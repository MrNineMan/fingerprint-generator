package dev.draft.fingerprintgenerator;

import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EntryService {
    @Autowired
    private EntryRepository entryRepository;

    // Create a new competition entry
    public Entry createEntry(Entry entry) throws PSQLException {

        return entryRepository.save(entry);
    }

    // Checks if the entry exists in the table using the visitor id
    public boolean entryExistByID(Entry entry) {
        return entryRepository.existsById(entry.getVisitorId());
    }

}