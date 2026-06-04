package com.ramishtaha.sahar.journal;

import com.ramishtaha.sahar.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The journal's small service layer (step 23). Even though Spring Data hands us a ready-made repository, a
 * service still earns its place: it is where the transaction boundary lives and where domain rules (here, "you
 * can't delete an entry that doesn't exist") are enforced - keeping the controller thin and the repository
 * free of policy.
 */
@Service
public class JournalService {

    private final JournalRepository repo;

    public JournalService(JournalRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<JournalEntry> list() {
        return repo.findAllByOrderByEntryDateDesc();
    }

    @Transactional
    public JournalEntry add(JournalForm form) {
        return repo.save(new JournalEntry(form.date(), form.energy(), form.note()));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("no journal entry " + id);
        }
        repo.deleteById(id);
    }
}
