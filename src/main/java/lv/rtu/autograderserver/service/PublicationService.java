package lv.rtu.autograderserver.service;

import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.repository.PublicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Service
public class PublicationService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PublicationRepository publicationRepository;

    public PublicationService(@NotNull PublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    public Optional<Publication> fetchOngoingPublicationByPublicId(String publicationId) {
        return publicationRepository.fetchOngoingPublication(publicationId);
    }
}
