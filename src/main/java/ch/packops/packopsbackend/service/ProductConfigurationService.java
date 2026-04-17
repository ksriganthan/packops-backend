package ch.packops.packopsbackend.service;

import ch.packops.packopsbackend.repository.ProductConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Kapischan Sriganthan
 */
@Service
public class ProductConfigurationService {

    @Autowired
    private final ProductConfigurationRepository productConfigurationRepository;

    public ProductConfigurationService(ProductConfigurationRepository productConfigurationRepository) {
        this.productConfigurationRepository = productConfigurationRepository;
    }

    // TODO: findAll
    // TODO: findById
    // TODO: save
    // TODO: update
    // TODO: delete
}
